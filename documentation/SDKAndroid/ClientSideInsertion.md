# Client-Side Insertion

## Your first ad request

You are now ready for your first ad request. You will need to create an AdswizzAdRequest object and configure it.

**Minimal request parameters** The `AdswizzAdRequest` requires at minimum to set the server url and a set of one ore more `AdswizzAdZone` or an `AdswizzShimZone`.

```kotlin
	data class AdswizzAdZone @JvmOverloads constructor(
        @Json(name = "zoneId")
        val zoneId: String,
        @Json(name = "maxAds")
        val maxAds: Int? = null,
        @Json(name = "maxDuration")
        val maxDuration: Duration? = null,
        //starting with SDK 7.11.0
    	@Json(name = "zoneAlias")
    	val zoneAlias: String? = null
)
```
Each `AdswizzAdZone` object is defined by a mandatory `zoneId` or `zoneAlias`, provided by the PIM/TAM, and optional `maxAds` and `maxDuration` fields. The optional fields are determined by the application logic and are not required.

**Note**
Since v7.11.0, `zoneId` parameter is no longer mandatory if `zoneAlias` is present. However, they cannot be set simultaneously with `null` or `not null` values, this will lead to a runtime exception.

```kotlin
//starting with SDK 7.11.0
data class AdswizzShimZone @JvmOverloads constructor(
    @Json(name = "zoneId")
    val zoneId: String? = null,
    @Json(name = "maxDeadAir")
    val maxDeadAir: Long? = null,
    @Json(name = "zoneAlias")
    val zoneAlias: String? = null
)
```
**Note**
Since v8.0.0, `maxDuration` and `maxDeadAir` parameters are now of type Duration? instead of Long? which makes the time or period values representation more clear ( no need to know if is seconds or milliseconds or other time values).


Each `AdswizzShimZone` object is defined by a mandatory `zoneId`/`zoneAlias` and an optional `maxDeadAir` field, where `zoneId` or `zoneAlias` is provided by the PIM.
The fields `zoneId` or `zoneAlias` checked in the same manner as AdswizzAdZone. The `maxDeadAir` parameter represents the minimal threshold time necessary to fill the remaining duration with shims ads.

The minimal request looks like the following: one or more zones and a server url 
```kotlin
	AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withZones(listOf(AdswizzAdZone("ZONEID_1_PROVIDED_BY_PIM"), ... AdswizzAdZone("ZONEID_n_PROVIDED_BY_PIM")))
            .build { adswizzAdRequest ->
			// handle resulted adswizzAdRequest
            }
```
The ad server and the zoneId(s) will be provided to you by an AdsWizz PIM.

> **Note**
> Ensure the provided `AdswizzAdZone` array is ordered and unique, as this directly impacts the sequence of ads returned by the Ad Server.


**Add shims zone to AdswizzAdRequest**
Only one shim zone can be set in `AdswizzAdRequest`.

```kotlin
	//starting with SDK 7.11.0
	AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withShimZone(AdswizzShimZone("SHIM_ZONE_ID")))
            .build { adswizzAdRequest ->
			// handle resulted adswizzAdRequest
            }
```


**Custom request parameters**

You may use custom parameters (key-value pairs) in your requests, in order to send various information, such as the TCF v2 consent string, the publisher-specific ID, first-party targeting data etc.
Here is an example:

```kotlin
	AdswizzAdRequest.Builder()
		.withCustomParameter("key1=URLEncodedValue1&key2=URLEncodedValue2")
		...
```

**Duration request parameters**

An optional parameter is duration, which sets the maximum total duration of ads to be selected, in seconds. It can be used to fine tune the ad delivery and request a specific total duration.
Duration is set in milliseconds. Here is an example that sets max duration to 90 seconds

```kotlin
	AdswizzAdRequest.Builder()
		...	
		.withDuration(90.toDuration(DurationUnit.SECONDS))
		...
```
**Note**
Since v8.0.0, `withDuration` now uses Duration kotlin type instead of Long.



**(Advanced) Enable expected ads reporting**

You may enable the reporting on the number of expected ads by setting the `enableExpectedAdsReporting` to `true` in your **_AdswizzAdRequest_** objects. By default, `enableExpectedAdsReporting` is `false`.

Here is an example:
```kotlin
    AdswizzAdRequest.Builder()
        ...
        .withExpectedAdsReporting(true)
        ...
```

Read more about this behavior here [(Advanced) Handling AdManager reporting for expected ads](#advanced-handling-admanager-reporting-for-expected-ads).

**AdRequestConnection**

At this point you need to create an AdRequestConnection object and call requestAds with the ad request object.

```kotlin            
val adRequestConnection = AdRequestConnection(adRequest) // Create the AdRequestConnection using the above adRequest

adRequestConnection.requestAds { adManager, error ->
            // Handle the response from server
            handleResponse(error, adManager)
        }
```
As a result of this call, the SDK will return an error if the call was a failure or an AdManager object if the result was a success.

## Working with AdManager object

If the request to the Adswizz Ad server was a success, the SDK will return an AdManager object, that you will own. This will be the way the SDK will communicates events back to your application.
To get this communication channel open, you need to set up a listener for the AdManager that conforms to the AdManagerListener interface. The AdManager will call

`onEventReceived(adManager: AdManager, event: AdEvent)` whenever events of interest happen in the SDK. Consult AdEvent.Type for a list of possible events from the AdswizzSDK.

If an error occurs in the SDK while using this object, `onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error)` will be called.

As a first step, an **_AdManager_** needs to have some settings. You can create an **_AdManagerSettings_** object and pass it to your newly created instance of **_AdManager_**. Otherwise, the **_AdMnager_** will use the default values.  
In this object you can specify if you want to play the ad with the SDK’s internal player or a player of your choice that must conform to the **_AdPlayer_** interface.  
Also you can specify the cache policy (default: none), the asset quality preference (default high), the video view ID and if the player should play the ads one by one or as a playlist (enqueueEnabled, default: true).

Next, you need to call the prepare method on the **_AdManager_** object.
This will buffer the ads if you decide to play them with the internal player. Here is what it looks like:

```kotlin
class MainActivity : AppCompatActivity(), AdManagerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withZones(listOf(AdswizzAdZone("ZONEID_PROVIDED_BY_PIM")))
            .build { adswizzAdRequest ->

                AdRequestConnection(adswizzAdRequest)
                    .requestAds { adManager, error ->
                        // Handle the response from server
                        if (adManager != null) {
                            //Handle success
                            adManager.adManagerSettings =
                                AdManagerSettings.Builder() // optional
                                    .adPlayerInstance(YOUR_AD_PLAYER_GOES_HERE) // optional
                                    .addCachePolicy(CachePolicy.ASSETS)         // optional
                                    .assetQuality(AssetQuality.MEDIUM)          // optional, other possible values AssetQuality.LOW, AssetQuality.HIGH, AssetQuality.AUTO
                                    .enqueueEnabled(false)                      // optional
                                    .videoViewId(1)                             // optional
                                    .timeoutIntervalForResource(10.0.toDuration(DurationUnit.SECONDS))	// optional, used by internal players, defaults to internal exoplayer timeout (8 sec)
                                    .automaticallyManageAudioFocus(true)		// optional, used by internal players, defaults to internal exoplayer audio focus handling
                                    .build()                                    // optional
                            adManager.setListener(this) // Get notifications from the Ad SDK
                            adManager.prepare() // Start buffering the ads in the AdManager
                            adManager.play() // Start playing when first add finish loading
                        } else {
                            //Handle failure
                            Log.e("Error", error.toString())
                        }
                    }
            }
    }

    override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
    }

    override fun onEventReceived(adManager: AdManager, event: AdEvent) {
        when(event.type) {
            AdEvent.Type.State.ReadyForPlay ->  {
                // An add has finished loading and is ready for play
            }
            AdEvent.Type.State.DidFinishPlaying -> {
                // Current ad has finished playing
            }
            AdEvent.Type.State.AllAdsCompleted -> {
                // All ads from the AdManager have finished
            }

            else  -> {
                //do nothing
                //If you want to handle other events individually you can use the same syntax as above,
                //just specify the event type
            }
        }
    }
}
```
To start the **_AdManager_** rolling the ads, you must call the play method on the **_AdManager_**, as we did in the example above.

## AdManager operations

Once presented with an AdManager, you can call different actions on the AdManager. Let’s break them down.

## AdManager interface
### prepare

Call this method to begin to cycle through the ads in the AdManager. This method ensures that the player buffers enough data so that ad playing starts smoothly. Upon calling this method, the ads start loading. The SDK will trigger the **_PreparingForPlay_** event, informing your app that buffering has begun for a specific ad. Once buffering is done, the **_ReadyForPlay_** event for the ad will be triggered. If the `adManager` instance has no ad (EmptyAds scenario), a **_ReadyForPlay_** event with `null` instead of an `AdEvent` instance will be triggered.
> **Note**
> Since 7.10.0 version, this method may throw an IllegalStateException if you are using the **_AdswizzSDK_** internal player without adding one of the following dependencies: `com.adswizz:exoplayer2x` or `com.adswizz:exoplayer-media3` in your project.
> The IllegalStateException will not be thrown if you pass your own player instance through **_AdManagerSettings_**.

### play

Call **_play_** when you want to play the ads. This can be done before or after the **_ReadyForPlay_** callback is triggered. The SDK will respond with the **_DidStartPlaying_** callback when the playback begins. If the playing has been paused, use the **_resume_** function, to resume playing instead.


### pause

The **_pause_** method will stop playing the ads in the AdManager. AdManager will trigger a **_DidPausePlaying_** event back to your app for confirmation.


### resume

Call **_resume_** when you want to play the ads after a pause. The SDK will trigger a **_DidResumePlaying_** event back to your app for confirmation.


### skipAd

If you need to skip an ad, you can call this method to skip the current ad from the AdManager. Your app will receive
a **_DidSkip_** or **_NotUsed_** event for the current ad and will start to play the next one. If no ads are available, an **_AllAdsCompleted_** will be sent,
signaling that all ads got processed in the AdManager.

#### skipAd with error

For more advanced skip handling, you can call the **_skipAd(error: Error)_** method to skip the current ad from the AdManager. It follows the same behavior as for the **_skipAd_** method above. However, by passing an `Error` parameter, the SDK will **not** fire any tracking URLs for `skip` VAST events, but instead it will fire the VAST error tracking URLs. Additionally, for the skipped ad it will also call the `fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error)` callback from [AdManagerListener interface](#admanagerlistener-interface) with the same error used in **_skipAd(error: Error)_**.


### reset

To skip all ads in the AdManager, starting with the current one, you can call this method. For the current ad skipped,
your app will trigger **_DidSkip_** or **_NotUsed_**, and for the rest **_NotUsed_**. An **_AllAdsCompleted_** event will be sent at the end.
Looping through the ads again will need a call to the **_prepare_** function.</br>

### ads

Get the ads list in the ad manager. An empty ads list means the Ad Manager will not play any ad.

### analyticsCustomData

Set an **_AnalyticsCustomData_** data class to provide custom key-value attributes to all **_AnalyticsEvent_** instances triggered by the AdswizzSDK for Analytics purposes. Read more here: [AdswizzSDK Analytics](SDKAndroid/AdswizzSDKAnalytics).


## AdManagerListener interface

Notifications sent from `AdManager` to your app are implemented in the AdManagerListener interface. After the listener is set using the `setListener(listener: AdManagerListener)` method of the AdManager, your app will be called for the following cases:

### onEventReceived 

Called when there is an event related to the current ad in the ad manager. Some of the events are: Initialized, FirstAdWillInitialize, PreparingForPlay, ReadyForPlay, DidStartPlaying, DidResumePlaying, DidFinishPlaying, Completed and so on. See `AdEvent` for a complete list. 

### onEventErrorReceived

Called when there was an error playing the ad.

### **_Note_**
Also, you can remove the specified listener by calling `removeAdManagerListener`.

### Ad events explained

Once you've obtained an **_AdManager_** instance, set the delegate and called `prepare()`, that **_AdManager_** instance will communicate back to your application through the **AdManagerListener** interface. 

The `fun onEventReceived(adManager: AdManager, event: AdEvent)` interface method informs your application that the current ad triggered an event of interest.
Through the **_AdEvent_** interface, the following information can be accessed:
  * **_ad_**: the ad `AdData` for which the event was triggered
  * **_event_**: the actual event `AdEventType` that was triggered

#### AdEvent types
Remember that, regardless if enqueue is enabled or not, an **_AdManager_** handles and plays ads sequentially starting with the first ad until the last ad (including "empty" ads). Hence, the order of the events is predictable to be able to consistently adhere to a state machine model.
There is though some differences when enqueue is on and some events for the second add can be interlaced with some events from the first, while events for each of the ads remaning sequential. 
AdEvents are grouped into three classes: admanager events (of type AdEvent.Type.State) which informs about current admanger state changes, VAST events (of type AdEvent.Type.Position) which informs the listener about playback progress and other events (of type AdEvent.Type.Other) which consist of events not grouped in the first two categories.
Next, let's explore the types of events that the **_AdManager_**  can trigger by inspecting the **_AdEventType_** enum.

#### AdEvent.Type.State group (AdManager state changes)

##### FirstAdWillInitialize

The **_FirstAdWillInitializee_** event signals the admanager will start initialising first ad in ad pod

##### Initialized

The **_Initialized_** the default state of admanager


##### PreparingForPlay

The **_PreparingForPlay_** event signals that the current ad will start loading its creative. You can expect that the **_PreparingForPlay_** event to be triggered for each ad managed by the **_AdManager_** (including "empty" ads). This event marks the beginning of the current ad creative lifecycle and that the internal player will start buffering.

##### ReadyForPlay

The **_ReadyForPlay_** event is triggered when the player playing the ad has finished loading (buffering) the creative and has enough buffer likely to keep up. Useful when playing ads with the internal player so that you can instruct the **_AdManager_** to start playing by calling the `play()` method. 
Note that this event does not guarantee that it will play without buffering mid ad if network conditions change.

##### DidStartPlaying

The **_DidStartPlaying_** event signals that the current ad started playing its creative.

##### DidPausePlaying

The **_DidPausePlaying_** event signals that the current ad was paused. Upon resuming the ad, the **_DidResumePlaying__** event will be triggered.

##### DidResumePlaying

The **_DidResumePlaying_** event signals that the current ad has resumed. 

##### WillStartBuffering

The **_WillStartBuffering_** event signal an interrupt in playback of the current ad due to slow network condition and buffering was started by the player


##### DidFinishBuffering

The **_DidFinishBuffering_** event signal buffering has completed and playback is resumed


##### DidFinishPlaying

The **_DidFinishPlaying_** event signals that the current ad has finished playing its creative. Note that this event is not triggered if the **_DidStartPlaying_** event has not been previously triggered as well.

##### Completed

The **_Completed_** event signals that the current ad has finished. It is sent also for "empty" ads.

##### AllAdsCompleted

The **_AllAdsCompleted_** event signals that the **_AdManager_** has finished all the ads. After **_AllAdsCompleted_** event is received, the **_AdManager_** is no longer able to play any ad. To reuse the **_AdManager_** after this point (although not recommended), you would need to first call the [reset()](#reset) method described in the [AdManager protocol](#admanager-protocol) section, and call `prepare()` again.

##### DidSkip

The **_DidSkip_** event signals current ad has been skipped


##### NotUsed

The **_NotUsed_** event signals ad has not been used


##### AdUpdated

The **_AdUpdated_** event signals ad has been updated.


##### PlaybackError

The **_PlaybackError_** event signals an error occurred during current ad playback.


##### Unknown

The **_Unknown_** event signals something unexpected happened during playback which cannot be mapped to other ad events state.


#### AdEvent.Type.position group (VAST events)


##### Loaded

The **_Loaded_** event signals that the current was loaded and buffered and ready to play.


##### Start

The **_Start_** event signals that the current was loaded and playback began.

##### FirstQuartile

The **_FirstQuartile_** event signals that the current ad reached the first quartile from the VAST linear node.

##### Midpoint

The **_Midpoint_** event signals that the current ad reached its midpoint from the VAST linear node.

##### ThirdQuartile

The **_ThirdQuartile_** event signals that the current ad reached the third quartile from the VAST linear node.

##### Complete

The **_Complete_** event signals that the current ad reached the complete quartile from the VAST linear node.

##### Progress

The **_Progress_** event signals that the current ad reached the progress value defined in the VAST linear node.


## Ad events edge cases

Note that **_AllAdsCompleted_** event signals that the **_AdManager_** has finished all the ads either by playing them or skiping them due to errors or other unknown events.
In the following example AllAdsCompleted is sent after two failed to play ads. 

<img src="../img/edge-cases-events.jpg" width="1000" />


## AdBaseManagerAdapter interface

Starting with version 7.3.1, the SDK is also able to request data in order for your application to benefit from additional customization while working with the AdManager object. To be able to customize the behavior, you need to set up an adapter with `setAdapter` for the AdBaseManagerAdapter that conforms to the `AdBaseManagerAdapter` interface.
Through the `AdBaseManagerAdapter` adapter, when firing VAST tracking URLs, the SDK is able to ask for your additional custom tracking URLs, which will be fired along using the same engine, thus ensuring retryability and other improvements under the hood.
The `AdBaseManagerAdapter` adapter allows your integration to pass your own custom tracking URLs for the following use cases:

```kotlin
fun urlsForTracking(event: Tracking.EventType, adBaseManager: AdBaseManager, adData: AdData): List<String> 
```
Called when the AdManager is about to trigger a VAST event. It passes the Tracking.EventType, for which the SDK can fire your custom additional URLs. Check the Tracking.EventType, enum for possible values.


```kotlin
fun urlsForImpression(adBaseManager: AdBaseManager, adData: AdData): List<String> 
```
Called when the AdManager is about to trigger impressions.


```kotlin
fun urlsForError(errorCode: VASTErrorCode, adBaseManager: AdBaseManager, adData: AdData): List<String> 
```
Called when the AdManager is about to trigger a VAST error. It passes the VastErrorCode, for which the SDK can fire your custom additional URLs. Check the VastErrorCode enum for possible values.


```kotlin
fun urlsForCompanionClickTracking(adBaseManager: AdBaseManager, adData: AdData): List<String>
```
Called when the AdManager is about to trigger companion banner click URLs. When a companion banner is tapped, the SDK can fire your custom additional URLs.


```kotlin
fun urlsForVideoClickTracking(adBaseManager: AdBaseManager, adData: AdData): List<String>
```
Called when the AdManager is about to trigger video view click URLs. When the video view is tapped, the SDK can fire your custom additional URLs.


```kotlin
fun urlsForNoAd(adBaseManager: AdBaseManager, noAdUrlString: String): List<String> 
```
Called when the AdManager is about to trigger an empty ads event. See `Handling AdManager with no ads` for more information.


## Handling AdManager with no ads

In some cases, the **_AdManager_** you receive won't have any playable ads. In this case, inspecting the `ads` property on the **_AdManager_** will return a zero count. Read more about this property in the above section.
A non-playable ad is referred to as an _empty ad_. Empty ads won't play any media; however your app still needs to instruct the **_AdManager_** to play them by calling the `play()` method.

Here is a simplified setup that illustrates the handling an **_AdManager_** with no ads:

```kotlin
......

override fun onEventReceived(adManager: AdManager, event: AdEvent) {
    when (event.type) {
         .....
        AdEvent.Type.State.ReadyForPlay -> {
            appLogs.addEntry("Ad ${event.ad?.id} - ready for play")
            
            //Start playing the next ad in the AdManager. 
            //Notice that there is no need to check if the ad is empty or not.
            //The SDK will handle it internally and send the appropriate AdEvent just like for a non empty ad.
            adManager?.play()
            
            //Note: just to be sure that the ad playback will start immediately wait for the <<AdEvent.Type.State.ReadyForPlay>> event before calling adManager?.play()
        }
        .....
        AdEvent.Type.State.AllAdsCompleted -> {
            appLogs.addEntry("Ad - all ads completed")
            
            //All ads from the AdManager have finished.
            //Here you can resume playing your content.
        }
    }
}
......
}
```
Important: failing to call `play()` on an **_AdManager_** with no ads will prevent the AdswizzSDK from correctly tracking and firing opportunities for reporting purposes.

## (Advanced) Handling AdManager reporting for expected ads

Before requesting ads from the Adswizz server in a [Client-Side Insertion](#client-side-insertion) scenario, on the **_AdswizzAdRequest_** object you can enable **_AdManager_** reporting for the number of expected ads by setting the `enableExpectedAdsReporting` property to `true`. See [Your first ad request](#your-first-ad-request).

When setting the `maxAds` value for an **_AdswizzAdZone_**, the Adswizz server will try to respond with that number of ads.
However, when it cannot fulfill that request, by setting the `enableExpectedAdsReporting` to `true`, the SDK will calculate the difference between the number of expected ads (totalMaxAds) and the number of ads from the response, and it will determine how many _empty ads_ there are:
```kotlin
numberOfEmptyAds = totalMaxAds - numberOfAdsFromVASTResponse
```

Note that the `totalMaxAds` also takes into account zones that didn't configure a specific `maxAds` in which case every such zone will count as +1 towards the total number of expected ads.

Read more about _empty ads_ here: [Handling AdManager with no ads](#handling-admanager-with-no-ads).

The `numberOfEmptyAds` computed above represents the number of _empty_ ads that will be appended to the ads container of the resulting **_AdManager_**.
For each _empty_ **_AdData_**, the SDK will fire the `urlsForNoAd(adBaseManager: AdBaseManager, noAdUrlString: String): List<String>`
callback of the `AdBaseManagerAdapter` interface.

Read more about `AdManagerDatasource` methods and behavior here: [Working with the AdManager object](#working-with-admanager-object).

**Example**
```kotlin
        ...
            AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
                .withServer("SERVER_PROVIDED_BY_PIM")
                .withExpectedAdsReporting(true)
                .withZones(listOf(AdswizzAdZone(zoneId = "12345", maxAds = 3), AdswizzAdZone(zoneId = "122245", maxAds = 2), AdswizzAdZone(zoneId = "17865")))
                .build { adswizzAdRequest ->
		}
        ....
```
In the above request, the number of ads requested is as follows:  3 (zoneId = "12345") + 2 (zoneId = "122245") + 1 (zoneId = "17865") = 6. If maxAds is not provided (like for zone "17865")
or is equal to 0, this means that 1 ad is requested. In this example the `numberOfEmptyAds` is `6 - numberOfAdsFromVASTResponse`.

## AdManager best practices

After you obtain an **_AdManager_** instance object and call `prepare()`, that **_AdManager_** starts its lifecycle to render the ad(s) and begins to communicate to its listeners via `AdManagerListener` interface. The communication is based on [Ad events](#ad-events-explained) and your model should listen for any such incoming signals and act accordingly.

Here are a few things to consider when working with the **_AdManager_**:
* your application is not required to process all the states for the current ad in the **_AdManager_** via the `onEventReceived(adManager: AdManager, event: AdEvent)` method. Setting a listener on the **_AdManager_** instance is completely optional, however, keep in mind that this is your only communication channel with the **_AdManager_**.
* in some cases, the **_AdManager_** may inform your app that an error occurred via the `onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error)` method. It is considered best practice to implement this method so that your application is well aware of any such error. However, when an error is received, we do not recommend the following:
    - manually stop or release the reference to the **_AdManager_** unless it is a hard requirement in your application business logic. To clear the **_AdManager_** reference, it is best to wait for the [AllAdsCompleted](#alladscompleted) ad event which signals that the **_AdManager_** has finished all the ads. This way, the **_AdManager_** is able to finish its lifecycle properly and that all edge cases are correctly handled (e.g any pending [empty ads](#handling-admanager-with-no-ads))
    - skip the current ad for which the error was received. If there are more ads to be played, the **_AdManager_** will automatically advance to the next ad by sending the [PreparingForPlay](#preparingforplay) ad event.
* wait for the [AllAdsCompleted](#alladscompleted) ad event to clear and deallocate the **_AdManager_**, typically by calling the [reset()](#reset) method and setting it to `null`.
* when you are done with the **_AdManager_** and before deallocate-ing it, calling [reset()](#reset) is **mandatory**. Consider [reset()](#reset) as an "invalidation" before setting the reference to null. See the code snippet below:
```kotlin
 ...
adManager.reset() //mandatory
adManager = null

```

## AdManager life cycle

After requesting your first ad from the Adswizz ad server, you are ready to play the ads from the AdManager object. Keep in mind that you may receive more than one ad from the server, but you are not required to play them all. If you decide to play multiple ads in your app, you need to listen to the events that the AdManager sends to your app and decide what is the appropriate action to take. Here is a diagram of the AdManager state.

Based on enqueEnabled properties there are two different AdManager lifecycle and ad events are triggered slightly different

### Enqueue enabled false. 
Ads are not queued in a playlist and preparing/buffering of ads are done sequentially. PreparingForPlaying and ReadyToPlay for each ad are sent after previous ad has completed.

<img src="../img/AdManagerState.png" width="1000" />

### Enqueue enabled true. 
Ads are queued in a playlist and the preparing/buffering is done in parallel with playing. PreparingForPlaying and ReadyToPlay for each ad are sent after previous ad has been prepared and PreparingForPlaying and ReadyToPlay for previous ad has been sent but before it has completed.
In this case PreparingForPlaying and ReadyToPlay for second ad can be triggered anywhere in the timeline of previous ad.

<img src="../img/AdManager-state-enqueue-on.jpg" width="1000" />