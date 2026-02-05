# Playing ads using your player using AdStreamPlayerAdapter interface [Beta]

Starting with 8.1.0 release, the **_AdswizzSDK_** introduces a new event-driven integration method for Server Side Insertions. The `AdStreamPlayerAdapter` interface now offers a more Kotlin-friendly approach to pass your player information to the `AdswizzAdStreamManager` object.

> **Note**
>
> The AdStreamPlayerAdapter approach is currently in beta and may be modified in future releases based on your feedback.


## Server-Side Insertion setup

Similar to the `AdPlayer` interface, you can set your `AdStreamPlayerAdapter` implementation in the **_AdStreamManagerSettings_** builder as follows:

```kotlin

val settings = AdManagerStreamingSettings.Builder()
    .adStreamPlayerAdapterInstanceInstance(YOUR_AD_STREAM_PLAYER_ADAPTER_INSTANCE_GOES_HERE)
    .build()

val streamManager = AdswizzAdStreamManager(settings)

streamManager.addListener(adStreamListener)

val decoratedUrl = AdswizzAdStreamManager.decorateUrl(it)

yourPlayerAdapter.loadStream(decoratedUrl)
yourPlayerAdapter.prepare()
yourPlayerAdapter.play()

// ...
// call cleanup when you want to decouple your adStreamPlayerAdapter instance to the attached streamManager and free the resources
streamManager.cleanup()

```

## AdStreamPlayerAdapter interface

**AdStreamPlayerAdapter** aims to simplify communication by using `kotlinx.coroutines.SharedFlow` to emit the `List<AdMetadataItem>` during stream playback.

```kotlin
/**
 * AdStreamPlayerAdapter interface offers a more Kotlin-friendly approach to pass your player information to the [AdswizzAdStreamManager] object </br>
 */
interface AdStreamPlayerAdapter {

    /**
     * Playback position of the current media with milliseconds precision
     * This variable reflect the continuous progress for each item that is currently playing.
     */
    val currentPlayhead: Duration

    /**
     * The incoming metadata from Player. This is used for adBreak detection.
     */
    val metadataSharedFlow: SharedFlow<List<AdMetadataItem>>

}
```
