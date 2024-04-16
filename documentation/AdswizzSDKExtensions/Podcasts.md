# AdswizzSDK Extensions Podcast

This guide is addressed to developers that are interested to integrate `com.adswizz:adswizz-sdk-extensions` in their Android application in order to take full advantage of `kotlin coroutines` using **_AdswizzSDK_**.

`AdswizzAdPodcastManager` exposes several extension function in order receive Ad/Playback/Download updates using a `kotlin flow` and it can be filtered, collected, mapped using standard operators.

## observeAdEvents

In order to observe ad events through an `AdswizzAdPodcastManager` instance, you can use the following extension function:

```kotlin
viewModelScope.launch {
    podcastManager = AdswizzAdPodcastManager()
    viewModelScope.launch {
        val manager = podcastManager ?: return@launch
        manager.observeAdEvents(viewModelScope)
            .mapNotNull { it.value }
            .collect { adEvent ->
                withContext(Dispatchers.Main) {
                    when (adEvent.type) {
                        AdEvent.Type.State.ReadyForPlay -> // received event
                           ... // other events
                        else -> Unit
                    }
                }
            }
    }
}
```

## observePodcastEvents
In order to observe podcast playback events through an `AdswizzAdPodcastManager` instance, you can use the following extension function:

```kotlin
viewModelScope.launch {
    val manager = adPodcastManager ?: return@launch
    manager.observePodcastEvents(this)
        .collect { podcastEvent ->
            withContext(Dispatchers.Main) {
                when(podcastEvent){
                    is AdswizzPodcastEvent.OnPlayHeadReport -> // received event
                }
            }
        }
}
```
`AdswizzPodcastEvent` is a sealed class encapsulating all methods defined in `AdPodcastManager.Listener` and it's defined as follows:

```kotlin
sealed class AdswizzPodcastEvent {
    data class DidPausePlayingUrl(val url: Uri) : AdswizzPodcastEvent()
    data class DidResumePlayingUrl(val url: Uri) : AdswizzPodcastEvent()
    data class DidFinishPlayingUrl(val url: Uri) : AdswizzPodcastEvent()
    data class WillStartPlayingUrl(val url: Uri) : AdswizzPodcastEvent()
    data class AdBreakStarted(val adManager: AdBaseManager) : AdswizzPodcastEvent()
    data class AdBreakEnded(val adManager: AdBaseManager) : AdswizzPodcastEvent()
    data class OnPlayHeadReport(val elapsedSec: Double, val durationSec: Double) : AdswizzPodcastEvent()
    data class Error(val error: kotlin.Error): AdswizzPodcastEvent()
}
```
## observeDownloadEvents
In order to observe podcast download events through an `AdswizzAdPodcastManager` instance, you can use the following extension function:

```kotlin
viewModelScope.launch {
    val manager = adPodcastManager ?: return@launch
    manager.observeDownloadEvents(this)
        .collect { podcastEvent ->
            withContext(Dispatchers.Main) {
                when(podcastEvent){
                    is AdswizzPodcastDownloadEvent.DidDownload -> // received event
                }
            }
        }
}
```
`AdswizzPodcastDownloadEvent` is a sealed class encapsulating all methods defined in `AdPodcastManagerDownloadListener` and it's defined as follows:

```kotlin
sealed class AdswizzPodcastDownloadEvent {
    data class DidDownload(val bytesDownloaded: Long,
                           val bytesExpected: Long,
                           val asset: DownloadableAsset
    ): AdswizzPodcastDownloadEvent()

    data class DidFinishDownload(val asset: DownloadableAsset): AdswizzPodcastDownloadEvent()

    data class DidReceiveError(val error: Error, val asset: DownloadableAsset): AdswizzPodcastDownloadEvent()

}
```

> **Note**
> Cancellation support is provided by the coroutine scope passed as parameter.

## AdswizzAdPodcastManager Actions

AdswizzAdPodcastManager public methods needs to be called primarily on the MainThread, therefore exposing the coroutine support on different dispatchers may lead to unpredictable behaviour. Although, calling `AdswizzAdPodcastManager` methods on different dispatchers can be done now using `AdswizzPodcastManagerAction`.

```kotlin
sealed class AdswizzPodcastManagerAction {
    data class Play(val url : Uri): AdswizzPodcastManagerAction()
    object Pause: AdswizzPodcastManagerAction()
    object Resume: AdswizzPodcastManagerAction()
    object Stop : AdswizzPodcastManagerAction()
    object SkipAd : AdswizzPodcastManagerAction()
    data class SeekTo(val position: Double) : AdswizzPodcastManagerAction()
    object Cleanup : AdswizzPodcastManagerAction()
}
// after receiving the PodcastManager instance you can use:
podcastManager.postAction(AdswizzPodcastManagerAction.Play(Your_Podcast_URL))

```