# Playing ads using your player

The **_AdswizzSDK_** enables you to choose whether to play the ad media with your player or let the SDK handle that for you.
By default, the SDK will use its internal player to play the ad.<br/>

Your player (referenced as External Player in some places) can be any player instance that your application will use to load and play the ad media file (ad creative).<br/>
Using this integration method will add an abstraction layer between **_AdswizzSDK_** and your application playback allowing you to use any player type you want. (**_AdswizzSDK_** uses Exoplayer instances for its internal players)<br/>

You can choose between the following communication methods that fit your needs better
* using `AdPlayer` interface. For more details, please visit this [section](Android/SDKAndroid/PlayAdsWithYourPlayer(AdPlayer).md)
* using `MediaPlayerState` interface added in **_AdswizzSDK_** 7.9.3 version. For more details, please visit this [section](Android/SDKAndroid/PlayAdsWithYourPlayer(MediaPlayerState).md)

