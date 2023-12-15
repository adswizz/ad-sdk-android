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
kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"
appcompat = "androidx.appcompat:appcompat:${appcompatVersion}"
coreKtx = "androidx.core:core-ktx:${coreKtxVersion}"
exoPlayer = "com.google.android.exoplayer:exoplayer:$exoPlayerVerison"
gmsPlayServicesAds = "com.google.android.gms:play-services-ads:$gmsPlayServicesAdsVersion"
constraintLayout = "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
processLifecycle = "androidx.lifecycle:lifecycle-process:$lifecycleRuntimeKtxVersion"
kotlinxCoroutines= "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion"
kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutinesVersion"
moshi = "com.squareup.moshi:moshi:$moshiVersion"
moshiAdapters = "com.squareup.moshi:moshi-adapters:$moshiVersion"
moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
protobufJavaLite = "com.google.protobuf:protobuf-javalite:$protobufJavaLiteVersion"
playServicesWearable = "com.google.android.gms:play-services-wearable:$playServicesWearableVersion"
lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleRuntimeKtxVersion"
androidMaterial = "com.google.android.material:material:$androidMaterialVersion"
commonsMath3 = "org.apache.commons:commons-math3:$commonsMath3Version"
gmsPlayServicesLocation = "com.google.android.gms:play-services-location:$gmsPlayServicesLocationVersion"
awsAndroidSdkCore = "com.amazonaws:aws-android-sdk-core:$awsAndroidSdkCoreVersion"
workManagerRuntime = "androidx.work:work-runtime-ktx:$workManagerRuntimeVersion"
roomRuntime = "androidx.room:room-runtime:$roomVersion"
roomCompiler = "androidx.room:room-compiler:$roomVersion"
roomKtx = "androidx.room:room-ktx:$roomVersion"
privacysandboxAdServices = "androidx.privacysandbox.ads:ads-adservices:$privacysandboxAdServicesVersion"
kotlinCoroutinesPlayServices = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$kotlinCoroutinesPlayServicesVersion"

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
commonsMath3 = "org.apache.commons:commons-math3:$commonsMath3Version"
```

We have tested the above dependencies using the following version numbers:<br>

```groovy
//Library
androidMinSdkVersion = 23
androidTargetSdkVersion = 34
androidCompileSdkVersion = 34

kotlin_version = '1.7.21'
appcompatVersion = '1.6.1'
coreKtxVersion = '1.9.0'
exoPlayerVerison = '2.10.0' // and '2.11.3' too
gmsPlayServicesAdsVersion = '20.0.0'
moshiVersion = '1.14.0'
androidMaterialVersion = '1.9.0'
constraintLayoutVersion = '2.1.4'
lifecycleRuntimeKtxVersion = '2.5.1'
kotlinxCoroutinesVersion = '1.6.4'
workManagerRuntimeVersion = '2.8.1'
protobufJavaLiteVersion = '3.23.3'
gmsPlayServicesLocationVersion = '21.0.1'
commonsMath3Version = '3.6.1'
roomVersion = '2.5.2'
privacysandboxAdServicesVersion = '1.1.0-beta03'
kotlinCoroutinesPlayServicesVersion = '1.6.4'

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
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
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
        mavenLocal()
        jcenter() // <-- repository should be added for ExoPlayer versions lower than 2.13.x (inclusive)

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
implementation 'com.adswizz:adswizz-sdk:version'
implementation 'com.adswizz:exoplayer2-10:version'
```

_NOTE_: SDK version prior to before 7.6.1 included the exoplayer dependency. Now this dependency is separate and you must include it for the SDK to function correctly. At this time (SDK 7.7.21), there are 7 exoplayer artifacts available, based on the included exoplayer version (2.10, 2.12, 2.14, 2.15, 2.16, 2.17 and 2.18).
Using different artifact version from exoplayer used in the app may result in crashing the app, thus the app need to include corresponding versions for sdk artifact and exoplayer library (i.e. add com.adswizz:exoplayer2-18:7.7.21 dependecy when using com.google.android.exoplayer:exoplayer:2.18.6 dependency)

In the code snippet above, replace <strong>version</strong> with the version of the phone and tablet SDK provided by AdsWizz (i.e. 7.8.0 - the latest version). Example:

```groovy

implementation 'com.adswizz:adswizz-sdk:7.8.0'
implementation 'com.adswizz:exoplayer2-10:7.8.0'
//or
//implementation 'com.adswizz:exoplayer2-12:7.8.0'
//implementation 'com.adswizz:exoplayer2-14:7.8.0'
//implementation 'com.adswizz:exoplayer2-15:7.8.0'
//implementation 'com.adswizz:exoplayer2-16:7.8.0'
//implementation 'com.adswizz:exoplayer2-17:7.8.0'
//implementation 'com.adswizz:exoplayer2-18:7.8.0'

```

_NOTE_: if you are compiling with an API > 30 (*Android 11*), you must also include a newer version of the Androidx WorkManager runtime, as follows:

```groovy
implementation 'androidx.work:work-runtime-ktx:2.7.1'
implementation 'androidx.work:work-multiprocess:2.7.1'
```
This is necessary since the because WorkManager version included in the SDK doesn't implement the PendingIntent _*FLAG_IMMUTABLE*_ requirements introduced in Android 12.

_NOTE_: If you are using com.adswizz:exoplayer2-16 and com.adswizz:exoplayer2-17 artifact you need to target SDK to minimum API version 31  (*Android 11*). In case com.adswizz:exoplayer2-17 a minimum version of API 33 version should be target.
For com.adswizz:exoplayer2-17 and com.adswizz:exoplayer2-18 the minSdkVersion must be 24 or higher.

    
3. The Adswizz Android SDK uses Google Play Services (`com.google.android.gms.ads.identifier.AdvertisingIdClient`) to get the Google Advertising ID (GAD). To do this, your application may need to have the following meta added in your `AndroidManifest.xml` file.
```xml
<meta-data
    android:name="com.google.android.gms.ads.AD_MANAGER_APP"
    android:value="true" />
```

The above meta should be placed inside the `application` tag, like in the below example:
 ```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    ...
    <meta-data
        android:name="com.google.android.gms.ads.AD_MANAGER_APP"
        android:value="true" />
    ...
</application>
 ```

_NOTE_: If the above `meta` is not added in your `AndroidManifest.xml` file, you will receive an error message in Logcat like this one `"The Google Mobile Ads SDK was initialized incorrectly."` and your application will crash during initialization.


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

Next, you need to initialize the AdswizzSDK. The recommended way to do this is in the onCreate of your application. If you've already extended
the application class, just add the following line inside the onCreate method:

```kotlin
AdswizzSDK.initialize(this)
```

If you haven't already extend the Application class you can do it now. It should look something like this:

```kotlin
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AdswizzSDK.initialize(this)
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

## SDK initialization when installationId and playerId are unknown at compile time

If the installationId or playerId are not known at compile time and you want to set them at runtime you can construct an `AdswizzSDKConfig` data class and pass it to the `AdswizzSDK.initialize` method. See below:

```kotlin
val installationId = ...
val playerId = ...
AdswizzSDK.initialize(context, AdswizzSDKConfig(installationId, playerId))
```
