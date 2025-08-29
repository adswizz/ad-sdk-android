package com.adswizz.streamingsample

import android.content.Context
import android.net.Uri
import android.view.Surface
import com.adswizz.common.AdPlayer
import com.adswizz.common.macro.PlayerCapabilities
import com.adswizz.common.macro.PlayerState
import com.adswizz.common.video.AdVideoState
import com.adswizz.sdk.AdswizzSDK
import com.adswizz.streamingsample.ext.BuildConfig
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.emsg.EventMessage
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.metadata.id3.*
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Suppress("DEPRECATION")
class ExternalAdPlayer(private val context: Context) : AdPlayer, Player.Listener, AnalyticsListener{

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
    private var duration: Long? = null
    private var listenerList: ConcurrentLinkedQueue<AdPlayer.Listener> =
        ConcurrentLinkedQueue()

    val player: SimpleExoPlayer =
        SimpleExoPlayer.Builder(context).build()
    private val mediaSources = ConcatenatingMediaSource()

    private val playlist = CopyOnWriteArrayList<Asset>()
    private var playingIndex = -1
    private var seekInProgress = false

    private var adVideoState: AdVideoState? = null


    // HLS streams region
    // This code region is required in order to play AIS enabled HLS streams
    data class HlsSegmentInfo(val url: String, val metaData: String, val startTimeMs: Long)
    private val AD_BREAK_INSERTION_TRESHOLD_MS = 500
    private val adBreaks = ConcurrentHashMap<String, HlsSegmentInfo>()
    private var playedTime = Duration.ZERO
    private val adBreakRunnable: (Duration) -> Unit = { time ->
        playedTime = time
        adBreaks.forEach { (key, value) ->
            if (abs(time.inWholeMilliseconds - value.startTimeMs) <= AD_BREAK_INSERTION_TRESHOLD_MS) { // insertion moment

                val metadata = adBreaks.remove(key)?.metaData
                if (!metadata.isNullOrBlank()) {
                    listenerList.forEach {
                        it.onMetadata(
                            listOf(AdPlayer.MetadataItem("value", metadata))
                        )
                    }
                }
            }
        }
    }
    private var timerHelper: TimerHelper = TimerHelper(period = 0.25.seconds, onTimePassed = adBreakRunnable)

    // end HLS streams region

    init {
        player.addListener(this)
        player.addAnalyticsListener(this)
    }

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
                    DataSource.Factory { RawResourceDataSource(context) }
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
        resetHlsDataIfNeeded()
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

    override fun seekTo(position: Duration) {
        try {
            seekInProgress = true
            player.seekTo(player.currentMediaItemIndex, position.inWholeMilliseconds)
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
    override fun getCurrentTime(): Duration {
        return if (BuildConfig.ENABLE_HLS_METADATA) {
            // this is needed for precise playback progress
            playedTime
        } else player.currentPosition.toDuration(DurationUnit.MILLISECONDS)
    }

    /**
     * @return the track duration from the player in seconds
     */
    override fun getDuration(): Duration? {
        return duration?.toDuration(DurationUnit.MILLISECONDS)
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
                    duration = player.duration
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
            duration = player.duration
            listenerList.forEach { it.onTrackChanged(playingIndex) }
        } else if (reason == Player.DISCONTINUITY_REASON_SEEK) {    // triggered by seek to end of track function
            listenerList.forEach { it.onSeekToTrackEnd(playingIndex) }
            playingIndex++
            duration = player.duration
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
        if (BuildConfig.ENABLE_HLS_METADATA) {
            handleHlsSegmentInfo(loadEventInfo.uri.toString(), mediaLoadData)
        }
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

    // HLS streams region
    // This code region is required in order to play AIS enabled HLS streams
     override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (!BuildConfig.ENABLE_HLS_METADATA) return

        val manifest = player.currentManifest
        if (manifest is HlsManifest) {
            manifest.mediaPlaylist.segments.forEach { segment ->
                val commentTag =
                    segment.title.substringAfter("comment=", missingDelimiterValue = "")
                        .trim('"')

                if (commentTag != "adwData=''" && commentTag.isNotBlank()) {
                    adBreaks.putIfAbsent(segment.url, HlsSegmentInfo(segment.url, commentTag, Long.MIN_VALUE))
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {

        if (!BuildConfig.ENABLE_HLS_METADATA) return

        if (isPlaying) timerHelper.start()
        else timerHelper.pause()
    }

    private fun handleHlsSegmentInfo(url: String, mediaLoadData: MediaLoadData){
        val current = adBreaks[url]
        if (current != null) {
            adBreaks[url] = current.copy(startTimeMs = mediaLoadData.mediaStartTimeMs)
        }
    }

    private fun resetHlsDataIfNeeded(){
        if (!BuildConfig.ENABLE_HLS_METADATA) return
        timerHelper.stop()
        timerHelper = TimerHelper(period = 0.25.seconds, onTimePassed = adBreakRunnable)
        adBreaks.clear()
    }
    // endregion

}
