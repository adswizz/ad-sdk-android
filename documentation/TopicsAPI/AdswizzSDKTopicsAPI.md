# AdswizzSDK Topics API

This guide is addressed to Android developers that are interested to integrate [Topics API](https://developer.android.com/design-for-safety/privacy-sandbox/topics) in their Android application in order to take full advantage of the out-of-the-box support for this service. For more details you need to contact your Adswizz PIM to enable this feature through the `installationId`.

## Prerequisites
* `installationId` with this feature enabled
* `AndroidManifest.xml` permissions enabled and `property` set
* `ad_services_config.xml` file created in your project

## Android Manifest changes
In order to access this feature **_AdswizzSDK_** will add non dangerous permissions (without a prompt dialog) to your `manifest` file.

```xml

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <!--    permissions added since 7.8.4  -->
    <uses-permission android:name="android.permission.ACCESS_ADSERVICES_TOPICS" />
    <uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
    <uses-permission android:name="android.permission.ACCESS_ADSERVICES_AD_ID" />


```

_NOTE_: If you want to exclude the permissions added, you need to include the `remove` node marker.

```xml

    <uses-permission android:name="android.permission.ACCESS_ADSERVICES_TOPICS"
        tools:node="remove" />

    <uses-permission android:name="com.google.android.gms.permission.AD_ID"
        tools:node="remove" />

    <uses-permission android:name="android.permission.ACCESS_ADSERVICES_AD_ID"
        tools:node="remove" />

```

## Ad Services Config
Through this configuration file the HostApp controls what `AdService` is enabled per each included SDK in their codebase.
First, you need to create an `ad_services_config` resource under the `res` folder in `xml` subdirectory.

```xml

<ad-services-config>

    <!-- Topics API →-->
    <includes-sdk-library name="ENROLLMENT-ID-PROVIDED" />
    <topics allowAdPartnersToAccess="ENROLLMENT-ID-PROVIDED" allowAllToAccess="false" />

</ad-services-config>

```

This can be set as a `property` in the `application` tag as follows:

```xml
 <application
        android:name="YOUR-APP"
        ...
        android:theme="@style/AppTheme">

        <property android:name="android.adservices.AD_SERVICES_CONFIG"
            android:resource="@xml/ad_services_config" />

	...
 
 </application>


```

_NOTE_: [Topics API](https://developer.android.com/design-for-safety/privacy-sandbox/topics) feature is available to a small percentage of Android devices with Privacy Preserving APIs enabled. Therefore, to benefit of all PPAPI's features it will take some time to be fully rolled out to a large percent of users.
