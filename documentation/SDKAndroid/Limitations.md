# Limitations when using SDK with your player

The **_AdswizzSDK_** internally uses main thread when calling your AdPlayer implementations and also expect the SDK API to be called from the main thread.
Failing to do that will result in exception when API is called from other thread or if your exoplayer runs on a different looper.

## AdPlayer interface

In case your exoplayer is running on main thread implementing AdPlayer interface is straightforward and exoplayer functions can be called 
immediately on the same stack call as calls from SDK to adplayer implementation like this:

```kotlin
	...
	fun getCurrentTime(): Double{
   		return exoplayerInstance.currentPosition
	}

    // the current track duration in seconds
	fun getDuration(): Double?{
	    return player.duration.toDouble()
    }
	...
```

In case your exoplayer is running on other looper and the calls to exoplayer are made in the same stack call from SDK calls made to your AdPlayer
implementation will result in an exoplayer exception like this (excerpt from exoplayer sources)

```kotlin
      "Player is accessed on the wrong thread.\n"
                  + "Current thread: '%s'\n"
                  + "Expected thread: '%s'\n"
                  + "See https://developer.android.com/guide/topics/media/issues/"
                  + "player-accessed-on-wrong-thread",
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
