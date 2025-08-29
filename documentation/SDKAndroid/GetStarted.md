# Get started

The AdswizzSDK facilitates the ad lifecycle management, right from your application.
Sensing listeners environment, it crafts the overall ad experience for superior addressability and augmented interactivity.

Additionally, in a client-side insertion scenario, it retrieves the ad with its associated assets, and handles the reporting operations.<br>
In a server-side insertion scenario, it takes the pressure off your app by making the interactivity and assets easily available.


## Prerequisites

* Android Studio Giraffe | 2022.3.1 (or later)
* (AGP) Gradle Build Tools 8.0.2 (or later)

## Changelog

Please check [here](Android/CHANGELOG.md) the latest AdswizzSDK version and all the new changes & fixes it comes with.

## Dependencies

 A list of external dependencies used in our phone and tablet SDK:

```groovy
kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${kotlin_version}"
coreKtx = "androidx.core:core-ktx:${coreKtxVersion}"
gmsPlayServicesAdsIdentifier = "com.google.android.gms:play-services-ads-identifier:$gmsPlayServicesAdsIdentifierVersion"
processLifecycle = "androidx.lifecycle:lifecycle-process:$lifecycleRuntimeKtxVersion"
kotlinxCoroutines= "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion"
kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutinesVersion"
moshi = "com.squareup.moshi:moshi:$moshiVersion"
moshiAdapters = "com.squareup.moshi:moshi-adapters:$moshiVersion"
moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
protobufJavaLite = "com.google.protobuf:protobuf-javalite:$protobufJavaLiteVersion"
playServicesWearable = "com.google.android.gms:play-services-wearable:$playServicesWearableVersion"
lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleRuntimeKtxVersion"
gmsPlayServicesLocation = "com.google.android.gms:play-services-location:$gmsPlayServicesLocationVersion"
awsAndroidSdkCore = "com.amazonaws:aws-android-sdk-core:$awsAndroidSdkCoreVersion"
workManagerRuntime = "androidx.work:work-runtime-ktx:$workManagerRuntimeVersion"
roomRuntime = "androidx.room:room-runtime:$roomVersion"
roomCompiler = "androidx.room:room-compiler:$roomVersion"
roomKtx = "androidx.room:room-ktx:$roomVersion"
kotlinCoroutinesPlayServices = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$kotlinCoroutinesPlayServicesVersion"
// only needed in media3 flavor
media3Exoplayer = "androidx.media3:media3-exoplayer:$media3Version"
media3ExoplayerDash = "androidx.media3:media3-exoplayer-dash:$media3Version"
media3ExoplayerHLS = "androidx.media3:media3-exoplayer-hls:$media3Version"
media3ExoplayerSS = "androidx.media3:media3-exoplayer-smoothstreaming:$media3Version"
// only needed in exoplayer2x flavor
exoPlayer = "com.google.android.exoplayer:exoplayer:$exoPlayerVerison"

// used only in "com.adswizz:adswizz-sdk-topics-api" artefact
privacysandboxAdServices = "androidx.privacysandbox.ads:ads-adservices:$privacysandboxAdServicesVersion"

```

For our Wear OS SDK we use:

```groovy
kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${kotlin_version}"
androidXWear = "androidx.wear:wear:$androidXWearVersion"
supportWearable = "com.google.android.support:wearable:$wearableSupportVersion"
googleWearable = "com.google.android.wearable:wearable:$googleWearableVersion"
playServicesWearable = "com.google.android.gms:play-services-wearable:$playServicesWearableVersion"
kotlinxCoroutines= "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion"
kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutinesVersion"
lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleRuntimeKtxVersion"
```

We have tested the above dependencies using the following version numbers:<br>

```groovy
//Library
androidMinSdkVersion = 23
androidTargetSdkVersion = 36
androidCompileSdkVersion = 36

kotlin_version = '1.8.22'
coreKtxVersion = '1.12.0'
exoPlayerVerison = '2.16.0' // and others up to 2.19.1
media3Version = '1.2.0' // and others up to 1.6.0
gmsPlayServicesAdsIdentifierVersion = '18.0.1'
moshiVersion = '1.14.0'
lifecycleRuntimeKtxVersion = '2.5.1'
kotlinxCoroutinesVersion = '1.7.3'
workManagerRuntimeVersion = '2.8.1'
protobufJavaLiteVersion = '3.23.3'
gmsPlayServicesLocationVersion = '21.0.1'
roomVersion = '2.6.1'
privacysandboxAdServicesVersion = '1.1.0-beta03'
kotlinCoroutinesPlayServicesVersion = '1.7.3'

//WearOS
wearableSupportVersion = '2.9.0'
playServicesWearableVersion = '18.1.0'
percentLayoutVersion = '1.0.0'
legacySupportVersion = '1.0.0'
recyclerViewVersion = '1.1.0'
lifecycleRuntimeKtxVersion = '2.5.1'
androidXWearVersion = '1.3.0'
googleWearableVersion = '2.9.0'
```

---
**NOTE**

If you are excluding any tested dependency version and using another version (that doesn't ensure backward/forward compatibility with the version we have tested) you may encounter unexpected behavior.<br>
Regarding this matter, please be aware that `com.google.android.gms:play-services-location:21.0.1` is not compatible with versions prior to `20.0.0`. Attempting to use it with such earlier versions may result in a crash and display the following error message: "Found interface com.google.android.gms.location.ActivityRecognitionClient, but class was expected."

---  

## Permissions

With the addition of the SDK to your project, only a couple of permissions will appear in your merged manifest file. These are the bare minimum in order for the SDK to function.

The AdswizzSDK uses the following permissions:

```xml
<manifest>
....
    <uses-permission android:name="android.permission.INTERNET" />

....
</manifest>
```

AdswizzWearSDK uses the following permissions:

```xml
<manifest>
....
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
....
</manifest>
```

To take full advantage of everything that the SDK has to offer, additional permissions will be needed.

The AdswizzSDK additional permissions are:

```xml
<manifest>
....
    <!-- Used for Interactive Ads to send a feedback to user when a interactivity has completed  -->
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- Used for choosing Ad creatives based on network conditions  -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- Used in sonar tracking endpoint for the gps location. Used in analytics when sending the batch to the server -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <!-- Used for polling and profile sonar endpoints to get the microphone status. Used for speech recognition during voice ads -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <!-- Used in interactivity for the download file and download pass file actions -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <!-- used in interactivity call action -->
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <!-- Used during creative selection based on network conditions. Used in interactivity call action. -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <!-- Used during creative selection based on network conditions. -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- Used for sonar profile and polling endpoints when it populates the wifi info -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <!-- Used for decoration. Used in sonar tracking endpoint for the gps location -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!-- we use it in sonar to add the list of installed apps and bundleVersion to the profile endpoint -->

    <!-- Required for 31+. -->
    <uses-permission android:name="com.google.android.gms.permission.AD_ID"/>

    <!-- Required for 33+. -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />


    <!-- Request legacy Bluetooth permissions on older devices. -->
    <uses-permission android:name="android.permission.BLUETOOTH"
        android:maxSdkVersion="30" />

    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />


    <!-- used for sonar polling endpoint where it sends the list of user activities -->
    <!-- Required for 28 and below. -->
    <uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />
    <!-- Required for 29+. -->
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

    <!--    permissions added since 7.8.4 version -->
    <!-- Required for Topics API -->
    <uses-permission android:name="android.permission.ACCESS_ADSERVICES_TOPICS" />


....
</manifest>
```

For AdswizzWearSDK there is only one additional permission:

```xml
<manifest>
....
    <!-- Used for speech recognition during voice ads -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
....
</manifest>
```

These additional permissions will have to be manually added in the host app manifest. Keep in mind that, due to missing permissions, some of the features of our SDK might not work. We handle the case of permissions missing so you don't have to worry about crashes.

If you want to keep some permissions only for the host app to use then the `AdswizzSDK.setPermissionStatus` function will help with denying the unwanted permissions to the Adswizz SDK. By default, the Adswizz SDK will use all the permissions that the host app uses. To find out if the Adswizz SDK is allowed to use a certain permission, call `AdswizzSDK.getPermissionStatus`.

### Disclaimer
- Including AdsWizz SDK in your project may require your application to ask for specific permissions, in order to take full advantage of the special formats and/or enhance targeting capabilities.
- A complete list of what permissions we require and why may be found here: https://support.adswizz.com/hc/en-us/articles/360018903057
- We highly encourage you to loop your product and legal teams in the process of agreeing which features to enable, and finding the best way to reflect this in your application’s Privacy Policy.


## Package visibility

Android 11 changes how apps can query and interact with other apps that the user has installed on a device. Using the <queries> element, apps can define the set of other packages that they can access. This element helps encourage the principle of least privilege by telling the system which other packages to make visible to your app, and it helps app stores like Google Play assess the privacy and security that your app provides for users.

The AdswizzAdSDK uses the following queries setup:

```xml
<manifest>
....
    <queries>
        <intent>
            <action android:name="android.speech.RecognitionService" />
        </intent>
        <intent>
            <action android:name="android.intent.action.VIEW" />
            <data android:mimeType="*/*" />
        </intent>
        <intent>
            <action android:name="android.intent.action.INSERT" />
        </intent>
        <intent>
            <action android:name="android.intent.action.DIAL" />
        </intent>
        <intent>
            <action android:name="android.intent.action.SENDTO" />
        </intent>

        <package android:name="com.google.android.apps.maps" />
        <package android:name="com.here.app.maps" />
        <package android:name="com.waze" />
        <package android:name="net.osmand" />
        <package android:name="com.sygic.aura" />
        <package android:name="com.nng.igo.primong.igoworld" />

        <package android:name="android.settings.APPLICATION_DETAILS_SETTINGS" />

    </queries>
....
</manifest>
```

AdswizzWearSDK uses the following queries setup:

```xml
<manifest>
....
    <queries>
        <intent>
            <action android:name="android.speech.RecognitionService" />
        </intent>
    </queries>
....
</manifest>
```

To change these queries in your app, use the following line of code to remove the default setup. Afterwards, you will have to write your own.

```xml
<queries tools:node="remove" /> 
```

If you remove the queries, some of the features of our SDK might not work. The system manages these automatically, so you don't have to worry about crashes.

## Adding the SDK to your Android Studio Project

You can easily integrate the AdswizzSDK into your project. There are 2 steps to this integration:

1. Inside your project level build.gradle add the following block inside the allprojects->repositories tag:

```groovy
maven {
    url "https://raw.githubusercontent.com/adswizz/ad-sdk-android/master/releases"
        credentials(HttpHeaderCredentials) {
            name = "Authorization"
            value = "Bearer YOUR-AUTH-TOKEN-PROVIDED-BY-ADSWIZZ-PIM"
        }
    authentication {
        header(HttpHeaderAuthentication)
    }
}
```

The final code should look something like this:

```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url "https://raw.githubusercontent.com/adswizz/ad-sdk-android/master/releases"
            credentials(HttpHeaderCredentials) {
                name = "Authorization"
                value = "Bearer YOUR-AUTH-TOKEN-PROVIDED-BY-ADSWIZZ-PIM"
            }
            authentication {
                header(HttpHeaderAuthentication)
            }
        }
    }
}
```

    If you are using a newer version of AndroidStudio project template, your settings.gradle file should look like this:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url "https://raw.githubusercontent.com/adswizz/ad-sdk-android/master/releases"
            credentials(HttpHeaderCredentials) {
                name = "Authorization"
                value = "Bearer YOUR-AUTH-TOKEN-PROVIDED-BY-ADSWIZZ-PIM"
            }
            authentication {
                header(HttpHeaderAuthentication)
            }
        }
    }
}
```


2. Then, inside your module level build.gradle add the following lines inside the dependencies block:

```groovy

// sdk module 
implementation "com.adswizz:adswizz-sdk:$SdkVersion"
        
// player module Media3 
// optional dependency ( needed if you are using Adswizz InternalPlayer in order to play the ads content)
implementation "com.adswizz:exoplayer-media3:$SdkVersion" 
//  add your media3 dependencies
implementation "androidx.media3:media3-exoplayer:$YourMedia3Version"
implementation "androidx.media3:media3-exoplayer-dash:$YourMedia3Version"
implementation "androidx.media3:media3-exoplayer-hls:$YourMedia3Version"
implementation "androidx.media3:media3-exoplayer-smoothstreaming:$YourMedia3Version"
```
> **Note**
> The following dependencies: "com.google.android.exoplayer:exoplayer", "androidx.media3:media3-exoplayer-dash", "androidx.media3:media3-exoplayer-hls", "androidx.media3:media3-exoplayer-smoothstreaming" must be present in your `build.gradle` in order to successfully use the `com.adswizz:exoplayer-media3` wrapper.

In the code snippet above, replace <strong>SdkVersion</strong> with the version of SDK provided by Adswizz (i.e. 7.12.0 - the latest version). Example:

```groovy

implementation 'com.adswizz:adswizz-sdk:7.12.0'
implementation "com.adswizz:exoplayer-media3:7.12.0"

```

<br>Player module changes across versions:
- SDK version prior to 7.6.1 included the exoplayer dependency.
- SDK version between [7.6.1; 7.9.3] the `com.adswizz:exoplayer2-*` or `com.adswizz:exoplayer-media3` dependency is separate and you must include it for the SDK to function correctly. There are 9 exoplayer artifacts available, based on the included exoplayer version (2.10, 2.12, 2.14, 2.15, 2.16, 2.17 and 2.18, 2.19 and media3).
Using different artifact version from exoplayer used in the app may result in crashing the app, thus the app need to include corresponding versions for sdk artifact and exoplayer library (i.e. add `com.adswizz:exoplayer2-18:7.7.21` dependency when using `com.google.android.exoplayer:exoplayer:2.18.6` dependency)
- SDK versions after 7.12.0 will have the <strong>player</strong> dependency as <strong>optional</strong> for integrations that doesn't use **_AdswizzSDK_** internal player. You can include only the `com.adswizz:adswizz-sdk` artefact if your are using your player to play the ad media. For more details, please visit [Play ads with your player](Android/SDKAndroid/PlayAdsWithYourPlayerMain.md) section.

> **Note**
> If you are using Adswizz InternalPlayer in order to play the ads content ( either com.adswizz:exoplayer-media3 or com.adswizz:exoplayer2x ) and  you are targeting `minSdkVersion 23` in your application build.gradle file, you need to add in your `gradle.properties` one of the following:
```groovy
// for AGP versions greater than 8.3
android.useFullClasspathForDexingTransform=true 
// for AGP versions lower than 8.3
android.enableDexingArtifactTransform=false

```
<br>
    
3. The Adswizz Android SDK uses Google Play Services (`com.google.android.gms.ads.identifier.AdvertisingIdClient`) to get the Google Advertising ID (GAD). To do this, your application will need the following permission in your `AndroidManifest.xml` file.
```xml
    <uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
```
> **Note**
> If this permission is explicitly removed, it will lead to limited ad targeting. 

## SDK initialization and cleanup

First, you need to add the installationId, provided by an AdsWizz engineer or PIM, to your manifest. It should look like this:

```xml
<application
    android:name="path.to.myApp.MyApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/AppTheme">
    ....
    <meta-data android:name="com.adswizz.core.installationId" android:value="ADD_YOUR_INSTALLATION_ID_HERE" />
    ....
</application>
```

Second, you need to add the playerId to your manifest. This is also obtained from an AdsWizz engineer or PIM. It should look like this:

```xml
<application
    android:name="path.to.myApp.MyApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/AppTheme">
    ....
    <meta-data android:name="com.adswizz.core.installationId" android:value="ADD_YOUR_INSTALLATION_ID_HERE" />
    <meta-data android:name="com.adswizz.core.playerId" android:value="ADD_YOUR_PLAYER_ID_HERE" />
    ....
</application>
```

If adding the installation id and player id at compile time is not an option, then read [SDK initialization when installationId and playerId are unknown at compile time](#sdk-initialization-when-installationid-and-playerid-are-unknown-at-compile-time).

### SDK initialisation in Android Application Component

You can initialize the AdswizzSDK in the `onCreate` method of your application component. If you've already extended
the application class, just add the following line inside the onCreate method:

```kotlin
AdswizzSDK.initialize(context = this, completion = {
    // AdswizzSDK is completely initialised and you can safely use it's functionalities
})
```

If you haven't already extend the Application class you can do it now. It should look something like this:

```kotlin
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AdswizzSDK.initialize(context = this, completion = {
            // AdswizzSDK is completely initialised and you can safely use it's functionalities
        })
    }
}
```

Don't forget to add this new class in your manifest. It should look like this:

```xml
<application
    android:name="path.to.myApp.MyApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/AppTheme">
    ....
</application>
```

For cleanup, you should use:

```kotlin
AdswizzSDK.cleanup()
```

Call it when you no longer need the SDK.

### SDK initialisation in Android Service Component

If you want to control **_AdswizzSDK_** lifecycle through an Android Service Component (eg. MediaBrowserServiceCompat, MediaSessionService, MediaLibraryService or others), you have to call initialise/cleanup methods exclusively in the Service lifecycle.
> **Note**
> Mixing initialise/cleanup method calls in different Android Components may introduce unexpected behaviour because some component can outlive others.
> An example would be: calling `AdswizzSDK.initialise()` in `Application.onCreate()` and `AdswizzSDK.cleanup()` in Playback service `onTaskRemoved()` or `onDestroy()`.

For initialisation you can use:
```kotlin
class YourMediaBrowserService: MediaBrowserServiceCompat() {
    //...
    override fun onCreate() {
        //...
        AdswizzSDK.initialize(context = this, completion = {
            // AdswizzSDK is completely initialised and you can safely use it's functionalities
        })
    }
}
```
For cleanup, you should use:

```kotlin
override fun onTaskRemoved(rootIntent: Intent?) {
    AdswizzSDK.cleanup()
    super.onTaskRemoved(rootIntent)
}
```
or (depending of your use case)

```kotlin
override fun onDestroy() {
    AdswizzSDK.cleanup()
    super.onDestroy()
}
```

## SDK initialization when installationId and playerId are unknown at compile time

If the installationId or playerId are not known at compile time and you want to set them at runtime you can construct an `AdswizzSDKConfig` data class and pass it to the `AdswizzSDK.initialize` method. See below:

```kotlin
val installationId = ...
val playerId = ...
AdswizzSDK.initialize(context, AdswizzSDKConfig(installationId, playerId))
```
