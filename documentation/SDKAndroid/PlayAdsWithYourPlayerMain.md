# Playing ads using your player

The **_AdswizzSDK_** enables you to choose whether to play the ad media with your player or let the SDK handle that for you.
By default, the SDK will use its internal player to play the ad.<br/>

Your player (referenced as External Player in some places) can be any player instance that your application will use to load and play the ad media file (ad creative).<br/>
Using this integration method will add an abstraction layer between **_AdswizzSDK_** and your application playback allowing you to use any player type you want. (**_AdswizzSDK_** uses Exoplayer instances for its internal players)<br/>

You can choose between the following communication methods that fit your needs better
* using `AdPlayer` interface. For more details, please visit this [section](Android/SDKAndroid/PlayAdsWithYourPlayer(AdPlayer).md)
* using `MediaPlayerState` interface added in **_AdswizzSDK_** 7.9.3 version. For more details, please visit this [section](Android/SDKAndroid/PlayAdsWithYourPlayer(MediaPlayerState).md)

<br/>Since **_AdswizzSDK_** 7.10.0 release, the player dependencies (com.adswizz:exoplayer2-* and com.adswizz:exoplayer-media3) are now optional for integrations that don't use AdswizzSDK's internal player. So, if you are using your player in order to play the ad content, you should **only** add the sdk module dependency in your `build.gradle` as follows: <br/>

```groovy

        implementation "com.adswizz:adswizz-sdk:7.11.1" // <--- sdk module
//      The com.adswizz:exoplayer is not needed in this integration since 7.10.0
//      implementation "com.adswizz:exoplayer2-19:7.11.1" // <--- old versioned Exoplayer module
//      ------  Your other dependencies -----

```