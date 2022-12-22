# Interactive ads

An interactive ad is an ad that has at least one detector and at least one action associated with it.

AdsWizz interactive ads require some permissions on your app.
Required permissions on phones:

```xml
    <!-- used for speech recognition on voice ads-->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <!-- used in interactivity for the download file and download pass file actions -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!-- used in interactivity call action -->
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <!-- used in interactivity call action -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

Required permissions on wearables:

```xml
    <!-- used for speech recognition on voice ads-->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
```

The following package visibility queries are also required:

```xml
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
```

The permissions have to be manually added by the host app. The queries are included in the AdswizzSDK.

There are four types of detectors, as follows: ShakeDetector, VoiceDetector, InAppNotification, TapTapDetector.

## Shake enabled interactive ads
ShakeDetector, as the name suggests, detects the **shake** of the phone or Wear OS smartwatch and executes the selected action.
For this type of detector to work when the app is in the background or the screen is locked, you will need to have a foreground service.

## Voice enabled interactive ads
The speech detector uses the microphone to record the voice of the user and then sends this information to Google to be analyzed and transformed into words. These words are then matched against the list of keywords that need to be detected. If a match happens, then the interactivity action is triggered.
AdsWizz SDK relies on the native Android Speech Recognizer for voice detection. The only permissions needed for this feature to work are ```android.permission.RECORD_AUDIO``` and ```"android.permission.INTERNET"```. Also, for this type of detector to work when the app is in the background or the screen is locked, you will need to have a foreground service, and the ```foregroundServiceType``` param should contain the keyword ```microphone```.

## TapTap enabled interactive ads
TapTapDetector, as the name suggests, detects the **double tap** of the phone or Wear OS smartwatch and executes the selected action.
For this type of detector to work when the app is in the background or the screen is locked you will need to have a foreground service.

## Handling interactive ad events

While the **_AdManger.Listener_** provides a list of **_AdEventType_** covering the life cycle of an ad it does not provide information on an interactive ad.
To get more insight into what is happening while an interactive ad is playing you can set the **_InteractivityListener_**.
To set the listener, add the following line of code:
```kotlin
AdswizzSDK.setInteractivityListener(adManager, interactivityListener)
```

A basic implementation of the **_InteractivityListener_** looks something like this:

```kotlin
class MyInteractivityListener : InteractivityListener {
    override fun onReceiveInteractivityEvent(
        adBaseManager: AdBaseManager,
        adData: AdData,
        event: InteractivityEvent
    ) {
        when(event) {
            InteractivityEvent.AD_WILL_BE_SKIPPED -> {
                // Ad will be skipped as a result of an action during interactive ad
            }
            InteractivityEvent.SKIP_AD ->  {
                // Ad was skipped as a result of an action during interactive ad
            }
            InteractivityEvent.EXTEND_AD -> {
                // Ad was extended with a new media as a result of an action
            }
        }
    }

    /**
     * Here you can provide your own implementation for coupon presenting
     * @return true if you want to use your own implementation
     * @return false otherwise
     */
    override fun shouldOverrideCouponPresenting(
        adBaseManager: AdBaseManager,
        couponUri: Uri
    ): Boolean {
        TODO()
    }
}
```