package com.adswizz.universalplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRouter
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.MediaBrowserServiceCompat
import androidx.media.utils.MediaConstants
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequest
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.podcast.AdPodcastManager
import com.ad.core.podcast.AdPodcastManagerSettings
import com.ad.core.streaming.AdManagerStreamingSettings
import com.ad.core.streaming.AdStreamManager
import com.ad.core.streaming.DvrBufferInfo
import com.adswizz.common.AdPlayer
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.core.podcast.AdswizzAdPodcastManager
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.sdk.AdswizzSDK
import com.adswizz.universalplayer.models.DB
import com.adswizz.universalplayer.models.MediaData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val MEDIA_ROOT_ID = "root_id"
private const val CLIENT_ROOT_ID = "_client_side_id_"
private const val SERVER_ROOT_ID = "_server_side_id_"
private const val PODCAST_ROOT_ID = "_podcast_side_id_"

private const val channelId = "com.adswizz.universalplayer.notification"
private const val urlRequest = "https://siriusxmdigital.deliveryengine.adswizz.com/vast/4.1/request?companionAds=true&aw_0_1st.companionzonesalias=siriusxm-companion-display-300x250%20%2Csiriusxm-companion-display-500x500&aw_0_req.adEvents=%5B%7B%22zoneAlias%22%3A%22siriusxm-nowplaying-audio%22%7D%5D&aw_0_1st.duration=245038&aw_0_awz.appVers=7.17.0%3A1800000000&aw_0_1st.version=7.12.1%3Aandroid34&aw_0_1st.ts=1747848565767&aw_0_req.permissions=CCCC1CCCCC1C10&calendar=0&aw_0_1st.playerId=SXMAndroid-Phone&aw_0_1st.cb=1747848565.3314&aw_0_1st.sessionid=1747848565769.998857&aw_0_req.appState=fg&sdkiad=1&call=1&pkpass=1&aw_0_req.bundleId=com.sirius&aw_0_req.tapOpp=true&aw_0_awz.listenerid=6e990a91-c740-4fbd-bcc0-29ae9fc796ae&aw_0_req.lmt=0&aw_0_1st.ifaType=androidGMS&aw_0_req.wopp=0&aw_0_cnt.artist=null&&aw_0_1st.channelId=51f88432-8311-37e0-b57d-9b0ab63f5ce9&&aw_0_1st.showId=ca91c400-cc81-4e27-a9bf-125d19514a87&&aw_0_cnt.title=Joe%20Fortenbaugh%20and%20Q%20Myers&&aw_0_1st.requestId=1644&&aw_0_1st.podId=1747848395_890818648&&aw_0_1st.ppid=1b3e8f00-d09b-4b77-b47e-78bf8c896173&&aw_0_enc.profileId=hls_48&aw_0_1st.zip=15293"


@Suppress("SpellCheckingInspection")
class AdswizzMediaBrowserService: MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private var myAdManager: AdManager? = null
    private var myPodcastManager: AdswizzAdPodcastManager? = null
    private var myAdstreamManager: AdswizzAdStreamManager? = null

    private var currentMediaId = ""
    private var currentServiceType = ""

    private lateinit var player: ExternalExoPlayer
    private var notificationDescription =  "AdswizzUniversalPlayer"

    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var channel: NotificationChannel
    private lateinit var audioFocusRequest: AudioFocusRequest

    private lateinit var bitmap: Bitmap

    /*
        region MediaBrowserServiceCompat
    */
    override fun onCreate() {
        println("AdswizzMediaBrowserService:: onCreate")
        super.onCreate()

        AdswizzSDK.initialize(applicationContext,null)
        player = ExternalExoPlayer(applicationContext)
        bitmap = ContextCompat.getDrawable(applicationContext, R.drawable.cover1)?.toBitmap()!!

        mediaSession = MediaSessionCompat(this, "AdswizzService").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
            setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PREPARE or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH)
                .setState(PlaybackStateCompat.STATE_STOPPED,0,1.0f)

            isActive = true
            setPlaybackState(stateBuilder.build())

            setCallback(callback)

            setSessionToken(sessionToken)
        }

        requestAudioFocus()
    }

    override fun onBind(intent: Intent?): IBinder? {
        println("AdswizzMediaBrowserService:: onBind $intent")
        return super.onBind(intent)
    }

    override fun onCustomAction(action: String, extras: Bundle?, result: Result<Bundle>) {
        println("AdswizzMediaBrowserService:: onCustomAction $action")
        super.onCustomAction(action, extras, result)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("AdswizzMediaBrowserService:: onStartCommand $intent :: ${intent?.action}")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        println("AdswizzMediaBrowserService:: onDestroy")
        releaseAudioFocus()

        //cleanup adswizz managers
        myAdManager?.reset()
        myAdManager = null

        myPodcastManager?.stop()
        myPodcastManager = null

        myAdstreamManager?.stop()
        myAdstreamManager = null
    }

    private val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
    private val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
    private val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
    private val CONTENT_STYLE_LIST = 1
    private val CONTENT_STYLE_GRID = 2

    /*
        region media browser (content discovery)
    */
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        val rootExtras = Bundle().apply {
            putBoolean(MediaConstants.BROWSER_SERVICE_EXTRAS_KEY_SEARCH_SUPPORTED, true)
            putBoolean(CONTENT_STYLE_SUPPORTED, true)
            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
        }
        println("AdswizzMediaBrowserService:: onGetRoot: $clientPackageName $clientUid $rootHints")
        return BrowserRoot(MEDIA_ROOT_ID, rootExtras)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        when (parentId){
            MEDIA_ROOT_ID -> {
                val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("Client Side").setSubtitle("Insertion").setMediaId(CLIENT_ROOT_ID).build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
                mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("Server Side").setSubtitle("Insertion").setMediaId(SERVER_ROOT_ID).build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
                mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("Podcast").setSubtitle("Insertion").setMediaId(PODCAST_ROOT_ID).build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
                result.sendResult(mediaItems)
            }
            CLIENT_ROOT_ID -> {
                //nothing
                val mediaItems = DB.getClientSideList().map{
                    MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle(it.title).setSubtitle(it.subtitle).setMediaUri(Uri.parse(it.uri)).setMediaId(it.mediaId).build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                }
                /*
                mutableListOf<MediaBrowserCompat.MediaItem>()
            mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_1").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_1.mp3")).setMediaId("podcastRAD1").build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
                 */
                result.sendResult(mediaItems.toMutableList())
            }
            SERVER_ROOT_ID -> {
                val mediaItems = DB.getServerSideList().map{
                    MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle(it.title).setSubtitle(it.subtitle).setMediaUri(Uri.parse(it.uri)).setMediaId(it.mediaId).build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                }
                result.sendResult(mediaItems.toMutableList())
                //result.detach()
            }
            PODCAST_ROOT_ID -> {
                val mediaItems = DB.getPodcastList().map{
                    MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle(it.title).setSubtitle(it.subtitle).setMediaUri(Uri.parse(it.uri)).setMediaId(it.mediaId).build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                }
                result.sendResult(mediaItems.toMutableList())
            }
        }
    }

    override fun onSearch(query: String, extras: Bundle?, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        println("AdswizzMediaBrowserService:: onSearch: $query")
        super.onSearch(query, extras, result)
    }

    override fun onLoadItem(itemId: String?, result: Result<MediaBrowserCompat.MediaItem>) {
        println("AdswizzMediaBrowserService:: onLoadItem: $itemId")
        super.onLoadItem(itemId, result)
    }

    /*
        @end region MediaBrowserServiceCompat
    */

    private fun requestAudioFocus(){
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                //setOnAudioFocusChangeListener(afChangeListener)
                setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }
            val result = am.requestAudioFocus(audioFocusRequest)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                println("Audio focus request")
            }
        }
    }

    private fun releaseAudioFocus(){
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocusRequest(audioFocusRequest)
        }
    }

    private fun startService(serviceType:String, mediaId:String, url:Uri?) {
        mediaSession?.let{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = NotificationChannel(channelId, "Uamp Service", NotificationManager.IMPORTANCE_LOW)
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            val builder = NotificationCompat.Builder(this, channelId).apply {
                setContentTitle(notificationDescription)
                setOngoing(true)
                /* click on notification restart the main activity */
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                val activityPI = PendingIntent.getActivity(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                setContentIntent(activityPI)

                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Stop the service when the notification is swiped away
                val initActionIntent = Intent(applicationContext, AdswizzMediaBrowserService::class.java).apply {
                    action = "delete"
                }
                val pendingInitActionIntent = PendingIntent.getService(applicationContext, 0, initActionIntent, PendingIntent.FLAG_MUTABLE)
                setDeleteIntent(pendingInitActionIntent)

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                setSmallIcon(R.drawable.ic_launcher)

                setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(it.sessionToken))
            }

            ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, this.javaClass))
            startForeground(1234, builder.build())

            stateBuilder.setState(PlaybackStateCompat.STATE_BUFFERING, 0, 1.0f)
            it.setPlaybackState(stateBuilder.build())
            currentMediaId = mediaId
            stopPlayers()
            currentServiceType = serviceType
            when(serviceType){
                "client" -> playClient(mediaId)
                "podcast" -> playPodcast(url.toString())
                "server" -> playServer(url.toString())
                else -> Unit
            }
        }
    }

    private fun stopPlayers(){
        myAdManager?.removeAdManagerListener()
        myAdManager = null

        myPodcastManager?.stop()
        myPodcastManager?.removeListener(adPodcastManagerListener)
        myAdstreamManager?.stop()
        myAdstreamManager?.removeListener(adStreamManagerListener)
    }

    private fun playNext(serviceType: String){
        println("AdswizzMediaBrowserService:: Play next type: $serviceType")
        printCurrentSelectedRoute("playNext")

        var list = listOf<MediaData>()
        when (serviceType){
            "client" -> list = DB.getClientSideList()
            "sever" -> list = DB.getServerSideList()
            "podcast" -> list = DB.getPodcastList()
        }
        val index = list.indexOfFirst { it.mediaId == currentMediaId }
        if ( index < list.size -1 ){
            val item = list[index+1]
            updateMediaMetadata(item)
            currentMediaId = item.mediaId
            when (serviceType){
                "client" -> playClient(item.mediaId)
                "sever" -> playServer(item.uri)
                "podcast" -> playPodcast(item.uri)
            }
        }
    }

    private fun playPrev(serviceType: String){
        println("AdswizzMediaBrowserService:: Play next type: $serviceType")
        printCurrentSelectedRoute("playPrev")

        var list = listOf<MediaData>()
        when (serviceType){
            "client" -> list = DB.getClientSideList()
            "sever" -> list = DB.getServerSideList()
            "podcast" -> list = DB.getPodcastList()
        }
        val index = list.indexOfFirst { it.mediaId == currentMediaId }
        if ( index -1 >= 0 ){
            val item = list[index-1]
            updateMediaMetadata(item)
            currentMediaId = item.mediaId
            when (serviceType){
                "client" -> playClient(item.mediaId)
                "sever" -> playServer(item.uri)
                "podcast" -> playPodcast(item.uri)
            }
        }
    }

    private fun playClient(zoneId: String){
        printCurrentSelectedRoute("playClient")
        CoroutineScope(Dispatchers.Main).launch {
            /*
            val req = AdRequest.Builder().withUrlString(urlRequest)
            req.requestAds { adManager, _ ->
                // Handle the response from server
                adManager?.prepare()
                adManager?.play()
                adManager?.setListener(adManagerListener)
                myAdManager = adManager
            }
            */

            val adRequestBuilder = AdswizzAdRequest.Builder().
                withServer("siriusxmdigital.deliveryengine.adswizz.com").
                withZoneAlias("siriusxm-companion-display-300x250 ,siriusxm-companion-display-500x500").
                withDuration(245038).
                withZones(listOf(AdswizzAdZone(zoneAlias = "siriusxm-nowplaying-audio"))).
                withCustomParameter("aw_0_cnt.artist=null&&aw_0_1st.channelId=51f88432-8311-37e0-b57d-9b0ab63f5ce9&&aw_0_1st.showId=ca91c400-cc81-4e27-a9bf-125d19514a87&&aw_0_cnt.title=Joe%20Fortenbaugh%20and%20Q%20Myers&&aw_0_1st.requestId=1644&&aw_0_1st.podId=1747848395_890818648&&aw_0_1st.ppid=1b3e8f00-d09b-4b77-b47e-78bf8c896173&&aw_0_enc.profileId=hls_48&aw_0_1st.zip=15293")
            /*
            val adRequestBuilder =
                AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
                    .withServer("demo.deliveryengine.adswizz.com")
                    .withHttpProtocol(AdswizzAdRequest.HttpProtocol.HTTPS)
                    .withZones(setOf(AdswizzAdZone(zoneId)))

             */
            adRequestBuilder.build {
                println("Url: ${it.uri}")
                val adRequestConnection = AdRequestConnection(it)

                adRequestConnection.requestAds { adManager, _ ->
                    // Handle the response from server
                    adManager?.prepare()
                    adManager?.play()
                    adManager?.setListener(adManagerListener)
                    myAdManager = adManager
                }
            }



        }
    }

    private fun playPodcast(uri: String){
        println("AdswizzMediaBrowserService:: Play next podcast: $uri")
        printCurrentSelectedRoute("playPodcast")
        CoroutineScope(Dispatchers.Main).launch {
            val podcastManager = AdswizzAdPodcastManager(AdPodcastManagerSettings.Builder().adPlayerInstance(player).build())
            myPodcastManager = podcastManager
            podcastManager.addListener(adPodcastManagerListener)
            podcastManager.play(Uri.parse(uri))
        }
    }

    private fun playServer(uri: String) {
        printCurrentSelectedRoute("playServer")
        CoroutineScope(Dispatchers.Main).launch {
            val adStreamManager = AdswizzAdStreamManager(
                AdManagerStreamingSettings.Builder().adPlayerInstance(player).build()
            )
            myAdstreamManager = adStreamManager
            adStreamManager.addListener(adStreamManagerListener)
            adStreamManager.play(uri)
        }
    }

    private fun printCurrentSelectedRoute(function: String){
        val mediaRouter = getSystemService(MEDIA_ROUTER_SERVICE) as MediaRouter
        val route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO)

        println("Route -> $function -> Route name: " + route.name)
        println("Route -> $function -> Route description: " + route.description)
        println("Route -> $function -> Route deviceType: " + route.deviceType)


        Toast.makeText(this.applicationContext, "Route name:  " + route.name, Toast.LENGTH_LONG)
    }

    /*
        region SDK callbacks
    */
    private val adManagerListener = object : AdManagerListener{
        override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
        }

        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
            println("AdswizzMediaBrowserService:: onEventReceived: ${event.type}")

            when (event.type){
                AdEvent.Type.State.DidStartPlaying -> {
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    val bundle = Bundle()
                    bundle.putDouble("duration", adManager.ads.sumOf { it.duration ?: 0.0 } )
                    stateBuilder.setExtras(bundle)
                    mediaSession?.setPlaybackState(stateBuilder.build())
                }
                AdEvent.Type.State.AllAdsCompleted -> playNext("client")
                AdEvent.Type.Position.FirstQuartile -> {
                    val position = adManager.ads.sumOf { it.duration ?: 0.0 } * 0.25
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, position.toLong(), 1.0f)
                    mediaSession?.setPlaybackState(stateBuilder.build())
                }
                AdEvent.Type.Position.Midpoint -> {
                    val position = adManager.ads.sumOf { it.duration ?: 0.0 } * 0.5
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, position.toLong(), 1.0f)
                    mediaSession?.setPlaybackState(stateBuilder.build())
                }
                AdEvent.Type.Position.ThirdQuartile -> {
                    val position = adManager.ads.sumOf { it.duration ?: 0.0 } * 0.75
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, position.toLong(), 1.0f)
                    mediaSession?.setPlaybackState(stateBuilder.build())
                }
                AdEvent.Type.Position.Complete -> {
                    val position = adManager.ads.sumOf { it.duration ?: 0.0 }
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, position.toLong(), 1.0f)
                    mediaSession?.setPlaybackState(stateBuilder.build())
                }
                else -> Unit
            }
        }
    }

    private val adPodcastManagerListener = object : AdPodcastManager.Listener{
        private var playHeadPosition = 0L
        override fun adBreakEnded(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
            println("AdswizzMediaBackground::adPodcastManagerListener adBreakEnded")
        }

        override fun adBreakStarted(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager) {
            println("AdswizzMediaBackground::adPodcastManagerListener adBreakStarted ${adBaseManager.ads.size}" )
        }

        override fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            stateBuilder.setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
            playNext("podcast")
        }

        override fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
        }

        override fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
        }

        override fun onError(adPodcastManager: AdPodcastManager, error: Error) {
        }

        override fun onPlayHeadReport(adPodcastManager: AdPodcastManager, elapsedSec: Double, durationSec: Double) {
            if (elapsedSec.toLong() > playHeadPosition ) {
                playHeadPosition = elapsedSec.toLong()
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, elapsedSec.toLong(), 1.0f)
                val bundle = Bundle()
                bundle.putDouble("duration", durationSec)
                stateBuilder.setExtras(bundle)
                mediaSession?.setPlaybackState(stateBuilder.build())
                bundle.putLong("elapsed", elapsedSec.toLong())
                mediaSession?.sendSessionEvent("playHeadUpdate", Bundle())
            }
        }

        override fun willStartPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri, adBaseManager: AdBaseManager?) {
            playHeadPosition = 0L
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

    }

    private val adStreamManagerListener = object : AdStreamManager.Listener{
        override fun adBreakEnded(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            println("AdswizzMediaBackground::adStreamManagerListener adBreakEnded")
        }

        override fun adBreakStarted(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            println("AdswizzMediaBackground::adStreamManagerListener adBreakStarted")
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            playNext("server")
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

        override fun onDvrMetadataReceived(adStreamManager: AdStreamManager, dvrBufferInfo: DvrBufferInfo?) {
            println("AdswizzMediaBackground::adStreamManagerListener onDvrMetadataReceived")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            println("AdswizzMediaBackground::adStreamManagerListener onError")
            playNext("server")
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager,
            metadataItem: AdPlayer.MetadataItem
        ) {
            println("AdswizzMediaBackground::adStreamManagerListener onMetadataChanged")
        }

        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

    }

    /*
        end region SDK callbacks
    */

    private fun startServiceWithMedia(serviceType:String, media: MediaData){
        updateMediaMetadata(media)
        startService(serviceType, media.mediaId, Uri.parse(media.uri))
    }

    private fun updateMediaMetadata(media: MediaData){
        val metadata = MediaMetadataCompat.Builder()
            .putText(MediaMetadataCompat.METADATA_KEY_TITLE, media.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, media.subtitle)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, media.mediaId)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            .build()
        mediaSession?.setMetadata(metadata)
    }
    /*
        region MediaSessionCompat callbacks
    */
    private val callback = object: MediaSessionCompat.Callback() {
        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            println("AdswizzMediaBrowserService:: onCommand - $command")
        }

        override fun onPrepare() {
            println("AdswizzMediaBrowserService:: onPrepare")
        }

        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            println("AdswizzMediaBrowserService:: onPrepareFromMediaId $mediaId")
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            println("AdswizzMediaBrowserService:: onPlayFromMediaId $mediaId")
            var found = false

            DB.getPodcastList().firstOrNull{
                it.mediaId == mediaId
            } ?.apply {
                found = true
                startServiceWithMedia("podcast", this)
            }


            if (!found){
                DB.getClientSideList().firstOrNull{
                    it.mediaId == mediaId
                } ?.apply {
                    found = true
                    startServiceWithMedia("client", this)
                }
            }

            if ( !found){
                DB.getServerSideList().firstOrNull{
                    it.mediaId == mediaId
                } ?.apply {
                    found = true
                    startServiceWithMedia("server", this)
                }
            }

            super.onPlayFromMediaId(mediaId, extras)
        }

        override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
            println("AdswizzMediaBrowserService:: onPrepareFromSearch $query")
            //super.onPrepareFromSearch(query, extras)
        }

        override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
            println("AdswizzMediaBrowserService:: onPrepareFromUri $uri")
           // super.onPrepareFromUri(uri, extras)
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            println("AdswizzMediaBrowserService:: Custom action?? $action")
            super.onCustomAction(action, extras)
        }

        override fun onPlay() {
            myAdManager?.resume()
            myPodcastManager?.resume()
            myAdstreamManager?.resume()
            println("AdswizzMediaBrowserService:: onPlay")
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            println("AdswizzMediaBrowserService:: onPlayFromSearch")
            super.onPlayFromSearch(query, extras)
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            println("AdswizzMediaBrowserService:: onMediaButtonEvent")
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onStop() {
            println("AdswizzMediaBrowserService:: onStop")
            myAdManager?.reset()
            myPodcastManager?.stop()
            myAdstreamManager?.stop()
            stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

        override fun onPause() {
            println("AdswizzMediaBrowserService:: onPause")
            myAdManager?.pause()
            myPodcastManager?.pause()
            myAdstreamManager?.pause()
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

        override fun onSkipToNext() {
            println("AdswizzMediaBrowserService:: onSkipToNext")
            playNext(currentServiceType)
            super.onSkipToNext()
        }

        override fun onSkipToPrevious() {
            println("AdswizzMediaBrowserService:: onSkipToPrevious")
            playPrev(currentServiceType)
            super.onSkipToPrevious()
        }

    }

    /*
        end region MediaSessionCompat callbacks
    */
}