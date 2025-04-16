# AdswizzSDK analytics

The AdswizzSDK collects and exposes information on SDK events that occur during its lifecycle. It was built to log different event types (informative and errors) that cover both integration robustness and ad lifecycle management in both client-side and server-side scenarios.

To enable you to complement your specific app analytics system (e.g. Firebase, Mixpanel etc) with AdsWizz SDK internal events, the analytics framework provides you with the ability to receive all the events from the SDK and forward them to your own ingestion pipelines by implementing our AnalyticsConnector interface.
To learn more about the types of events you can listen and use, please reach out to your Integration Manager at AdsWizz.

AdsWizz may also use this mechanism to collect and analyze SDK performance metrics in order to identify integration-specific issues that might affect capacity or the impressions/opportunities ratio.

To register and receive the analytics events, you have to do two things:

1. Implement our AnalyticsConnector interface
```kotlin
class MyAnalyticsConnector: AnalyticsConnector {
    override fun onLog(analyticsEvent: AnalyticsEvent) {
        // every time we log something you will also get the callback here and can
        // do whatever you want with the event
    }

    override fun onSend() {
        // When this gets called you should flush the stored events ASAP.
        // Can be called when SDK un-initializes
    }
}
```
2. Add yourself as a listener, using the following lines of code:
```kotlin
myAnalyticsConnector = MyAnalyticsConnector()
AdswizzSDK.analytics?.add(myAnalyticsConnector)
```

---
**NOTE**

The `AdswizzSDK` keeps a weak reference to the `AnalyticsConnector`, so it's the host app's responsibility to keep a strong reference to the `AnalyticsConnector` instance while listening for analytics events.


## Customize AnalyticsEvent

In addition to being able to intercept and process Analytics events logged by the AdswizzSDK through an **_AnalyticsConnector_** implementation, you can add your own attributes associated with the **_AnalyticsEvent_** objects.
The AdswizzSDK is able to pass down your key-value collection of custom attributes/properties for all events generated from the moment a request is built, through requesting ads and during ad playback.
The exposed **_AnalyticsCustomData_** data class encapsulates any additional data you wish to pass to the **_AnalyticsEvent_**. The params you provide into `AnalyticsCustomData.params` will be reflected in the **_customParams_** property of the **_AnalyticsEvent_** objects.
To inject your `AnalyticsCustomData`, you need to pass it to the AdswizzSDK using one of the approaches detailed below.

### Custom analytics data through AdRequest

You can instruct the AdswizzSDK to track your custom analytics data by passing your **_AnalyticsCustomData_** object to your **_AdRequest.Builder_** implementation or **_AdswizzAdRequest.Builder_** whichever you are using. Note that AdswizzSDK will keep a strong reference to your **_AnalyticsCustomData_** object.

Here is an example using an **_AdRequest_**:

```kotlin
val analyticsCustomData = AnalyticsCustomData(mutableMapOf("a" to "b", "c" to 7))

val adRequest: adRequest = AdRequest.Builder()
            .withUrlString("SERVER_URL_PROVIDED_BY_PIM")
            .withAnalyticsCustomData(analyticsCustomData)
            .build()
```

Here is an example using the **_AdswizzAdRequest_**:

```kotlin
        val analyticsCustomData = AnalyticsCustomData(mutableMapOf("a" to "b", "c" to 7))

        AdswizzAdRequest.Builder()
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withZones(
                listOf(
                    AdswizzAdZone("ZONEID_1_PROVIDED_BY_PIM"),
                    AdswizzAdZone("ZONEID_n_PROVIDED_BY_PIM")
                )
            )
            .withAnalyticsCustomData(analyticsCustomData)
            .build { adswizzAdRequest ->
            }
```

### Custom analytics data through Ad Manager

Another approach to pass the `AnalyticsCustomData` object is by using the **_analyticsCustomData_** property of the **_AdBaseManager_** instance. Here you can update either the entire reference of the `AnalyticsCustomData` or alter only the `params` property. The AdswizzSDK will use the (updated) `params` to populate the `customParams` property of any subsequent **_AnalyticsEvent_** triggered after your update.

```kotlin
val analyticsCustomData = AnalyticsCustomData(mutableMapOf("a" to "b", "c" to 7))

adBaseManager.analyticsCustomData = analyticsCustomData
```