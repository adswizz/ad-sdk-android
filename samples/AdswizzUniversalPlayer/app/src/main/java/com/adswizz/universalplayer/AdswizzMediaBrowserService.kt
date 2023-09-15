package com.adswizz.universalplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.MediaBrowserServiceCompat
import androidx.media.utils.MediaConstants
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
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


class AdswizzMediaBrowserService: MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private var myAdManager: AdManager? = null
    private var myPodcastManager: AdswizzAdPodcastManager? = null
    private var myAdstreamManager: AdswizzAdStreamManager? = null

    private var currentPlayList = ""
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
        println("AdswizzMediaBrowserService:: onStartCommand ${intent} :: ${intent?.action}")
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
                channel = NotificationChannel("channelId", "My Foreground Service", NotificationManager.IMPORTANCE_LOW)
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            val builder = NotificationCompat.Builder(this, "channelId").apply {
                setContentTitle(notificationDescription)

                /* click on notification restart the main activity */
                val intent = Intent(applicationContext, MainActivity::class.java)
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
            if ( serviceType == "client") {
                playClient(mediaId)
            }
            else if ( serviceType == "podcast"){
                playPodcast(url.toString())
            }
            else if (serviceType == "server"){
                playServer(url.toString())
            }
            else {
                println("AdswizzMediaBrowserService:: startService: unknown")
            }
        }
    }

    private fun stopPlayers(){
        myAdManager?.reset()
        myPodcastManager?.stop()
        myAdstreamManager?.stop()
    }

    private fun playNext(serviceType: String){
        println("AdswizzMediaBrowserService:: Play next type: $serviceType")

        var list = listOf<MediaData>()
        when (serviceType){
            "client" -> list = DB.getClientSideList()
            "sever" -> list = DB.getServerSideList()
            "podcast" -> list = DB.getPodcastList()
        }
        val index = list.indexOfFirst { it.mediaId == currentMediaId }
        if ( index < list.size -1 ){
            val item = list[index+1]
            when (serviceType){
                "client" -> playClient(item.mediaId)
                "sever" -> playServer(item.uri)
                "podcast" -> playPodcast(item.uri)
            }
        }
    }

    private fun playPrev(serviceType: String){
        println("AdswizzMediaBrowserService:: Play next type: $serviceType")

        var list = listOf<MediaData>()
        when (serviceType){
            "client" -> list = DB.getClientSideList()
            "sever" -> list = DB.getServerSideList()
            "podcast" -> list = DB.getPodcastList()
        }
        val index = list.indexOfFirst { it.mediaId == currentMediaId }
        if ( index -1 >= 0 ){
            val item = list[index-1]
            when (serviceType){
                "client" -> playClient(item.mediaId)
                "sever" -> playServer(item.uri)
                "podcast" -> playPodcast(item.uri)
            }
        }
    }

    private fun playClient(zoneId: String){
        CoroutineScope(Dispatchers.Main).launch {
            val adRequestBuilder =
                AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
                    .withServer("demo.deliveryengine.adswizz.com")
                    .withHttpProtocol(AdswizzAdRequest.HttpProtocol.HTTPS)
                    .withZones(setOf(AdswizzAdZone(zoneId)))
            adRequestBuilder.build {
                println("Url: ${it.uri}")
                val adRequestConnection = AdRequestConnection(it)

                adRequestConnection.requestAds { adManager, error ->
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
        CoroutineScope(Dispatchers.Main).launch {
            val podcastManager = AdswizzAdPodcastManager(AdPodcastManagerSettings.Builder().adPlayerInstance(player).build())
            myPodcastManager = podcastManager
            podcastManager.addListener(adPodcastManagerListener)
            podcastManager.play(Uri.parse(uri))
        }
    }

    private fun playServer(uri: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val adStreamManager = AdswizzAdStreamManager(
                AdManagerStreamingSettings.Builder().adPlayerInstance(player).build()
            )
            myAdstreamManager = adStreamManager
            adStreamManager.addListener(adStreamManagerListener)
            adStreamManager.play(uri)
        }
    }

    /*
        region SDK callbacks
    */
    private val adManagerListener = object : AdManagerListener{
        override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
        }

        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
            if (event.type == AdEvent.Type.State.DidStartPlaying ){
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                val bundle = Bundle()
                bundle.putDouble("duration", adManager.ads.sumOf { it.duration ?: 0.0 } )
                stateBuilder.setExtras(bundle)
                mediaSession?.setPlaybackState(stateBuilder.build())
            }
            else if (event.type == AdEvent.Type.State.AllAdsCompleted){
                playNext("client")
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
            //TODO("Not yet implemented")
        }

        override fun adBreakStarted(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            //TODO("Not yet implemented")
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            playNext("server")
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            //TODO("Not yet implemented")
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            TODO("Not yet implemented")
        }

        override fun onDvrMetadataReceived(adStreamManager: AdStreamManager, dvrBufferInfo: DvrBufferInfo?) {
            //TODO("Not yet implemented")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            //TODO("Not yet implemented")
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager,
            metadataItem: AdPlayer.MetadataItem
        ) {
            //TODO("Not yet implemented")
        }

        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            //TODO("Not yet implemented")
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

    }

    /*
        end region SDK callbacks
    */

    private fun startServiceWithMedia(serviceType:String, media: MediaData){
        val metadata = MediaMetadataCompat.Builder()
            .putText(MediaMetadataCompat.METADATA_KEY_TITLE, media.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, media.subtitle)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, media.mediaId)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 120*1000)
            .build()
        mediaSession?.setMetadata(metadata)
        startService(serviceType, media.mediaId, Uri.parse(media.uri))
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

            DB.getPodcastList().firstOrNull() {
                it.mediaId == mediaId
            } ?.apply {
                found = true
                startServiceWithMedia("podcast", this)
            }


            if (!found){
                DB.getClientSideList().firstOrNull() {
                    it.mediaId == mediaId
                } ?.apply {
                    found = true
                    startServiceWithMedia("client", this)
                }
            }

            if ( !found){
                DB.getServerSideList().firstOrNull() {
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