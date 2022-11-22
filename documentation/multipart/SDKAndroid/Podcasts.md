# Podcasts

Podcast apps that integrate AdswizzSDK will benefit from real-time reporting, companion display and interactive ads with formats such as ShakeMe and VoiceAds. Of course, these features are available only if the podcasts are delivered by AdsWizz's AIS. The setup uses an underlying functionality similar to Server Side Insertion as well as some Client Side Insertion familiar objects such as **_AdManager_**.

## Getting started

To get started, you need to create an **_AdswizzAdPodcastManager_** object and give it a **_AdPodcastManager.Listener_** 

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

The **_AdswizzAdPodcastManager_** object can be configured through a _AdPodcastManagerSettings_ and enables providing your own implementation of a player that implements the _AdPlayer_ interface.

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

This is your only entry point to drive the podcast functionality further and typically your app would need a single **_AdswizzAdPodcastManager_** instance. 

## Playback operations


### play

Calling this method from **_AdswizzAdPodcastManager_** with an URL will begin playing either remotely or locally from a location on disk. This method should be called  even if the podcast is played with an external player, because the remote URL will be enhanced by the SDK with extra parameters that enable the delivery of ads from an Adswizz server. As a response, AdswizzSDK will call back function `fun willStartPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri, adBaseManager: AdBaseManager?)` with the newly formatted URL.

Additionally, `play(uri: Uri)` will also attempt to generate and build an **_AdBaseManager_** for handling ads

### pause

Calling this method will pause a podcast that is played by the SDK’s internal player or the provided custom player. You'll get notified by `fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`

### resume

Calling this method will resume a podcast that is played by the SDK’s internal player or the provided custom player. You'll get notified by `fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`

### stop

Calling this method will stop a podcast that is played by the SDK’s internal playeror the provided custom player. You'll get notified by `fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`

### seekTo

Calling `fun seekTo(position: Double)` will trigger a seek by setting the current playback time to the specified `position` parameter measured in seconds.

Take note that the **_AdPodcastManager.Listener_** also notifies you when an ad break starts and ends and also there is a callback that provides you the play head information(elapsed time in seconds and duration of podcast also in seconds)

## Download operations

**_AdswizzAdPodcastManager_** also exposes download functionality to be able to store and play podcasts from the device's storage.
As best practice, downloading a podcast should continue to run even when your app is put in background. Downloading functionality is enabled by the android's WorkManager library and is subjected to the inherited limitations. Also the download constraints can be supplied through the _AdPodcastManagerSettings_ configuration passed to the **_AdswizzAdPodcastManager_**:

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

To begin downloading a podcast, the `startDownloadPodcast(uri: Uri, location: Uri, completion: ((Boolean, Exception?) -> Unit)? = null)` method takes a remote Uri to download from and a location file Uri as destination to save the media file once the download is complete. Similar to `play(uri: Uri)` method, the remote Uri will be enhanced by **_AdswizzAdPodcastManager_** with extra parameters that enable the delivery of ads from an Adswizz server when the podcast will actually be played.

### stop download

Calling the `stopDownloadPodcast(location: Uri)` method cancels a previously started download operation for a podcast at its location file Uri as destination on disk.

### remove download

Calling `removePodcast(location: Uri)` method removes a previously downloaded podcast at its location file Uri as destination on disk.

## **_AdPodcastManager.Listener_** interface

Playback and ad breaks related notifications sent from **_AdswizzAdPodcastManager_** to your app are implemented in the **_AdPodcastManager.Listener_** interface.

### fun willStartPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri, adBaseManager: AdBaseManager?)

This callback will be called after the podcasts starts playing

### fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)

This callback will be called after the podcasts finishes playing or is stopped

### fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)

This callback will be called after the podcasts pauses playing

### fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)

This callback will be called after the podcasts resumes playing

### fun adBreakStarted(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager)
This callback signals the ad break started. During podcast playing, the **_AdswizzAdPodcastManager_** listens to the current `playhead` property of the player and detects ad breaks in the podcast based on the associated playhead start positions of the ads stitched within the podcast.

### fun adBreakEnded(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager)
This callback signals the ad break ended

### fun onError(adPodcastManager: AdPodcastManager, error: Error)
Every time there is an error in the podcast functionality you'll get notified through this callback

### fun onPlayHeadReport(adPodcastManager: AdPodcastManager, elapsedSec: Double, durationSec: Double)

Every time the current playhead changes, your app will get notified in this function. The duration reflects the total expected podcast duration. This method can also be used to update UI accordingly.


## **_AdPodcastManagerDownloadListener_** interface

Download related notifications sent from **_AdswizzAdPodcastManager_** to your app are implemented in the **_AdPodcastManagerDownloadDelegate_** interface.

## Handling interactive ads during podcasts

For more information on how to handle interactive ads such as ShakeMe and Voice Ads please refer to [Interactive ads](#interactive-ads) ads section.
Note that the interactivity component of an ad requires fetching data from a remote service which may not be available for downloaded podcasts especially in a no internet connection scenario.

## Remote Audio Data

Podcasts come with _Remote Audio Data_ (RAD) support. Based on the industry standard by [NPR](https://rad.npr.org/), each podcast with RAD metadata will send events everytime its playhead crosses a certain point in time. 
To enable RAD measurement for your podcasts, additional ZeroConfig configuration is needed. Contact your AdsWizz partner integration manager to setup your app's AdswizzSDK ZeroConfig instance.
After this is in-place, as best practice you should present your users the option to opt-in or out for RAD measurement when playing podcasts. 

This can be achieved with a single line of code:

```kotlin
AdswizzSDK.disableRAD = false //or `true` if user opted out
```       
By default, when `AdswizzSDK` is initialized, `disableRAD` property is set to `true`, so RAD measurement is **disabled** by default.
 
The `disableRAD` property is applied to all podcasts and can be updated on the fly. When RAD measurement is disabled, all current event uploads are cancelled and the local database is emptied.

Disabling RAD data collection at the ZeroConfig level takes precedence over disabling it from code. If RAD is disabled in ZeroConfig, setting a new value to `disableRAD` property will be the same as setting this value to `false` everytime.

### Handling RAD id3 metadata using your player

Remote Audio Data measurement is based on reading specific `id3` metadata formats from the podcast being played. The SDK is able to read these specific metadata formats when playing with the internal player.
However, when playing podcasts using your own player you are responsable for passing the metadata items to the SDK. You can find more details at [AdPlayer.Listener interface](#adplayer-interface) and [MetadataItem](#adplayer-interface) sections.


### fun didDownload(adPodcastManager: AdPodcastManager, bytesDownloaded: Long, bytesExpected:Long, asset: DownloadableAsset)

This method gets called when a podcast has already started downloading and a chunk of data was received. It sends all the necessary data to compute a progress needed for any UI updates. It also encapsulates the asset being downloaded into a **_DownloadableAsset_** which is kept alive until the download finishes.

The **_DownloadableAsset_** object provides info on the asset that being handled:
* url - the remote URL that is downloading from
* mimeType - the creative type e.g "audio/mp3"
* expectedContentLength - should match bytesExpected in the progress callback
* state - the current state e.g `downloading`, `ready`, `failed`
* location - the local file URL path where the asset will be saved once download completes

### fun didFinishDownload(adPodcastManager: AdPodcastManager,  asset: DownloadableAsset)

Called when the asset finished downloading all of its data.

### fun didReceive(adPodcastManager: AdPodcastManager,  error:Error, asset: DownloadableAsset){

Called when a network error occurred while trying to download the asset.

