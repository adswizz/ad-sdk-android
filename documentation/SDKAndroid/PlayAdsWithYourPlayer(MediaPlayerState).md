# Playing ads using your player using MediaPlayerState interface [BETA]

Since 7.9.3 release, the **_AdswizzSDK_** will provide a new event driven integration method. The `MediaPlayerState` interface will use a more Kotlin friendly approach in order to pass your player events to the `AdManager`s

> **Note**
> This MediaPlayerState approach is currently in beta release and it may be changed during the following releases after processing your feedback.
> For now, the MediaPlayerState will be available only in Client Side insertion scenarios without video ads capabilities.


## Client-side setup 

Similar with `AdPlayer` interface, once you receive the `AdManager` instance through an `AdswizzAdRequest`, you will be able to set your MediaPlayerState implementation to the **_AdManagerSettings_** builder like follows:


```kotlin
	AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withZones(setOf(AdswizzAdZone("ZONEID_1_PROVIDED_BY_PIM"), ... AdswizzAdZone("ZONEID_n_PROVIDED_BY_PIM")))
            .build { adswizzAdRequest ->
			
			val adRequestConnection = AdRequestConnection(adRequest) // Create the AdRequestConnection using the above adRequest

			//request ads
			adRequestConnection.requestAds { adManager, error ->
					// Handle the response from server
				if (adManager != null) {
                            //Handle success
                            adManager.adManagerSettings =
                                AdManagerSettings.Builder() 
                                    .mediaPlayerStateInstance(YOUR_MEDIA_PLAYER_STATE_INSTANCE_GOES_HERE) 
                                    .assetQuality(AssetQuality.MEDIUM)          // optional, other possible values AssetQuality.LOW, AssetQuality.HIGH, AssetQuality.AUTO
                                    .build()                                    // optional
                            adManager.setListener(this) // Get notifications from the Ad SDK
                            
                            
                            /*
                                Here you can iterate the ads array and set them in your player.
                            */
                            adManager.ads.foreach{ adData ->
                                 loadAdInYourPlayer(adData.mediaUrlString)
                            	// load item.url with item.id as unique media item id
                            	// and after loading all items prepare the player
                            }
                                                        
                            adManager.prepare() // The AdManager will start observe the MediaPlayerState events
                        
                            
                            /*
                            	Here you can start your player
                            */
                            //player.play()
                            
                        } else {
                            //Handle failure
                            Log.e("Error", error.toString())
                        }
                    }
			}
			
    }

```


## MediaPlayerState interface

**MediaPlayerState** aims to provide a simpler communication mechanism based on `kotlinx.coroutines.SharedFlow` by emitting `PlayerEvent`s resulted during the ads playback.

```kotlin
interface MediaPlayerState {

	// Playhead position of currently playing item
    val currentPlayhead: Duration

    // The media item total duration.
    val currentDuration: Duration

    // This is the event channel which the player uses to comunicate to sdk
    // player events changes (like buffering, pause, resume, playing, etc.)
    val playerStatusFlow: SharedFlow<PlayerEvent>

    // Reports the volume for the player. This value should be between [0..1].
    // A value of 0.0 indicates silence; a value of 1.0 (the default) indicates full audio volume for the player instance.
    val playerVolume: Float

    // Used for targetability purposes.
    // A list of player capabilities.
    val playerCapabilities: List<PlayerCapabilities>

    // Used for targetability purposes.
    // A list of player states as described by the IAB, which will help achieve better targetability.
    val playerState: List<PlayerState>

	//Type of generic errors player should send. They are VAST compliant errors
    sealed class PlayerError(err: Exception){
        data class GeneralError(val error: Exception) : PlayerError(error)
        data class FileNotFound(val error: Exception) : PlayerError(error)
        data class Timeout(val error: Exception) : PlayerError(error)
        data class MediaFileNotSupported(val error: Exception) : PlayerError(error)
        data class MediaFileDisplayError(val error: Exception) : PlayerError(error)
    }

    /**
     * Various player states send as event changes through playerStatusFlow
     */
    sealed class PlayerEvent(open val id: String){
        /*
           This event is triggered when the player starts loading the media file (ad creative) for the corresponding ad.
              - here you have to pass an id, that is the instanceId from AdData
        */
        data class Loading(override val id: String): PlayerEvent(id)

        /*
            This event is triggered when the player finishes loading the media file (ad creative) for the corresponding ad.
              - here you have to pass an id, that is the instanceId from AdData
        */
        data class LoadingFinished(override val id: String): PlayerEvent(id)

        /*
           This event is triggered when the media file (ad creative) for the corresponding ad failed to load or failed during playback.
              - here you have to pass an id, that is the instanceId from AdData
        */
        data class Failed(override val  id: String, val error: PlayerError): PlayerEvent(id)


        /*
          This event is triggered when the playback has paused when a buffering state occurred.
          This event should be triggered only for current playing ad creative.
               - here you have to pass an id, that is the instanceId from AdData
        */
        data class Buffering(override val id: String): PlayerEvent(id)

        /*
          This event is triggered when a media item has resumed after buffering finishes.
          This event should be triggered only for current media file (ad creative) during its playing phase
            - here you have to pass an id, that is the instanceId from AdData
        */
        data class BufferingFinished(override val id: String): PlayerEvent(id)

        /*
          This event should be triggered when your player will begin to play the media file (ad creative).
            - here you have to pass an id, that is the instanceId from AdData
        */
        data class StartPlaying(override val id: String): PlayerEvent(id)

        /*
          This event should be triggered when your player paused current the media file (ad creative).
             - here you have to pass an id, that is the instanceId from AdData
        */
        data class Pause(override val id: String): PlayerEvent(id)

        /*
          This event should be triggered when your player resumes the current media file (ad creative).
             - here you have to pass an id, that is the instanceId from AdData
        */
        data class Resume(override val id: String): PlayerEvent(id)

        /*
          This event should be triggered when your player finishes playing the current media file (ad creative).
              - here you have to pass an id, that is the instanceId from AdData
        */
        data class Finished(override val id: String): PlayerEvent(id)

        /*
           This event should be triggered when your player skipped the current media file (ad creative).
              - here you have to pass an id, that is the instanceId from AdData and an Optional PlayerError.
              - error: PlayerError should be passed when the skip action is done due to Playback Exceptions.
        */
        data class SkipAd(override val id: String, val error: PlayerError? = null): PlayerEvent(id)
    }
}
```


## Events sent by MediaPlayerState implementation. 

Events sent by MediaPlayerState implementation are similar to AdPlayer.Listener function callbacks and should be sent in a specific order.

`Loading` and `LoadingFinished`. This pair of events should be sent foreach `media items` ( `ads` ) when the player starts loading the corresponding items. These events are needed for firing VAST related events

`Buffering` and `BufferingFinished`. This pair of events should be sent every time player is stalling while buffering current content, but only during the playing phase of the items. `Buffering` and `BufferingFinished` during loading phase servers no purpose in SDK.
The mediaId should reflect current item playing when buffering occurred. If there is buffering or caching for other media item than the current one, these events should not be triggered and can cause SDK to trigger duplicate vast events.

`Failed` events should be sent any time the player raise an exception no matter where on the playing timeline occurs. They can be triggered in the loading part or when playing and for each item that raise the exception.

`StartPlaying` events should be sent every time the player changes to the playing the current item. 

`Finish` should be sent for each media item that was scheduled to play even if the coresponding media item failed during loading time. This event must be paired with StartPlaying
if the corresponding media item has finished loading and started playing or Failed event if the corresponding mediat  item failed at loading time.

`Pause` and `Resume`. This pair of events should be sent each time the player is paused and resumed. 

`SkipAd` should be sent if the media item is skipped during playing it


**** Note: All paired events: `Loading` and `LoadingFinished`, `Buffering` and `BufferingFinished`, `Pause` and `Resume` should be sent in pairs except when a `Failed` event or `Finish` event is triggered and closing events of the pair
might be missing (LoadingFinished, BufferFinished, Resume)


## Events diagram
Here is a diagram showcasing order of events that should be triggered by MediaPlayerState implementation

<img src="../img/MediaPlayerState-events-order.png" width="1000" />
