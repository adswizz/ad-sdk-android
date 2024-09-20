# Playing ads using your player using AdPlayer interface

This integration method uses your own `AdPlayer` interface implementation and it can be used across all ad-insertion scenarios (Client Side, Server Side, Podcast).<br/>
The `AdPlayer` interface is a bidirectional communication protocol between your player and **_AdswizzSDK_** managers, where you have to implement methods such as: `load`, `enqueue` called by the **_AdswizzSDK_** managers and forward your player events through `AdPlayer.Listener` methods.

## Client-side setup

For client-side insertion, to use the external player you have to provide an **_AdManagerSettings_** object with an instance of your player before calling ```adManager.prepare()```. See below:

```kotlin
	class ExternalExoPlayer(val context: Context) : AdPlayer{
		...
	
	}

	...
	AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
		.withServer()
		.withZones(adZones)
		.withCustomParameter(additionalCustomParam)
		.build { adRequest ->
	
			val adRequestConnection = AdRequestConnection(adRequest)

			adRequestConnection.requestAds { adManager, error ->
				// setup adManager using AdManagerSettings
			
				val eap = ExternalExoPlayer(context) // ExternalExoPlayer implements AdPlayer interface

			
				//customize adManager using AdManagerSettings
				adManager.adManagerSettings = AdManagerSettings.Builder()
						.adPlayerInstance(eap) // provide your AdPlayer implementation
						.assetQuality(assetQuality) // set desired asset quality
						.videoViewId(newVideoId) // 
						.build()

				handleResponse(adManager) //make other preparations and play the ad pod 
			}
		
	}    
```

## Server-side setup

For server-side insertion, to use the external player you have to provide an **_AdManagerStreamingSettings_** object with an instance of your player during initialization of ```AdswizzAdStreamManager```. See below:

```kotlin
	class ExternalExoPlayer(val context: Context) : AdPlayer{
		...
	
	}

	val adStreamManagerSettings = AdManagerStreamingSettings.Builder()
						.videoViewId(newVideoId)
						.enqueueEnabled(enqueueEnabled)
						.dvrEnabled(dvrEnable)
						.adPlayerInstance(it)
						.build()

	val adStreamManager = AdswizzAdStreamManager(adStreamManagerSettings)
				
	...            
	adStreamManager.play(streamUrl)
```

## Podcast setup

For podcast setup, to use the external player you have to provide an **_AdPodcastManagerSettings_** object with an instance of your player during initialization of ```AdswizzAdPodcastManager```. See below:

```kotlin
	class ExternalExoPlayer(val context: Context) : AdPlayer{
		...
	
	}

	...

	val player = ExternalExoPlayer(context)


	val adPodcastManagerSettings = AdPodcastManagerSettings.Builder()
			 .adPlayerInstance(player)
		 
	val adPodcastManager = AdswizzAdPodcastManager(adPodcastManagerSettings.build())

	...
	adPodcastManager.play(uri)
```

## AdPlayer Interface

```kotlin
interface AdPlayer {

    // region VAST Related

    // Player name
    val name: String

    // Player version.
    val version: String

    // Player capabilities as described in VAST document
    val playerCapabilities: List<PlayerCapabilities>

    // Player state as described in VAST document
    val playerState: List<PlayerState>

    // endregion


    // region Player Options

    // The cache assets hint; the AdPlayer may or may not implement this AdManager hint
    var cacheAssetsHint: Boolean

    // The enqueue functionality hint; the AdPlayer may or may not implement this AdManager hint
    var enqueueEnabledHint: Boolean

    /**
     * This val only counts for server side playback (streams)
     *
     * @returns true if the player buffers the content while paused and starts from that same point when it resumes
     * @returns false if the player does not buffer while paused, and when it resumes the playback starts from the live frame of the stream
     */
    val isBufferingWhilePaused: Boolean

    // endregion


    // region Player Controls

    fun load(creativeUrlString: String)

    fun play()

    fun pause()

    fun reset()

    fun release() = Unit

    fun seekToTrackEnd()

    /**
     * @since 7.7.0
     */
    fun seekTo(position: Double)

    // endregion


    // region Enqueue Related

    fun enqueue(creativeUrlString: String, index: Int) {
        /* does nothing */
    }

    fun dequeue(index: Int) {
        /* does nothing */
    }

    // endregion


    // region Current Values

    // Current player volume. The value will be between 0.0f and 1.0f
    var volume: Float

    // the current playback time in seconds
    fun getCurrentTime(): Double

    // the current track duration in seconds
    fun getDuration(): Double?

    // the current status for a player. The enum with all possible values is defined below
    fun status(): Status

    // endregion

    // region Video

    /**
     * Sets a surface to render the video content
     *
     * @param surface - the surface to be used to render the video content
     *
     * @since 7.3.0
     */
    fun setVideoSurface(surface: Surface) {
        /* does nothing */
    }

    /**
     * Removes a surface previously set to render the video content
     *
     * @param surface - the surface that was used to render the video content
     *
     * @since 7.3.0
     */
    fun clearVideoSurface(surface: Surface) {
        /* does nothing */
    }

    /**
     * Sets the video view state for viewability purposes
     *
     * @param videoState - the state of the video view. Possible values: MINIMIZED, COLLAPSED, NORMAL, EXPANDED, FULLSCREEN
     *
     * @since 7.3.0
     */
    fun setVideoState(videoState: AdVideoState?) {
        /* does nothing */
    }

    // endregion

    // region Listener

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    interface Listener {

        /**
         * @param index: if enqueue is used then index should represent the loading item index. If enqueue is not used then index should always be null
         */
        fun onLoading(index: Int?)

        /**
         * @param index: if enqueue is used then index should represent the loading item index. If enqueue is not used then index should always be null
         */
        fun onLoadingFinished(index: Int?)

        fun onBuffering()

        fun onBufferingFinished()

        fun onPlay()

        fun onPause()

        fun onResume()

        fun onEnded()

        /**
         * @param currentTrackIndex: index of the current track
         */
        fun onSeekToTrackEnd(currentTrackIndex: Int)

        /**
         * @param newTrackIndex: index of the new track
         */
        fun onTrackChanged(newTrackIndex: Int) {
            // default implementation does nothing. This is only needed for enqueue functionality
        }

        /**
         * Fire this callback on error
         */
        fun onError(error: String)
        
        /**
         * Fire this when you want to skip with an error
         * this function will not fire skip tracking but error tracking with vast code 400
         *
         * @param error - error which is the reason for this skipAd call
         */
        fun onSkipAd(error: Error){
            // default implementation does nothing.
        }

        fun onMetadata(metadataList: List<MetadataItem>) {
            // default implementation does nothing. This is only needed for server side insertion
        }

        fun onVolumeChanged(volume: Float) {
            // default implementation does nothing
        }

        /**
         * Called when video size changes and also when it starts with the initial values
         *
         * @param player - this player
         * @param width - the video width
         * @param height - the video height
         *
         * @since 7.3.0
         */
        fun onVideoSizeChanged(player: AdPlayer, width: Int, height: Int) {
            // default implementation does nothing
        }
    }

    // endregion


    // region Helpers

    data class MetadataItem(val key: String, val value: String)

    enum class Status {
        // The player is initialized but not playing and does not have an item to play. This should be the default state
        INITIALIZED,

        // The player failed to load the item
        FAILED,

        // The player is about to begin buffering
        BUFFERING,

        // The player has finished buffering
        BUFFERING_FINISHED,

        // The player is playing the item
        PLAYING,

        // The player has been paused
        PAUSED,

        // The player has finished the whole item. This would be the last state for an item
        FINISHED,

        // The player state is unknown.
        UNKNOWN;
    }

}
```

Keep in mind that you need to call the right events on the listener so that the adManager knows to take the right actions.

### Name and version

The player name and version can be any strings that you want. They will identify the player used.

### Player capabilities

Below is a quote from [VAST_4.2_final_june26.pdf](https://iabtechlab.com/wp-content/uploads/2019/06/VAST_4.2_final_june26.pdf) representing a description of all player capabilities:

```text
● skip to indicate the user's ability to skip the ad
● mute to indicate the user's ability to mute/unmute audio
● autoplay to indicate the player's ability to autoplay media with audio, also implies mautoplay
● mautoplay to indicate the player's ability to autoplay media when muted
● fullscreen to indicate the user's ability to enter fullscreen
● icon to indicate the player's ability to render NAI icons from VAST
```

In the SDK, this list is represented with the following enum:

```kotlin
enum class PlayerCapabilities(val rawValue: String) {
    SKIP("skip"),
    MUTE("mute"),
    AUTOPLAY("autoplay"),
    MAUTOPLAY("mautoplay"),
    FULLSCREEN("fullscreen"),
    ICON("icon");
}
```

The ```playerCapabilities``` represents a list of the supported player capabilities.

### Player State

Currently, there are 2 player states defined in the VAST document ([VAST_4.2_final_june26.pdf](https://iabtechlab.com/wp-content/uploads/2019/06/VAST_4.2_final_june26.pdf)), as follows:

```text
● muted to indicate the player is currently muted
● fullscreen to indicate the player is currently full screen
```

In the SDK, this list is represented with the following enum:

```kotlin
enum class PlayerState(val rawValue: String) {
    MUTED("muted"),
    FULLSCREEN("fullscreen"),
    FOREGROUND("foregrounded"),
    BACKGROUND("backgrounded");
}
```

The ```playerState``` represents a list of the current states in which the player is.

### cacheAssetsHint property

If true, then the user expects that the audio/video media files are cached by the player. This is a hint, so the AdPlayer implementation may or may not take it into consideration.
If the property is set to true and the player in use is the *internal player*, the SDK will start downloading the assets one by one. The ideal way of using the cache is to wait for all the assets to be downloaded; otherwise,  
enqueue should be enough for your needs. If you still decide to play the ads before the download is complete, it will cancel the rest of the downloads, and the playback will fall back on enqueue/load depending  
on the selected settings. The playback will be instant because the already downloaded data is used right away.


### enqueueEnabledHint property

If true, then the user expects that the audio/video media files are enqueued in a playlist. This means that the playing will be smoother than when playing one by one. Also, this is only a hint. The SDK is able to work even if the AdPlayer implementation does not take this hint into consideration.

### isBufferingWhilePaused property

The value returned by this property is used only for server-side playback (streams). Return true if the player buffers the content while paused and starts from that same point when it resumes. Return false if the player does not buffer the content while paused, and when it resumes the playback starts from the live frame of the stream.

### load function

The ```fun load(creativeUrlString: String)``` function informs the AdPlayer that it should start the loading the media file. It is called by the AdManager when it detects that the loading hasn't started. This means that the ```fun onLoading(index: Int?)``` function was not previously called.

If enqueue is disabled, then most likely the loading will happen after the ```load(creativeUrlString: String)``` function is called.

When this function is called, the AdPlayer implementation should start loading the item and respond with the ```fun onLoading(index: Int?)``` callback when the loading of the media file is about to begin and with ```fun onLoadingFinished(index: Int?)``` when the loading has been completed.

### play function

When the AdManager wants the playing to begin for the first time, it will call ```fun play()```. For this case, the AdPlayer will respond with ```fun onPlay()```. When the AdManager wants the playing to resume after a pause it will also call ```fun play()``` but this time the AdPlayer should respond with ```fun onResume()```.

During playback, when the current track ends and the next one starts playing, the ```fun onTrackChanged(newTrackIndex: Int)``` should be called.

### pause function

There may be a time when the user wants to pause the AdManager. When this happens the AdManager will call ```fun pause()```. In this case the AdPlayer will respond with ```fun onPause()```.

### reset function

The ```fun reset()``` function is called when the AdManager wants to stop everything and bring the AdPlayer to its initial state.

### release function

The ```fun release() = Unit``` function is called when any instance of: `AdManager`, `AdswizzAdStreamManager`,`AdswizzAdPodcastManager` wants to free up the resources used is their lifecycle.

### seekToTrackEnd function

The ```fun seekToTrackEnd()``` function is called when the AdManager wants the player to seek to the current track's end.

### enqueue function

During execution of the ```adManager.prepare()``` function, the AdManager will call the ```fun enqueue(creativeUrlString: String, index: Int)``` function for each ad.  
The index represents the ad position in the AdManager, starting at 0. The creativeUrlString represents the ad media file.

When this function is called, the AdPlayer implementation may start loading the enqueued items and respond with the ```fun onLoading(index: Int?)``` callback when the loading of a media file is about to begin, and with ```fun onLoadingFinished(index: Int?)``` when the loading has been completed.

### dequeue function

There may be certain situations when the player wants to remove an ad from the playlist. In this case, it will call ```fun dequeue(index: Int)```. The index represents the ad position in the AdManager that should be removed.

### Player volume

The ```volume``` variable can be used to get the player's current volume or to set the player current volume. The values are between 0.0f and 1.0f; 0.0f means muted and 1.0f means max volume.

### getCurrentTime and getDuration

The ```fun getDuration(): Double?``` function should return the duration of the track in seconds when it can be obtained from the media resource; otherwise, null.
The ```fun getCurrentTime(): Double``` function should return the current position of the playhead in the current track in seconds. If no media resource exists, then 0.0 should be returned. The value returned by this function should be between 0.0 and the duration returned by ```getDuration()```. When the playing finishes at the end of the track, the value returned should be equal to ```getDuration()```.

### status function

The current status for a player is obtained by the AdManager using the ```fun status(): Status``` function.

The enum with all possible values is defined below:

```kotlin
enum class Status {
    // The player is initialized but not playing and does not have an item to play. This should be the default state
    INITIALIZED,

    // The player failed to load the item or failed to play the item
    FAILED,

    // The player is about to begin buffering
    BUFFERING,

    // The player has finished buffering
    BUFFERING_FINISHED,

    // The player is playing the item
    PLAYING,

    // The player has been paused
    PAUSED,

    // The player has finished the whole item. This would be the last state for an item
    FINISHED,

    // The player state is unknown
    UNKNOWN;
}
```

The initial player status should be ```INITIALIZED```. It should have this value also after ```fun reset()``` is called.
If the loading fails, the ```FAILED``` status should be set. Also, this should be set when an error occurs.
During playback, if the player needs to buffer the media, then it should change the status to ```BUFFERING``` and, when the buffering ends, it should change it to ```BUFFERING_FINISHED```. When it starts buffering it should also call ```fun onBuffering()```, and when it finishes, ```fun onBufferingFinished()```. In this way the AdManager will be notified that buffering is started or has completed.
When the player actively plays the ad, the status should be ```PLAYING``` and, if it is paused, the status should be ```PAUSED```.
When the player finishes to playing a track, the status should change to ```FINISHED```. It should also call ```fun onEnded()``` to notify the AdManager that the playback has finished.

### video functions

If the player needs to render video content, then there are a couple of functions to implement:
- ```setVideoSurface```: this should provide a ```Surface``` to the player and it should use it to render the video content.
- ```clearVideoSurface```: when this is called, the ```Surface``` should be removed and the player should stop rendering the video in it. The playback may continue.

Just before you start rendering to the surface, you should call ```fun onVideoSizeChanged(player: AdPlayer, width: Int, height: Int)``` so that the listener knows the initial frame size of the video. Whenever this size changes, you should call it again with the new values, just before the frame with the new size is rendered.

Also, for macro expansion purposes it is recommended to implement the ```setVideoState``` function, which will be called when the state of the video view changes. This should be useful to determine if ```FULLSCREEN``` will be returned when calling ```playerState```.


### listener functions

The AdPlayer functions ```fun addListener(listener: Listener)``` and ```fun removeListener(listener: Listener)``` should be used to add and remove a listener.

All AdManagers (client-side manager, server-side manager and podcast manager) register themselves using the above function in order to receive events from the player . It is the 
responsibility of your player to forward events from player to manager using the AdPlayer.Listener interface.

For a complete SDK functionality, all the callback in the AdPlayer.Listener should be called when your player sends events to your object, in order to forward these events to the AdManager. For a complete implementation, use the sample code provided in StreamingSample with External Player in the sample directory.

Some additional callbacks from the ```Listener``` interface are explained below.
Whenever an error occurs, the ```fun onError(error: String)``` callback should be called.

When doing server-side insertion (streams), the ```fun onMetadata(metadataList: List<MetadataItem>)``` function should be called whenever there is metadata in the stream. This is very important for streams. 

It is also required if your player is used in a podcast context for reporting any ID3 metadata formats to enable [Remote Audio Data](#remote-audio-data) measurement. The MetadataItem should have a "RAD" key
and the corresponding RAD json string as value.
For client-side insertion, this callback is not used.

Whenever the player volume changes, the ```fun onVolumeChanged(volume: Float)``` callback should be called.


## AdPlayer.Listener Interface

```kotlin
   interface Listener {

        /**
         * @param index: if enqueue is used then index should represent the loading item index.
         * If enqueue is not used then index should always be null
         */
        fun onLoading(index: Int?)

        /**
         * @param index: if enqueue is used then index should represent the loading item index.
         * If enqueue is not used then index should always be null
         */
        fun onLoadingFinished(index: Int?)

        /**
         * Fire this callback when the buffering has started
         */
        fun onBuffering()

        /**
         * Fire this callback when the buffering has finished
         */
        fun onBufferingFinished()

        /**
         * Fire this callback when the play starts
         */
        fun onPlay()

        /**
         * Fire this callback when the player has paused
         */
        fun onPause()

        /**
         * Fire this callback when the player resumed
         */
        fun onResume()

        /**
         * Fire this callback when the player has finished playing
         */
        fun onEnded()

        /**
         *
         * @param currentTrackIndex: index of the current track
         */
        fun onSeekToTrackEnd(currentTrackIndex: Int)

        /**
         * On track changed.
         *
         * @param newTrackIndex - index of the new track
         */
        fun onTrackChanged(newTrackIndex: Int) {
            // default implementation does nothing. This is only needed for enqueue functionality
        }

        /**
         * Fire this callback on error
         */
        fun onError(error: String)

        /**
         * Fire this callback when metadata is received in streaming
         * It is fired only on server side insertion
         * @param metadataList: list of metadata items received in stream
         */
        fun onMetadata(metadataList: List<MetadataItem>) {
            // default implementation does nothing. This is only needed for server side insertion or podcasting
        }

        /**
         * Fire this callback when player volume has changed
         */
        fun onVolumeChanged(volume: Float) {
            // default implementation does nothing
        }

        /**
         * Called when video size changes and also when it starts with the initial values
         *
         * @param player - this player
         * @param width - the video width
         * @param height - the video height
         *
         * @since 7.3.0
         */
        fun onVideoSizeChanged(player: AdPlayer, width: Int, height: Int) {
            // default implementation does nothing
        }
    }
```


## Sample AdPlayer Interface Implementation

```kotlin
	class ExternalExoPlayer(val context: Context) : AdPlayer, Player.EventListener, AnalyticsListener, CacheManager.Listener, AudioListener, MetadataOutput, VideoListener {
		...
		override val name = "ExternalExoPlayer"
	    override val version = "1.23.456"
    	override val playerCapabilities: List<PlayerCapabilities> = listOf(PlayerCapabilities.SKIP, PlayerCapabilities.MUTE, PlayerCapabilities.FULLSCREEN)
		override var cacheAssetsHint: Boolean = true
	    override var enqueueEnabledHint: Boolean = true
    	override val isBufferingWhilePaused: Boolean = true
    	
    	...
    	private var listenerList: ConcurrentLinkedQueue<WeakReference<AdPlayer.Listener>> = ConcurrentLinkedQueue()

    	private val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context)
	    private val mediaSources = ConcatenatingMediaSource()
	    
	    ...
	    init {
			player.addListener(this)
			player.addMetadataOutput(this)
			player.addAudioListener(this)
			player.addAnalyticsListener(this)
			player.addVideoListener(this)
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
				player.prepare(mediaSources)
			}
		}

		private fun buildMediaSource(urlString: String): MediaSource {
			val uri = Uri.parse(urlString)
			val undecoratedUri = Uri.parse(urlString).buildUpon().clearQuery().build()

			when {
				urlString.startsWith("rawresource://", true) -> {
					val dataSourceFactory =
						DataSource.Factory { RawResourceDataSource(CommonContext.getContext()) }
					val extractorsFactory: DefaultExtractorsFactory =
						DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
					return ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
						.setTag(urlString).createMediaSource(uri)
				}
				urlString.startsWith("file:///", true) -> {
					val dataSourceFactory = DataSource.Factory { FileDataSource() }
					val extractorsFactory: DefaultExtractorsFactory =
						DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
					return ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
						.setTag(urlString).createMediaSource(uri)
				}
				else -> {
					val dataSourceFactory =
						DefaultHttpDataSourceFactory(com.adswizz.common.Utils.getDefaultUserAgent())

					when (Util.inferContentType(undecoratedUri)) {
						C.TYPE_DASH -> {
							return DashMediaSource.Factory(dataSourceFactory).setTag(urlString)
								.createMediaSource(uri)
						}
						C.TYPE_HLS -> {
							return HlsMediaSource.Factory(dataSourceFactory).setTag(urlString)
								.createMediaSource(uri)
						}
						C.TYPE_SS -> {
							return SsMediaSource.Factory(dataSourceFactory).setTag(urlString)
								.createMediaSource(uri)
						}
						else -> {
							val extractorsFactory: DefaultExtractorsFactory =
								DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
							return if (cacheAssetsHint) {
								ProgressiveMediaSource.Factory(
									CacheManager.getCacheDataSourceFactory(
										dataSourceFactory
									), extractorsFactory
								)
									.setTag(urlString)
									.createMediaSource(uri)
							} else {
								ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
									.setTag(urlString).createMediaSource(uri)
							}
						}
					}
				}
			}
		}

		override fun play() {
			if (cacheAssetsHint) {
				CacheManager.cancelAllDownloads()
			}
			player.playWhenReady = true
		}

		override fun pause() {
			player.playWhenReady = false
		}

		override fun reset() {
			player.stop(true)
			player.playWhenReady = false
			status = AdPlayer.Status.INITIALIZED
			duration = null

			mediaSources.clear()
			playlist.clear()
			playingIndex = -1
			seekInProgress = false
		}

        override fun release(){
            reset()
            player.removeVideoListener(this)
            player.removeAnalyticsListener(this)
            player.removeAudioListener(this)
            player.removeMetadataOutput(this)
            player.removeListener(this)
            player.release() 
        }

		override fun seekToTrackEnd() {
			try {
				seekInProgress = true
				player.seekTo(playingIndex + 1, 0L)
			} catch (e: Exception) {
				seekInProgress = false
				if (status != AdPlayer.Status.FINISHED) {
					status = AdPlayer.Status.FINISHED
					listenerList.forEach { it.get()?.onSeekToTrackEnd(playingIndex) }
				}
			}
		}

		override fun seekTo(position: Double) {
			try {
				seekInProgress = true
				player.seekTo(player.currentWindowIndex, (position * 1000.0).toLong())
			} catch (e: Exception) {
				seekInProgress = false
			}
		}

		// endregion
		
		// region Enqueue Related

		override fun enqueue(creativeUrlString: String, index: Int) {
			if (cacheAssetsHint && !creativeUrlString.startsWith("rawresource://", true))
				CacheManager.addAssetToCache(creativeUrlString, this)

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
						player.prepare(mediaSources)
					}
				}
			}
		}

		override fun dequeue(index: Int) {
			if ((index >= 0) && (index < playlist.size)) {
				if (cacheAssetsHint) {
					val tag = mediaSources.getMediaSource(index).tag.toString()
					if (!tag.startsWith("rawresource://", true))
						CacheManager.removeAssetFromCache(tag)
				}

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
		
		   // region Listener

    override fun addListener(listener: AdPlayer.Listener) {
        cleanupNullsInListenerList()

        listenerList.forEach {
            if (it.get() == listener) {
                return
            }
        }
        listenerList.add(WeakReference(listener))
    }

    override fun removeListener(listener: AdPlayer.Listener) {
        cleanupNullsInListenerList()
        listenerList.forEach {
            if (it.get() == listener) {
                listenerList.remove(it)
                return
            }
        }
    }

    private fun cleanupNullsInListenerList() {
        listenerList.forEach {
            if (it.get() == null) {
                listenerList.remove(it)
            }
        }
    }

    // endregion


    //region Player Event Listener

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                if (playingIndex >= 0) {
                    status = AdPlayer.Status.BUFFERING
                    if (!seekInProgress)
                        listenerList.forEach { it.get()?.onBuffering() }
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
                        listenerList.forEach { it.get()?.onBufferingFinished() }
                }

                when (playWhenReady) {
                    true -> {
                        val oldStatus = status
                        if (status != AdPlayer.Status.PLAYING) {
                            status = AdPlayer.Status.PLAYING
                            if (oldStatus == AdPlayer.Status.PAUSED) {
                                listenerList.forEach { it.get()?.onResume() }
                            } else {
                                loadCompleted(playingIndex)     // this is needed because ExoPlayer calls onPlayerStateChanged before onLoadCompleted
                                listenerList.forEach { it.get()?.onPlay() }
                            }
                        }
                    }
                    false -> {
                        if (status == AdPlayer.Status.PLAYING) {
                            status = AdPlayer.Status.PAUSED
                            listenerList.forEach { it.get()?.onPause() }
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
                        listenerList.forEach { it.get()?.onEnded() }
                    }
                }
            }
            Player.STATE_IDLE -> {
                status = AdPlayer.Status.INITIALIZED
            }
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super<Player.EventListener>.onPlayerError(error)
        status = AdPlayer.Status.FAILED
        val errorMessage = "Something went wrong with external exoplayer: ${error?.message}"
        listenerList.forEach { it.get()?.onError(errorMessage) }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        var posChanged = false
        if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {  // transition from one track to the next
            playingIndex++
            duration = player.duration.toDouble() / 1000
            listenerList.forEach { it.get()?.onTrackChanged(playingIndex) }
            posChanged = true
        } else if (reason == Player.DISCONTINUITY_REASON_SEEK) {    // triggered by seek to end of track function
            listenerList.forEach { it.get()?.onSeekToTrackEnd(playingIndex) }
            playingIndex++
            duration = player.duration.toDouble() / 1000
            posChanged = true
        }

        if (cacheAssetsHint && posChanged) {
            val cancelIndex = playingIndex - 1
            if (cancelIndex >= 0 && cancelIndex < mediaSources.size) {
                CacheManager.cancelDownload(mediaSources.getMediaSource(playingIndex - 1).tag.toString())
            }
        }
    }

    // endregion


    // region MetadataOutput

    override fun onMetadata(metadata: com.google.android.exoplayer2.metadata.Metadata) {
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
                    is Id3Frame -> {
                        // do nothing
                    }
                    is EventMessage -> {
                        // do nothing
                    }
                }
            }
        }
        listenerList.forEach { it.get()?.onMetadata(metadataList) }
    }

    //endregion
    
    	//region forward events
    	
		override fun onLoadStarted(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData) {
			eventTime?.windowIndex?.let { wi ->
				if ((wi >= 0) && (wi < playlist.size)) {
					val item = playlist.elementAt(wi + removedMediaSourcesCount)
					item.assetState = AssetState.LOADING
					if (item.lastLoadingCallbackSent == LastLoadingCallbackSent.NONE) {
						item.lastLoadingCallbackSent = LastLoadingCallbackSent.LOADING
						val i = if (enqueueEnabledHint) wi + removedMediaSourcesCount else null
						listenerList.forEach { it.get()?.onLoading(i) }
					}
				}
			}
		}

		override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData) {
			eventTime?.windowIndex?.let { wi ->
				loadCompleted(wi + removedMediaSourcesCount)
			}
		}

		override fun onLoadError(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData, error: IOException, wasCanceled: Boolean) {
			eventTime.windowIndex.let { wi ->
				if ((wi >= 0) && (wi < playlist.size)) {
					playlist.elementAt(wi).assetState = AssetState.FAILED

					if(wi >= 0 && wi < mediaSources.size) {
						if(mediaSources.getMediaSource(wi).tag == loadEventInfo?.uri?.toString()) {
							mediaSources.removeMediaSource(wi)
							if(enqueueEnabledHint) {
								removedMediaSourcesCount++
							}
						}
					}
					val errorMessage = "ExoPlayer failed to load media: ${loadEventInfo.uri} with ${error.message}"
					listenerList.forEach { it.get()?.onError(errorMessage) }
				}
			}
		}

		override fun onLoadCanceled(eventTime: AnalyticsListener.EventTime, loadEventInfo: MediaSourceEventListener.LoadEventInfo, mediaLoadData: MediaSourceEventListener.MediaLoadData) {
			eventTime?.windowIndex?.let { wi ->
				if ((wi >= 0) && (wi < playlist.size))
					playlist.elementAt(wi).assetState = AssetState.CANCELED
			}
		}

		private fun loadCompleted(index: Int) {
			if ((index >= 0) && (index < playlist.size)) {
				val item = playlist.elementAt(index)
				item.assetState = AssetState.COMPLETED
				if (item.lastLoadingCallbackSent == LastLoadingCallbackSent.LOADING) {
					item.lastLoadingCallbackSent = LastLoadingCallbackSent.LOADING_FINISHED
					val i = if (enqueueEnabledHint) index else null
					listenerList.forEach { it.get()?.onLoadingFinished(i) }
				}
			}
		}
		
		override fun onVolumeChanged(volume: Float) {
			super<AudioListener>.onVolumeChanged(volume)
			listenerList.forEach { it.get()?.onVolumeChanged(volume) }
		}

		// endregion

		// region VideoListener

		override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
			listenerList.forEach { it.get()?.onVideoSizeChanged(this, width, height) }
		}

		//endregion
    
	}
```

## Cache Manager
Pre-caching the media (creative) assets will enable for your app to serve audio/video ads in poor network conditions or possibly when a network connection is not available. This will improve user experience, because an ad can play instantly, and prevent buffering mid ad.
The **CacheManager** is a singleton that enables us to cache, and also playback media that is already cached. It works in close relation with the player, so if you have your own implementation of the AdPlayer interface
you can use it, but also ignore it. Right now it only works for players that are based on **ExoPlayer**.

### Working with CacheManager object

There is a listener that you can implement to receive updates about the status of a cache object (**CacheManager.Listener**)
```kotlin
CacheManager {
//...
    interface Listener {
        fun onDownloadStarted(assetUri: String)
        fun onDownloadCompleted(assetUri: String)
        fun onDownloadFailed(assetUri: String, error: Error)
    }
    //...
}
```
To add an asset to the download queue you can call
```kotlin
CacheManager.addAssetToCache(assetUri: String, listener: Listener? = null)
```
where assetUri is the URL for the asset, and the listener is your implementation of the above interface. As you can see, the listener is optional. If you add
the listener here, you will only receive updates for this particular asset.

```kotlin
CacheManager.cancelDownload(assetUri: String) // Cancels the download specify by assetUri

CacheManager.removeListener(assetUri: String, listener: Listener) // removes all the listeners for that specific assetUri

CacheManager.addGlobalListener(listener: Listener) // adds a listener that receives callbacks for every assetUri
CacheManager.removeGlobalListener(listener: Listener) // removes a global listener
```

The **url** of an asset acts as its **ID** in the cache. Playing an asset from cache is straight-forward:
When you are creating the media source for your exoplayer instance, you can get the CacheDataSourceFactory from the CacheManager as follows:

```kotlin
CacheManager.getCacheDataSourceFactory(upstreamFactory: DefaultHttpDataSourceFactory)
```
This returns a CacheDataSourceFactory. An usage example would look something like this:
```kotlin
private fun buildMediaSource(urlString: String): MediaSource {
        val dataSourceFactory = DefaultHttpDataSourceFactory("AdSDK")
        val uri = Uri.parse(urlString)
        return ProgressiveMediaSource.Factory(CacheManager.getCacheDataSourceFactory(dataSourceFactory))
                            .setTag(urlString)
                            .createMediaSource(uri)

        }
```