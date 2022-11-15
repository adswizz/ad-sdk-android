

# Server-Side Insertion


## Your first stream manager


AdswizzSDK handles interactive ad insertions, while playing HLS or ICY live streams from Ad Insertion Servers. This is possible by reading the player's metadata for specific Adswizz data and convert it in Adswizz interactive formats or companion banners.

To get started, you need to create an **_AdswizzAdStreamManager_** object with a URL pointing to the ad server your Integration Manager provided you with.


```kotlin

import com.adswizz.core.streaming.AdswizzAdStreamManager

class YourClass {

    private var streamManager: AdswizzAdStreamManager? = null

    fun createStreamManager() {
        streamManager = AdswizzAdStreamManager(null)
    }
}

```

Once the stream manager is constructed it is recommended to set a listener:

```kotlin
class YourClass {

    private var streamManager: AdswizzAdStreamManager? = null

    private val listener = object : AdStreamManager.Listener {
        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            println("Will start playing url: $url")
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            println("Did finish playing url: $url")
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            println("Did paused playing url: $url")
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            println("Did resume playing url: $url")
        }

        override fun adBreakStarted(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            println("Ad break started: adBaseManager $adBaseManager")
        }

        override fun adBreakEnded(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            println("Ad break ended: adBaseManager $adBaseManager")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            println("Error - ${error.message} for adStreamManager: $adStreamManager")
        }

        override fun onMetadataChanged(adStreamManager: AdStreamManager, metadataItem: AdPlayer.MetadataItem) {
            println("Metadata received - adStreamManager: $adStreamManager - metadata count: ${metadataItem.value.count()}")
        }

        override fun onDvrMetadataReceived(adStreamManager: AdStreamManager, dvrBufferInfo: DvrBufferInfo?) {
            println("DVR Metadata received - adStreamManager: $adStreamManager - hasDVRenabled=${dvrBufferInfo != null}")
        }
    }

    fun createStreamManager() {
        streamManager = AdswizzAdStreamManager(null)
        streamManager?.addListener(listener)
    }
}

```

The stream object can play the url using his internal player or using an external player provided by you. This should be specified in the **_AdManagerStreamingSettings_**. Also the enqueueEnabled feature can be set in this class. Below is a sample on how to set the external player:


```kotlin
    fun createStreamManager() {
        val settings = AdManagerStreamingSettings.Builder()
            .adPlayerInstance(externalPlayer)
            .enqueueEnabled(false)                  // optional
            .build()
        streamManager = AdswizzAdStreamManager(settings)
        ...
    }
```
###play
To start playing the stream do the following:

```kotlin
    streamManager?.play(AIS_STREAM_URL)
```

As a response, **_AdswizzSDK_** will call back `fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri)` with the provided url that has some extra query params added.

###stop
To stop the playing of stream call the stop function:

```kotlin
    streamManager?.stop()
```

The SDK will respond with the callback `fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri)`. The url is the same as for `willStartPlayingUrl`.

###pause and resume
The stream can be paused and resumed:

```kotlin
    //...
    streamManager?.pause()
    //...
    streamManager?.resume()
    //...
```

The SDK will respond with the callbacks `fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri)` and `fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri)` respectively. The url is the same as for `willStartPlayingUrl`.

## DVR Timeshifting capability

The SDK offers DVR timeshifting capability on HLS streams provided by AIS that have this feature enabled. To enable this feature on SDK side you have to specify this in the **_AdManagerStreamingSettings_** as follows:

```kotlin
    fun createStreamManager() {
        val settings = AdManagerStreamingSettings.Builder()
            .adPlayerInstance(externalPlayer)
            .dvrEnabled(true)
            .build()
        streamManager = AdswizzAdStreamManager(settings)
        ...
    }
```

 Please note that enabling DVR in Stream Manager's settings will automatically disable enqueue capability to ensure the functionality works as intended.

After enabling DVR start with normal play first

```kotlin
    streamManager?.play(AIS_STREAM_URL)
```

After the stream starts playing, if the stream server supports and has enabled the DVR feature for that particular stream it will provide more information through the **_AdStreamManager.Listener_** by calling

```kotlin
fun onDvrMetadataReceived(adStreamManager: AdStreamManager, dvrBufferInfo: DvrBufferInfo?)
```

When you get this callback called with a non-null **dvrBufferInfo** you know that you can now perform timeshift by using

```kotlin
    streamManager?.play(AIS_STREAM_URL, timestamp)
```

Do note that the timestamp is in epoch milliseconds format (milliseconds elapsed from 1st of January 1970) and it should be a value between **dvrBufferInfo.start** and **dvrBufferInfo.latest**.

The fields of the **DvrBufferInfo** are:

* start - represents the earliest moment that can be accessed with a timeshift play action (epoch milliseconds timestamp)
* latest - represents the latest moment that can be accessed (it differs from now because the server works with the stream in chunks) (epoch milliseconds timestamp)
* maxBufferSize - represents the largest buffer the server is capable to store (milliseconds value) and this gives the largest window the client can timeshift in the past. eg 36.000.000ms -> 10h window

## AdStreamManager Listener interface

The available callbacks that are called by the stream manager are described below:

```kotlin
    interface Listener {
        fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri)
        fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri)
        fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri)
        fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri)
        fun adBreakStarted(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager)
        fun adBreakEnded(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager)
        fun onMetadataChanged(adStreamManager: AdStreamManager, metadataItem: AdPlayer.MetadataItem)
        fun onError(adStreamManager: AdStreamManager, error: Error)
    }
```

### fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri)
After executing the play function on the stream manager object, the SDK will call this function with the original url decorated with extra parameters.

### fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri)
After executing the stop function on the stream manager object, the SDK will call this function with the original url decorated with extra parameters. The decorated url will be the same as the one in the `willStartPlayingUrl` callback.

### fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri)
After executing the pause function on the stream manager object, the SDK will call this function with the original url decorated with extra parameters. The decorated url will be the same as the one in the `willStartPlayingUrl` callback.

### fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri)
After executing the resume function on the stream manager object, the SDK will call this function with the original url decorated with extra parameters. The decorated url will be the same as the one in the `willStartPlayingUrl` callback.

### fun adBreakStarted(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager)
When an ad break is detected in the stream, the SDK will execute this callback. It will provide an **_AdBaseManager_** object that can be used for the whole duration of the ad break. You can use it to listen to different Ad related events and also to request a skip of current ad.
When you get this **_AdBaseManager_** object, the player will automatically play the ad, since it is part of the stream. Most of the times there will be only one ad in the **_AdBaseManager_** object but you can expect other ads to be inserted from the SDK if the ad is extended. This can happen, for instance, when the listener interacts with an ad (i.e. ShakeMe) and the action is to play an extension of the ad.

### fun adBreakEnded(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager)
When the ad break from the stream has ended you will get notified to clean up anything related to the provided **_AdBaseManager_**.

### fun onMetadataChanged(adStreamManager: AdStreamManager, metadataItem: AdPlayer.MetadataItem)
Whenever the metadata is changed on the stream played by the SDK, you will be notified with this callback.

### fun onError(adStreamManager: AdStreamManager, error: Error)
When an error occurs during your interaction with the stream manager this callback will be called by the SDK.


## AdStreamManager life cycle

Below is a diagram of the AdStreamManager states:

<img src="img/AdStreamManagerState.png" width="1000" />
