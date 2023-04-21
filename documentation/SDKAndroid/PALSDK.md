# (Optional) Interoperability with the PAL SDK

The AdswizzSDK can work with Google's PAL SDK to access video demand from DV360. The AdswizzSDK is not directly linked to the PAL SDK but exposes a public API to facilitate the communication.

## What you need to do
Firstly, you need to integrate the PAL SDK into your app.

Then you have to create a **_PalNonceHandlerInterface_** object that contains the `nonce` string of the PAL SDK's `NonceManager` object and provides the following events to the PAL SDK's `NonceManager` object: `sendAdClick()`, `sendPlaybackStart()`,   `sendPlaybackEnd()` and `sendTouch(event: MotionEvent)`. A `NonceManager`object can be obtained using `NonceLoader`. More details on how to operate with these objects can be found in the 'Programmatic Access Library' (PAL) documentation provided by Google. We have used version 20.0.1.

Once you get a **_PalNonceHandlerInterface_**, there is a final step to take: link the AdswizzSDK with the **_PalNonceHandlerInterface_** object. This can be done using `AdRequest.Builder`.

```kotlin
AdswizzAdRequest.Builder()
    .withServer(SERVER_URL)
    .withZones(setOf(AdswizzAdZone(ZONE_ID)))
    ...
    .withNonceHandler(palNonceHandler) //set here the palNonceHandler object previously created
    .build { adswizzAdRequest ->
     }
```
