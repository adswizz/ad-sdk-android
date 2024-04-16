# AdswizzSDK Extensions Server Side Insertion

This guide is addressed to developers that are interested to integrate `com.adswizz:adswizz-sdk-extensions` in their Android application in order to take full advantage of `kotlin coroutines` using **_AdswizzSDK_**.


`AdswizzAdStreamManager` exposes several extension function in order receive Ad/Stream updates using a `kotlin flow` and it can be filtered, collected, mapped using standard operators.

## observeAdEvents

In order to observe ad events through an `AdswizzAdStreamManager` instance, you can use the following extension function:

```kotlin
viewModelScope.launch {
    adStreamManager = AdswizzAdStreamManager(null)
    viewModelScope.launch {
        val manager = adStreamManager ?: return@launch
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

## observeStreamEvents
In order to observe stream events through an `AdswizzAdStreamManager` instance, you can use the following extension function:

```kotlin
viewModelScope.launch {
    val manager = adStreamManager ?: return@launch
    manager.observeStreamEvents(this)
        .collect { streamEvent ->
            withContext(Dispatchers.Main) {
                when(streamEvent){
                    is AdswizzStreamEvent.AdBreakStarted -> // received adBreakEvent
                }
            }
        }
}
```
`AdswizzStreamEvent` is a sealed class encapsulating all methods defined in `AdStreamManager.Listener` and it's defined as follows:
```kotlin
sealed class AdswizzStreamEvent {

    data class DidPausePlayingUrl(val url: Uri) : AdswizzStreamEvent()
    data class DidResumePlayingUrl(val url: Uri) : AdswizzStreamEvent()
    data class DidFinishPlayingUrl(val url: Uri) : AdswizzStreamEvent()
    data class WillStartPlayingUrl(val url: Uri) : AdswizzStreamEvent()
    data class AdBreakStarted(val adManager: AdBaseManager) : AdswizzStreamEvent()
    data class AdBreakEnded(val adManager: AdBaseManager) : AdswizzStreamEvent()
    data class Error(val error: kotlin.Error): AdswizzStreamEvent()
    data class OnMetadataChanged(val metadataItem: AdPlayer.MetadataItem): AdswizzStreamEvent()
    data class OnDvrMetadataReceived(val dvrBufferInfo: DvrBufferInfo?): AdswizzStreamEvent()
}
```
> **Note**
> Cancellation support is provided by the coroutine scope passed as parameter.

## AdswizzAdStreamManager Actions

AdswizzAdStreamManager public methods needs to be called primarily on the MainThread, therefore exposing the coroutine support on different dispatchers may lead to unpredictable behaviour. Although, calling `StreamManager` methods on different dispatchers can be done now using `AdswizzStreamManagerAction`.

```kotlin
sealed class AdswizzStreamManagerAction {
    data class Play(val url : String, val timeStamp: Long? = null): AdswizzStreamManagerAction()
    object Pause: AdswizzStreamManagerAction()
    object Resume: AdswizzStreamManagerAction()
    object Stop : AdswizzStreamManagerAction()
    object Cleanup : AdswizzStreamManagerAction()
}
...
// after receiving the StreamManager instance you can use:
streamManager.postAction(AdswizzStreamManagerAction.Play(Your_STREAM_URL))

```
