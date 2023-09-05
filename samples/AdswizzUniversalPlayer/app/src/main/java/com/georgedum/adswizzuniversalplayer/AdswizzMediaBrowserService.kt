package com.georgedum.adswizzuniversalplayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
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
import android.support.v4.media.session.MediaSessionCompat.PENDING_INTENT_FLAG_MUTABLE
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.utils.MediaConstants
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.podcast.AdPodcastManager
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.core.podcast.AdswizzAdPodcastManager
import com.adswizz.sdk.AdswizzSDK
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val MY_MEDIA_ROOT_ID = "root_id"
private const val CLIENT_ROOT_ID = "_client_side_id_"
private const val SERVER_ROOT_ID = "_server_side_id_"
private const val PODCAST_ROOT_ID = "_podcast_side_id_"

private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class AdswizzMediaBrowserService: MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private var myAdManager: AdManager? = null
    private var myPodcastManager: AdswizzAdPodcastManager? = null
    //protected lateinit var mediaSessionConnector: MediaSessionConnector
    private var handler: Handler? = null
    private var playList = mutableListOf<MediaDescriptionCompat>()

    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var channel: NotificationChannel
    private lateinit var audioFocusRequest: AudioFocusRequest

    private val bitmap = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.cover3)
    companion object{
        fun getTitleByMediaIt(id:String) :String{
            return when(id){
                "_client_side_id_" -> "Client Side"
                "_server_side_id_" -> "Server Side"
                "_podcast_side_id_" -> "Podcasts"
                else -> "Unknown"
            }
        }
    }

    override fun onCreate() {
        println("AdswizzMediaBrowserService:: onCreate")
        super.onCreate()
        playList.add(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_1").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_1.mp3")).setMediaId("podcast_RAD_1").build())
        playList.add(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_QueenBohemian.mp3").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3")).setMediaId("podcast_RAD_QueenBohemian").build())
        playList.add(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_brokenTS.mp3").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_brokenTS.mp3")).setMediaId("podcast_RAD_brokenTS").build())
        playList.add(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_muchosEvents.mp3").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_muchosEvents.mp3")).setMediaId("podcast_RAD_muchosEvents").build())

        AdswizzSDK.initialize(applicationContext,null)
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, PENDING_INTENT_FLAG_MUTABLE)
            }
        handler =
            Handler((if (Looper.myLooper() != null) Looper.myLooper() else Looper.getMainLooper())!!)

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "AdswizzService").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
            setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
//             Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player

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

            // MySessionCallback() has methods that handle callbacks from a media controller
//            mediaSessionConnector = MediaSessionConnector(this)
//            mediaSessionConnector.setPlaybackPreparer(UampPlaybackPreparer())
//            // Set the session's token so that client activities can communicate with it.
            println("AdswizzMediaBrowserService:: Sessiontoken: $sessionToken")
            setSessionToken(sessionToken)

            println("AdswizzMediaBrowserService playback state: ${controller.playbackState}")
            println("AdswizzMediaBrowserService is active: ${controller.isSessionReady}")
            println("AdswizzMediaBrowserService controller get session activity: ${controller.sessionActivity}")
            println("AdswizzMediaBrowserService controller get session info: ${controller.sessionInfo}")
            println("AdswizzMediaBrowserService controller get playback info: ${controller.playbackInfo.playbackType}")
            println("AdswizzMediaBrowserService controller get extras: ${controller.extras}")
            println("AdswizzMediaBrowserService controller get flags: ${controller.flags}")
        }

        val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Request audio focus for playback, this registers the afChangeListener

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
        //mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession))
        //startService(Intent(this, AdswizzMediaBrowserService::class.java))
    }

    private inner class UampPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        /**
         * UAMP supports preparing (and playing) from search, as well as media ID, so those
         * capabilities are declared here.
         *
         * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
         */
        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            println("AdswizzMediaBrowserService:: UampPlaybackPreparer:: onPrepare")
//            val recentSong = storage.loadRecentSong() ?: return
//            onPrepareFromMediaId(
//                recentSong.mediaId!!,
//                playWhenReady,
//                recentSong.description.extras
//            )
        }

        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            println("AdswizzMediaBrowserService:: UampPlaybackPreparer:: onPrepareFromMediaId $mediaId")
//            val bitmap = BitmapFactory.decodeResource(null, R.drawable.cover)
//            val metadata = MediaMetadataCompat.Builder()
//                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, "Running ads")
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Adswizz")
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
//                .build()
//
//            mediaSession?.setMetadata(metadata)
//            mediaSource.whenReady {
//                val itemToPlay: MediaMetadataCompat? = mediaSource.find { item ->
//                    item.id == mediaId
//                }
//                if (itemToPlay == null) {
//                    Log.w(TAG, "Content not found: MediaID=$mediaId")
//                    // TODO: Notify caller of the error.
//                } else {
//
//                    val playbackStartPositionMs =
//                        extras?.getLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, C.TIME_UNSET)
//                            ?: C.TIME_UNSET
//
//                    preparePlaylist(
//                        buildPlaylist(itemToPlay),
//                        itemToPlay,
//                        playWhenReady,
//                        playbackStartPositionMs
//                    )
//                }
//            }
        }

        /**
         * This method is used by the Google Assistant to respond to requests such as:
         * - Play Geisha from Wake Up on UAMP
         * - Play electronic music on UAMP
         * - Play music on UAMP
         *
         * For details on how search is handled, see [AbstractMusicSource.search].
         */
        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
            println("AdswizzMediaBrowserService:: UampPlaybackPreparer:: onPrepareFromSearch")

//            mediaSource.whenReady {
//                val metadataList = mediaSource.search(query, extras ?: Bundle.EMPTY)
//                if (metadataList.isNotEmpty()) {
//                    preparePlaylist(
//                        metadataList,
//                        metadataList[0],
//                        playWhenReady,
//                        playbackStartPositionMs = C.TIME_UNSET
//                    )
//                }
//            }
        }

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ) = false

        /**
         * Builds a playlist based on a [MediaMetadataCompat].
         *
         * TODO: Support building a playlist by artist, genre, etc...
         *
         * @param item Item to base the playlist on.
         * @return a [List] of [MediaMetadataCompat] objects representing a playlist.
         */
//        private fun buildPlaylist(item: MediaMetadataCompat): List<MediaMetadataCompat> =
//            mediaSource.filter { it.album == item.album }.sortedBy { it.trackNumber }
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

    fun startService(serviceType:String, url:Uri?) {
        mediaSession?.let{
            val controller = it.controller
            val mediaMetadata = controller.metadata
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
//                setContentText(description.subtitle)
//                setSubText(description.description)
//                setLargeIcon(description.iconBitmap)

                // Enable launching the player by clicking the notification
                setContentIntent(controller.sessionActivity)

                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Stop the service when the notification is swiped away
                val initActionIntent = Intent(this@AdswizzMediaBrowserService, this@AdswizzMediaBrowserService.javaClass)
                initActionIntent.action = "delete"
                val pendingInitActionIntent = PendingIntent.getService(this@AdswizzMediaBrowserService, 0, initActionIntent,
                    PENDING_INTENT_FLAG_MUTABLE)
//            initActionData = NotificationData(
//                R.drawable.ic_notification_not_available_24dp,
//                getString(R.string.notification_not_available),
//                pendingInitActionIntent,
//                true
//            )
                setDeleteIntent(
                    pendingInitActionIntent
//                MediaButtonReceiver.buildMediaButtonPendingIntent(
//                    context,
//                    PlaybackStateCompat.ACTION_STOP
//                )
                )

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                setSmallIcon(R.drawable.ic_launcher)
                color = ContextCompat.getColor(this@AdswizzMediaBrowserService, R.color.primaryDark)

                val pendingActionIntent = Intent(this@AdswizzMediaBrowserService, this@AdswizzMediaBrowserService.javaClass)
                pendingActionIntent.action = "pause"
                val pendingPauseIntent = PendingIntent.getService(this@AdswizzMediaBrowserService, 0, pendingActionIntent, PENDING_INTENT_FLAG_MUTABLE)
                // Add a pause button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_notification_pause_24dp,
                        "Pause",
                        pendingPauseIntent
                    )
                )
                //setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(it.sessionToken))
                //setStyle(NotificationCompat.DecoratedCustomViewStyle())
                setStyle(NotificationCompat.InboxStyle())
                //setContent()
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
                            .withZones(setOf(AdswizzAdZone("12556")))
                    adRequestBuilder.build {
                        println("Url: ${it.uri}")
                        val adRequestConnection = AdRequestConnection(it)

                        adRequestConnection.requestAds { adManager, error ->
                            // Handle the response from server
                            println("Error : $error")
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
                    val podcastManager = AdswizzAdPodcastManager()
                    myPodcastManager = podcastManager
                    podcastManager.addListener(adPodcastManagerListener)
                    url?.let {  uri ->
                        podcastManager.play(uri)
                    }

                }
            }
            else{
                //nothing
            }
        }
    }

    val adManagerListener = object : AdManagerListener{
        override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
            //TODO("Not yet implemented")
        }

        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
            //TODO("Not yet implemented")
            if (event.type == AdEvent.Type.State.DidStartPlaying ){
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                mediaSession?.setPlaybackState(stateBuilder.build())
            }
        }
    }

    val adPodcastManagerListener = object : AdPodcastManager.Listener{
        override fun adBreakEnded(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
            //TODO("Not yet implemented")
        }

        override fun adBreakStarted(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
            //TODO("Not yet implemented")
        }

        override fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            //TODO("Not yet implemented")
        }

        override fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            //TODO("Not yet implemented")
        }

        override fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            //TODO("Not yet implemented")
        }

        override fun onError(adPodcastManager: AdPodcastManager, error: Error) {
            //TODO("Not yet implemented")
        }

        override fun onPlayHeadReport(
            adPodcastManager: AdPodcastManager,
            elapsedSec: Double,
            durationSec: Double
        ) {

        }

        override fun willStartPlayingUrl(
            adPodcastManager: AdPodcastManager,
            url: Uri,
            adBaseManager: AdBaseManager?
        ) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
            mediaSession?.setPlaybackState(stateBuilder.build())
        }

    }

    override fun onDestroy() {
        println("AdswizzMediaBrowserService:: onDestroy")
    }


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
            val mediaDescription = playList.find {
                it.mediaId == mediaId
            }?.let{
                val metadata = MediaMetadataCompat.Builder()
                    .putText(MediaMetadataCompat.METADATA_KEY_TITLE, it.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Adswizz")
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,it.mediaId)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
                    .build()
                mediaSession?.setMetadata(metadata)
                startService("podcast", it.mediaUri)
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

    private  val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
    private  val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
    private  val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
    private  val CONTENT_STYLE_LIST = 1
    private  val CONTENT_STYLE_GRID = 2

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        //TODO("Not yet implemented")
        val rootExtras = Bundle().apply {
            putBoolean(
                MediaConstants.BROWSER_SERVICE_EXTRAS_KEY_SEARCH_SUPPORTED, true)
//            putBoolean("android.media.browse.SEARCH_SUPPORTED", true)
            putBoolean(CONTENT_STYLE_SUPPORTED, true)
            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
        }
        println("AdswizzMediaBrowserService:: onGetRoot: $clientPackageName $clientUid $rootHints")
        println("AdswizzMediaBrowserService getExtraSuggested:${rootHints?.getBoolean(MediaBrowserService.BrowserRoot.EXTRA_SUGGESTED)}")
        //if (allowBrowsing(clientPackageName, clientUid)) {
        return BrowserRoot(MY_MEDIA_ROOT_ID, rootExtras)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        when (parentId){
            MY_MEDIA_ROOT_ID -> {
                val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("Client").setMediaId(CLIENT_ROOT_ID).build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
                mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("Server").setMediaId(SERVER_ROOT_ID).build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
                mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("Podcast").setMediaId(PODCAST_ROOT_ID).build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
                result.sendResult(mediaItems)
            }
            CLIENT_ROOT_ID -> {
                //nothing
                val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                mediaItems.add(MediaBrowserCompat.MediaItem(MediaDescriptionCompat.Builder().setTitle("podcast_RAD_1").setMediaUri(Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_1.mp3")).setMediaId("podcastRAD1").build(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
                result.sendResult(mediaItems)
            }
            SERVER_ROOT_ID -> {
                result.sendResult(mutableListOf<MediaBrowserCompat.MediaItem>())
                //result.detach()
            }
            PODCAST_ROOT_ID -> {
                println("AdswizzMediaBrowserService:: onLoadChildren: $parentId")
                val extras = Bundle()
                extras.putLong(
                    MediaConstants.METADATA_KEY_IS_EXPLICIT,
                    MediaConstants.METADATA_VALUE_ATTRIBUTE_PRESENT)
                extras.putInt(
                    MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_STATUS,
                    MediaConstants.DESCRIPTION_EXTRAS_VALUE_COMPLETION_STATUS_PARTIALLY_PLAYED)

                val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                playList.forEach {
                    mediaItems.add(MediaBrowserCompat.MediaItem(it, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
                }

                result.sendResult(mediaItems)
            }
        }
    }

    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        println("AdswizzMediaBrowserService:: onSearch: $query")
        super.onSearch(query, extras, result)
    }

    override fun onLoadItem(itemId: String?, result: Result<MediaBrowserCompat.MediaItem>) {
        println("AdswizzMediaBrowserService:: onLoadItem: $itemId")
        super.onLoadItem(itemId, result)
    }
}