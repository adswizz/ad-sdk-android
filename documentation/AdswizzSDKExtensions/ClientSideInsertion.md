# AdswizzSDK Extensions Client Side Insertion

This guide is addressed to developers that are interested to integrate `com.adswizz:adswizz-sdk-extensions` in their Android application in order to take full advantage of [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) using **_AdswizzSDK_**.

## Ad Request

Since **_AdswizzSDK_** 7.9.0 you can request ads using a suspend function as follows:

```kotlin
    private var adManager: AdManager? = null
    ...

    viewModelScope.launch { 
        // or your custom scope 
        val manager = requestAdManagerWith { 
            zones = listOf(AdswizzAdZone(ZONE_ID))
            server = SERVER_URL
       }.value ?: return@launch
        
    adManager = manager
    }
```
`requestAdManagerWith` method takes a lambda function where you can customise the ad request with same parameters as `AdswizzAdRequest.Builder()`.
> **Note**
> Cancellation support is provided by the received `job` when launching the suspendable function. 

```kotlin
    val job = viewModelScope.launch {
    // or your custom scope 
    val manager = requestAdManagerWith {
        zones = listOf(AdswizzAdZone(ZONE_ID))
        server = SERVER_URL
        }
    }
    // requestAdManager method can be cancelled on demand or when viewModelScope gets canceled
    job.cancel()
}
```

## AdManager Actions

AdManager public methods needs to be called primarily on the MainThread, therefore exposing the coroutine support on different dispatchers may lead to unpredictable behaviour. Although, calling `AdManager` methods on different dispatchers can be done now using `AdManagerAction`.

```kotlin
sealed class AdManagerAction{
    object Prepare: AdManagerAction()
    object Play: AdManagerAction()
    object Pause: AdManagerAction()
    object Resume: AdManagerAction()
    object Reset : AdManagerAction()
    data class SkipAd(val error: Error? = null) : AdManagerAction()
}
...
// after receiving the AdManager instance you can use:
adManager.postAction(AdManagerAction.Prepare)

```

## Observe Ad Events

`AdManager` exposes an extension function in order receive Ad updates using a `kotlin flow` and it can be filtered, collected, mapped using standard operators.

```kotlin
viewModelScope.launch {
    val manager =  requestAdManagerWith {
        zones = listOf(AdswizzAdZone(ZONE_ID))
        server = SERVER_URL
    }.value ?: return@launch

    manager.observeAdEvents(this)
        .mapNotNull { it.value }
        .onStart { manager.postAction(AdManagerAction.Prepare) }
        .collect { res ->
            withContext(Dispatchers.Main) {
                when (res.type) {
                    AdEvent.Type.State.ReadyForPlay -> ...
                    else -> Unit
                }
            }
        }
}
```
> **Note**
> Cancellation support is provided by the coroutine scope passed as parameter.
