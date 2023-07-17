[![AdsWizz](../img/adswizz_logo.png)](https://www.adswizz.com)
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [7.7.24] - 2023-07-17

### Added
 - Added an interface called `Logger`. This enables consumers of the AdswizzSDK to receive log messages by utilizing the `log` callback. It is primarily intended for debugging purposes.
 
### Changed
 - The **_Integration Guide_** has been revised to incorporate a new section dedicated to debugging. [This section](Android/SDKAndroid/Debugging.md) provides detailed information on the logger mechanism.

## [7.7.23] - 2023-06-21

### Fixed
 - Improved voice detection for voice ads when users seek in podcasts.
 - Fixed an issue with the voice ad detection system in silent mode when users pause the playback.

## [7.7.22] - 2023-06-14
### Added
 - Added the `optimizeCompanionDisplay` parameter in the `AdCompanionOptions` class, resolving a ReactNative problem when utilizing `AdCompanionView` within a layout.

### Fixed
 - Fixed an issue with companion display when using the SDK within a ReactNative environment.
 - Improved voice detection when users seek in podcasts
 - Fixed the issue with OMSDK's `PercentageInView` analytics not functioning correctly after a change in orientation for video ads. 
 
### Changed
- Updated `com.google.android.gms:play-services-ads` dependency from version `18.2.0` to version `20.0.0`
- Improved **_Integration Guide_** 

## [7.7.21] - 2023-05-22
### Added
- Added ExoPlayer 2.18 and 2.17 artifacts in the SDK library release.
- Added `removeAdBaseManagerListener()` and `removeAdBaseManagerAdapter()` methods in `AdBaseManager`.
- Added `removeAdManagerListener()` method in `AdManager`.
- Added the `automaticallySecureConnectionForAdURL(isEnabled: Boolean)` method in `AdPodcastManagerSettings.Builder` and `AdManagerStreamingSettings.Builder`. This setting is enabled by default.

### Changed
- Updated the ExoPlayer artifact dependency to the latest minor versions (2.10.8, 2.12.3, 2.14.2, 2.15.1, 2.16.1) instead of the previous versions (2.10.0, 2.12.0, 2.14.0, 2.15.0, 2.16.0).
- Improved **_Integration Guide_** 

## [7.7.20] - 2023-04-26
### Changed
- Updated `com.google.android.gms:play-services-location` dependency from version 17.0.0 to version 21.0.1
- Improved **_Integration Guide_** 


## [7.7.19] - 2023-03-13
### Added
- Added support for `[APIFRAMEWORKS]` and `[OMIDPARTNER]` VAST Macros for OMSDK Audibility
- **GPP** consent:
    - New public property in the `AdswizzSDK` shared instance:
      - `gppConsent` for passing a GPP consent string (as per IAB's new GPP privacy standard)

### Fixed
- Fixed a crash happening when clicking on a companion banner or a video ad with an invalid url protocol or with no application registered to handle that specific deep link url

### Changed
- Changed `PalNonceHandlerInterface` to reflect changes in Google PAL library which was updated from 17.0.5 to 20.0.1
	- removed method `sendAdImpression`
	- added two methods `sendPlaybackStart` and `sendPlaybackEnd` which are triggered in pairs for each video ad at start and at the end of video
- Updated the Integration Guide: _Interoperability with the PAL SDK_, _Sample projects_ and _AdsWizzSDK general settings_ sections
- Renamed the decoration parameter `aw_0_1st.nonce` to `aw_0_1st.pal_nonce`
- Improved companion banner display logic

## [7.7.18] - 2023-02-16
### Fixed
- Fixed a problem in the `AdswizzAdPodcastManager` class that manifested as a broken communication between ExoPlayer and adManager's listener callbacks that were not called. 


## [7.7.17] - 2023-02-07
### Added
- Static function `fun decorateUrl(url: String): Uri` exposed in the `AdswizzAdStreamManager` class to decorate any streamURL with targetability parameters
- Static function `fun decorateUrl(url: String): Uri` exposed in the `AdswizzAdPodcastManager` class to decorate any podcastURL with targetability parameters

### Fixed
- A crash happening during the SDK initialization when the `Android System Webview` is disabled, force stopped or is currently being updated.	

## [7.7.16] - 2022-12-23
### Changed
- Ad request URL decorator:
  - update logic on the `listenerId` param to use the zero `UUID` format (`00000000-0000-0000-0000-000000000000`) when user opt out from advertising ID
- `AdData` object's `adType` member:
  - determine the `adType` exposed by the `AdData` protocol by checking the MIME type from the VAST MediaFile(s)

## [7.7.15] - 2022-11-17
### Added
- New public method in **_AdManager_** interface:
	- `skipAd(error: Error)` Skip current ad with an error reason. It will fire error tracking instead of skip tracking
- New public method in **_AdPlayer.Listener_** interface:
	- `onSkipAd(error: Error)` When called, inform `AdPlayer.Listener` implemented object that a skip with error should be triggered. The default AdManager implementation will call `skipAd(error: Error)`


## [7.7.14] - 2022-11-08
### Added
- User-Agent support:
    - Added the option for the **_AdswizzSDK_** to internally use a custom User-Agent when firing ad requests and tracking URLs to the AdsWizz server (configurable via ZeroConfig). The default value for the User-Agent has the `web` format

### Changed
- Session Id support:
    - improved the **_AdswizzSDK_** handling for the internal `sessionId` (configurable via ZeroConfig)
- Integration Guide:
    - improved structure and privacy/permissions coverage

## [7.7.13] - 2022-10-14
### Added:
- New public method on class **_AdswizzAdRequest.Builder_**:
	- `withExpectedAdsReporting(enable: Boolean)` - calling this method with the `true` flag will enable
	reporting of 'empty ads' when the ad request returns fewer ads than actually requested using the `maxAds`
	property of the **_AadswizzAdZone_** object.

### Changed:
- The [Integration Guide](Android/SDKAndroid/ClientSideInsertion#(Advanced)-Enable-expected-ads-reporting) was updated to explain how to use the new **_AdswizzAdRequest.Builder_**'s public method named `withExpectedAdsReporting(enable: Boolean)`

## [7.7.12] - 2022-09-21
### Changed:
Podcasting:
	- the ability to disable client-side reporting (impression and quartile tracking URLs)

## [7.7.11] - 2022-08-09
### Added:
- New public method on class **_AdManagerSettings_**
	- `preferredMaxBitrate()` called to set a preferred bitrate for asset quality selection

### Fixed
- fixes afrRequest where a banner mismatch occurs. When this happens, the `didFailToDisplayAd` method of `AdCompanionClient.Listener` is called

### Changed:
- The [Integration Guide](Android/SDKAndroid/AdswizzGeneralSettings#preferredMaxBitrate) was modified to explain how to use the new **_AdManagerSettings_**'s public function named `preferredMaxBitrate(bitrate: Int)`

## [7.7.9-7.7.10]
### Fixed
- bug fixes

## [7.7.8] - 2022-08-02
### Added:
- FireOS compatibility limitations to the integration guide

### Fixed:
- fire the tracking events properly when an ad is skipped in a multiple ads in ad manager scenario

## [7.7.7] - 2022-06-29
### Added:
- New public method on class **_AdVideoView_**
	- `activityOnDestroy()` to be called when parent container (Activity or Fragment) is destroyed

### Changed:
- The [Integration Guide](Android/SDKAndroid/VideoAds#Video-View-Cleanup) was modified to explain how to use the new **_AdVideoView_**'s public function named `activityOnDestroy()`

## [7.7.6] - 2022-06-16
### Added:
- Podcast RAD support
- Added analytics for Voice Ads interactivity to track which keyword triggered the action

### Fixed:
- AdCompanion Banner sends `didFail` event correctly when the static resource fails to load

### Changed:
- The [Integration Guide](Android/SDKAndroid/CompanionBanner) was modified to better explain the AdCompanion integration in a server-side insertion scenario

## [7.7.5] - 2022-05-16
### Fixed:
- proguard rule that keeps a newly introduced `AdEvent.Type.State.PlaybackError` to signal when the player had playback issues

## [7.7.4] - 2022-05-11
### Added:
- Improved support for `duration` and `maxDuration` in the multiple zones format for ad requests.
  Added function `withDuration(duration: Long?)` in class `AdswizzAdRequest`, which sets the maximum total duration of ads to be selected. The value is in milliseconds
  
### Fixed
- Fix for missing a browser app on the device when interactivity triggers a browse action

## [7.7.3] - 2022-04-29
### Added:
- expose `hasCompanion` flag on `AdData` to reflect if the ad has companion banner information attached

## [7.7.2] - 2022-04-14
### Fixed
- improved compatibility when host app compiles with API 31 & 32 (SDK is still compiled using API 30)

## [7.7.1] - 2022-03-31
### Changed
- OMSDK library updates

### Fixed
- Restrict the ProGuard rules only to the AdsWizz packages

## [7.7.0] - 2022-03-11
### Added
- Podcast support
    - New public class **_AdPodcastManager_**
        - New public member `addListener(listener: Listener)` of type **_AdPodcastManager.Listener_**
        - New public member `removeListener(listener: Listener)` of type **_AdPodcastManager.Listener_**
        - New public member `addDownloadListener(listener: AdPodcastManagerDownloadListener)` of type **_AdPodcastManagerDownloadListener_**
        - New public member `removeDownloadListener(listener: AdPodcastManagerDownloadListener)` of type **_AdPodcastManagerDownloadListenerr_**
        - New public member `adPodcastManagerSettings` of type **_AdPodcastManagerSettings_**
        - New public method `play(uri: Uri)`
        - New public method `stop()`
        - New public method `pause()`
        - New public method `resume()`
        - New public method `seekTo(position: Double)`
        - New public method `skipAd()`
    - New public class **_AdPodcastManagerSettings.Builder__**
        - New public member `adPlayerInstance(adsPlayerInstance: AdPlayer)`
        - New public member `withDownloadConditions(conditions: Set<DownloadCondition>)`
    - New public interface **_AdPodcastManager.Listener_**
        - New public method `willStartPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri, adBaseManager: AdBaseManager?)`
        - New public method `didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`
        - New public method `didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`
        - New public method `didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri)`
        - New public method `adBreakStarted(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager)`
        - New public method `adBreakStarted(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager)`
        - New public method `adBreakEnded(adPodcastManager: AdPodcastManager, adBaseManager: AdBaseManager)`
        - New public method `onError(adPodcastManager: AdPodcastManager, error: Error)`
        - New public method `onPlayHeadReport(adPodcastManager: AdPodcastManager, elapsedSec: Double, durationSec: Double)`
    - New public interface **_DownloadableAsset_**
        - New public member `url`
        - New public member `mimeType`
        - New public member `expectedContentLength`
        - New public member `state` of type **_DownloadableAssetState_**
        - New public member `location`
    - New public class **_AdswizzAdPodcastManager_**
        - New public method `startDownloadPodcast(uri: Uri, location: Uri, completion: ((Boolean, Exception?) -> Unit)? = null)`
        - New public method `stopDownloadPodcast(location: Uri)`
        - New public method `removePodcast(location: Uri)`
    - New public interface **_AdPodcastManagerDownloadListener_**
        - New public method `didDownload(adPodcastManager: AdPodcastManager, bytesDownloaded: Long, bytesExpected: Long, asset: DownloadableAsset)`
        - New public method `didFinishDownload(adPodcastManager: AdPodcastManager,  asset: DownloadableAsset)`
        - New public method `didReceive(adPodcastManager: AdPodcastManager,  error:Error, asset: DownloadableAsset)`

## [7.6.3] - 2022-02-25
### Added
- Support for setting the `viewId` of **_AdVideoView_** programmatically after creation
- The SDK now detects the mute/unmute events for the internal player in sync with the system volume
- Exposed the `pricing` node from VAST in the **_AdData_**'s `pricing` member
- Added TCFV2 support via the **_AdswizzAdRequest.Builder_**'s method named `withUserConsentV2(userConsentV2: String?)`
- Added Amazon IFA (`aw_0_1st.ifa` query item) to the ad request decoration for FireOS devices

### Fixed
- Fixed omsdk session still active after playback timeout error
- Resetting the **_AdManager_** instance by calling `adManager?.reset()` will not trigger the VAST skip event

## [7.6.2] - 2022-01-14
### Added
- `play` and `pause` events are now marked in the Analytics API  
- We officially added support for ExoPlayer v2.15 and v2.16

### Fixed
- Fixed an issue with the `ad_type` being null for the Analytics API


## [7.6.1] - 2021-11-18
### Added
- New in **_AdVideoView_**:
  - public field `playState` of type `AdVideoPlayState` that reflects the video play state
  - **_PlayStateListener_** listener interface that exposes changes to the video play state as a callback
  - public method `setPlayStateListener`
- The SDK now properly outputs error logs in case of invalid URL given to the `AdswizzAdStreamManager` _play_ method

### Fixed
- Fixed potential ANR during SDK initialization
- Fixed some AdVideoView issues occurring during multiple video ads playback


## [7.6.0] - 2021-11-04
### Added
- Streaming playback with **DVR** capabilities (timeshift) for a HLS stream provided by the AIS playback server. All additions for **DVR** are listed below:
    - New **_AdStreamManager.Listener_** event added:
        - `onDvrMetadataReceived(adStreamManager: AdStreamManager, dvrBufferInfo: DvrBufferInfo?)` callback that is called when starting streaming of on HLS stream provided by a DVR-enabled AIS server and an `AdManagerStreamingSettings` with `dvrEnabled` set to `true`
    - New public members in **_AdswizzAdStreamManager_**:
        - **_play(url: String, timestamp: Long)_** method related to DVR
    - New public members in **_AdStreamManagerSettings.Builder_**:
        - **_dvrEnabled(dvrEnabled: Boolean)_** adds a capability for the stream manager (e.g `dvr`)
    - New public class **_DvrBufferInfo_** with public members:
        - **_start_** is the start UNIX timestamp for the DVR stream
        - **_latest_** is the latest UNIX timestamp for the DVR stream
        - **_maxBufferSize_** is the length of the DVR buffer
- Generic intents (e.g generic skip action) specified at ZeroConfig level to be attached to Voice Ads interactivity

### Changed
- The Adswizz SDK has been separated into 2 modules: one contains our core functionalities, the other one contains logic related to the internal `AdPlayer`, which relies on **ExoPlayer**. This enables us to be compatible with multiple **ExoPlayer** versions. For more details, consult our [Integration Guide](Android/SDKAndroid/GetStarted).
- We've changed the host package for some classes:
    - `import com.ad.core.adBaseManager.AdPlayer` has changed to `import com.adswizz.common.AdPlayer`
    - `import com.ad.core.utils.phone.SDKError` has changed to `com.adswizz.common.SDKError`

## [7.5.7] - 2021-11-11
### Fixed
- Fixes an issue where some old devices could get an ANR while initializing the SDK

## [7.5.6] - 2021-09-28
### Changed
- Removed the usage of the `getInstalledApplications` (`QUERY_ALL_PACKAGES`) API, in order to preserve the compliance with Google's [Package Visibility policy](https://support.google.com/googleplay/android-developer/answer/10158779)

### Fixed
- Fixed an issue with Analytics Event logging when the custom params data is modified by the host app

## [7.5.5] - 2021-09-10
### Fixed
- Fixed an issue for the server-side insertion scenarios, where, during the ad break, the host app couldn't detect if an ad has a banner attached or not (the SDK informed incorrectly every time that there was no companion banner)

## [7.5.4] - 2021-08-18
### Fixed
- Fixed the error scenarios when an ad fails to play from a pod of multiple ads (some tracking events were fired incorrectly)

## [7.5.3] - 2021-08-09
### Fixed
- `AdCompanionView` with `extraExposureTime` and multiple ads per pod work as intended
- `AdVideoView`’s `SurfaceView` survives when the client app is in the background and then returns to the foreground
- Error events are correctly collected even when multiple errors are triggered from the VAST file

## [7.5.2] - 2021-07-20
### Added
- New **_AdBaseManagerAdapter_** methods to trigger custom URLs when tracking clicks in **_AdCompanionView_** and **_AdVideoView_**
    - `urlsForCompanionClickTracking(adBaseManager: AdBaseManager, adData: AdData): List<String>`
    - `urlsForVideoClickTracking(adBaseManager: AdBaseManager, adData: AdData): List<String>`
- Support for `playerExpand` and `playerCollapse` VAST events for video ads using **_AdVideoView_**

### Fixed
- Find and parse `adwData` value metadata in the entire raw string rather than just at the start


## [7.5.1] - 2021-07-07
### Changed
- Improved Analytics dimensions reported for audio/video ads with companion banners

### Fixed
- Fixed an issue where `clickThrough` functionality for some `HTML`-type banners does not work as expected


## [7.5.0] - 2021-06-28
### Added
- Advanced Analytics System
- New public members in the **_AdswizzSDK_**:
    - `hasAProcessInForeground()` - returns true if one of the processes has a component in the foreground
- New _optional_ (they have a default implementation in the interface for kotlin) events in **_AdVideoView.Listener_** that match the lifecycle of a video:
    - `onVideoStarted` - triggered when the video ad starts playing
    - `onVideoEnded` - triggered when the video ad has finished playing
    - `onVideoBufferingStart` - triggered when buffering for a video starts
    - `onVideoBufferingEnd` - triggered when buffering for a video has ended
- New separate SDK artifact that is compiled against the full protobuf Java library instead of the lite version that the current artifact uses. Choose one that suits your implementation:
    - `implementation "com.adswizz:adswizz-sdk:7.5.0"` for the protobuf Lite version (recommended for mobile applications)
    - `implementation "com.adswizz:adswizz-sdk-protobufJava:7.5.0"` for the protobuf Java artifact

### Fixed
- An issue that may cause `clickThrough` not being fired for the `AdCompanionView` created after the ad playback starts
- An issue that may prevent `AdCompanionView` being rendered in Android emulators.

## [7.4.1] - 2021-11-11
### Fixed
- Fixes an issue where some old devices could get an ANR while initializing the SDK

## [7.4.0] - 2021-04-23

### Added
- New public members in the **_AdswizzSDK_**:
    - `disableDataCollection` boolean to enable/disable data collection at device level
    - `AdswizzSDKConfig` param to `AdswizzSDK.initialize` method to be able to pass installationId and playerId at runtime
    - `xpaid` that contains the AudioGraph ID value corresponding to the active listener
    - `getPermissionStatus` and `setPermissionStatus` to control the permissions that may be used by the Adswizz SDK
- New public members in **_AdBaseManager_**:
    - `ads` that contains the list of ads obtained after fetching
    - `analyticsCustomData` to dynamically set/update Analytics events custom params
- New public members in **_AdData_**:
    - `width` and `height` that contain the width and height from the currently selected VAST media file
    - `extensions` that contains a list of all `<Extension>` tag info from inline and wrappers
    - `adParameters` that contains the `<AdParameters>` tag info, if it exists, from the currently selected creative
    - `creativeExtensions` that contains a list of all `<CreativeExtension>` tag info, from the currently selected creative
- New public members in the **_AssetQuality_** enum:
    - `AUTO` value for the SDK to select the asset quality based on the network connection
- New members in **_AnalyticsEvent_**:
    - `customParams` for plugins to access the custom attributes
- New member in **_AdRequest.Builder_**:
    - `analyticsCustomData` to set Analytics events custom params
- New member in **_AdswizzAdRequest.Builder_**:
    - `analyticsCustomData` to set Analytics events custom params
- Added an `xmlString` member in all VAST parsed data classes. It contains the string that was used for generating the data class during parsing.
- Support for custom params in `AnalyticsEvent` through the `AnalyticsCustomData` data class
- New data class, `AnalyticsCustomData`, that contains analytics event custom params
- For `AnalyticsEvent`, added the following attributes, when available: `appState`, `adCount`, `volume`, `transactionId`, `skipOffset`, `adType`, `adSystem`, `adSequence`, `creativeId`, `width` and `height`.
- More analytics events logged during the ad lifecycle
- Added IPC support for video ad playback
- New service class, `AdVideoService`, that is used for video inter-process communication
- Added functionality to support denying the host app permissions to the Adswizz SDK

### Removed
- Removed the properties on the `AdManager` object: `adCount` and `totalAdsDuration`

### Changed
- All `SDKErrorCode` values defined within the AdswizzSDK are now incremented with 2000, i.e `201` has become `2201`
- The `com.adswizz.core.installationId` and `com.adswizz.core.playerId` attributes are no longer mandatory in the manifest, unless they are set through `AdswizzSDKConfig` when the AdswizzSDK is initialized
- Changed the Android permission model. Now the SDK will ask for the bare minimum set of permissions required in order to work. For most permissions that were removed the functionality will not be present if they are not added by the host app.

### Fixed
- `AdCompanionView` will fire the creativeView event only when it is visible to the user (`View.isShown` is true and `View.alpha` > 0 and `View.width` > 0 and `View.height` > 0 and the activity is in resume state)


## [7.3.6] - 2021-04-01

### Fixed
- An issue related to the OMSDK config file.
- An issue related to the `companionZone` member in the `AfrConfig` class.

## [7.3.5] - 2021-03-15

### Changed
- Server-Side Insertion scenarios update: added a `companionZone` member in the `AfrConfig` class. The zone ID provided via this member will be used in companion banner requests when the streaming server (AIS) does not provide a valid companion zone ID via metadata.  

## [7.3.4] - 2021-03-11

### Changed
- AdCompanionView content will scale down to fit its container and will be centered in the view

### Fixed
- A bug related to OMSDK not working on the server side

## [7.3.3] - 2021-03-04

### Added
- Added concatenated metadata support for server-side insertion

### Changed
- AdCompanionView content will scale up or down to fit its container and will be centered in the view

## [7.3.2] - 2021-02-25

### Changed
- AdCompanionView content alignment changed from top-left to center.

## [7.3.1] - 2021-02-12

### Added
- Added support for the `pause`, `resume`, `mute` and `unmute` VAST events
- Added support for viewability (video) measurement via OMSDK
- Two new properties on the `AdManager` object: `adCount` and `totalAdsDuration`
- Added an `AdEvent.Type.Other.EmptyAd` module event that will be triggered when a missed no-ad opportunity appears
- Added a new `AdBaseManagerAdapter` interface to `AdBaseManager` to fire additional URLs for impressions, tracking events and errors
- Added `preferredResourceType` to `AdCompanionOptions` to select the desired companion resource type
- URL firing retry mechanism extended for timeout and drop connection situations
- Added new flags to the permissions field used to decorate the URLs. These are for: motion activity, foreground service and foreground service of type microphone
- Added `aw_0_req.appState` param during decoration of URLs

### Removed
- For the no-ads VAST response (known as the EmtpyAds scenario) the error returned by `AdRequestConnection.requestAds()` is now null. The `AdResponseCompletionHandler` will return a valid `adManager` instance (without an error object).

### Changed
- The `AdEvent.Type.State.ReadyForPlay` module event is now triggered when the `adManager` instance contains an empty ad and the `prepare()` method has been called
- `resize_mode` has been replaced with `video_resize_mode` in order to remove an ExoPlayer dependency
- When `ExoPlaybackException` is thrown in the `InternalAdPlayer` the error description now contains the stack trace
- Continuous Play measurement has been changed. When pausing and resuming, the SDK will not reset the continuous play duration anymore.
- The location required flag was changed to `false` in order to be able to publish on other app stores (e.g. the Amazon Appstore)

### Fixed
- An issue related to companion click-through on Android 6 (API level 23)
- Improved compatibility for the `HTMLResource` banners from VAST


## [7.3.0] - 2020-12-22

### Added
- Added support for skippable ads, marked in VAST by the `skipoffset` attribute. Ads will be skippable from the start if VAST specifies a skipoffset equal to 0.
- Added `skipOffset` in the `AdData` public interface to signal after how many seconds the ad can be skipped by the user.
- New interactive format for voice with variable listening time.
- Implemented OMSDK capabilities for server side
- Video Ads capabilities added to the SDK
- Added `videoViewId` in `AdManagerSettings` and in the `AdManagerStreamingSettings` public interface to help setup video support.
- Added the `AdVideoView` public class to control rendering video content.
- Added a basic sample of how to set up and run video ads.

## [7.2.2] - 2020-11-16
- Fixed an issue that impacts the decoration when the wearable SDK is also used

## [7.2.1] - 2020-10-26
- Ad Companion behavior change: when multiple **AdCompanionView** objects are used or created for the same ad, then there will be a single **createView** event that will be fired per ad
- Updated the documentation regarding Voice Detectors
- Improved **BasicSample** app

## [7.2.0] - 2020-09-30
### Added
- Enqueue capability for AdPlayer
- Pre-caching functionality for ad media
- SDK analytics
- Multi-process support for AdCompanion
- Tap-tap detector for interactive ads
- Open Measurement plugin
- Context-based targeting
- Added **cacheAssetsHint**, **enqueueEnabledHint**, **seekToTrackEnd**, **enqueue** and **dequeue** to the **AdPlayer** interface. The changes to the **AdPlayer** interface are not backwards compatible.
- Added **onSeekToTrackEnd** and **onTrackChanged** to the **AdPlayer.Listener** interface. The changes to the **AdPlayer.Listener** interface are not backwards compatible.
- Added **clearContent** and **reconnect** to **AdCompanionView**

## [7.1.6] - 2020-09-07
- Fixed an issue with decoration on the server side

## [7.1.5] - 2020-08-05
- Renamed the decoration parameter "aw_0_1st.playerid" to "aw_0_1st.playerId"

## [7.1.4] - 2020-08-05
- Removed Protobuf dependency

## [7.1.3] - 2020-07-24
- Introduced a new obfuscation parameter to avoid "Duplicate class a.b.a.c.a" compilation errors

## [7.1.2] - 2020-07-03
- Fixed a minor issue related to the Multiple Zone Request support

## [7.1.1] - 2020-05-20
- Fixed a possible "ConcurrentModificationException" that may appear when using an external ad player.

## [7.1.0] - 2020-05-08
### Added
- SDK support for **WearOS** capable of voice and shake detection
- Support for **InApp Notification** detector
- Support for **Voice** Detector
- Support for **multiple zone request** of ads from AdsWizz Ad Server.
- Support for multiple VAST Macros: TIMESTAMP, CACHEBUSTING, CONTENTPLAYHEAD, MEDIAPLAYHEAD, BREAKPOSITION, BLOCKEDADCATEGORIES,
 ADCOUNT, TRANSACTIONID, ADTYPE, IFA, IFATYPE, CLIENTUA, DEVICEUA, SERVERSIDE, APPBUNDLE, VASTVERSIONS, PLAYERCAPABILITIES,
 PLAYERSTATE, ADPLAYHEAD, ASSETURI, ADSERVINGID, ERRORCODE, LIMITADTRACKING
- Support for various VAST Events: skip, error, loaded, notUsed
- Support to list all VAST companions in the AdData interface
- Improved integration documentation
- Added changelog
- Added **AdswizzAdZone** class
- Added **PlayerState** class
- Added **PlayerCapabilities** class
- Added **volume**, **name**, **playerState** and **playerCapabilities** to the AdPlayer interface
- Added two new statuses for AdPlayer.Status: **LOADING**, **LOADING_FINISHED**
- ```withZones(zones: Set<AdswizzAdZone>)``` to *AdswizzAdRequest.Builder*
- Added new interface **ModuleLifecycle**
- Added new interface **ModuleLifecycleConfig**

### Removed
- ```withZoneId(zoneId: String)``` from *AdswizzAdRequest.Builder*
- removed ```withDuration(duration: Double?)``` from *AdswizzAdRequest.Builder*

## [7.0.0] - 2020-02-27
### Added
- Brand new AddswizzSDK written in **Kotlin**
- Support for **Java**
- Support for **GDPR** and **CCPA** consent
### Changed
- New implementation with breaking changes. Requires integration from scratch of the new **SDK**.
