package com.georgedum.adswizzuniversalplayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.georgedum.adswizzuniversalplayer.models.DB
import com.georgedum.adswizzuniversalplayer.models.MediaData
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
    private lateinit var player: ExternalExoPlayer


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
        bitmap = ContextCompat.getDrawable(applicationContext, R.drawable.cover1)?.toBitmap()!!//BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.cover3)
//        val sessionActivityPendingIntent =
//            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
//                PendingIntent.getActivity(this, 0, sessionIntent, PENDING_INTENT_FLAG_MUTABLE)
//            }
//        handler =
//            Handler((if (Looper.myLooper() != null) Looper.myLooper() else Looper.getMainLooper())!!)

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "AdswizzService").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
            setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
//             Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_PREPARE or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_URI or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH)
                .setState(PlaybackStateCompat.STATE_STOPPED,0,1.0f)
            //setSessionActivity(sessionActivityPendingIntent)
            isActive = true
            setPlaybackState(stateBuilder.build())

            val metadata = MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, "Running ads")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Adswizz")
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,"podcast_RAD_1")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
                .build()

            setMetadata(metadata)
            val mediaSessionCompatList = mutableListOf<MediaSessionCompat.QueueItem>()
            mediaSessionCompatList.add(MediaSessionCompat.QueueItem(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_1").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_1.mp3")).setMediaId("podcast_RAD_1").build(),
                1))
            mediaSessionCompatList.add(MediaSessionCompat.QueueItem(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_QueenBohemian.mp3").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3")).setMediaId("podcast_RAD_QueenBohemian").build(),
               2))
            mediaSessionCompatList.add(MediaSessionCompat.QueueItem(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_brokenTS.mp3").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_brokenTS.mp3")).setMediaId("podcast_RAD_brokenTS").build(),
                3))
            mediaSessionCompatList.add(MediaSessionCompat.QueueItem(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_muchosEvents.mp3").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_muchosEvents.mp3")).setMediaId("podcast_RAD_muchosEvents").build(),
                4))
            setQueue(mediaSessionCompatList)

            setCallback(callback)

            setSessionToken(sessionToken)
        }

        //mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession))
        //startService(Intent(this, AdswizzMediaBrowserService::class.java))
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
    }

    private val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
    private val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
    private val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
    private val CONTENT_STYLE_LIST = 1
    private val CONTENT_STYLE_GRID = 2

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
            val description =  "AdswizzUniversalPlayer"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel =
                NotificationChannel(
                    "channelId",
                    "My Foreground Service",
                    NotificationManager.IMPORTANCE_LOW
                )
                val manager = getSystemService(
                    NotificationManager::class.java
                )
                manager.createNotificationChannel(channel)
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            val builder = NotificationCompat.Builder(this, "channelId").apply {
                // Add the metadata for the currently playing track
                setContentTitle(description)
                val intent = Intent(applicationContext, MainActivity::class.java)
                val activityPI = PendingIntent.getActivity(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                setContentIntent(activityPI)

                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Stop the service when the notification is swiped away
                val initActionIntent = Intent(this@AdswizzMediaBrowserService, this@AdswizzMediaBrowserService.javaClass)
                initActionIntent.action = "delete"
                val pendingInitActionIntent = PendingIntent.getService(this@AdswizzMediaBrowserService, 0, initActionIntent,
                    PendingIntent.FLAG_MUTABLE)


                setDeleteIntent(pendingInitActionIntent)

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                setSmallIcon(R.drawable.ic_launcher)
                color = ContextCompat.getColor(this@AdswizzMediaBrowserService, R.color.primaryDark)

                val pendingActionIntent = Intent(this@AdswizzMediaBrowserService, this@AdswizzMediaBrowserService.javaClass)
                pendingActionIntent.action = "pause"
                val pendingPauseIntent = PendingIntent.getService(this@AdswizzMediaBrowserService, 0, pendingActionIntent, PendingIntent.FLAG_MUTABLE)
                // Add a pause button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_notification_pause_24dp,
                        "Pause",
                        pendingPauseIntent
                    )
                )
                setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(it.sessionToken))
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this).notify(10, builder.build())
                return
            }
            ContextCompat.startForegroundService(
                applicationContext,
                Intent(applicationContext, this.javaClass)
            )
            startForeground(1234, builder.build())
            it.isActive = true
            stateBuilder.setState(PlaybackStateCompat.STATE_BUFFERING, 0, 1.0f)
            it.setPlaybackState(stateBuilder.build())

            if ( serviceType == "client") {
                CoroutineScope(Dispatchers.Main).launch {
                    val adRequestBuilder =
                        AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
                            .withServer("demo.deliveryengine.adswizz.com")
                            .withHttpProtocol(AdswizzAdRequest.HttpProtocol.HTTPS)
                            .withZones(setOf(AdswizzAdZone(mediaId)))
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
            else if ( serviceType == "podcast"){
                CoroutineScope(Dispatchers.Main).launch {
                    val podcastManager = AdswizzAdPodcastManager(AdPodcastManagerSettings.Builder().adPlayerInstance(player).build())
                    myPodcastManager = podcastManager
                    podcastManager.addListener(adPodcastManagerListener)
                    url?.let {  uri ->
                        podcastManager.play(uri)
                    }

                }
            }
            else if (serviceType == "server"){
                CoroutineScope(Dispatchers.Main).launch {
                    val adStreamManager = AdswizzAdStreamManager(AdManagerStreamingSettings.Builder().adPlayerInstance(player).build())
                    myAdstreamManager = adStreamManager
                    adStreamManager.addListener(adStreamManagerListener)
                    url?.let{   uri ->
                        adStreamManager.play(uri.toString())
                    }
                }
            }
            else{

            }
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
                mediaSession?.setPlaybackState(stateBuilder.build())
            }
            else if (event.type == AdEvent.Type.State.AllAdsCompleted){
                //playNext("client")
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
            //playNext("podcast")
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
            TODO("Not yet implemented")
        }

        override fun adBreakStarted(
            adStreamManager: AdStreamManager,
            adBaseManager: AdBaseManager
        ) {
            TODO("Not yet implemented")
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            TODO("Not yet implemented")
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            TODO("Not yet implemented")
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            TODO("Not yet implemented")
        }

        override fun onDvrMetadataReceived(
            adStreamManager: AdStreamManager,
            dvrBufferInfo: DvrBufferInfo?
        ) {
            TODO("Not yet implemented")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            TODO("Not yet implemented")
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager,
            metadataItem: AdPlayer.MetadataItem
        ) {
            TODO("Not yet implemented")
        }

        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            TODO("Not yet implemented")
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
//            val metadata = MediaMetadataCompat.Builder()
//                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, "Rad queen")
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Bohemina")
//                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "Podcast RAD_QueenBohemian")
//                .build()
//            mediaSession?.setMetadata(metadata)
//            startService("podcast", Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3"))

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
            stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
//            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            // Abandon audio focus
//            am.abandonAudioFocusRequest(audioFocusRequest)
//            unregisterReceiver(myNoisyAudioStreamReceiver)
//            // Stop the service
//            service.stopSelf()
//            // Set the session inactive  (and update metadata and state)
//            mediaSession.isActive = false
//            // stop the player (custom call)
//            player.stop()
//            // Take the service out of the foreground
//            service.stopForeground(false)
        }

        override fun onPause() {
            println("AdswizzMediaBrowserService:: onPause")
            myAdManager?.pause()
            myPodcastManager?.pause()
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
//            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            // Update metadata and state
//            // pause the player (custom call)
//            player.pause()
//            // unregister BECOME_NOISY BroadcastReceiver
//            unregisterReceiver(myNoisyAudioStreamReceiver)
//            // Take the service out of the foreground, retain the notification
//            service.stopForeground(false)
        }

        override fun onSkipToNext() {
            println("AdswizzMediaBrowserService:: onSkipToNext")
            super.onSkipToNext()
        }

        override fun onSkipToPrevious() {
            println("AdswizzMediaBrowserService:: onSkipToPrevious")
            super.onSkipToPrevious()
        }

    }

    /*
        end region MediaSessionCompat callbacks
    */
}