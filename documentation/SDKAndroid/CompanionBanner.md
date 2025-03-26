# Companion Banner

The AdswizzSDK enables you to configure companion banners if you are provided by the AdsWizz PIM with a companion zone ID.


## Adding an AdCompanionView

The easiest way to add a companion view is from XML. You can also add the view programmatically.
Once created, the SDK will keep track of all of your companion views and fill them with ad-related content as the
ad is playing. By default, an **_AdCompanionView_** instance is inactive, meaning that, as long as it's not visible on screen, it won't load the content (during the ad break) or fire tracking events. You can temporarily "deactivate" a companion view at any time by either hiding it, resizing it so that the frame width or height is zero, making it transparent or removing it from the view tree. The SDK will automatically keep track of your companion state and stop or resume rendering. If you want to show or hide your **_AdCompanionView_** based on incoming ad, you can easily check "hasCompanion" flag from **_AdData_** interface.
### Adding AdCompanionView in XML
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">
....
    <com.ad.core.companion.AdCompanionView
            android:id="@+id/companionView1"
            android:layout_width="0dp"
            android:layout_height="0dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>
....
</androidx.constraintlayout.widget.ConstraintLayout>
```
### Adding AdCompanionView programmatically
```kotlin
        val companionView = AdCompanionView(context)
        yourContainer.addView(companionView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
```

### AdCompanionView background color
**_AdCompanionView_** will set by default its background color to `Color.WHITE` if the background attribute is not previously set.
In order to change this behaviour you can set background attribute to transparent or any other color, as follows: 
 - `android:background="@android:color/transparent"`
 - `companionView.setBackgroundColor(Color.TRANSPARENT)`

## Setting up

There are two scenarios for setting up the companion banner: one for client-side insertion and one for server-side insertion or podcasting.

### Client-side setup
First, when you create an **_AdswizzAdRequest_**, you must configure the **_companionZones_** with the value provided by the PIM.
</br>You can either use the zoneId or zoneAlias in the request builder, as follows:

The new `withCompanionZoneIds(companionZoneIds: List<Int>)` method allows you to specify multiple numeric companion zone IDs. It replaces the deprecated `withCompanionZones(companionZones: String?)` method from previous versions.

```kotlin
        AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withZones(listOf(AdswizzAdZone("ID_1"), AdswizzAdZone("ID_2")))
             //.withCompanionZones("123,5678")  // Deprecated as of v7.12.0
            .withCompanionZoneIds(listOf(123, 5678)) // New method from v7.12.0
            .build { adswizzAdRequest ->
//                    handle the request
            }
```

The new `withCompanionZoneAliases(companionZoneAliases: List<String>)` method provides a convenient way to specify companion zones using string aliases instead of numeric IDs. The aliases should be formatted as follows:

```kotlin
        AdswizzAdRequest.Builder() //Build the Ad Request with the needed parameters
            .withServer("SERVER_PROVIDED_BY_PIM")
            .withZones(listOf(AdswizzAdZone("ID_1"), AdswizzAdZone("ID_2")))
            .withCompanionZoneAliases(listOf("ALIAS_1", "ALIAS_2")) //convenience method for zone aliases
            .build { adswizzAdRequest ->
                 //handle the request
             }
```

> **Note**
> Setting simultaneously `companionZoneIds` and `companionZoneAliases` in the AdswizzAdRequest.Builder will throw an Exception.

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
adCompanionOptions.extraExposureTime = 1.2 // seconds.
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

## Optimize companion display

Starting with version 7.7.22, a new parameter called `optimizeCompanionDisplay` has been added to `AdCompanionOptions` class. 

```kotlin
val adCompanionOptions = AdCompanionOptions()
adCompanionOptions.optimizeCompanionDisplay = false //change to false only for ReactNative environments
AdswizzSDK.setAdCompanionOptions(adCompanionOptions)
```

By default, it is set to `true`, allowing for toggling between two distinct approaches for loading content in companion banners. When set to `true`, the `AdCompanionView` object dynamically adds and removes `WebView` child views, which may lead to display problems in ReactNative. However, when set to `false`, the child views remain throughout the entire companion lifecycle, resolving the display issues in ReactNative but resulting in a slight increase in memory usage.

## Companion best practices

When you create an **_AdCompanionView_** instance, **_AdswizzSDK_** will start monitoring the companion view lifecycle and render the associated content during the ad playback. It begins to communicate to its listener via `AdCompanionView.Listener` interface. The communication focuses on updates for content loading / displaying by the **_AdCompanionView_** and your model should listen for any such incoming signals and act accordingly. i.e perform any further UI updates if needed, listen for errors to be able to display placeholders, etc.
> **Note**
> You can create and display multiple **_AdCompanionView_** instances in your application within different view hierarchies. **_AdswizzSDK_** will render the same ad related content for all companion banner instances while an AdManager starts playing the ads.
> You should use the same **_AdCompanionView_** instance during an AdBreak, in order to avoid resource display delays. Therefore, you should not destroy/remove the current view for consecutive ads having "hasCompanion" flag defined **_AdData_** interface

Note that it is your responsibility to keep a strong reference the **_AdCompanionView_** instances that you create in your application (although, this is typically achieved by default when you add the companion view(s) to your own view hierarchy in your app).

Here are a few things to consider when working with the **_AdCompanionView_**:
* content is loaded and rendered within an **_AdCompanionView_** only if the view is considered **visible** on screen. The visibility checks performed by the **_AdswizzSDK_** include but not limited to:
  - Android `View` properties such as `alpha` and `visibility`
  - non zero size for `width` and `height`
  - on display (screen) visibility. The **_AdCompanionView_** should be displayed within screen bounds.
* it is not mandatory to implement any of the callbacks for the **_AdCompanionView_** from the `AdCompanionView.Listener` interface. Setting a listener to your **_AdCompanionView_** instance is completely optional, however, keep in mind that this is your only communication channel with the **_AdCompanionView_** when it comes to content rendering.
* in some cases, the **_AdCompanionView_** may inform your app that an error occurred via the `fun didFailToDisplayAd(adCompanionView: AdCompanionView, error: Error)` listener method. It is considered best practice to implement this method so that your application is well aware of any such error. When an error is received, you may want to show a placeholder instead of the empty companion view or remove it entirely from your views hierarchy.
* if your app is using `jetpack compose`, we highly encourage you to take a look at the **_JetpackComposeSample_** to the [Sample Project section](Android/SDKAndroid/SampleProjects) showcasing the **_AdCompanionView_** and **_AdVideoView_** interoperability with `composable UI` components.
