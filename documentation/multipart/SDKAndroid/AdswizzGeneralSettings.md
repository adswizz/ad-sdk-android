# AdswizzSDK general settings

## GDPR consent

AdsWizz services are GDPR compliant. As a result, **_AdswizzSDK_** will decorate urls that connect to AdsWizz services accordingly to reflect the desired GDPR user consent. To modify the GDPR consent you need to configure it like this:

```kotlin
    AdswizzSDK.gdprConsent = GDPRConsent.GRANTED
```

If not configured, the default value for GDPR consent in **_AdswizzSDK_** will be  **_NOT_APPLICABLE_**. This means that the user is not a subject of the GDPR. Other possible values are:
  * **_NOT_ASKED_**: the user was not asked yet about GDPR consent
  * **_GRANTED_**: the user has granted access
  * **_DENIED_**: the user has denied access

## CCPA config

With the introduction of CCPA in US, **_AdswizzSDK_** gives you the possibility to forward the user consent accordingly.

```kotlin
    AdswizzSDK.ccpaConfig = CCPAConfig(CCPAConsent.YES, CCPAConsent.YES, CCPAConsent.YES)
```

The first value represents the explicit notice for collecting consent. The next value is the opt-out of the sale of his personal information. The last one is the limitation of scope to the Limited Service Provider Agreement. For all tree, possible values are: **_NOT_APPLICABLE_**, **_YES_**, **_NO_**. The default value for all of them is **_NOT_APPLICABLE_**.

## Permissions
By default `AdswizzSDK` uses permissions set by host app in AndroidManifest file to operate different modules. 
However it is possible, in a programatic way, to control wich permission the SDK can use

### Get status
To get the current permission status in the SDK, use `AdswizzSDK's` method called `getPermissionStatus(permission: String): Boolean` with permission string described in android.Manifest.permission frameowrk. This method returns true if AdswizzSDK can use the API guarded by the permission, or false otherwise.

### Disable permissions programmatically
You can instruct `AdswizzSDK` whether it can use the data exposed by any permission set in AndroidManifest by calling the `fun setPermissionStatus(permission: String, value: Boolean)` method. Pass true for status parameter if `AdswizzSDK` is allowed to use the permission or false if the AdswizzSDK is denied access.

This is the list of permissions you can programatically set using the methods above. See also [Permissions](#permissions) section in Get started chapter

```kotlin
		permission.INTERNET,
		permission.VIBRATE,
		permission.ACCESS_FINE_LOCATION,
		permission.RECORD_AUDIO,
		permission.WRITE_EXTERNAL_STORAGE,
		permission.CALL_PHONE,
		permission.READ_PHONE_STATE,
		permission.ACCESS_NETWORK_STATE,
		permission.ACCESS_WIFI_STATE,
		permission.ACCESS_COARSE_LOCATION,
		permission.BLUETOOTH,
		permission.ACTIVITY_RECOGNITION,
		permission.WRITE_CONTACTS,
		permission.WRITE_CALENDAR,
		permission.CAMERA
```

For example these two lines will inhibit `VIBRATE`  and `ACCESS_FINE_LOCATION` permission 

```kotlin

	AdswizzSDK.setPermissionStatus("android.permission.VIBRATE", false)
	AdswizzSDK.setPermissionStatus("android.permission.ACCESS_FINE_LOCATION", false)

```
or using Android SDK permission constants

```kotlin
	import android.Manifest
	
	...

	AdswizzSDK.setPermissionStatus(permission.VIBRATE, false)
    AdswizzSDK.setPermissionStatus(permission.ACCESS_FINE_LOCATION, false)
    
```

## Disable data collection

Adswizz SDK supports enabling or disabling anonymous data collection at device level. By default the data is collected. If you want to disable data collection at any time you can call:

```kotlin
    AdswizzSDK.disableDataCollection = true
```

If you want to re-enable it simply add the following line:

```kotlin
    AdswizzSDK.disableDataCollection = false
```

## XPAID header

For each call Adswizz SDK checks the response and updates the xpaid header (aw_0_awz.xpaid) value. The host app can obtain it by calling **_AdswizzSDK.xpaid_**.

## Integrator Context

In order to correctly do macro expansion the integrator should implement the `IntegratorContext` interface and provide it to the SDK.

```kotlin
    AdswizzSDK.integratorContext = IntegratorContextImplementation()
```

The interface definition is provided below:

```kotlin
/**
 * Interface to be implemented by the integrator to provide host app information to the SDK
 */
interface IntegratorContext {

    // the player used to play the content. It may be the same as the ad player or a different one. It may not exist for server-side insertion
    var contentPlayer: AdPlayer?

    // Indicates whether the SDK’s intended use case was video, audio, or hybrid
    var adType: Ad.AdType?

    // Indicates whether the intended use case was pre-roll, mid-roll, post-roll, standalone or other
    var breakPosition: BreakPosition?
}
```

## Extra exposure time for an AdCompanionView

By default, the **_AdCompanionView_** will end displaying the content after the ad finishes playing. If you need to keep the companion on the screen for a longer period of time (or indefinitely), you can configure it like this.

```kotlin
    val adCompanionOptions = AdCompanionOptions(exposureTime)
    AdswizzSDK.setAdCompanionOptions(adCompanionOptions)
```


## Asset Quality

The asset quality can be set in the _AdManagerSettings_. Below is a code snippet:

```kotlin
adManager.adManagerSettings = AdManagerSettings.Builder()
    .assetQuality(AssetQuality.MEDIUM)
    .build()
```
Possible values are:
- .LOW: it will try to select a media file with a bitrate up to 179199 bps
- .MEDIUM: will try to select a media file with a bitrate between 179200 bps and 665599 bps
- .HIGH: will try to select a media file with a bitrate at least 665600 bps
- .AUTO: it will try to select a media file based on the internet connection that the device has: the better the connection the higher the bitrate. In case of video resources first the frame size is taken into account, the better the connection the higher the frame size. Internet connections is categories in the following buckets: 2g, 2.5g, 2.75g, 3g, 3.5g, "3.55g", "3.6g", 3.75g, 4g, 5g and wifi.

Default value for asset quality is .HIGH.

## preferredMaxBitrate

Instead of setting asset quality, the preferredMaxBitrate can be set in the _AdManagerSettings_ which allows
the selection of an asset with quality that matches preferredMaxBitrate or the closest one to it.

```kotlin
adManager.adManagerSettings = AdManagerSettings.Builder()
    .preferredMaxBitrate(32000)
    .build()
```

Setting a non-zero positive value will trigger another selection logic, where asset quality is ignored if was 
previously set. The selection logic is the following: it first try to find an asset with the bitrate specified. 
In case it doesn't find one, it tries to find the closest bitrate available with the bitrate less than the specified one,
and if it doesn't find one it tries with the closest one that has a higher bitrate.


### Enable RAD

You can enable or disable Remote Audio Data measurement for your podcasts based on your user's consent. It can be configured like this:

```kotlin
AdswizzSDK.disableRAD = false //or `true` if user opted out
```

