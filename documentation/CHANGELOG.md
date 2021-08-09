[![AdsWizz](img/adswizz_logo.png)](https://www.adswizz.com)
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [7.5.3] - 2021-08-09
### Fixed
- `AdCompanionView` with `extraExposureTime` and multiple ads per pod work as intended
- `AdVideoView`’s `SurfaceView` survives when the client app is in background and then returns on foreground
- The error events are correctly collected even when multiple errors are triggered from the VAST file

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
- Fixed an issue where `clickThrough` functionality for some `HTML` type banner does not work as expected


## [7.5.0] - 2021-06-28
### Added
- Advanced Analytics System
- New public members in **_AdswizzSDK_**:
    - `hasAProcessInForeground()` - returns true if one of the processes has a component in foreground
- New _optional_ (they have a default implementation in the interface for kotlin) events in **_AdVideoView.Listener_** that match the lifecycle of a video:
    - `onVideoStarted` - triggered when the video ad starts playing
    - `onVideoEnded` - triggered when the video ad finished playing
    - `onVideoBufferingStart` - triggered when buffering for a video will start
    - `onVideoBufferingEnd` - triggered when buffering for a video ended
- New separate sdk artifact that is compiled against the protobuf full Java library instead of the lite version the current artifact uses. Choose one that suits your implementation:
    - `implementation "com.adswizz:adswizz-sdk:7.5.0"` for the protobuf Lite version (recommended for mobile applications)
    - `implementation "com.adswizz:adswizz-sdk-protobufJava:7.5.0"` for the protobuf Java artifact

### Fixed
- An issue that may cause `clickThrough` not being fired for the `AdCompanionView` created after the ad playback starts
- An issue that may prevent `AdCompanionView` being rendered in Android Emulators.

## [7.4.0] - 2021-04-23

### Added
- New public members in **_AdswizzSDK_**:
    - `disableDataCollection` boolean to enable/disable data collection at device level
    - `AdswizzSDKConfig` param to `AdswizzSDK.initialize` method to be able to pass installationId and playerId at runtime
    - `xpaid` that contains the AudioGraph ID value corresponding to the active listener
    - `getPermissionStatus` and `setPermissionStatus` to control the permissions that may be used by the Adswizz SDK
- New public members in **_AdBaseManager_**:
    - `ads` that contains the list of ads obtained after fetching
    - `analyticsCustomData` to dynamically set/update Analytics events's custom params
- New public members in **_AdData_**:
    - `width` and `height` that contains the width and height from the currently selected vast media file
    - `extensions` that contains a list with all `<Extension>` tag info from inline and wrappers
    - `adParameters` that contains the `<AdParameters>` tag info, if exists, from the currently selected creative
    - `creativeExtensions` that contains a list with all `<CreativeExtension>` tag info, from the currently selected creative
- New public members in **_AssetQuality_** enum:
    - `AUTO` value for the sdk to select the asset quality based on network connection
- New members in **_AnalyticsEvent_**:
    - `customParams` for plugins to access the custom attributes
- New member in **_AdRequest.Builder_**:
    - `analyticsCustomData` to set Analytics events's custom params
- New member in **_AdswizzAdRequest.Builder_**:
    - `analyticsCustomData` to set Analytics events's custom params
- Added `xmlString` member in all vast parsed data classes. It contains the string that was used for generating the data class during parsing.
- Support for custom params in `AnalyticsEvent` through `AnalyticsCustomData` data class
- New data class `AnalyticsCustomData` that contains the analytics event's custom params
- For `AnalyticsEvent` added the following attributes, when available: `appState`, `adCount`, `volume`, `transactionId`, `skipOffset`, `adType`, `adSystem`, `adSequence`, `creativeId`, `width` and `height`.
- More analytics events logged during the ads lifecyle
- Added IPC support for video ads playback
- New service class `AdVideoService` that is used for video inter process communication
- Added functionality to support denying host app permissions to the Adswizz SDK

### Removed
- Removed the properties on the `AdManager` object: `adCount` and `totalAdsDuration`

### Changed
- All `SDKErrorCode` values defined within the AdswizzSDK are now incremented with 2000 i.e `201` became `2201`
- `com.adswizz.core.installationId` and `com.adswizz.core.playerId` attributes are no longer mandatory in manifest unless they were set through `AdswizzSDKConfig` when AdswizzSDK is initialized
- Changed the android permission model. Now the sdk will ask for the bare minimum amount of permission in order to work. For most permissions that were removed the functionality will not be present if they will not be added by the host app.

### Fixed
- `AdCompanionView` will fire creativeView event only when it is visible to the user (`View.isShown` is true and `View.alpha` > 0 and `View.width` > 0 and `View.height` > 0 and the activity is in resume state)


## [7.3.6] - 2021-04-01

### Fixed
- An issue related to the OMSDK config file.
- An issue related to `companionZone` member in the `AfrConfig` class.

## [7.3.5] - 2021-03-15

### Changed
- Server-Side Insertion scenarios update: added `companionZone` member in the `AfrConfig` class. The zone id provided via this member will be used in companion banner requests when the streaming server (AIS) does not provide a valid companion zone id via metadata.  

## [7.3.4] - 2021-03-11

### Changed
- AdCompanionView content will scale down to fit its container and will be centered in the view

### Fixed
- A bug related to OMSDK not working on server side

## [7.3.3] - 2021-03-04

### Added
- Added concatenated metadata support for server side insertion

### Changed
- AdCompanionView content will scale up or down to fit its container and will be centered in the view

## [7.3.2] - 2021-02-25

### Changed
- AdCompanionView content alignment changed from top-left to center.

## [7.3.1] - 2021-02-12

### Added
- Added support for `pause`, `resume`, `mute` and `unmute` VAST events
- Added support for viewability (video) measurement via OMSDK
- Two new properties on the `AdManager` object: `adCount` and `totalAdsDuration`
- Added `AdEvent.Type.Other.EmptyAd` module event that will be triggered when a missed no-ad opportunity appears
- Added a new `AdBaseManagerAdapter` interface to `AdBaseManager` to be used to fire additional urls for impressions, tracking events and errors
- Added `preferredResourceType` to `AdCompanionOptions` to be used to selected the desired companion resource type
- Url firing retry mechanism extended for timeout and drop connection situations
- Added new flags to the permissions field used to decorate the urls. These are for: motion activity, foreground service and foreground service of type microphone
- Added `aw_0_req.appState` param during decoration of urls

### Removed
- For the no-ads VAST response (known as the EmtpyAds scenario) the error returned by `AdRequestConnection.requestAds()` is now null. The `AdResponseCompletionHandler` will return a valid `adManager` instance (without an error object).

### Changed
- The `AdEvent.Type.State.ReadyForPlay` module event is now triggered when the `adManager` instance contains an empty ad and the `prepare()` method has been called
- `resize_mode` was replaced with `video_resize_mode` in order to remove an ExoPlayer dependency
- When `ExoPlaybackException` is thrown in the `InternalAdPlayer` the error description contains now the stack trace
- Continuous Play measurement has been changed. While pausing and resuming the SDK will not reset the continuous play duration anymore.
- Location required flag was changed to `false` in order to be able to publish on other app stores (e.g. Amazon Appstore)

### Fixed
- An issue related to companion click through on Android 6 (API level 23)
- Improved compatibility for the `HTMLResource` banners from VAST


## [7.3.0] - 2020-12-22

### Added
- Added support for skippable ads, marked in VAST by `skipoffset` attribute. Ads will be skippable from the start if VAST specify a skipoffset equal to 0.
- Added `skipOffset` in `AdData` public interface to signal after how many seconds can the ad be skipped by the user.
- New interactive format for voice with variable listening time.
- Implemented OMSDK capabilities for server side
- Video Ads capabilities added to the SDK
- Added `videoViewId` in `AdManagerSettings` and in `AdManagerStreamingSettings` public interface to help setup video support.
- Added `AdVideoView` public class to control rendering video content.
- Added a basic sample on how to setup and run video ads.

## [7.2.2] - 2020-11-16
- Fixed an issue that impacts the decoration when the wearable sdk is also used

## [7.2.1] - 2020-10-26
- Ad Companion behaviour change: when multiple **AdCompanionView** objects are used or created for the same ad then there will be a single **createView** event that will be fired per ad
- Updated documentation regarding Voice Detectors
- Improved **BasicSample** app

## [7.2.0] - 2020-09-30
### Added
- Enqueue capability for AdPlayer
- Pre-caching functionality for ad media
- Sdk analytics
- Multi-process support for AdCompanion
- Tap-tap detector for interactive ads
- Open measurement plugin
- Context based targeting
- Added **cacheAssetsHint**, **enqueueEnabledHint**, **seekToTrackEnd**, **enqueue** and **dequeue** to **AdPlayer** interface. The changes to **AdPlayer** interface are not backwards compatible.
- Added **onSeekToTrackEnd** and **onTrackChanged** to **AdPlayer.Listener** interface. The changes to **AdPlayer.Listener** interface are not backwards compatible.
- Added **clearContent** and **reconnect** to **AdCompanionView**

## [7.1.6] - 2020-09-07
- Fixed an issue with decoration on the server side

## [7.1.5] - 2020-08-05
- Renamed decoration parameter "aw_0_1st.playerid" -> "aw_0_1st.playerId"

## [7.1.4] - 2020-08-05
- Removed Protobuf dependency

## [7.1.3] - 2020-07-24
- Introduced a new obfuscation parameter to avoid "Duplicate class a.b.a.c.a" compilation errors

## [7.1.2] - 2020-07-03
- Fixed a minor issue related to the 'Multiple Zone Request' support

## [7.1.1] - 2020-05-20
- Fixed a possible "ConcurrentModificationException" that may appear when using the external ad player.

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
- Support to list all VAST companions in AdData interface
- Improved integration documentation
- Added changelog
- Added **AdswizzAdZone** class
- Added **PlayerState** class
- Added **PlayerCapabilities** class
- Added **volume**, **name**, **playerState** and **playerCapabilities** to AdPlayer interface
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
