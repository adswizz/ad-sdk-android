package com.adswizz.testSDKRequest

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.ad.core.AdSDK
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequest
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.adManager.AdManagerSettings
import com.ad.core.companion.AdCompanionView
import com.ad.core.podcast.AdPodcastManager
import com.ad.core.podcast.AdPodcastManagerSettings
import com.ad.core.streaming.AdManagerStreamingSettings
import com.ad.core.streaming.AdStreamManager
import com.ad.core.streaming.DvrBufferInfo
import com.ad.core.utils.common.extension.addUriParameter
import com.ad.core.utils.common.extension.decodeB64
import com.adswizz.common.AdPlayer
import com.adswizz.core.podcast.AdswizzAdPodcastManager
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.player.CacheManager
import com.adswizz.sdk.AdswizzSDK
import com.adswizz.sdk.AdswizzSDKConfig
import com.adswizz.testSDKRequest.data.Request
import com.adswizz.testSDKRequest.databinding.PlayerActivityBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.metadata.emsg.EventMessage
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.metadata.id3.ApicFrame
import com.google.android.exoplayer2.metadata.id3.CommentFrame
import com.google.android.exoplayer2.metadata.id3.GeobFrame
import com.google.android.exoplayer2.metadata.id3.Id3Frame
import com.google.android.exoplayer2.metadata.id3.PrivFrame
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayerActivity: AppCompatActivity() {
    private lateinit var binding: PlayerActivityBinding

    private var adManager: AdManager? = null
    private var adStreamManager: AdswizzAdStreamManager? = null
    private var adPodcastManager: AdPodcastManager? = null

    private var liveLog = MutableLiveData<String>()
    private var started = false

    private lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PlayerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.text = "${Request.requestType} Request"

        binding.btnClose.setOnClickListener{
            if ( started ) {
                cleanupManagersAndListeners()
                finish()
            }
        }

        binding.companion.listener = object : AdCompanionView.Listener{
            override fun didDisplayAd(adCompanionView: AdCompanionView) {
                liveLog.postValue( "adCompanion didDisplayAd" )
            }

            override fun didEndDisplay(adCompanionView: AdCompanionView) {
                liveLog.postValue("adCompanion didEndDisplay" )
            }

            override fun didFailToDisplayAd(adCompanionView: AdCompanionView, error: Error) {
            }

            override fun onRenderProcessGone(adCompanionView: AdCompanionView, didCrash: Boolean) {
            }

            override fun shouldOverrideClickThrough(
                adCompanionView: AdCompanionView,
                uri: Uri
            ): Boolean {
                return true
            }

            override fun willLeaveApplication(adCompanionView: AdCompanionView) {
                liveLog.postValue("adCompanion willLeaveApplication")
            }

            override fun willLoadAd(adCompanionView: AdCompanionView) {
                liveLog.postValue("adCompanion willLoadAd")
            }

        }

        liveLog.observe(this){
            binding.log.text = it + "\r\n" + binding.log.text
        }
        player = ExoPlayerFactory.newSimpleInstance(applicationContext)
        startPlaying()
    }

    private val adManagerListener = object : AdManagerListener{
        override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
            liveLog.postValue("Error:: ${ad?.id}" )
        }

        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
            liveLog.postValue("Event:: ${event.type} ${event.ad?.id}")
        }
    }

    private val adStreamManagerListener = object : AdStreamManager.Listener{
        override fun adBreakEnded(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            liveLog.postValue("adStreamManager:: adBreakEnded" )
        }

        override fun adBreakStarted(
            adStreamManager: AdStreamManager,
            adBaseManager: AdBaseManager
        ) {
            liveLog.postValue("adStreamManager:: adBreakStarted" )
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            liveLog.postValue("adStreamManager:: didFinishPlayingUrl $url" )
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            liveLog.postValue("adStreamManager:: didPausePlayingUrl $url" )
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            liveLog.postValue("adStreamManager:: didResumePlayingUrl $url" )
        }

        override fun onDvrMetadataReceived(
            adStreamManager: AdStreamManager,
            dvrBufferInfo: DvrBufferInfo?
        ) {
            liveLog.postValue("adStreamManager:: onDvrMetadataReceived $dvrBufferInfo" )
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            liveLog.postValue("adStreamManager:: onError $error" )
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager,
            metadataItem: AdPlayer.MetadataItem
        ) {
            liveLog.postValue("adStreamManager:: onMetadataChanged $metadataItem" )
        }

        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            println("*** adStreamManager  willStartPlayingUrl $url")
            liveLog.postValue("adStreamManager:: willStartPlayingUrl $url" )
        }

    }

    private val adPodcastManagerListener = object: AdPodcastManager.Listener{
        override fun adBreakEnded(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
            liveLog.postValue("adPodcastManager:: adBreakEnded" )
        }

        override fun adBreakStarted(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
            liveLog.postValue("adPodcastManager:: adBreakStarted" )
        }

        override fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            liveLog.postValue("adPodcastManager:: didFinishPlayingUrl $url" )
        }

        override fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            liveLog.postValue("adPodcastManager:: didPausePlayingUrl $url" )
        }

        override fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            liveLog.postValue("adPodcastManager:: didResumePlayingUrl $url" )
        }

        override fun onError(adPodcastManager: AdPodcastManager, error: Error) {
            liveLog.postValue("adPodcastManager:: onError $error" )
        }

        override fun onPlayHeadReport(
            adPodcastManager: AdPodcastManager,
            elapsedSec: Double,
            durationSec: Double
        ) {
            liveLog.postValue("adPodcastManager:: onPlayHeadReport $elapsedSec $durationSec" )
        }

        override fun willStartPlayingUrl(
            adPodcastManager: AdPodcastManager,
            url: Uri,
            adBaseManager: AdBaseManager?
        ) {
            liveLog.postValue("adPodcastManager:: willStartPlayingUrl $url" )
        }

    }

    private val playerListener = object : Player.EventListener{

    }

    private val playerMetadataListener = object : MetadataOutput {
        override fun onMetadata(metadata: Metadata?) {
            val metadataList = mutableListOf<AdPlayer.MetadataItem>()
            if (metadata != null) {
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
                            if (!entry.value.isNullOrEmpty()) {
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
                        is ApicFrame -> {
                            if (!entry.description.isNullOrEmpty()) {
                                metadataList.add(
                                    AdPlayer.MetadataItem(
                                        "description",
                                        entry.description.toString()
                                    )
                                )
                            }
                            if (!entry.mimeType.isNullOrEmpty()) {
                                metadataList.add(
                                    AdPlayer.MetadataItem(
                                        "description",
                                        entry.mimeType.toString()
                                    )
                                )
                            }
                        }
                        is CommentFrame -> {
                            if (!entry.text.isNullOrEmpty()) {
                                metadataList.add(AdPlayer.MetadataItem("text", entry.text))
                            }
                        }
                    }
                }
            }
            metadataList.forEach {
                if ( it.key == "text" && it.value.startsWith("adswizzContext=")){
                    val context = it.value.replace("adswizzContext=","")
                    val adLine = context.decodeB64()
                    liveLog.postValue("Event:: ad received $adLine")
                }
                else {
                    liveLog.postValue("Event:: onMetadata ${it.key} ${it.value}")
                }
            }
        }

    }

    private fun makeUrlRequest(): Uri {
        //replace http with https
        var uri = Uri.parse(Request.serverUrl)

        uri = uri.addUriParameter("aw_0_1st.playerId", Request.playerId)
        uri = uri.addUriParameter("aw_0_1st.sessionid", Request.sessionId)
        uri = uri.addUriParameter("aw_0_1st.version", Request.sdkVersion)

        uri = uri.addUriParameter("aw_0_1st.gpslat", String.format("%2.6f", Request.location.lat) )
        uri = uri.addUriParameter("aw_0_1st.gpslong",String.format("%2.6f", Request.location.lon) )

        if (Request.zoneId.isNotEmpty()){
            //[{"zoneId":"17012","maxAds":2,"maxDuration":40000}]
            uri = uri.addUriParameter("aw_0_req.adEvents","[{\"zoneId\":\"${Request.zoneId}\"}]" )
        }

        return uri
    }

    private fun startPlaying(){
        AdswizzSDK.initialize(applicationContext, AdswizzSDKConfig("GenericDemo", Request.playerId)){
            when( Request.requestType ){
                "ClientSide" -> startPlayingClient()
                "ServerSide" -> startPlayingServer()
                "Podcasting" -> startPodcasting()
                "else" -> println("*** unknown request type")
            }
        }
    }

    private fun startPlayingClient(){
        val url = makeUrlRequest()
        val req = AdRequest.Builder().withUrlString(url.toString()).build()

        val adRequestConnection = AdRequestConnection(req)
        adRequestConnection.requestAds{ adMgr, error ->
            adManager = adMgr
            adManager?.let{
                val adManagerSettings = AdManagerSettings.Builder().videoViewId(1000).build()
                //set also companion and viedeoview
                var hasCompanion = false
                it.ads.forEach {
                    if ( it.hasCompanion ){
                        hasCompanion = true
                    }
                }
                if (hasCompanion){
                    binding.companion.visibility = View.VISIBLE
                    binding.video.visibility = View.INVISIBLE
                }
                else{
                    binding.companion.visibility = View.INVISIBLE
                    binding.video.visibility = View.VISIBLE
                }
                it.adManagerSettings = adManagerSettings
                liveLog.value = "adManager:: ads no: ${it.ads.size}"
                it.setListener(adManagerListener)
                it.prepare()
                it.play()
            }
            started = true
        }
    }

    private fun startPlayingServer() {
        val url = makeUrlRequest()
        //player.playWhenReady = true
        println("*** server $url")
//        val mediaSources = ConcatenatingMediaSource()
//        mediaSources.addMediaSource(buildMediaSource(url.toString()))
//        player.prepare(mediaSources)
//        player.addMetadataOutput(playerMetadataListener)
//        player.playWhenReady = true
        val adManagerSettings = AdManagerStreamingSettings.Builder().videoViewId(1000).build()
        adStreamManager = AdswizzAdStreamManager(adManagerSettings)
        adStreamManager?.addListener(adStreamManagerListener)

        adStreamManager?.play(url.toString())
        started = true
    }

    private fun startPodcasting() {
        val url = makeUrlRequest()
        val adManagerSettings = AdPodcastManagerSettings.
            Builder().
        build()
        adPodcastManager = AdswizzAdPodcastManager(adManagerSettings)
        adPodcastManager?.addListener(adPodcastManagerListener)
        adPodcastManager?.play(url)
        started = true
    }

    private fun cleanupManagersAndListeners(){
        adManager?.let{
            it.removeAdManagerListener()
            it.reset()
        }
        adManager = null

        adStreamManager?.let{
            it.removeListener(adStreamManagerListener)
            it.stop()
            it.cleanup()
        }
        adStreamManager = null


        adPodcastManager?.let{
            it.removeListener(adPodcastManagerListener)
            it.stop()
        }
        adPodcastManager = null
    }


    private fun buildMediaSource(urlString: String): MediaSource {
        val uri = Uri.parse(urlString)
        val undecoratedUri = Uri.parse(urlString).buildUpon().clearQuery().build()

        val dataSourceFactory = DefaultHttpDataSourceFactory("ArtemisKotlin")

        return when (Util.inferContentType(undecoratedUri)) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory).setTag(urlString).createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory).setTag(urlString).createMediaSource(uri)
            C.TYPE_SS ->  SsMediaSource.Factory(dataSourceFactory).setTag(urlString).createMediaSource(uri)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory).setTag(urlString).createMediaSource(uri)
        }

    }
}