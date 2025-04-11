package com.adswizz.basicsample

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import com.adswizz.common.MediaPlayerState
import com.adswizz.common.macro.PlayerCapabilities
import com.adswizz.common.macro.PlayerState
import com.adswizz.common.video.AdVideoState
import com.adswizz.sdk.AdswizzSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(UnstableApi::class)
class ExternalMediaPlayerState(val context: Context) : MediaPlayerState, Player.Listener,
    AnalyticsListener {

    private var playerCurrentDuration = 0L
    private var playerCurrentPlayHead = 0L
    private var playerCurrentVolume = 0.0f


    override val currentPlayhead: Duration
        get() = playerCurrentPlayHead.toDuration(DurationUnit.MILLISECONDS)

    override val currentDuration: Duration
        get() = playerCurrentDuration.toDuration(DurationUnit.MILLISECONDS)

    private enum class PlayerStatus{
        Initialized, Loading, LoadingFinished, Failed, Buffering, BufferingFinished, Playing, Paused, Finished
    }
    private var currentPlayerStatus = PlayerStatus.Initialized

    private val creativeMap = mutableMapOf<String, Int>()

    private val items = mutableListOf<String>()

    override val playerVolume: Float
        get() = playerCurrentVolume

    private var adVideoState: AdVideoState? = null

    override val playerCapabilities: List<PlayerCapabilities>
        get() = listOf(PlayerCapabilities.SKIP, PlayerCapabilities.MUTE, PlayerCapabilities.FULLSCREEN)

    override val playerState: List<PlayerState>
        get() {
            val ml = mutableListOf<PlayerState>()

            if (adVideoState == AdVideoState.FULLSCREEN)
                ml.add(PlayerState.FULLSCREEN)
            if (playerVolume == 0.0f)
                ml.add(PlayerState.MUTED)
            if (AdswizzSDK.hasAProcessInForeground()) {
                ml.add(PlayerState.FOREGROUND)
            } else {
                ml.add(PlayerState.BACKGROUND)
            }
            return ml
        }

    private val handlerThread = HandlerThread("ExoPlayerThread").apply {
        start()
    }

    private val backgroundLooper: Looper = handlerThread.getLooper()
    private val handler = Handler(backgroundLooper)

    private val updateInterval = 300L // 1 second

    private val updatePlayheadRunnable = object : Runnable {
        override fun run() {
            if (player.isPlaying) {
                playerCurrentPlayHead = player.currentPosition
                playerCurrentDuration = player.duration
            }
            playerCurrentVolume = player.volume
            println("*** player : pos: $playerCurrentPlayHead dur: $playerCurrentDuration vol: $playerCurrentVolume")
            handler.postDelayed(this, updateInterval)
        }
    }

    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setLooper(backgroundLooper) // Custom background looper
        .build()

    private val _playerStatusFlow = MutableSharedFlow<MediaPlayerState.PlayerEvent>()
    override val playerStatusFlow: SharedFlow<MediaPlayerState.PlayerEvent> = _playerStatusFlow.asSharedFlow()

    private var seekInProgress = false

    init {
        player.addListener(this)
        player.addAnalyticsListener(this)
//        handler.post(updatePlayheadRunnable)
    }

    fun loadMediaItem(id: String, url: Uri, index: Int){
        seekInProgress = true
        items.add(index, id)
        creativeMap[url.toString()] = index
        Handler(handlerThread.looper).post {
            player.addMediaItem(
                index, MediaItem.Builder().setUri(url).setMediaId(id)
                    .build()
            )
            if ( index == 0){
                player.prepare()
                handler.post(updatePlayheadRunnable)
            }
        }
    }

    fun resetPlayer(){
        Handler(backgroundLooper).post {
            player.stop()
            player.clearMediaItems()
            player.playWhenReady = false
            creativeMap.clear()
            items.clear()
        }
        handler.removeCallbacks(updatePlayheadRunnable)
    }

    private fun emitHelper(event: MediaPlayerState.PlayerEvent){
        CoroutineScope(Dispatchers.IO).launch {
            _playerStatusFlow.emit(event)
        }
    }
    //region Analytics Listener

    // monitor removed media sources and offset the windowIndex to reflect the ad index
    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        val wi = eventTime.windowIndex
        val adIndex = creativeMap[loadEventInfo.uri.toString()]
        if (wi == adIndex) {
            emitHelper(MediaPlayerState.PlayerEvent.Loading(items.get(wi)))
            currentPlayerStatus = PlayerStatus.Loading
        }
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        val wi = eventTime.windowIndex
        val adIndex = creativeMap[loadEventInfo.uri.toString()]

        if (wi == adIndex) {
            emitHelper( MediaPlayerState.PlayerEvent.LoadingFinished(items.get(wi)))
            currentPlayerStatus = PlayerStatus.LoadingFinished
        }
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        val wi = eventTime.windowIndex
        val adIndex = creativeMap[loadEventInfo.uri.toString()]

        if (wi == adIndex) {
            emitHelper( MediaPlayerState.PlayerEvent.Failed(items.get(wi), MediaPlayerState.PlayerError.GeneralError(error) ))
            currentPlayerStatus = PlayerStatus.Failed
        }
    }

    //region Player Event Listener
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

        when (playbackState) {
            Player.STATE_BUFFERING -> {
                currentPlayerStatus = PlayerStatus.Buffering
                if (!seekInProgress) {
                    player.currentMediaItem?.mediaId?.let { id ->
                        emitHelper( MediaPlayerState.PlayerEvent.Buffering(id))
                    }
                }
            }
            Player.STATE_READY -> {
                if (currentPlayerStatus == PlayerStatus.Buffering) {
                    currentPlayerStatus = PlayerStatus.BufferingFinished
                    if (seekInProgress)
                        seekInProgress = false
                    else {
                        player.currentMediaItem?.mediaId?.let { id ->
                            emitHelper(MediaPlayerState.PlayerEvent.BufferingFinished(id))
                        }
                    }
                }

                when (playWhenReady) {
                    true -> {
                        val oldStatus = currentPlayerStatus
                        if (currentPlayerStatus != PlayerStatus.Playing) {
                            currentPlayerStatus = PlayerStatus.Playing
                            if (oldStatus == PlayerStatus.Paused) {
                                player.currentMediaItem?.mediaId?.let { id ->
                                    emitHelper(MediaPlayerState.PlayerEvent.Resume(id))
                                }
                            } else {
                                player.currentMediaItem?.mediaId?.let { id ->
                                    emitHelper(MediaPlayerState.PlayerEvent.StartPlaying(id))
                                }
                            }
                        }
                    }
                    false -> {
                        if (currentPlayerStatus == PlayerStatus.Playing) {
                            currentPlayerStatus = PlayerStatus.Paused
                            player.currentMediaItem?.mediaId?.let { id ->
                                emitHelper(MediaPlayerState.PlayerEvent.Pause(id))
                            }
                        }
                    }
                }
            }
            Player.STATE_ENDED -> {
                if (playWhenReady) {
                    player.playWhenReady = false
                } else {
                    if (currentPlayerStatus != PlayerStatus.Finished) {
                        currentPlayerStatus = PlayerStatus.Finished
                        player.currentMediaItem?.mediaId?.let { id ->
                            emitHelper(MediaPlayerState.PlayerEvent.Finished(id))
                        }
                    }
                }
            }
            Player.STATE_IDLE -> {
                currentPlayerStatus = PlayerStatus.Initialized
            }
        }
    }


    override fun onPlayerError(error: PlaybackException) {

        super<Player.Listener>.onPlayerError(error)
        player.currentMediaItem?.localConfiguration?.let { conf ->
            emitHelper(
                MediaPlayerState.PlayerEvent.Failed(
                    conf.uri.toString(),
                    MediaPlayerState.PlayerError.GeneralError(
                        error
                    )
                )
            )
        }
        currentPlayerStatus = PlayerStatus.Failed
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION){
            emitHelper(MediaPlayerState.PlayerEvent.Finished(oldPosition.mediaItem?.mediaId.orEmpty()))
            emitHelper(MediaPlayerState.PlayerEvent.StartPlaying(newPosition.mediaItem?.mediaId.orEmpty()))
        }
        // this approach will trigger automatically finish when media item is removed
        else if (reason == Player.DISCONTINUITY_REASON_REMOVE){
            // removed if error has happened
            emitHelper(MediaPlayerState.PlayerEvent.Finished(oldPosition.mediaItem?.mediaId.orEmpty()))
        }
    }
    // endregion


    fun skipAd(error: Error? = null) {
        try {
            seekInProgress = true
            if ((player.mediaItemCount) > player.currentMediaItemIndex + 1) {
                player.currentMediaItem?.localConfiguration?.let { conf ->
                    val err = error?.let{
                        MediaPlayerState.PlayerError.GeneralError(Exception(it.message))
                    }
                    player.currentMediaItem?.mediaId?.let { id ->
                        emitHelper(
                            MediaPlayerState.PlayerEvent.SkipAd(
                                id,
                                err
                            )
                        )
                    }
                }
                player.seekTo(player.currentMediaItemIndex + 1, 0L)
            } else {
                skipCurrent(error)
            }
        } catch (e: Exception) {
            skipCurrent(error)
        }
    }


    /*
        Host app player functions
     */
    fun play(){
        Handler(handlerThread.looper).post {
            player.playWhenReady = true
        }
    }

    fun resume(){
        play()
    }

    fun pause(){
        Handler(handlerThread.looper).post {
            player.playWhenReady = false
        }
    }


    private fun skipCurrent(error: Error?){
        seekInProgress = false
        player.currentMediaItem?.mediaId?.let { id ->
            val err = error?.let{
                MediaPlayerState.PlayerError.GeneralError(Exception(it.message))
            }
            emitHelper(
                MediaPlayerState.PlayerEvent.SkipAd(
                    id,
                    err
                )
            )
        }
    }

}