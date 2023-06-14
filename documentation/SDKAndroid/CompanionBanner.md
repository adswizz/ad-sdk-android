# Companion Banner

The AdswizzSDK enables you to configure companion banners if you are provided by the AdsWizz PIM with a companion zone ID.


## Adding an AdCompanionView

The easiest way to add a companion view is from XML. You can also add the view programmatically.
Once created, the SDK will keep track of all of your companion views and fill them with ad-related content as the
ad is playing. By default, an **_AdCompanionView_** instance is inactive, meaning that, as long as it's not visible on screen, it won't load the content (during the ad break) or fire tracking events. You can temporarily "deactivate" a companion view at any time by either hiding it, resizing it so that the frame width or height is zero, making it transparent or removing it from the view tree. The SDK will automatically keep track of your companion state and stop or resume rendering. If you want to show or hide your **_AdCompanionView_** based on incoming ad, you can easily check "hasCompanion" flag from **_AdData_** interface.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">
....
    <com.ads.coresdk.companion.AdCompanionView
            android:id="@+id/companionView1"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>
....
</androidx.constraintlayout.widget.ConstraintLayout>
```
## Setting up

There are two scenarios for setting up the companion banner: one for client-side insertion and one for server-side insertion or podcasting.

### Client-side setup
First, when you create an **_AdswizzAdRequest_**, you must configure the **_companionZones_** with the value provided by the PIM.

```kotlin
        AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withZones(setOf(AdswizzAdZone("ZONEID_PROVIDED_BY_PIM")))
            .withCompanionZones("COMPANION_PROVIDED_BY_PIM") // A comma-separated list of zone IDs from where companions will be selected. Ex: "1234,5678"
            .build { adswizzAdRequest ->
            }
```

Once the request is made, if there is a companion associated with the ad, it will be loaded into your **_AdCompanionView_** object.

### Server side & Podcasts setup

In order for an Ad Companion Banner to work with a server-side or podcast integration you must create an AFR Config object that should be provided to the SDK at initialization. With the information obtained from AdsWizz, this object can be configured like in the code snippet below:

```kotlin
    AdswizzSDK.initialize(context: Context)

    AdswizzSDK.afrConfig = AfrConfig(
            AdswizzAdRequest.HttpProtocol.HTTP, //protocol http or https
            "demo.deliveryengine.adswizz.com", // provide your own delivery engine endpoint address here
            "www/delivery/afr.php", // endpoint
            "17213" // companion zone id - optional
        )
```

## Companion events

You can associate a listener with your AdCompanionView that implements the AdCompanionView.Listener interface.
You will be notified when the ad has loaded the companion view, if the companion view was displayed, or if it ended
the display. If there was an error while loading the companion, you will be notified through this listener.
Also, you can decide whether or not to override click-through on the companion view.

## Extra exposure time

By default, the AdCompanionView will end displaying the content after the ad finishes playing.
If you need to keep the companion on the screen for a longer time(or indefinitely), you can configure it like this:

```kotlin
val adCompanionOptions = AdCompanionOptions()
adCompanionOptions.extraExposureTime = 1.2// these are seconds.
AdswizzSDK.setAdCompanionOptions(adCompanionOptions)
```

## Preferred resource type

You can select the preferred resource type from a list of 4 options: image, html, iframe and first resource available. Below is a snippet:

```kotlin
val adCompanionOptions = AdCompanionOptions()
adCompanionOptions.preferredResourceType = AdCompanionResourceType.IMAGE
AdswizzSDK.setAdCompanionOptions(adCompanionOptions)
```

If not set, the default value is first resource available.
If the preferred resource type is not found, first resource available will be selected by default.

**NOTE**

Starting with version 7.7.22, `AdCompanionOptions` has a new parameter `optimizeCompanionDisplay` with a default value of true, which is used to toggle between two different ways of loading content in companion banner. When set to true, the companion object dynamically add and remove some child views which can cause display issues in ReactNative. When is set to false, the child views are present all the companion lifecycle which solves the display issues in ReactNative but introduce a small memory overhead.
