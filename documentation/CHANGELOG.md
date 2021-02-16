# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [7.3.1] - 2021-02-12

### Added
- Added support for pause, resume, mute and unmute vast events
- Added video support in OMSDK plugin
- Added *EmptyAd* module event that will be sent when a missed no-ad opportunity appears
- Added *ReadyForPlay* module event that will be sent when the adManager is empty and *prepare()* is called
- Added *AdBaseManagerAdapter* to *AdBaseManager* to be used to fire additional urls for impressions, tracking events and errors
- Added *preferredResourceType* to *AdCompanionOptions* to be used to selected the desired companion resource type
- Url firing retry mechanism extended for timeout and drop connection situations
- Added new flags to the permissions field used to decorate the urls. These are for: motion activity, foreground service and foreground service of type microphone
- Added *aw_0_req.appState* param during decoration of urls

### Removed
- From *AdRequestConnection.requestAds()* the (adManager, error) response scenario used for no-ads vast response was removed

### Changed
- *resize_mode* was replaced with *video_resize_mode* to remove a dependency on exoPlayer
- When *ExoPlaybackException* is thrown in the *InternalAdPlayer* the error description contains the stack trace
- Countinous Play measurement has changed. Now pausing/resuming the player will not reset the continous play duration.
- Set location required was changed to false to be able to publish on other app stores (e.g. Amazon Appstore)

### Fixed
- An issue related to companion click through on Android 6 (api level 23)
- HTML sizmek banners


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
