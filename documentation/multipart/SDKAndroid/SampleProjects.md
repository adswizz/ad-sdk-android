# Sample projects

The best way to see AdswizzSDK in action is by studying the example projects included in the `/samples` folder. Download the `/samples` folder using the below `git` command:
```properties
$ git clone https://github.com/adswizz/ad-sdk-android.git
Username: adswizz-sdk
Password: your_access_token (provided by AdsWizz)
```

## BasicSample

This sample demonstrates a basic client-side insertion scenario by showing how to create and customize an _**AdswizzAdRequest**_. It shows how to use it to create an _**AdRequestConnection**_ and finally request ads. Next, it demonstrates basic usage of an instance of _**AdManager**_ once it has been obtained from the SDK.

## StreamingSample

This sample demonstrates a basic server-side insertion scenario by showing how to create and customize an _**AdswizzAdStreamManager**_.
If the ad that comes from the stream is interactive, you can observe the interactivity in both the **Phone** and the **Smartwatch** scenarios.

## StreamingSample with External Player

This sample demonstrates a basic server-side insertion scenario similar to the above sample, with the only difference being the external player that has been used in this case.

## DvrStreamingSample

This sample demonstrates a basic server-side insertion scenario that also contains the DVR timeshifting capabilities of an AIS DVR enabled stream.

## PreCache Sample

This sample demonstrates a client-side insertion scenario with enqueue + precache enabled. It makes an _**AdswizzAdRequest**_, then it starts downloading the ads, and when the download is complete it starts the playback. Alternatively, you can play the ads before the download is complete. In this second scenario please notice that the enqueue functionality is used instead.

## Video Ad Sample

This app demonstrates a client-side insertion scenario with video by showing how to add a _**videoViewId**_ to the _**AdManagerSettings**_ and also a _**AdVideoView**_ to the layout. It is similar to the Basic Sample.

## Video Ads Sample Full Screen External Player

This app demonstrates a client-side insertion with video by showing how to add a _**videoViewId**_ to the _**AdManagerSettings**_ and also a _**AdVideoView**_ to the layout. The video is full screen, the application can change layout from portrait to landscape, and it uses an external player.

## Podcast sample

This sample shows how your app can integrate the **_AdswizzSDK_** podcasting support by using an **_AdswizzAdPodcastManager_**. It demonstrates how to download a podcast locally, as well as playing it from disk.