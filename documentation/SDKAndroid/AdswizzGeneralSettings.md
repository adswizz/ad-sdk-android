# AdswizzSDK general settings

## GDPR consent

AdsWizz services are GDPR compliant. The **_AdswizzSDK_** will decorate urls that connect to AdsWizz services accordingly, to reflect the desired GDPR user consent. To modify the GDPR consent, you need to configure it like this:

```kotlin
    AdswizzSDK.gdprConsent = GDPRConsent.GRANTED
```

If not configured, the default value for GDPR consent in the **_AdswizzSDK_** is **_NOT_APPLICABLE_**. This means that the user is not subject to the GDPR. Other possible values are:
  * **_NOT_ASKED_**: the user has not been asked yet about GDPR consent
  * **_GRANTED_**: the user has granted access
  * **_DENIED_**: the user has denied access

## CCPA config

With the introduction of CCPA in US, the **_AdswizzSDK_** enables you to forward the user consent accordingly.

```kotlin
    AdswizzSDK.ccpaConfig = CCPAConfig(CCPAConsent.YES, CCPAConsent.YES, CCPAConsent.YES)
```

The first value represents the explicit notice for collecting consent. The next value is the opt-out of the sale of personal information. The last one is the limitation of scope to the Limited Service Provider Agreement. For all tree, possible values are: **_NOT_APPLICABLE_**, **_YES_**, **_NO_**. The default value for all of them is **_NOT_APPLICABLE_**.

### GPP

With the introduction of GPP standard from IAB, **_AdswizzSDK_** gives you the possibility to forward the gpp consent string accordingly.

```kotlin
AdswizzSDK.gppConsent = "My GPP Consent String"

```

The default value for **gppConsent** is nil, which means this parameter won't be used by **_AdswizzSDK_**. The usage of the GPP consent string is similar to the TCF V2 consent string.



## Permissions
By default, the `AdswizzSDK` uses permissions set by host app in the AndroidManifest file to operate different modules. 
However, it is possible, in a programamtic way, to control which permissions the SDK can use.

### Get status
To get the current permission status in the SDK, use the `AdswizzSDK's` `getPermissionStatus(permission: String): Boolean` method with the permission string described in the android.Manifest.permission framework. This method returns true if the AdswizzSDK can use the API guarded by the permission, or false otherwise.

### Disable permissions programmatically
You can instruct the `AdswizzSDK` whether it can use the data exposed by any permission set in AndroidManifest by calling the `fun setPermissionStatus(permission: String, value: Boolean)` method. Pass true for the status parameter if the `AdswizzSDK` is allowed to use the permission or false if the AdswizzSDK is denied access.

This is the list of permissions you can programmatically set using the methods above. See also the [Permissions](Android/SDKAndroid/GetStarted#permissions)) section in the Get started chapter

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

For example, these two lines will inhibit `VIBRATE`  and `ACCESS_FINE_LOCATION` permissions: 

```kotlin

	AdswizzSDK.setPermissionStatus("android.permission.VIBRATE", false)
	AdswizzSDK.setPermissionStatus("android.permission.ACCESS_FINE_LOCATION", false)

```
Or, using Android SDK permission constants

```kotlin
	import android.Manifest
	
	...

	AdswizzSDK.setPermissionStatus(permission.VIBRATE, false)
    AdswizzSDK.setPermissionStatus(permission.ACCESS_FINE_LOCATION, false)
    
```

## Disable data collection

The Adswizz SDK supports enabling or disabling anonymous data collection at device level. By default, the data is collected.To disable data collection at any time, you can call:

```kotlin
    AdswizzSDK.disableDataCollection = true
```

To re-enable data collection, add the following line:

```kotlin
    AdswizzSDK.disableDataCollection = false
```

## XPAID header

For each call, the Adswizz SDK checks the response and updates the xpaid header (aw_0_awz.xpaid) value. The host app can obtain it by calling **_AdswizzSDK.xpaid_**.

## Integrator Context

To perform macro expansion correctly, the integrator should implement the `IntegratorContext` interface and provide it to the SDK.

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
- .LOW: will try to select a media file with a bitrate up to 179199 bps
- .MEDIUM: will try to select a media file with a bitrate between 179200 bps and 665599 bps
- .HIGH: will try to select a media file with a bitrate of at least 665600 bps
- .AUTO: will try to select a media file based on the internet connection that the device has: the better the connection, the higher the bitrate. For video resources, the frame size is taken into account: the better the connection, the higher the frame size. Internet connections are classified as follows: 2g, 2.5g, 2.75g, 3g, 3.5g, "3.55g", "3.6g", 3.75g, 4g, 5g and Wi-Fi.

The default value for asset quality is .HIGH.

## preferredMaxBitrate

Instead of setting asset quality, the preferredMaxBitrate can be set in the _AdManagerSettings_ which enables
the selection of an asset with the same quality as the preferredMaxBitrate or the closest one to it.

```kotlin
adManager.adManagerSettings = AdManagerSettings.Builder()
    .preferredMaxBitrate(32000)
    .build()
```

Setting a non-zero positive value will trigger another selection logic, where asset quality is ignored if was 
previously set. The selection logic is the following: it first tries to find an asset with the bitrate specified. 
If it doesn't find one, it tries to find the closest bitrate available that is lower than the specified one,
and if it doesn't find one it tries with the closest bitrate that is higher than the specified one.


### Enable RAD

You can enable or disable Remote Audio Data measurement for your podcasts based on your user's consent. It can be configured like this:

```kotlin
AdswizzSDK.disableRAD = false //or `true` if user opted out
```

