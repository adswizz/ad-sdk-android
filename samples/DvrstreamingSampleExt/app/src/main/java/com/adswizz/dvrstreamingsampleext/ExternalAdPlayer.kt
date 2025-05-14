package com.adswizz.dvrstreamingsampleext

import android.content.Context
import android.net.Uri
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.extractor.metadata.emsg.EventMessage
import androidx.media3.extractor.metadata.icy.IcyInfo
import androidx.media3.extractor.metadata.id3.ApicFrame
import androidx.media3.extractor.metadata.id3.CommentFrame
import androidx.media3.extractor.metadata.id3.GeobFrame
import androidx.media3.extractor.metadata.id3.Id3Frame
import androidx.media3.extractor.metadata.id3.PrivFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.id3.UrlLinkFrame
import androidx.media3.common.Metadata
import com.adswizz.common.AdPlayer
import com.adswizz.common.CommonContext
import com.adswizz.common.macro.PlayerCapabilities
import com.adswizz.common.macro.PlayerState
import com.adswizz.common.video.AdVideoState
import com.adswizz.sdk.AdswizzSDK
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList


@UnstableApi
@Suppress("DEPRECATION")
class ExternalAdPlayer(context: Context) : AdPlayer, Player.Listener, AnalyticsListener {

    private enum class AssetState {
        INITIALIZED,
        LOADING,
        COMPLETED,
        FAILED,
        CANCELED
    }

    private enum class LastLoadingCallbackSent {
        NONE,
        LOADING,
        LOADING_FINISHED
    }

    private data class Asset(
        var urlString: String,
        var assetState: AssetState = AssetState.INITIALIZED,
        var lastLoadingCallbackSent: LastLoadingCallbackSent = LastLoadingCallbackSent.NONE
    )

    // region VAST Related

    override val name = "ExternalExoPlayer"
    override val version = "1.23.456"
    override val playerCapabilities: List<PlayerCapabilities> =
        listOf(PlayerCapabilities.SKIP, PlayerCapabilities.MUTE, PlayerCapabilities.FULLSCREEN)
    override val playerState: List<PlayerState>
        get() {
            val ml = mutableListOf<PlayerState>()
            if (adVideoState == AdVideoState.FULLSCREEN)
                ml.add(PlayerState.FULLSCREEN)
            if (volume == 0.0f)
                ml.add(PlayerState.MUTED)
            if (AdswizzSDK.hasAProcessInForeground()) {
                ml.add(PlayerState.FOREGROUND)
            } else {
                ml.add(PlayerState.BACKGROUND)
            }
            return ml
        }

    // endregion


    // region Player Options

    override var cacheAssetsHint: Boolean = true
    override var enqueueEnabledHint: Boolean = true
    override val isBufferingWhilePaused: Boolean = true

    // endregion


    private var status: AdPlayer.Status = AdPlayer.Status.INITIALIZED
    private var duration: Double? = null
    private var listenerList: ConcurrentLinkedQueue<AdPlayer.Listener> =
        ConcurrentLinkedQueue()

    val player: SimpleExoPlayer =
        SimpleExoPlayer.Builder(context).build()
    private val mediaSources = ConcatenatingMediaSource()

    private val playlist = CopyOnWriteArrayList<Asset>()
    private var playingIndex = -1
    private var seekInProgress = false

    private var adVideoState: AdVideoState? = null


    init {
        player.addListener(this)
        player.addAnalyticsListener(this)
    }

    //eionut: cleanup should be used by AdManagers when they do cleanup
    fun cleanup() {
        player.removeAnalyticsListener(this)
        player.removeListener(this)
    }

    override fun toString(): String {
        return "ExternalExoPlayer (name = $name, version = $version)"
    }


    // region Player Controls

    override fun load(creativeUrlString: String) {
        if (!enqueueEnabledHint) {
            val oldPlayWhenReady = player.playWhenReady
            reset()
            player.playWhenReady = oldPlayWhenReady

            playlist.add(Asset(creativeUrlString, AssetState.INITIALIZED))
            val source = buildMediaSource(creativeUrlString)
            mediaSources.addMediaSource(source)

            playingIndex = 0
            seekInProgress = true
            player.setMediaSource(mediaSources)
            player.prepare()
        }
    }

    private fun buildMediaSource(urlString: String): MediaSource {
        val uri = Uri.parse(urlString)
        val undecoratedUri = Uri.parse(urlString).buildUpon().clearQuery().build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(urlString)
            .setUri(uri)
            .build()

        when {
            urlString.startsWith("rawresource://", true) -> {
                val dataSourceFactory =
                    DataSource.Factory { RawResourceDataSource(CommonContext.getContext()) }
                val extractorsFactory: DefaultExtractorsFactory =
                    DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
                return ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                    .createMediaSource(mediaItem)
            }
            urlString.startsWith("file:///", true) -> {
                val dataSourceFactory = DataSource.Factory { FileDataSource() }
                val extractorsFactory: DefaultExtractorsFactory =
                    DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
                return ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                    .createMediaSource(mediaItem)
            }
            else -> {
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent(com.adswizz.common.Utils.getDefaultUserAgent())

                when (Util.inferContentType(undecoratedUri)) {
                    C.TYPE_DASH -> {
                        return DashMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                    }
                    C.TYPE_HLS -> {
                        return HlsMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                    }
                    C.TYPE_SS -> {
                        return SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                    }
                    else -> {
                        val extractorsFactory: DefaultExtractorsFactory =
                            DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
                        return ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                            .createMediaSource(mediaItem)
                    }
                }
            }
        }
    }

    override fun play() {
        player.playWhenReady = true
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun reset() {
        player.stop()
        player.clearMediaItems()
        player.playWhenReady = false
        status = AdPlayer.Status.INITIALIZED
        duration = null

        removedMediaSourcesCount = 0
        mediaSources.clear()
        playlist.clear()
        playingIndex = -1
        seekInProgress = false
    }

    override fun seekToTrackEnd() {
        try {
            seekInProgress = true
            if ((player.mediaItemCount) > playingIndex + 1) {
                player.seekTo(playingIndex + 1, 0L)
            } else {
                skipCurrent()
            }
        } catch (e: Exception) {
            skipCurrent()
        }
    }

    private fun skipCurrent(){
        seekInProgress = false
        if (status != AdPlayer.Status.FINISHED) {
            status = AdPlayer.Status.FINISHED
            listenerList.forEach { it.onSeekToTrackEnd(playingIndex) }
        }
    }

    override fun seekTo(position: Double) {
        try {
            seekInProgress = true
            player.seekTo(player.currentMediaItemIndex, (position * 1000.0).toLong())
        } catch (e: Exception) {
            seekInProgress = false
        }
    }

    // endregion


    // region Enqueue Related

    override fun enqueue(creativeUrlString: String, index: Int) {
        if (enqueueEnabledHint) {
            if ((index >= 0) && (index <= playlist.size)) {
                playlist.add(index, Asset(creativeUrlString, AssetState.INITIALIZED))
                val source = buildMediaSource(creativeUrlString)
                mediaSources.addMediaSource(index, source)

                if (playingIndex >= index)
                    playingIndex++

                if (mediaSources.size == 1) {
                    playingIndex = 0
                    seekInProgress = true
                    player.setMediaSource(mediaSources)
                    player.prepare()
                }
            }
        }
    }

    override fun dequeue(index: Int) {
        if ((index >= 0) && (index < playlist.size)) {
            playlist.removeAt(index)
            mediaSources.removeMediaSource(index)
            if (playingIndex >= index)
                playingIndex--
        }
    }

    // endregion


    // region Current Values

    override var volume: Float
        set(value) {
            player.volume = value
        }
        get() = player.volume

    /**
     * @return the current play head from the player in seconds
     */
    override fun getCurrentTime(): Double {
        return player.currentPosition.toDouble() / 1000
    }

    /**
     * @return the track duration from the player in seconds
     */
    override fun getDuration(): Double? {
        return duration
    }

    /**
     * @return the current internal status from our media player
     */
    override fun status(): AdPlayer.Status {
        return status
    }

    // endregion


    // region Video

    override fun setVideoSurface(surface: Surface) {
        player.setVideoSurface(surface)
    }

    override fun clearVideoSurface(surface: Surface) {
        player.clearVideoSurface(surface)
    }

    override fun setVideoState(videoState: AdVideoState?) {
        adVideoState = videoState
    }

    // endregion


    // region Listener

    override fun addListener(listener: AdPlayer.Listener) {
        listenerList.forEach {
            if (it == listener) {
                return
            }
        }
        listenerList.add(listener)
    }

    override fun removeListener(listener: AdPlayer.Listener) {
        listenerList.remove(listener)
    }

    // endregion


    //region Player Event Listener

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                if (playingIndex >= 0) {
                    status = AdPlayer.Status.BUFFERING
                    if (!seekInProgress)
                        listenerList.forEach { it.onBuffering() }
                }
            }
            Player.STATE_READY -> {
                if (duration == null) {
                    duration = player.duration.toDouble() / 1000
                }
                if (status == AdPlayer.Status.BUFFERING) {
                    status = AdPlayer.Status.BUFFERING_FINISHED
                    if (seekInProgress)
                        seekInProgress = false
                    else
                        listenerList.forEach { it.onBufferingFinished() }
                }

                when (playWhenReady) {
                    true -> {
                        val oldStatus = status
                        if (status != AdPlayer.Status.PLAYING) {
                            status = AdPlayer.Status.PLAYING
                            if (oldStatus == AdPlayer.Status.PAUSED) {
                                listenerList.forEach { it.onResume() }
                            } else {
                                loadCompleted(playingIndex + removedMediaSourcesCount)     // this is needed because ExoPlayer calls onPlayerStateChanged before onLoadCompleted
                                listenerList.forEach { it.onPlay() }
                            }
                        }
                    }
                    false -> {
                        if (status == AdPlayer.Status.PLAYING) {
                            status = AdPlayer.Status.PAUSED
                            listenerList.forEach { it.onPause() }
                        }
                    }
                }
            }
            Player.STATE_ENDED -> {
                if (playWhenReady) {
                    player.playWhenReady = false
                } else {
                    if (status != AdPlayer.Status.FINISHED) {
                        status = AdPlayer.Status.FINISHED
                        listenerList.forEach { it.onEnded() }
                    }
                }
            }
            Player.STATE_IDLE -> {
                status = AdPlayer.Status.INITIALIZED
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super<Player.Listener>.onPlayerError(error)
        status = AdPlayer.Status.FAILED
        val errorMessage = "Something went wrong with external exoplayer: ${error.message}"
        listenerList.forEach {
            it.onError(errorMessage)
            it.onEnded()
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {  // transition from one track to the next
            playingIndex++
            duration = player.duration.toDouble() / 1000
            listenerList.forEach { it.onTrackChanged(playingIndex) }
        } else if (reason == Player.DISCONTINUITY_REASON_SEEK) {    // triggered by seek to end of track function
            listenerList.forEach { it.onSeekToTrackEnd(playingIndex) }
            playingIndex++
            duration = player.duration.toDouble() / 1000
        }

    }

    // endregion


    // region MetadataOutput

    override fun onMetadata(metadata: Metadata) {
        val metadataList = mutableListOf<AdPlayer.MetadataItem>()
        for (i in 0 until metadata.length()) {
            when (val entry = metadata.get(i)) {
                is IcyInfo -> {
                    if (entry.title != null) {
                        metadataList.add(AdPlayer.MetadataItem("title", entry.title ?: ""))
                    }
                    if (entry.url != null) {
                        metadataList.add(AdPlayer.MetadataItem("url", entry.url ?: ""))
                    }
                }
                is TextInformationFrame -> {
                    if (entry.value.isNotEmpty()) {
                        metadataList.add(AdPlayer.MetadataItem("value", entry.value))
                    }
                    if (!entry.description.isNullOrEmpty()) {
                        metadataList.add(
                            AdPlayer.MetadataItem(
                                "description",
                                entry.description.toString()
                            )
                        )
                    }
                }
                is UrlLinkFrame -> {
                    // do nothing
                }
                is PrivFrame -> {
                    // do nothing
                }
                is GeobFrame -> {
                    // do nothing
                }
                is ApicFrame -> {
                    if (!entry.description.isNullOrEmpty()) {
                        metadataList.add(
                            AdPlayer.MetadataItem(
                                "description",
                                entry.description.toString()
                            )
                        )
                    }
                    if (entry.mimeType.isNotEmpty()) {
                        metadataList.add(
                            AdPlayer.MetadataItem(
                                "description",
                                entry.mimeType
                            )
                        )
                    }
                }
                is CommentFrame -> {
                    if (entry.text.isNotEmpty()) {
                        metadataList.add(AdPlayer.MetadataItem("text", entry.text))
                    }
                }
                is Id3Frame -> {
                    // do nothing
                }
                is EventMessage -> {
                    // do nothing
                }
            }
        }
        listenerList.forEach { it.onMetadata(metadataList) }
    }

    //endregion


    //region Analytics Listener

    // monitor removed media sources and offset the windowIndex to reflect the ad index
    private var removedMediaSourcesCount = 0

    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        val wi = eventTime.windowIndex
        if ((wi >= 0) && ( (wi+ removedMediaSourcesCount) < playlist.size)) {
            val item = playlist.elementAt(wi + removedMediaSourcesCount)
            item.assetState = AssetState.LOADING
            if (item.lastLoadingCallbackSent == LastLoadingCallbackSent.NONE) {
                item.lastLoadingCallbackSent = LastLoadingCallbackSent.LOADING
                val i = if (enqueueEnabledHint) wi + removedMediaSourcesCount else null
                listenerList.forEach { it.onLoading(i) }
            }
        }
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        loadCompleted(eventTime.windowIndex + removedMediaSourcesCount)
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        val wi = eventTime.windowIndex
        if ((wi >= 0) && (wi < playlist.size)) {
            playlist.elementAt(wi).assetState = AssetState.FAILED

            if(wi < mediaSources.size) {
                if(mediaSources.getMediaSource(wi).mediaItem.mediaId == loadEventInfo.uri.toString()) {
                    mediaSources.removeMediaSource(wi)
                    if(enqueueEnabledHint) {
                        removedMediaSourcesCount++
                    }
                    val errorMessage =
                        "ExoPlayer failed to load media: ${loadEventInfo.uri} with ${error.message}"
                    listenerList.forEach {
                        it.onError(errorMessage)
                        if (wi == playingIndex) it.onEnded()
                    }
                }
            }
        }
    }

    override fun onLoadCanceled(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        val wi = eventTime.windowIndex
        if ((wi >= 0) && (wi < playlist.size))
            playlist.elementAt(wi).assetState = AssetState.CANCELED
    }

    private fun loadCompleted(index: Int) {
        if ((index >= 0) && (index < playlist.size)) {
            val item = playlist.elementAt(index)
            item.assetState = AssetState.COMPLETED
            if (item.lastLoadingCallbackSent == LastLoadingCallbackSent.LOADING) {
                item.lastLoadingCallbackSent = LastLoadingCallbackSent.LOADING_FINISHED
                val i = if (enqueueEnabledHint) index else null
                listenerList.forEach { it.onLoadingFinished(i) }
            }
        }
    }

    //endregion

    // region Audio Listener

    override fun onVolumeChanged(volume: Float) {
        super<Player.Listener>.onVolumeChanged(volume)
        listenerList.forEach { it.onVolumeChanged(volume) }
    }

    // endregion

    // region VideoListener

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        listenerList.forEach { it.onVideoSizeChanged(this, videoSize.width, videoSize.height) }
    }

    // endregion

}
