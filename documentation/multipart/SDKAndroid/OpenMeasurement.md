# Open Measurement in AdswizzSDK

**_AdswizzSDK_** includes the [Open Measurement SDK](https://iabtechlab.com/standards/open-measurement-sdk/), an industry standard developed by the IAB and designed to enable third-party viewability and verification measurement for audio and video ads served through VAST servers. **_Open Measurement_** is enabled by default in AdswizzSDK and ads must be configured to traffic **_<AdVerifications>_** in their VAST. Additionally, refrain from covering the **_AdVideoView_** container with any overlays (transparent or opaque) because these are taken into account by the OM SDK, marked as obstructions and therefore affect viewability and reporting.
For more info see [Viewability](#viewability) on how to register your overlays as "friendly" obstructions by using **_AdVideoFriendlyObstruction_** objects.

## Video controls obstructions

Video controls such as pause buttons or progress bars provide important playback information to your users. On mobile, it is common practice to render large, touch-friendly controls over the container which renders video content. When using **_AdswizzSDK_**, one way to show these controls is by adding them above the **_AdVideoView_** container. Usually, the controls are child elements of this view that covers (even partially) the underlying video player.
When video ad viewability is calculated through the Open Measurement SDK, all views overlaying the video content are considered obstructions and reduce the viewability rate. If an overlay (even fully transparent) sits above the entire video ad view, it is possible for inventory to be declared completely unviewable.
The Open Measurement SDK allows for video controls to be considered "friendly" obstructions that contribute to the user’s experience. Once registered as "friendly", these controls are excluded from ad viewability measurement. Also it should be noted that the Open Measurement SDK automatically adds all views in the **_AdVideoView_** subview tree as friendly obstructions so they do not count for viewability measurement.

Here are some examples of elements that are appropriate to be registered as friendly obstructions:
  * transparent overlays that might be used to capture user taps
  * playback buttons
    + pause
    + play
    + skip
    + fullscreen
    + minimize
    + Cast/Airplay
    + progress/seek
  * other playback relevant controls

Usually, elements like pop ups or dialogs should not be registered.