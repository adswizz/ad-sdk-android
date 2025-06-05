package com.adswizz.basicsample

import android.content.Context
import android.net.Uri
import android.util.Patterns
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
class ExternalMediaPlayerState(val context: Context, val player: ExoPlayer) : MediaPlayerState, Player.Listener,
    AnalyticsListener {

    override val currentPlayhead: Duration
        get() = player.currentPosition.toDuration(DurationUnit.MILLISECONDS)

    private enum class PlayerStatus{
        Initialized, Failed, Buffering, BufferingFinished, Playing, Paused, Finished
    }
    private var currentPlayerStatus = PlayerStatus.Initialized

    private val items = mutableListOf<String>()

    private val loadingFinishedSet = mutableSetOf<String>()

    override val playerVolume: Float
        get() = player.volume

    private var adVideoState: AdVideoState? = null

    override val playerCapabilities: List<PlayerCapabilities>
        get() = listOf(PlayerCapabilities.SKIP, PlayerCapabilities.MUTE, PlayerCapabilities.FULLSCREEN)

    override val playerState: List<PlayerState>
        get() {
            val ml = mutableListOf<PlayerState>()

            if (adVideoState == AdVideoState.FULLSCREEN)
                ml.add(PlayerState.FULLSCREEN)
            if (player.volume == 0.0f)
                ml.add(PlayerState.MUTED)
            if (AdswizzSDK.hasAProcessInForeground()) {
                ml.add(PlayerState.FOREGROUND)
            } else {
                ml.add(PlayerState.BACKGROUND)
            }
            return ml
        }

    private val _playerStatusFlow = MutableSharedFlow<MediaPlayerState.PlayerEvent>()
    override val playerStatusFlow: SharedFlow<MediaPlayerState.PlayerEvent> = _playerStatusFlow.asSharedFlow()

    private var seekInProgress = false

    init {
        player.addListener(this)
        player.addAnalyticsListener(this)
    }

    fun play(){
        player.playWhenReady = true
        seekInProgress = true
    }

    fun loadMediaItem(itemid: String, itemUrl: Uri, index: Int){
        seekInProgress = true
        items.add(index, itemid)
        player.addMediaItem(index, MediaItem.Builder().setUri(itemUrl).setMediaId(itemid)
            .build())
    }

    fun resetPlayer(){
        seekInProgress = false
        items.clear()
        player.stop()
        player.clearMediaItems()
    }

    private fun emitHelper(event: MediaPlayerState.PlayerEvent){
        CoroutineScope(Dispatchers.IO).launch {
            _playerStatusFlow.emit(event)
        }
    }
    //region Analytics Listener
    private fun sendLoadingFinished(mediaId: String){
        if ( loadingFinishedSet.contains(mediaId))
            return

        loadingFinishedSet.add(mediaId)
        emitHelper(MediaPlayerState.PlayerEvent.LoadingFinished(mediaId))
    }

    // monitor removed media sources and offset the windowIndex to reflect the ad index
    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        val wi = eventTime.windowIndex
        if (wi in 0 until items.size) {
            emitHelper(MediaPlayerState.PlayerEvent.Loading(items[wi]))
        }
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        val wi = eventTime.windowIndex
        if (wi in 0 until items.size) {
            sendLoadingFinished(items[wi])
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

        if (wi in 0 until items.size) {
            val errorMessage =
                "ExoPlayer failed to load media: ${loadEventInfo.uri} with ${error.message}"
            //we should map exoplayer to
            emitHelper( MediaPlayerState.PlayerEvent.Failed(items[wi], MediaPlayerState.PlayerError.GeneralError(Exception(errorMessage)) ))
            currentPlayerStatus = PlayerStatus.Failed
            items.removeAt(wi)
        }
        player.removeMediaItem(wi)
    }

    //region Player Event Listener
    @Deprecated("Deprecated in Java")
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
                            if (oldStatus == PlayerStatus.Paused) {
                                player.currentMediaItem?.mediaId?.let { id ->
                                    emitHelper(MediaPlayerState.PlayerEvent.Resume(id))
                                }
                            } else {
                                player.currentMediaItem?.mediaId?.let { id ->
                                    sendLoadingFinished(id)
                                    emitHelper(MediaPlayerState.PlayerEvent.StartPlaying(id))
                                }
                            }
                            currentPlayerStatus = PlayerStatus.Playing
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
                items.clear()
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
                player.stop()
            }
            Player.STATE_IDLE -> {
                currentPlayerStatus = PlayerStatus.Initialized
            }
        }
    }


    override fun onPlayerError(error: PlaybackException) {
        player.currentMediaItem?.mediaId?.let { mediaId ->
            emitHelper(
                MediaPlayerState.PlayerEvent.Failed(
                    mediaId,
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
            if (player.mediaItemCount > player.currentMediaItemIndex + 1) {
                val err = error?.let{
                    MediaPlayerState.PlayerError.GeneralError(Exception(it.message))
                }
                player.currentMediaItem?.localConfiguration?.let { conf ->
                    player.currentMediaItem?.mediaId?.let { id ->
                        emitHelper(
                            MediaPlayerState.PlayerEvent.SkipAd(
                                id,
                                err
                            )
                        )
                    }
                } ?: run{
                    if ( player.mediaItemCount > 0 ){
                        val id = player.getMediaItemAt(0).mediaId
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

    private fun skipCurrent(error: Error?){
        seekInProgress = false
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
        }?:run{
            //there is no current media item
            if ( player.mediaItemCount > 0 ){
                val id = player.getMediaItemAt(0).mediaId
                emitHelper(
                    MediaPlayerState.PlayerEvent.SkipAd(
                        id,
                        err
                    )
                )
            }
        }
    }


}