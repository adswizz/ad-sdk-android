# Podcasts

Podcast apps that integrate the AdswizzSDK will benefit from real-time reporting, companion display and interactive ads with formats such as ShakeMe and VoiceAds. Of course, these features are available only if the podcasts are delivered by AdsWizz's AIS. The setup uses an underlying functionality similar to Server-Side Insertion as well as some Client-Side Insertion objects, such as **_AdManager_**.

## Getting started

To get started, you need to create an **_AdswizzAdPodcastManager_** object and give it an **_AdPodcastManager.Listener_** 

```kotlin
import com.adswizz.core.podcast.AdswizzAdPodcastManager

class YourModelClass {
    private var podcastManager: AdswizzAdPodcastManager? = null

    private val podcastListener = object : AdPodcastManager.Listener {
        override fun willStartPlayingUrl(
            adPodcastManager: AdPodcastManager,
            url: Uri,
            adBaseManager: AdBaseManager?
        ) {
        }

        override fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
        }

        override fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
        }

        override fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
        }

        override fun adBreakStarted(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
        }

        override fun adBreakEnded(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
        }

        override fun onError(adPodcastManager: AdPodcastManager, error: Error) {
        }

        override fun onPlayHeadReport(
            adPodcastManager: AdPodcastManager,
            elapsedSec: Double,
            durationSec: Double
        ) {
        }
    }

    fun createPodcastManager() {
        ....
        podcastManager = AdswizzAdPodcastManager()
        podcastManager?.addListener(podcastListener)
        ...
    }
}
```

The **_AdswizzAdPodcastManager_** object can be configured through an _AdPodcastManagerSettings_ and enables you to provide your own implementation of a player that implements the _AdPlayer_ interface.

```kotlin
fun createPodcastManager() {
        val settings = AdPodcastManagerSettings.Builder()
            .adPlayerInstance(customPlayerInstance)
            .build()
        podcastManager = AdswizzAdPodcastManager(settings)
        podcastManager?.addListener(podcastListener)
        ...
    }
```

This is your only entry point to drive the podcast functionality further; typically, your app will need a single **_AdswizzAdPodcastManager_** instance. 

## Playback operations


### play

Calling this method from **_AdswizzAdPodcastManager_** with an URL will begin playing either remotely or locally from a location on disk. This method should be called  even if the podcast is played with an external player, because the remote URL will be enhanced by the SDK with extra parameters that enable the delivery of ads from an Adswizz server. As a response, the AdswizzSDK will call back the function `fun willStartPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri, adBaseManager: AdBaseManager?)` with the newly formatted URL.

Additionally, `play(uri: Uri)` will attempt to generate and build an **_AdBaseManager_** for handling ads.

### pause

Calling this method will pause a podcast that is played by the SDK’s internal player or the provided custom player. You'll get notified by `fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`

### resume

Calling this method will resume a podcast that is played by the SDK’s internal player or the provided custom player. You'll get notified by `fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`

### stop

Calling this method will stop a podcast that is played by the SDK’s internal player or the provided custom player. You'll get notified by `fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`

### seekTo

Calling `fun seekTo(position: Duration)` will trigger a seek by setting the current playback time to the specified `position` parameter.

Take note that the **_AdPodcastManager.Listener_** also notifies you when an ad break starts and ends; there is also a callback that provides you with the play head information(elapsed time in seconds and duration of podcast, also in seconds).

## Download operations

**_AdswizzAdPodcastManager_** also exposes download functionality to be able to store and play podcasts from the device's storage.
As best practice, downloading a podcast should continue to run even when your app is put in the background. Downloading functionality is enabled by the Android's WorkManager library and is subject to the inherited limitations. Also, the download constraints can be supplied through the _AdPodcastManagerSettings_ configuration passed to the **_AdswizzAdPodcastManager_**:

```kotlin
fun createPodcastManager() {
        val settings = AdPodcastManagerSettings.Builder()
            .adPlayerInstance(customPlayerInstance)
            .withDownloadConditions(setOf(
                DownloadCondition.NotRoaming, 
                DownloadCondition.OnlyWhenCharging
            ))
            .build()
        podcastManager = AdswizzAdPodcastManager(settings)
        podcastManager?.addListener(podcastListener)
        ...
    }
```

### start download

To begin downloading a podcast, the `startDownloadPodcast(uri: Uri, location: Uri, completion: ((Boolean, Exception?) -> Unit)? = null)` method takes a remote Uri to download from and a location file Uri as destination to save the media file once the download is complete. Similar to the `play(uri: Uri)` method, the remote Uri will be enhanced by **_AdswizzAdPodcastManager_** with extra parameters that enable the delivery of ads from an Adswizz server when the podcast is actually played.

### stop download

Calling the `stopDownloadPodcast(location: Uri)` method cancels a previously started download operation for a podcast at its location file Uri as destination on disk.

### remove download

Calling the `removePodcast(location: Uri)` method removes a previously downloaded podcast at its location file Uri as destination on disk.

### automaticallySecureConnectionForAdURL

Enables secure connections when **_AdswizzAdPodcastManager_** object fires additional internal requests. The default value is `true`.
Here is an example on how to instruct the **_AdswizzAdPodcastManager_** object to secure or not its internal requests:

```kotlin
    fun createPodcastManager() {
        val settings = AdPodcastManagerSettings.Builder()
            .automaticallySecureConnectionForAdURL(false) // Optional; true by default
            .build()
        podcastManager = AdswizzAdPodcastManager(settings)
    }
```

> The **_AdswizzAdPodcastManager_** object will always attempt to use a secured connection (HTTPS protocol) when firing internal requests for fetching additional ad breaks related information from the stream metadata.
> This approach avoids the need for whitelisting these requests in your application.

### automaticallyManageAudioFocus

This method is used by internal players in order to handle [Audio Focus](https://developer.android.com/media/optimize/audio-focus), the default value is `false`. If enabled, `AdswizzAdPodcastManager`'s audio focus will be handled by the Exoplayer audio focus handling mechanism.

```kotlin
fun createPodcastManager() {
    val settings = AdPodcastManagerSettings.Builder()
        .automaticallyManageAudioFocus(true)		// optional, used by internal players, defaults to internal exoplayer audio focus handling
        .build()
    podcastManager = AdswizzAdPodcastManager(settings)
}
```

## **_AdPodcastManager.Listener_** interface

Playback and ad break related notifications sent from **_AdswizzAdPodcastManager_** to your app are implemented in the **_AdPodcastManager.Listener_** interface.

### fun willStartPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri, adBaseManager: AdBaseManager?)

This callback will be called after the podcast starts playing.

### fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)

This callback will be called after the podcast finishes playing or is stopped.

### fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)

This callback will be called after the podcast pauses playing.

### fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)

This callback will be called after the podcast resumes playing.

### fun adBreakStarted(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager)
This callback signals that an ad break has started. During podcast playing, the **_AdswizzAdPodcastManager_** listens to the current `playhead` property of the player and detects ad breaks in the podcast based on the associated playhead start positions of the ads stitched within the podcast.

### fun adBreakEnded(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager)
This callback signals that the ad break has ended.

### fun onError(adPodcastManager: AdPodcastManager, error: Error)
Every time there is an error in the podcast functionality, you'll get notified through this callback

### fun onPlayHeadReport(adPodcastManager: AdPodcastManager, elapsedSec: Double, durationSec: Double)

Every time the current playhead changes, your app will get notified in this function. The duration reflects the total expected podcast duration. This method can also be used to update the UI accordingly.

### **_Note_**
In order to receive various ad-related events, you can set a listener by calling `setListener` method to the provided **_AdBaseManager_** object.
Also, you can remove the specified listener by calling `removeAdBaseManagerListener`.

## **_AdPodcastManagerDownloadListener_** interface

Download-related notifications sent from **_AdswizzAdPodcastManager_** to your app are implemented in the **_AdPodcastManagerDownloadDelegate_** interface.

## Handling interactive ads during podcasts

For more information on how to handle interactive ads such as ShakeMe and Voice Ads, please refer to the [Interactive ads](Android/SDKAndroid/InteractiveAds.md#interactive-ads) section.
Note that the interactivity component of an ad requires fetching data from a remote service that may not be available for downloaded podcasts, especially when there is no internet connection.

## Remote Audio Data

Podcasts come with _Remote Audio Data_ (RAD) support. Based on the industry standard by [NPR](https://rad.npr.org/), each podcast with RAD metadata will send events everytime its playhead crosses a certain point in time. 
To enable RAD measurement for your podcasts, additional ZeroConfig configuration is needed. Contact your AdsWizz partner integration manager to set up your app's AdswizzSDK ZeroConfig instance.
Once this is in place, as best practice, you should present your users with the option to opt in to or opt out of RAD measurement when playing podcasts. 

This can be achieved with a single line of code:

```kotlin
AdswizzSDK.disableRAD = false //or `true` if user opted out
```       
By default, when the `AdswizzSDK` is initialized, the `disableRAD` property is set to `true`, so RAD measurement is **disabled** by default.
 
The `disableRAD` property is applied to all podcasts and can be updated on the fly. When RAD measurement is disabled, all current event uploads are cancelled and the local database is emptied.

Disabling RAD data collection at the ZeroConfig level takes precedence over disabling it from code. If RAD is disabled in ZeroConfig, setting a new value to the `disableRAD` property will be the same as setting this value to `false` every time.

### Handling RAD id3 metadata using your player

Remote Audio Data measurement is based on reading specific `id3` metadata formats from the podcast being played. The SDK is able to read these specific metadata formats when playing with the internal player.
However, when playing podcasts using your own player, you are responsible for passing the metadata items to the SDK. You can find more details in the [AdPlayer.Listener interface](Android/SDKAndroid/Playingyourplayer#adplayer-interface) and [MetadataItem](Android/SDKAndroid/Playingyourplayer#adplayer-interface) sections.


### fun didDownload(adPodcastManager: AdPodcastManager, bytesDownloaded: Long, bytesExpected:Long, asset: DownloadableAsset)

This method gets called when a podcast has already started downloading and a chunk of data was received. It sends all the necessary data to compute progress, needed for any UI updates. It also encapsulates the asset being downloaded into a **_DownloadableAsset_**, which is kept alive until the download finishes.

The **_DownloadableAsset_** object provides info on the asset is being handled:
* url - the remote URL that the podcast is being downloaded from
* mimeType - the creative type e.g "audio/mp3"
* expectedContentLength - should match bytesExpected in the progress callback
* state - the current state, e.g. `downloading`, `ready`, `failed`
* location - the local file URL path where the asset will be saved once download completes

### fun didFinishDownload(adPodcastManager: AdPodcastManager,  asset: DownloadableAsset)

Called when the asset finished downloading all of its data.

### fun didReceive(adPodcastManager: AdPodcastManager,  error:Error, asset: DownloadableAsset){

Called when a network error occurred while trying to download the asset.


## Augment the podcastURL using decorateUrl static function
Starting with SDK version 7.7.17 a new static function named `fun decorateUrl(url: String): Uri` was added to the `AdswizzAdPodcastManager` class, in case you need to use it in a synchronous context.
<br>Example of usage:

```kotlin
       val listURl = listOf("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_1.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_brokenTS.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_muchosEvents.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_npr.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_npr1.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_roCountDown.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_rusky.mp3",
                "http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_sampleFile.mp3")
        val bDecoration = prefs.getBoolean("settings_podcast_static_decoration_preference", false)
        if (bDecoration){
            listURl.forEach {
                val uri = AdswizzAdPodcastManager.decorateUrl(it)
                println("AdswizzAdPodcastManager.decorateUrl:: $it -> $uri")
            }
        }

	
```

```kotlin
http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3

will be transformed in

http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3?companionAds=true&aw_0_1st.gpslat=44.32147278&aw_0_1st.gpslong=26.18935183&aw_0_awz.appVers=1.0.0(ExoPlayer%202.10)%3A1&aw_0_1st.version=7.6.0%3Aandroid33&aw_0_1st.ts=1675766788266&aw_0_req.permissions=11111111111110&calendar=1&aw_0_1st.playerId=ArtemisKotlinPlayerId&aw_0_1st.cb=1675766788.-24331&aw_0_1st.sessionid=1675766721938.277118&aw_0_req.appState=fg&speech=1&sdkiad=1&photoLib=1&call=1&pkpass=1&mic=1&aw_0_req.bundleId=com.adswizz.artemis&aw_0_req.tapOpp=true&repokey=MjkxNDc1NTMzNw%3D%3D&playLive=true&aw_0_awz.listenerid=735e3556-e032-4f6f-8382-b83ff789f895&aw_0_req.lmt=0&aw_0_1st.ifaType=androidGMS&aw_0_req.wopp=0
```
