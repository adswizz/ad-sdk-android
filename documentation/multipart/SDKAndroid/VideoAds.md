# Video ads

AdswizzSDK is capable of handling video content when an ad server delivers video ads. The ad lifecycle is the same as for audio ads. The additional steps needed to play video ads are described below.

## Adding an AdVideoView

**_AdVideoView_** is an instance of **_View_** responsible for rendering video content during video ads playback. Just like companion views - see [Companion Banner](#companion-banner) section, a video view can be created either programmatically or by adding it in a layout xml file.

Once created, you have ownership over this view and you can add it to your hierarchy like any other UI component.

The **_AdVideoView_** uses the following custom attributes:
- **_video_view_id_**: is used to identify the video view. It should be a non-negative integer value and it should be unique among all ad video views used by the host app. If a negative value is put then an exception will be thrown. In case this attribute is omitted then an auto-generated id (of negative value) will be assigned by the SDK.

- **_resize_mode_**: when a video is rendered, the SDK needs to know how to resize the video frame in comparison with the video view. This attribute can be changed at any time. The following options are available:
     * **_fit_**: either the width or height is decreased to obtain the desired aspect ratio.
     * **_fixed_width_**: the width is fixed and the height is increased or decreased to obtain the desired aspect ratio.
     * **_fixed_height_**: the height is fixed and the width is increased or decreased to obtain the desired aspect ratio.
     * **_fill_**: the specified aspect ratio is ignored.
     * **_zoom_**: either the width or height is increased to obtain the desired aspect ratio.


- **_video_surface_type_**: the type of surface used to render the video
    * **_auto_select_**: the AdswizzSDK will choose the best option
    * **_none_**: the video will not be rendered in the view
    * **_surface_view_**: the AdswizzSDK will use **_SurfaceView_** to render the video.
    * **_texture_view_**: the AdswizzSDK will use **_TextureView_** to render the video.

**NOTE** you can change the **_video_view_id_** assigned after creating the **AdVideoView** programmatically or through inflating a layout by using the `setVideoViewId()` function. Take care that the **_video_view_id_** you set follows the same rules (unique and it has to be the same as the one passed in **_AdManagerSettings_**).

Example:
```kotlin
    <com.ad.core.video.AdVideoView
        android:id="@+id/videoView1"
        app:resize_mode="fit"
        app:video_surface_type="surface_view"
        app:video_view_id="1"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

## Setup video support

The first step is to let the SDK know that you wish to render video content to your users. To do that, you'll need to pass the video view id of the **_AdVideoView_** when setting up the Ad Manager via **_AdManagerSettings_**:

```kotlin
    adManager.adManagerSettings = AdManagerSettings.Builder()
        .videoViewId(1)
        ....
        .build()
```

At this point the video view doesn't have to be visible / added to your hierarchy yet. But remember that you own the video view instance and it is your responsability to keep track of.

The **_AdVideoView_** will render video content during the video ad playback. After the video ad finished playing, the video view may be dismissed.
You can only set one ad video view id on an Ad Manager but you can reuse the same video view id for multiple ad managers if they don't run at the same time.

To clean the **_AdVideoView_** you can use the **_cleanContent()_** function. This may be useful when the ad finishes playing if you want to clean the last video frame.

## Subclassing AdVideoView

**_AdVideoView_** can be subclassed and used just like any other **_View_** based component. However you should not temper with the subviews managed by the SDK. Nor should you disable user interaction on **_AdVideoView_** because by doing so you will prevent from correctly reporting click through or click trackings.


## Video View Listener

There are no specific video events to respond to, however you can set a listener to your **_AdVideoView_** instance that implements the **_AdVideoView.Listener_** interface. Just like companion views, you will be notified to decide whether or not to override click through target URL on the video view. You will also be notified when the application is about to go to background due to a click through. The interface is displayed below:

```kotlin
interface Listener {
    /**
     * Called immediately when the video starts playing
     * @param adVideoView The `AdVideoView` instance which hosts the playing video
     */
    fun onVideoStarted(adVideoView: AdVideoView) {}

    /**
     * Called immediately when the video ends playing
     * @param adVideoView The `AdVideoView` instance which hosts the playing video
     */
    fun onVideoEnded(adVideoView: AdVideoView) {}

    /**
     * Called immediately when the video starts buffering
     * @param adVideoView The `AdVideoView` instance which hosts the playing video
     */
    fun onVideoBufferingStart(adVideoView: AdVideoView) {}
    /**
     * Called immediately when the video ends buffering
     * @param adVideoView The `AdVideoView` instance which hosts the playing video
     */
    fun onVideoBufferingEnd(adVideoView: AdVideoView) {}

    /**
     * Called when a clickThrough url is available on the AdVideoView
     * @param adVideoView The `AdVideoView` instance which hosts the playing video
     * @param clickThroughUrl the url for clickThrough action or null if it was unset
     */
    fun onVideoClickThroughChanged(adVideoView: AdVideoView, clickThroughUrl: String?) {}

    /**
     * Called immediately when a touch event is detected over the ad video view.
     * Give the host application a chance to override the default behaviour when a video `clickThrough` event occurs.
     * @return true if the host application wants to handle itself the target url
     * @return false otherwise. In this case the target url will be handled internally by the SDK
     *
     * @param adVideoView: The `AdVideoView` instance which received the touch event
     */
    fun shouldOverrideVideoClickThrough(adVideoView: AdVideoView): Boolean

    /**
     *  Called when the application is about to go to background as a result of
     *  a video `clickThrough` event and the target url is opened in the external browser.
     *
     *  @param adVideoView: The `AdVideoView` instance which triggers the event
     */
    fun willLeaveApplication(adVideoView: AdVideoView)
}
```
```kotlin
fun setListener(l: Listener?) {}
```

In case you want to listen for video play state changes you can use this listener:

```kotlin
interface PlayStateListener {
    /**
     * Called whenever the video play state changes between
     * [AdVideoPlayState.IDLE], [AdVideoPlayState.BUFFERING] & [AdVideoPlayState.PLAYING]
     *
     * @param adVideoView: The `AdVideoView` instance which triggers the event
     * @param playState: the new play state value
     */
    fun onVideoPlayStateChanged(adVideoView: AdVideoView, playState: AdVideoPlayState)
}
```
```kotlin
fun setPlayStateListener(listener: PlayStateListener?) {}
```

## Viewability

Viewability enables AdswizzSDK to allow third party verification measurement for both audio and video ads. See [Open Measurement in AdswizzSDK](#open-measurement-in-adswizzsdk) for more details.

### Friendly obstructions

When there are any native elements which are part of the **_AdVideoView_** container, such as a skip button, some logo text, or other kind of overlay, you should register them as "friendly" obstructions to prevent them from counting towards coverage of the video ad during playback. This applies to any view that obstructs the **_AdVideoView_** and is not in it's subview tree.

After you identify such elements that should be marked as friendly, you can create an **_AdVideoFriendlyObstruction_** object by passing in the **_view_** which is causing the obstruction, a **_purpose_** (see `AdVideoFriendlyObstructionPurpose` enum for all possible cases) and an optional **_detailedReason_**. After that, just
register the newly created obstruction object to your **_AdVideoView_** instance.


```kotlin

....

val videoView = rootView.findViewById(R.id.videoView)
val closeButton = rootView.findViewById(R.id.closeButton)
....

val obstruction = AdVideoFriendlyObstruction(closeButton, AdVideoFriendlyObstructionPurpose.CLOSE_AD, "Close button")
videoView.registerFriendlyObstruction(obstruction)

```

When you're done with them, these obstructions can be removed one by one by calling:

```kotlin
videoView.unregisterFriendlyObstruction(obstruction)
```
or all at once by calling:

```kotlin
videoView.unregisterAllFriendlyObstructions()
```

To get the list with all friendly obstructons for a view you can use:

```kotlin
val list = videoView.getFriendlyObstructionList()
```


## Video state

Rounding up the viewability support, AdswizzSDK enables you to communicate your **_AdVideoView_** state by using the **_state_** property. Set the state whenever the video view transitions to one of the following:

### NORMAL
The video view's default playback size.

### EXPANDED
The video view has expanded from its original size.

### COLLAPSED
The video view has been reduced from its original size. The video is still potentially visible.

### FULLSCREEN
The video view has entered fullscreen mode.

### MINIMIZED
The video view is collapsed in such a way that the video is hidden. The video may or may not still be progressing in this state, and sound may be audible.

Below is an example:

```kotlin

// Default state is AdVideoState.NORMAL
val videoView = rootView.findViewById(R.id.videoView)
....

// When your video view transitions to another state (e.g fullscreen).
// Inform AdswizzSDK for any state change for viewability purposes.
videoView.state = AdVideoState.FULLSCREEN

```

## Video View Cleanup
While **_AdVideoView_** has its cleanup mechanism, for a better performance, the `activityOnDestroy()` method of **_AdVideoView_** needs be called when the Activity or the Fragment containing that **_AdVideoView_** instance is about to be destroyed.
