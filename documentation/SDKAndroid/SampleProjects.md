# Sample projects

The best way to see AdswizzSDK in action is by studying the example projects included in the `/samples` folder. Download the `/samples` folder using the below `git` command:
```properties
$ git clone https://github.com/adswizz/ad-sdk-android.git
Username: adswizz-sdk
Password: your_access_token (provided by AdsWizz)
```

## Basic Sample

This sample demonstrates a basic client-side insertion scenario by showing how to create and customize an _**AdswizzAdRequest**_. It shows how to use it to create an _**AdRequestConnection**_ and finally request ads. Next, it demonstrates basic usage of an instance of _**AdManager**_ once it has been obtained from the SDK.

## Basic MediaPlayerState Sample

This sample demonstrates a basic client-side insertion scenario using an instance of a  _**MediaPlayerState**_ interface.

## Basic Sample External AdPlayer 

This sample demonstrates a basic client-side insertion scenario using an instance of a  _**AdPLayer**_ interface.

## Basic Sample Empty Ads

This sample demonstrates a basic client-side insertion scenario when no ad is returned by the SDK and the specific empty ads events being triggered in this case.

## Streaming Sample

This sample demonstrates a basic server-side insertion scenario by showing how to create and customize an _**AdswizzAdStreamManager**_.
If the ad that comes from the stream is interactive, you can observe the interactivity in both the **Phone** and the **Smartwatch** scenarios.

## Streaming Sample (with External Player)

This sample demonstrates a basic server-side insertion scenario similar to the above sample, with the only difference being the external player that has been used in this case.

## DvrStreaming Sample

This sample demonstrates a basic server-side insertion scenario that also contains the DVR time shifting capabilities of an AIS DVR enabled stream.

## DvrStreaming Sample (with External Player)

This sample demonstrates a server-side insertion scenario similar to the above sample, the only difference being the external player that has been used in this case.

## PreCache Sample

This sample demonstrates a client-side insertion scenario with enqueue + precache enabled. It makes an _**AdswizzAdRequest**_, then it starts downloading the ads, and when the download is complete it starts the playback. Alternatively, you can play the ads before the download is complete. In this second scenario please notice that the enqueue functionality is used instead.

## Video Ad Sample

This app demonstrates a client-side insertion scenario with video by showing how to add a _**videoViewId**_ to the _**AdManagerSettings**_ and also a _**AdVideoView**_ to the layout. It is similar to the Basic Sample.

## Video Ads Sample (Full Screen External Player)

This app demonstrates a client-side insertion with video by showing how to add a _**videoViewId**_ to the _**AdManagerSettings**_ and also a _**AdVideoView**_ to the layout. The video is full screen, the application can change layout from portrait to landscape, and it uses an external player.

## Podcast Sample

This sample shows how your app can integrate the **_AdswizzSDK_** podcasting support by using an **_AdswizzAdPodcastManager_**. It demonstrates how to download a podcast locally, as well as playing it from disk.

## UAMP Integration Sample

This sample demonstrates how your app can integrate with the **_AdswizzSDK_** on both the client side and server side, as well as for podcasting, using a UAMP-like architecture. It illustrates how to play podcasts from a playlist with **_AdswizzAdPodcastManager_**, how to play various ads from a selection of ad types using **_AdManager_**, and how to stream different streams from a list with **_AdswizzAdStream_**. The design is grounded in the UAMP architecture. For further details, please visit https://github.com/android/uamp/blob/main/docs/FullGuide.md.

## Basic Sample using extension artefact

This sample demonstrates a basic client-side, server-side and podcast insertion scenario using the **_com.adswizz.adswizz-sdk-extension_** artefact which simplifies creation and consumption of ads by the use of coroutines and sharedflows.

## JetpackCompose Sample

This sample demonstrates how to build your UI wrapper for **_AdCompanionView_** and **_AdVideoView_** using `AndroidView` component. In those composable functions are presented the steps required to properly use **_AdswizzSDK_** UI components in a compose screen or application and it showcases the main features/usa-cases such as : resizing, add/remove to view hierarchy, view listeners.