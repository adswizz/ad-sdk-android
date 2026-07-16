[<img src="../img/adswizz_logo.png" alt="AdsWizz" style="width:30%;">](https://www.adswizz.com)
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [8.3.0] - 2026-07-20
### Changed
- Bumped Kotlin to 2.2.21, preserving backward compatibility with Kotlin 1.8.22 and AGP 8.6.0.
- Increased Android `compileSdk` to API 37 (Android 17); binary compatibility is maintained down to API 35.
- Aligned SDK library versions with the Kotlin upgrade. For more information, see the [Dependencies section](Android/SDKAndroid/GetStarted#dependencies).

## [8.2.6] - 2026-07-10
### Added
- Added support for a custom Macro [LINEAR_MEDIAPLAYHEAD_UTC] which represents the absolute UTC playback position on a linear feed relative to the ad-break. The macro can be provided using `AdMacroProvider.getLinearMediaPlayheadEpochMillis()` new method, for more details please visit the [Vast Macros](Android/SDKAndroid/VastMacros.md) page.


## [8.2.5] - 2026-07-01
### Changed
- Relaxed the `ProxyUrlConfig` behaviour to accept `path` and `baseUrl` parameters with or without trailing or ending slashes. For more details, please visit the updated [Using Proxy Server](Android/SDKAndroid/ProxyServer.md) page.

### Fixed
- Fixed an `IllegalArgumentException` that occurred when `AdswizzSDK.cleanup()` was called exclusively in Android Automotive OS environments.

## [8.2.4] - 2026-06-19
### Changed
- Improved the execution order between `AdEvent.Type.State.AllAdsCompleted` and the `AdPlayer.reset()` to be called one after another. Now, the SDK guarantees the `AdPlayer.reset()` is called after sending all `AdEvent.Type.State` events to the host app.
- Changed `AdManagerListener.onEventReceived` to fire `AdEvent.Type.State.PlaybackError` event in every error scenario, replacing the `AdEvent.Type.Other.Error` used in a few cases.
> **Note**
> Release not available due to `IllegalArgumentException` introduced in 8.2.3 version. Please update to version [8.2.5](Android/CHANGELOG#_825-2026-07-01) or later.

## [8.2.3] - 2026-06-15
### Changed
- Improved the `IN-CAR` detection mechanism for newer devices when connected to Android Auto or running directly in Android Automotive OS.
- Changed the behavior when the all ads have failed in the ad break when using `MediaPlayerState`. Starting with 8.2.3 version, the **_AdswizzSDK_** will automatically advance the ad index and fire `AllAdsCompleted` event when the remaining ads have failed.
- For each failed ad will be fired `AdEvent.Type.State.PlaybackError` in `AdManagerListener.onEventReceived` and  `AdManagerListener.onEventErrorReceived` callbacks.
> **Note**
> Release not available due to `IllegalArgumentException` introduced in this version. Please update to version [8.2.5](Android/CHANGELOG#_825-2026-07-01) or later.

## [8.2.2] - 2026-05-07
### Added
- Added new property `minExtraDuration` in `AdswizzShimZone` data class. This specifies the minimum duration that the ad server is required to fill for the shim zone.
- Added new `DebugProxy` data class that can be set into `AdswizzSDKConfig` at SDK initialization. This class can be used for routing SDK HTTP requests through a local debug proxy (e.g. Charles, mitmproxy) only in debug builds.

### Changed
- Improved interactive ads behavior to present the notifications based on configurable scenarios.
- Removed the `maxDeadAir` property from `AdswizzShimZone`.

### Fixed
- Fixed an issue where OMSDK `volume change` events were duplicated when device volume was changed.
- Fixed the missing events in the background - foreground transition after an interactivity was triggered.

## [8.2.1] - 2026-04-14
### Added
- Added a new method `fun withQueryParameters(parameters: Map<String, String>)` to the AdswizzAdRequest.Builder. This method accepts unencoded key value pairs used to append additional parameters to AdswizzAdRequest, for more details please visit the updated [Client side - Custom request parameters](Android/SDKAndroid/ClientSideInsertion.md) section.

## [8.2.0] - 2026-03-31
### Added
- Added a new mechanism to support VAST macros that cannot be automatically filled by the AdswizzSDK. This can be achieved using the new `AdMacroProvider` interface, for more details please visit the [Vast Macros](Android/SDKAndroid/VastMacros.md) page. 
- Added a new methods in order to pass the `AdMacroProvider` instance:
  - using AdRequest.Builder's `fun withAdMacroProvider(adMacroProvider: AdMacroProvider)` new method
  - using AdswizzAdRequest.Builder's `fun withAdMacroProvider(adMacroProvider: AdMacroProvider)` new method
  - using the `AdManager.adMacroProvider` property
- Added new XML attribute `ad_companion_render_type` for `AdCompanionWebView`. This feature enables to choose between `ImageView` or `WebView` to render companion resources. For more details, please visit the updated [AdCompanionView](Android/SDKAndroid/CompanionBanner.md) page.
- Added a new mechanism to support generic XML objects defined within `CreativeExtension` and `Extension` VAST tags. The parsed XML objects are exposed through new `XmlNode` data class, accessible from `CreativeExtension` and `Extension` classes.
- Added a new parameter `proxyConfig` of type `ProxyConfig` in `AdswizzSDKConfig`. This parameter is used during SDK initialization to route all SDK requests through a specified proxy server. For more details, please visit the updated [Using Proxy Server](Android/SDKAndroid/ProxyServer.md) page.


### Changed
- The legacy `IntegratorContext` interface is now deprecated and replaced by the new added `AdMacroProvider`.

## [8.1.0] - 2026-02-11
### Added
- Added a new event-driven integration method for Server Side Insertion. You can now pass your `AdStreamPlayerAdapter` implementation to the `AdManagerStreamingSettings.Builder` object by calling the `adStreamPlayerAdapterInstance` method. For more details, please visit the [Playing Streams With Your Player using AdStreamPlayerAdapter](Android/SDKAndroid/PlayAdsWithYourPlayer(AdStreamPlayerAdapter).md) page.
- Added a new sample project named **_BasicAdStreamPlayerAdapterSample_** to the [Sample Project section](Android/SDKAndroid/SampleProjects.md) showcasing the **_AdStreamPlayerAdapter_** integration in a Server Side Insertion scenario.
- Added a new extension function `fun Metadata.toAdMetadataList(): List<AdMetadataItem>` used as convenience method for mapping the Exoplayer's Metadata to Adswizz Metadata. This extension function is present in both `com.adswizz.exoplayer2x` and `com.adswizz.exoplayer-media3` artifacts.

### Changed
- Moved the `AdPlayer.MetadataItem` nested data class to new `AdMetadataItem` standalone class, this will introduce a breaking change that can be fixed importing the new `com.adswizz.common.AdMetadataItem`. For more details, please refer to the [How to migrate to AdswizzSDK v8.1.0+](Android/Migrations/v8_1_0.md) page.
- Changed the signature of `AdPlayer.Listener.onMetadata(metadataList: List<MetadataItem>)` to `AdPlayer.Listener.onMetadata(metadataList: List<AdMetadataItem>)` in order to use the new top-level class.
- Updated the existing sample projects to use the new `AdMetadataItem` data class.
- Changed the `AdStreamManager.Listener`'s methods to have a default empty implementation permitting selectively overrides.
- Changed the signature of `AdStreamManager.Listener.onMetadataChanged(adStreamManager: AdStreamManager, metadataItem: MetadataItem)` to `AdStreamManager.Listener.onMetadataChanged(adStreamManager: AdStreamManager, metadataItem: AdMetadataItem)` in order to use the new top-level class.
- Discontinued support for the `com.adswizz:adswizz-sdk-topics-api` artifact following the deprecation of the Android Privacy Sandbox Topics API. For more details about the deprecation and the removal timeline, please refer to the [Privacy Sandbox status](https://privacysandbox.google.com/overview/status) page.

### Fixed
- Fixed an issue with 'Interactive Ads' behavior where `InteractivityListener.shouldOverrideCouponPresenting` was not consistently triggered. The method is now called every time the 'Download Coupon' action is initiated via shake detection.

## [8.0.3] - 2026-01-14
### Added
- Open Measurement SDK:
    - Support for `UniversalAdId` injection from VAST into OMSDK's **_AdSession_**. For more details please visit [UniversalAdId Support in OM SDK](https://iabtechlab.com/universaladid-support-in-om-sdk/)
- VAST tracking URLs:
    - Support for `creativeView` tracking URLs from VAST Inline\Creative\Linear and Wrapper(s)\Creative\Linear to be triggered alongside impressions for each loaded **_AdData_**

### Changed
- Open Measurement SDK:
    - Service release that updates to the latest OM SDK v1.6.2 (certified by IAB). See what's new in Open Measurement SDK [here](https://iabtechlab.com/standards/open-measurement-sdk/)
    - The OM SDK v1.6.2 added a new device attestation mechanism in order to prevent device spoofing. This feature is adding to the **_AdswizzSDK_** a new `privacypass.ATTEST` permission and `androidx.webkit` dependency, for more technical implementation details please visit [Device Attestation](https://iabtechlab.com/om-sdk-device-attestation-implementation-guidance/) page.

## [8.0.2] - 2025-11-06

### Changed
- Optimised the VAST parser memory footprint by reducing object allocations and discarding the temporary xml strings and models faster.

## [8.0.1] - 2025-09-11

### Added
- Added new fields to the `AdData` interface
  - `adTitle` The ad title field from VAST response.
  - `adDescription` The ad description field from VAST response.
- Added Kotlin sources for `adswizz.core.*` public interfaces, data classes & enums into the SDK library release.

## [8.0.0] - 2025-08-29

### Changed
- All API functions with time or period parameters now use Kotlin's [`Duration`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.time/-duration/) instead of the less intuitive `Double` or `Long` types. For more details, please refer to the [How to migrate to AdswizzSDK v8.0.0+](Android/Migrations/v8_0_0) page.
  - `AdPlayer` interface functions changed `Double` return type or parameter to `Duration` type:
      - `abstract fun getCurrentTime(): Duration`
      - `abstract fun getDuration(): Duration?`
      - `abstract fun seekTo(position: Duration)`
  - `AdswizzAdRequest` class:
      - `fun withDuration(duration: Duration?)`
  - `AdManagerSettings` class:
      - `fun timeoutIntervalForResource(timeout: Duration?)`
  - `AdCompanionOptions` class:
      - `var extraExposureTime: Duration`
  - `AdManager` interface
      - `abstract fun getCurrentTime(): Duration`
  - `AdswizzAdPodcastManager` class
      - `open fun seekTo(position: Duration)`
  - `AdData` interface
      - `abstract val duration: Duration?`
      - `abstract val skipOffset: Duration?`
  - `AdswizzAdZone` class
      - `val maxDuration: Duration?`
  - `AdswizzShimZone` class
      - `val maxDeadAir: Duration?`
  - `AdRequestConnection` class
      - `var timeout: Duration?`

- Optimized the **_AdswizzSDK_**'s consumer `proguard-rules.pro` to help reduce the final APK size. For more details, please refer to the [How to migrate to AdswizzSDK v8.0.0+](Android/Migrations/v8_0_0) page.

### Added
- Starting with 8.0.0 version, the `AdCompanionView.Listener` interface includes a new `didReceivedError(adCompanionView: AdCompanionView, error: Error)` method called when some error occurred after companion has loaded. For more details, please refer to the updated [Companion Banner](Android/SDKAndroid/CompanionBanner.md) page.

## [7.12.5] - 2025-08-08

### Changed
- Updated `Streaming Sample (with External Player)` to showcase how to pass to **_AdswizzSDK_** the metadata received in HLS Manifest for AIS enabled streams.
- Improved `AdswizzAdStreamManager` to filter duplicated metadata received during the same adBreak.


## [7.12.4] - 2025-07-28

### Added
- Starting with 7.12.4 version, the `AdData` interface includes a new `selectedCompanion` property. This can be used to resize content based on the companion’s VAST aspect ratio once it has loaded. For more details, please refer to the updated [Companion Banner](Android/SDKAndroid/CompanionBanner.md) page.

### Changed
- Increased the `Complete` quartile tolerance dynamically based on the ad duration.
- Improved seek detection tolerance, which affects quartile tracking accuracy.

### Fixed
- Fixed flickering issues that occurred when multiple companion display ads are shown back-to-back. 

## [7.12.3] - 2025-06-05

### Fixed
- Fixed an event reporting issue when the same `MediaPlayerState` instance is shared across multiple `AdManagers`.


## [7.12.2] - 2025-05-26

### Added
- New public methods in `AdswizzAdStreamManager`:
  - `requestDVRMetadata(completionBlock: (Result<AdStreamDVRMetadata>) -> Unit)` - fetches upcoming DVR frames generated by AIS servers.
  - `cancelDVRMetadataRequest()` - cancels an ongoing DVRMetadata request.

### Changed
- public `DVRMetadata` data class has been renamed to `AdStreamDVRMetadata` as per the **_AdswizzSDK_**'s naming conventions
- updated `DvrStreaming Sample` and `DvrStreaming Sample (with External Player)` to showcase the `requestDVRMetadata` method.

### Fixed
- Fixed an issue in `AdswizzAdPodcastManager` where `AdEvent.Type.State.DidResumePlaying` was incorrectly emitted when an interactive ad was skipped.

## [7.12.1] - 2025-04-17

### Changed
- Removed the `currentDuration` field from the `MediaPlayerState` interface. Starting with 7.12.1 version, the `MediaPlayerState` integrations will rely on the duration provided in the `VAST` response. For more details, please refer to the updated [Playing Ads With Your Player using MediaPlayerState](Android/SDKAndroid/PlayAdsWithYourPlayer(MediaPlayerState).md) page.
- The `com.adswizz:exoplayer-media3` artefact is now compatible up to `androidx.media3:media3-exoplayer:1.6.0`.
- Updated the [Playing Ads With Your Player using MediaPlayerState](Android/SDKAndroid/PlayAdsWithYourPlayer(MediaPlayerState).md) page based on the new features.

### Fixed
- Fixed **_AdswizzSDK_**'s internal `AdPlayer` compatibility when `com.google.android.exoplayer:exoplayer:2.18.2` artefact is used in the dependency tree.

## [7.12.0] - 2025-03-26

### Added
- New public methods in `AdswizzAdRequest.Builder`:
    - `withCompanionZoneIds(companionZones: List<Int>)`
    - `withCompanionZoneAliases(companionZoneAliases: List<String>)`
- Added new artifact `com.adswizz:exoplayer2x` to the SDK. This library provides compatibility with `com.google.android.exoplayer` versions 2.16.0 to 2.19.1. For more information, refer to the updated [Get Started](Android/SDKAndroid/GetStarted) section.
- Added a new [How to migrate to AdswizzSDK v7.12.0+](Android/Migrations/v7_12_0) section to help address breaking changes introduced in v7.12.0.

### Changed
- Changed the default `VastVersion` to VAST 4.1 protocol in the `AdswizzAdRequest`. Starting with 7.12.0 version, the `AdswizzAdRequest.Builder.withVastVersion(vastVersion: VastVersion)` method will only accept `VastVersion.V40` and `VastVersion.V41` values.
- Improved **_AdswizzSDK_** listeners to ensure callbacks are invoked exclusively on the `MainThread`.
- The support for `com.adswizz:exoplayer2-14` and `com.adswizz:exoplayer2-15` artefacts is discontinued. The old versions are still available in `adswizz` github repository up to 7.11.1 versions.
- Change the `AdswizzAdRequest.Builder.withZones(zones: Set<AdswizzAdZone>)` method signature to `AdswizzAdRequest.Builder.withZones(zones: List<AdswizzAdZone>)`. For more details, please refer to the [How to migrate to AdswizzSDK v7.12.0+](Android/Migrations/v7_12_0) page.
- The legacy `AdswizzAdRequest.Builder.withCompanionZones(companionZones: String?)` method is deprecated and replaced with `AdswizzAdRequest.Builder.withCompanionZoneIds()`. For more details, please refer to the [How to migrate to AdswizzSDK v7.12.0+](Android/Migrations/v7_12_0) page.
- The `com.adswizz:exoplayer-media3` artefact is now compatible up to `androidx.media3:media3-exoplayer:1.5.1`. The **_AdswizzSDK_** library will not be bundled anymore with `androidx.media3:media3-exoplayer` as transitive dependency. For more details, please refer to the [How to migrate to AdswizzSDK v7.12.0+](Android/Migrations/v7_12_0) page.
- Updated the [Get Started](Android/SDKAndroid/GetStarted), [Client Side Insertion](Android/SDKAndroid/ClientSideInsertion) and [Companion Banner](Android/SDKAndroid/CompanionBanner) pages based on the new features.

## [7.11.1] - 2025-03-11

### Changed
- Adjusted the VAST parser to be more flexible when handling the `MediaFiles` tag.
- Improved **_AdswizzSDK_**'s internal `AdPlayer` error handling.
- Updated the [Playing ads using your player](Android/SDKAndroid/PlayAdsWithYourPlayer(AdPlayer).md) section based on **_AdswizzSDK_**'s internal AdPlayer changes.

### Fixed
- Fixed URL decoration to improve targeting in scenarios where `android.permission.ACCESS_COARSE_LOCATION` is granted to the **_AdswizzSDK_**, but `android.permission.ACCESS_FINE_LOCATION` is not.
- Fixed `AdswizzAdPodcastManager` behaviour to play the remote podcast url when Client-Side-Reporting flag is enabled.


## [7.11.0] - 2025-02-21
### Added
- Added a new class **_AdswizzShimZone_** that can be used in a `AdswizzAdRequest` in order to receive shim ad(s). For more details, please visit [AdswizzShimZone](Android/SDKAndroid/ClientSideInsertion#your-first-ad-request) section.

### Changed
- The **_AdswizzAdZone_** can be configured using either `zoneAlias` or `zoneId`, but not both, as they are mutually exclusive. These changes remain backward compatible.
- The `AdswizzAdRequest.Builder` now includes a new method, `withShimZone`, allowing you to pass an `AdswizzShimZone` object to request shim ad fillers for the current request.
- Adjusted the VAST parser to be more flexible when handling the `MediaFile` tag.
- Loosened `AdswizzAdPodcastManager` response validation when Client Side Reporting is disabled.
- Improved the Server Side Insertion section in [Integration Guide](Android/SDKAndroid/ServerSideInsertion)

### Fixed
- Fixed some Class Verification warnings that occurred on lower AndroidOS versions.


## [7.10.2] - 2025-01-17
### Fixed
- Fixed the `Complete` quartile issue introduced in version [7.10.1](Android/CHANGELOG#_7101-2024-12-10), which in certain scenarios failed to trigger as expected.

## [7.10.1] - 2024-12-10
> **Note**
> Usage is not recommended due to the `Complete` quartile issue introduced in this version. Please update to version [7.10.2](Android/CHANGELOG#_7102-2025-01-17) or later.

### Changed
- The **_AdswizzSDK_** TopicsAPI module is now an optional dependency in the SDK library release, published as `com.adswizz:adswizz-sdk-topics-api`. For more details, please visit [Topics API](Android/TopicsAPI/AdswizzSDKTopicsAPI.md) section.
- OMSDK library updates. Now, **_AdswizzSDK_** uses 1.5.2 version. See what's new in Open Measurement SDK [here](https://iabtechlab.com/wp-content/uploads/2024/08/OMSDK_What-is-new-in-1.5_master.docx.pdf).

### Fixed
- Fixed some DiskRead warnings when [StrictMode](https://developer.android.com/reference/android/os/StrictMode) is applied. 
- Fixed a proguard repackaging issue when the optional dependency (`com.adswizz:exoplayer2-*` or `com.adswizz:exoplayer-media3`) is added.

## [7.10.0] - 2024-10-18
### Changed
- The **_AdswizzSDK_** player dependency (`com.adswizz:exoplayer2-*` and `com.adswizz:exoplayer-media3`) is now optional for integrations that don't use **_AdswizzSDK_** internal player. Starting with version 7.10.0, if you’re using your own player to handle ad media, you only need to include the `com.adswizz:adswizz-sdk` artifact. For more details, please visit [Play ads with your player](Android/SDKAndroid/PlayAdsWithYourPlayerMain.md) section.
- Updated Android targetSdkVersion to API version 35 (Android 15).
- Updated Kotlin version to 1.8.22
- Updated SDK libraries to match the Kotlin version. For additional information, refer to [Dependencies section](Android/SDKAndroid/GetStarted#dependencies)
- Removed `androidx.appcompat:appcompat` library from the **_AdswizzSDK_** dependency tree.

### Fixed
- Fixed an `NullPointerException` occurred when a Podcast download task is immediately canceled.

## [7.9.3] - 2024-09-17
### Added
- Added a new event-driven integration method for Client Side Insertion. You can now pass your `MediaPlayerState` implementation to the `AdManagerSettings.Builder` object by calling the `mediaPlayerStateInstance` method. For more details, please visit the [Playing Ads With Your Player using MediaPlayerState](Android/SDKAndroid/PlayAdsWithYourPlayer(MediaPlayerState).md) page.
- Added new field to the `AdData` interface named `intanceId: String`. This enables you to avoid collisions and uniquely identify each ad in the AdManager's list.
- Added a new sample project named **_JetpackComposeSample_** to the [Sample Project section](Android/SDKAndroid/SampleProjects) showcasing the interoperability of **_AdCompanionView_** and **_AdVideoView_** with `jetpack compose` UI components.
- Added a new sample project named **_BasicMediaPlayerStateSample_** to the [Sample Project section](Android/SDKAndroid/SampleProjects) showcasing the **_MediaPlayerState_** integration in a ClientSideInsertion scenario.

### Changed
- Removed `org.jetbrains.kotlin:kotlin-reflect`, `org.apache.commons:commons-math3` and `androidx.databinding:viewbinding` libraries from the **_AdswizzSDK_** dependency tree.
- Removed `android.permission.VIBRATE` and `android.permission.ACCESS_NETWORK_STATE` from `com.adswizz:adswizz-sdk` AndroidManifest files. The AdswizzSDK now relies solely on permissions requested by the host app. 

### Fixed
- Fixed an `SecurityException` introduced since 7.8.0 release, which occurred only on devices running Android 9(API 28) or lower when `android.permission.ACCESS_WIFI_STATE` was not present in `AndroidManifest.xml` file.

## [7.9.2] - 2024-07-18
### Added
- Added new public boolean property `AdswizzSDK.isInitialized` that allows to monitor whether **_AdswizzSDK_** is initialised or not.
- Added console warnings when **_AdswizzSDK_** properties/methods are accessed before initialisation block.
- Added a new method `automaticallyManageAudioFocus(automaticallyManageAudioFocus: Boolean)` to the: `AdManagerSettings.Builder`, `AdManagerStreamingSettings.Builder`,`AdPodcastManagerSettings.Builder`. This method is used by internal players in order to handle [Audio Focus](https://developer.android.com/media/optimize/audio-focus), the default value is `false`. If enabled, the management will be automatically handled by internal players.
### Changed
- Removed deprecated library version of `com.google.android.gms:play-services-ads` and replaced with `com.google.android.gms:play-services-ads-identifier:18.0.1` 
- Improved quartile reporting behaviour on certain network conditions.
### Fixed
- Fixed an internal player playback issue when assets failed to download.

## [7.9.1] - 2024-06-06
### Added
- Added support for `[STOREID]` VAST macro. For more details please visit [IAB VAST 4.x Macros page](https://interactiveadvertisingbureau.github.io/vast/vast4macros/vast4-macros-latest.html#macro-spec-storeid)
### Changed
- Improved **_AdswizzSDK_** memory usage on internal allocated objects when GC is applied.
### Fixed
- Fixed the issue where the `AdCompanionView` resource was not displayed when a static decoration was applied in a Server Side Insertion scenario with an external player.
- Fixed the issue where the `AdCompanionView` resource was not displayed if the app lost focus (backgrounded or other) while the companion banner resource is about to be fetched.
- Fixed the `call` parameter value in the URL decoration to make it independent of the `android.permission.CALL_PHONE` permission.


## [7.9.0] - 2024-04-16
### Added
- Added a new method `timeoutIntervalForResources(timeout: Double)` to the `AdManagerSettings.Builder`. This method is utilized by internal players to manage network connection timeouts for media files (ad creatives). If not set, the default ExoPlayer timeout of 8 seconds is used.
- Added a new coroutine-friendly artifact in the SDK library release with the ID `com.adswizz:adswizz-sdk-extensions` as an optional dependency. It needs to be explicitly declared in `build.gradle` files. This includes helpers for the main functionalities in **_AdswizzSDK_**. For more details, visit the [AdswizzSDK-Extensions](Android/AdswizzSDKExtensions/ClientSideInsertion) section.
- Added a new sample project named **_AdswizzSDK Extensions_** to the [Sample Project section](Android/SDKAndroid/SampleProjects) showcasing the new artifact **_com.adswizz.adswizz-sdk-extension_**
### Changed
- Removed `androidx.constraintlayout` and `com.google.android.material` transitive dependencies from **_AdswizzSDK_** artifacts.
- The **_AdswizzSDK_** Profile Collector will be triggered upon each SDK initialization.
- Improved the [Integration Guide](Android)

## [7.8.9] - 2024-04-10
### Added
- New default method `release` in **_AdPlayer_** interface with empty implementation. This method will be called when any instance of: `AdManager`, `AdswizzAdStreamManager`,`AdswizzAdPodcastManager` wants to free up the resources used is their lifecycle. For more details visit [AdPlayer Interface Implementation](Android/SDKAndroid/Playingyourplayer#sample-adplayer-interface-implementation) section.
> This new method won’t affect you if you’re using the SDK’s internal player. Moreover, if you’re using an external player, this new method may have no impact on you, depending on how you’ve implemented the **_AdPlayer_** interface.

### Fixed
- Fixed an internal player memory leak due to Exoplayer's retained instance in DefaultBandwidthMeter singleton.

## [7.8.8] - 2024-03-13
### Added
- Update **_AdswizzSDK_** initialisation process in [GetStarted](Android/SDKAndroid/GetStarted) page with code snippets that covers media service playback.
- Added [Ad events explained](Android/SDKAndroid/ClientSideInsertion#Ad-events-explained) new section, which brings better overview for the `AdEvent`'s received after an ad request was created.
- Added [Enqueue](Android/SDKAndroid/ClientSideInsertion#Enqueue-enabled-false) new section, with additional information and diagrams for both `enqueue=enabled/disabled` use cases.
- Update [CompanionBanner](Android/SDKAndroid/CompanionBanner) page with code snippets that covers following scenarios:
    - adding `AdCompanionView` programmatically
    - adding `AdCompanionView` in XML
    - `AdCompanionView` background color usages
### Fixed
- `Mute`/`Unmute` VAST events will be triggered only for ads that have entered the playing state.
- Fixed an `IndexOutOfBoundsException` that occurred when calling the `AdManager`'s `reset()` method from the `DidStartPlaying` event in the `AdManagerListener` callback.

## [7.8.7] - 2024-02-14
### Added
- Added ExoPlayer Media3 in the SDK library release with artefact id `com.adswizz:exoplayer-media3`. Starting from 7.8.7 **_AdswizzSDK_** will be bundled with `androidx.media3:media3-exoplayer` support. The current media3 version is `1.2.0` including helpers for: `media3-exoplayer-dash`, `media3-exoplayer-hls` and `media3-exoplayer-smoothstreaming`.

### Changed
- The support for `com.adswizz:exoplayer2-10` and `com.adswizz:exoplayer2-12` artefacts is discontinued due to `jCenter` repository deprecation. The old versions are still available in `adswizz` github repository.

### Fixed
- Removed an exoplayer transitive dependency in `com.adswizz:adswizz-sdk` module.
- Fixed a `NullPointerException` due to a race condition when logging `AnalyticsEvent`. 

## [7.8.6] - 2024-02-09
### Fixed
- Fixed an internal `ConcurrentModificationException` occurred when the public fields of **_AdswizzSDK_** were set ahead of initialisation block or without waiting the `completion` callback.

## [7.8.5] - 2024-01-19
> **Note**
> Usage not recommended, due to some issues introduced in `7.8.3`. Update to at least version `7.8.7`.

### Added
- Added `advertiser` field to the `AdData` public interface, enabling access to the advertiser data obtained from the VAST response. The `Advertiser` class contains `id` and `value` members. These members are populated from the following VAST tag: `<Advertiser id="the_advertiser_id">the_advertiser_value</Advertiser>`.

## [7.8.4] - 2023-12-18
> **Note**
> Usage not recommended, due to some issues introduced in `7.8.3`. Update to at least version `7.8.7`.

### Added
 - Added early support for [Topics API](https://developer.android.com/design-for-safety/privacy-sandbox/topics). For more details visit [AdswizzSDK Topics API](Android/TopicsAPI/AdswizzSDKTopicsAPI) page.

### Changed
 - OMSDK library updates. Now, **_AdswizzSDK_** will use 1.4+ version. See what's new in Open Measurement SDK [here](https://iabtechlab.com/wp-content/uploads/2022/08/OMSDK_What-is-new-in-1.4_master.pdf).

### Fixed
 - Fixed Kotlin - Java compatibility issue with Interfaces' default methods implementation.

## [7.8.3] - 2023-11-20
> **Note**
> Usage not recommended, due to some issues introduced in this release. Update to at least version `7.8.7`.

### Added
 - Added [Global Privacy Control](https://privacycg.github.io/gpc-spec/#abstract) functionality. From now on, **_AdswizzSDK_** added support to set `Sec-GPC` header to all AdRequests. It can be enabled/disabled through newly added field like follows: `AdswizzSDK.consent.gpc = true`. If this field is not populated, then the default value is disabled. 

### Changed
 - Due to an increasing number of user consents supported by **_AdswizzSDK_** like: `gdprConsent`, `ccpaConfig`, `gppConsent` and recently added `gpcConsent`, therefore 7.8.3 version of **_AdswizzSDK_** added a new method in order to handle user consents. This is exposed through `consent` member located in **_AdswizzSDK_** object and is used to group all consents (actual and new ones) into a single place. `AdswizzPrivacyConsent` field is backwards compatible with older versions and old fields are now deprecated.
 - The initialize method in **_AdswizzSDK_** supports now a completion handler defined: `fun initialize(context: Context, adswizzSDKConfig: AdswizzSDKConfig? = null, completion: (() -> Unit)? = null)`. Starting from 7.8.3, synchronous startup time will become shorter. Using all **_AdswizzSDK_** functionalities after receiving the completion callback becomes the recommended way now.
 - Improved memory footprint and reduced startup time.

## [7.8.2] - 2023-10-19
### Added
 - Added ExoPlayer 2.19 artifact in the SDK library release.
 - Update `adswizz-wear-sdk` artifact to latest version in the SDK library release.

### Changed
 - Updated Android targetSdkVersion to API version 34 (Android 14).
 - Removed `WeakReference` internal wrapper from public listeners due to some inconsistencies that may occur. **_AdswizzSDK_** keeps now strong references for the given listeners. These changes include the following classes/interfaces: `AdBaseManager`, `AdManager`, `AdStreamManager`, `AdPodcastManager`, `AdVideoView`. Visit [7.8.2 Migration](Android/Migrations/v7_8_2) page for details on how to adapt to these updates.

## [7.8.1] - 2023-09-06
### Changed
 - Renamed the decoration parameter `aw_0_1st.adEvents` to `aw_0_req.adEvents`
### Fixed
 - Resolved decoration parameter `aw_0_1st.version` to work properly when obfuscation is applied.

## [7.8.0] - 2023-08-30
### Changed
 - Updated Kotlin version to 1.7.21 
 - Updated SDK libraries. For additional information, refer to [Library dependencies updates](Android/Migrations/v7_8_0#prerequisites)
 - Updated Android targetSdkVersion to API version 33, see also [7.8.0 Migration](Android/Migrations/v7_8_0)
 - The **_Integration Guide_** has been revised to incorporate new sections dedicated to PinpointAnalytics and Migrations.
 - Breaking changes described in PinpointAnalytics and Migrations sections

### Fixed
 - Notification channel improvements

## [7.7.25] - 2023-07-25
### Fixed
 - Resolved a crash introduced in version `7.7.24` that caused the SDK to crash when receiving events from ExoPlayer.

## [7.7.24] - 2023-07-19

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
