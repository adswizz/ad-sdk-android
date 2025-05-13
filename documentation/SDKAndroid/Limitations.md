# Limitations when using **_AdswizzSDK_** with your Exoplayer instance

The **_AdswizzSDK_** interacts with your **AdPlayer** implementation on the **main thread**. Additionally, all SDK API calls are expected to be made **from the main thread**.
Calling the SDK from a different thread, or using an ExoPlayer instance bound to a different looper, will result in **runtime exceptions**.

## AdPlayer implementation called by SDK

If your **ExoPlayer** instance operates on the main thread, implementing the AdPlayer interface is straightforward. SDK calls to your AdPlayer implementation will occur on the same stack, allowing direct invocation of ExoPlayer methods, like so:

```kotlin
class MyAdPlayer(private val exoPlayer: ExoPlayer) : AdPlayer {
	...
	fun getCurrentTime(): Double{
   		return exoPlayer.currentPosition
	}

    // the current track duration in seconds
	fun getDuration(): Double?{
	    return exoPlayer.duration.toDouble()
	}
	
	
	override fun load(creativeUrlString: String) {
		...
		exoPlayer.setMediaSource(mediaSources) //or other player method
		exoPlayer.prepare()
		...
	}
	
	override fun play() {
        exoPlayer.playWhenReady = true
    }

    override fun pause() {
        exoPlayer.playWhenReady = false
    }
	...
}
```


## When ExoPlayer Is Running on a Different Looper

If your **ExoPlayer** instance is not attached to the main looper (e.g., initialized with a custom HandlerThread), direct calls from the **_AdswizzSDK_** to your **AdPlayer** implementation—executed on the main thread—will result in runtime exceptions. This is because **ExoPlayer** enforces thread affinity and expects all method calls to be made from its own looper thread.

Such violations will typically result in an exception like the following:

```kotlin
	java.lang.IllegalStateException: Player is accessed on the wrong thread.
	Expected thread: 'ExoPlayer:Handler'
	Actual thread: 'main'
	
	
	class MyAdPlayer(private val exoPlayer: ExoPlayer) : AdPlayer {

		override fun playAd(adUri: Uri) {
			// This call is made from the main thread, but ExoPlayer expects its own looper
			exoPlayer.setMediaItem(MediaItem.fromUri(adUri))
			exoPlayer.prepare()
			exoPlayer.play() // Will throw exception if ExoPlayer uses a different thread
		}
	}
	
```

To avoid this, you'll need to post the calls to ExoPlayer’s handler or use a wrapper that ensures thread-safe communication.

```kotlin
class MyAdPlayer(private val exoPlayer: ExoPlayer) : AdPlayer {
    private val playerHandler = Handler(exoPlayer.applicationLooper)
	
	fun getCurrentTime(): Double{
   		//return exoPlayer.currentPosition.toDouble() - this will result in crash
   		//do not use exoplayer current position but a computed variable previously updated
   		return myPrivateComputedCurrentPosition
	}

    // the current track duration in seconds
	fun getDuration(): Double?{
	    //return exoPlayer.duration.toDouble()  - this will result in crash
	    //do not use exoplayer current duration but a computed variable previously updated
   		return myPrivateComputedCurrentDuration
	}
	
	
	override fun load(creativeUrlString: String) {
		playerHandler.post {
			exoplayerInstance.setMediaSource(mediaSources) //or other player method
			exoplayerInstance.prepare()
		}
	}
	
	override fun play() {
		playerHandler.post {
    	    exoplayerInstance.playWhenReady = true
    	}
    }

    override fun pause() {
	    playerHandler).post {
        	exoplayerInstance.playWhenReady = false
        }
    }
    
    // Implement other methods similarly
	...
}
```



## Calling AdswizzSDK from a background thread

Calling AdswizzSDK from a background thread will result in a an exception if you are using SDK internal player with the same exoplayer message

```kotlin
      "Player is accessed on the wrong thread.\n"
                  + "Current thread: '%s'\n"
                  + "Expected thread: '%s'\n"
                  + "See https://developer.android.com/guide/topics/media/issues/"
                  + "player-accessed-on-wrong-thread",
```
