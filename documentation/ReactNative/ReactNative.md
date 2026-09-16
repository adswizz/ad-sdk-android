# Integrating the Adswizz Android SDK into a React Native app

This guide walks through wiring the Adswizz Android SDK into a React Native app built with Expo's workflow. Since the **_AdswizzSDK_** offers only Kotlin/Java support, the integration goes through a native Android bridge — either a custom Expo Module or a legacy native module — where you can embed the ad requests, playback control, ad events, and the `AdCompanionView` to JavaScript.

## Prerequisites

- A React Native Expo project (`npx expo prebuild -p android`, or a project that already has an `android/` directory).
- A custom native module to host the bridge code. E.g: [Expo Module](https://docs.expo.dev/modules/overview/)
- Android Studio Giraffe | 2022.3.1 (or later)
- (AGP) Gradle Build Tools 8.0.2 (or later)


## 1. Adding the SDK to your React Native Project

### Declare the Maven repository at the root

To make the repo visible to every subproject, declare it once in the **root** `android/build.gradle`, inside `allprojects.repositories`:

```groovy
// android/build.gradle
allprojects {
  repositories {
    google()
    mavenCentral()

    maven {
      url = uri("https://raw.githubusercontent.com/adswizz/ad-sdk-android/master/releases")
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
### Add the SDK dependencies

Add the SDK as a dependency **everywhere its classes are referenced directly** — that usually means both your host app module and your bridge module.

```groovy
dependencies {
    // android/app/build.gradle

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
}
```

```groovy
// modules/<your-bridge-module>/android/build.gradle
dependencies {
    implementation "com.adswizz:adswizz-sdk:$SdkVersion"
    implementation "com.adswizz:exoplayer-media3:$SdkVersion"
}
```

For additional version information and available SDK modules, refer to the [Android SDK integration guide](Android/SDKAndroid/GetStarted.md).

## 2. Initialize the SDK in `Application`

You can initialize the AdswizzSDK in the `onCreate` method of your `ReactApplication` component. 
```kotlin
// android/app/src/main/java/.../MainApplication.kt
import com.adswizz.sdk.AdswizzSDK
import com.adswizz.sdk.AdswizzSDKConfig
import com.ad.core.companion.AdCompanionOptions

class MainApplication : Application(), ReactApplication {
  override fun onCreate() {
    super.onCreate()
    // ... existing React Native / Expo bootstrap ...

    AdswizzSDK.initialize(
      applicationContext,
      AdswizzSDKConfig("your-installation-id", "your-player-id")
    ) {
      AdswizzSDK.setAdCompanionOptions(AdCompanionOptions(optimizeCompanionDisplay = false))
    }
  }
}
```
> **Important:** To use `AdCompanionView` in React Native, you must call `AdswizzSDK.setAdCompanionOptions(AdCompanionOptions(optimizeCompanionDisplay = false))`. Disabling `optimizeCompanionDisplay` prevents the SDK from applying some internal optimizations regarding the view hierarchy that are not supported by the React bridge,

## 3. Bridge the native companion view to RN

Companion ads render through a native `AdCompanionView`, which needs to be wrapped in an Expo Module `View` so it can be placed in the React tree like any other component.</br>
For more AdCompanionView information, please visit the [Companion Banner](Android/SDKAndroid/CompanionBanner.md) documentation.

```kotlin
// modules/<your-bridge-module>/android/src/main/java/.../YourBridgeView.kt
package expo.modules.yourbridge

import android.content.Context
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.views.ExpoView
import com.ad.core.companion.AdCompanionView

class YourBridgeView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private val companionView = AdCompanionView(
    context = context,
    renderType = AdCompanionView.RenderType.IMAGE_VIEW
  ).also {
    it.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    addView(it)
  }
}
```
> Prefer `AdCompanionView.RenderType.IMAGE_VIEW` for standard banner companions. It renders the creative into an `ImageView`, which integrates cleanly with React Native's layout system and avoids the overhead of a `WebView` based render type.

Register it in the module's `ModuleDefinition`:

```kotlin
override fun definition() = ModuleDefinition {
  Name("YourBridge")
  View(YourBridgeView::class) {}
}
```

Expo's codegen exposes this as a React component from your module's generated `.tsx` wrapper. Render it wherever the companion ad should appear:

```tsx
import { YourBridgeView } from './modules/your-bridge/src';

<YourBridgeView style={{ flex: 1, width: '100%' }} />
```

## 4. Bridge the public API (ad requests / AdManager)

The rest of the SDK's public API — requesting ads and controlling playback — is exposed as native functions on the module.

### Requesting ads

```kotlin
private var adRequestConnection: AdRequestConnection? = null
private var adManager: AdManager? = null

AsyncFunction("requestAds") { promise: Promise ->

  AdswizzAdRequest.Builder()
    .withServer("your.server.adswizz.com")
    .withHttpProtocol(AdswizzAdRequest.HttpProtocol.HTTP)
    .withZones(listOf(AdswizzAdZone("your-zone-id")))
    .withCompanionZoneIds(listOf(yourCompanionZoneId))
    .build { adRequest ->
      adRequestConnection = AdRequestConnection(adRequest)
      adRequestConnection?.requestAds { manager, error ->
        if (manager != null && error == null) {
          adManager = manager
          adManager?.prepare()
          promise.resolve(true)
        } else {
          promise.resolve(false)
        }
      }
    }
}
```

### Playback control

Player calls should run on the main thread using the `runOnQueue(Queues.MAIN)` method in order to dispatch the calls on the main (UI) thread.

```kotlin
AsyncFunction("play") { adManager?.play() }.runOnQueue(Queues.MAIN)
AsyncFunction("pause") { adManager?.pause() }.runOnQueue(Queues.MAIN)
AsyncFunction("resume") { adManager?.resume() }.runOnQueue(Queues.MAIN)
```

### Observing ad events

You can wrap the `AdManagerListener` callbacks fired on the native side and forward them to the JS layer as follows:

```kotlin
class AdEventRecord(
  @Field val type: String = "",
  @Field val message: String = ""
) : Record {
  fun toEventBody(): Map<String, Any?> = mapOf("type" to type, "message" to message)
}

Events("onAdEvent")

AsyncFunction("observe") { promise: Promise ->
  adManager?.setListener(object : AdManagerListener {
    override fun onEventReceived(adManager: AdManager, event: AdEvent) {
      sendEvent(
        "onAdEvent",
        AdEventRecord(type = event.type?.toString() ?: "UNKNOWN", message = event.toString()).toEventBody()
      )
    }

    override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
      sendEvent(
        "onAdEvent",
        AdEventRecord(type = "ERROR", message = error.message ?: error.toString()).toEventBody()
      )
    }
  })
  promise.resolve(null)
}
```

## 5. Consume from TypeScript

Expose a typed API from your module with three files:

```ts
// modules/your-bridge/src/YourBridge.types.ts
export type AdEventPayload = { type: string; message: string };
export type YourBridgeModuleEvents = { onAdEvent: (event: AdEventPayload) => void };
```

```ts
// modules/your-bridge/src/YourBridgeModule.ts
import { NativeModule, requireNativeModule } from 'expo';
import { YourBridgeModuleEvents } from './YourBridge.types';

declare class YourBridgeModule extends NativeModule<YourBridgeModuleEvents> {
  play(): Promise<void>;
  pause(): Promise<void>;
  resume(): Promise<void>;
  requestAds(): Promise<boolean>;
  observe(): Promise<void>;
}

export default requireNativeModule<YourBridgeModule>('YourBridge');
```

```ts
// modules/your-bridge/src/YourBridgeModule.web.ts
import { registerWebModule, NativeModule } from 'expo';
import { YourBridgeModuleEvents } from './YourBridge.types';

class YourBridgeModule extends NativeModule<YourBridgeModuleEvents> {
  async play(): Promise<void> {}
  async pause(): Promise<void> {}
  async resume(): Promise<void> {}
  async requestAds(): Promise<boolean> { return false; }
  async observe(): Promise<void> {}
}

export default registerWebModule(YourBridgeModule, 'YourBridgeModule');
```

Then drive it from a component:

```tsx
import { useEffect } from 'react';
import { YourBridgeModule, YourBridgeView } from './modules/your-bridge/src';

function AdScreen() {
  useEffect(() => {
    const subscription = YourBridgeModule.addListener('onAdEvent', (event) => {
      console.log(event.type, event.message);
    });
    return () => subscription.remove();
  }, []);

  const handleRequestAds = async () => {
    const success = await YourBridgeModule.requestAds();
    if (success) await YourBridgeModule.observe();
  };

  return (
    <>
      <Button title="Request Ads" onPress={handleRequestAds} />
      <Button title="Play" onPress={() => YourBridgeModule.play()} />
      <Button title="Pause" onPress={() => YourBridgeModule.pause()} />
      <Button title="Resume" onPress={() => YourBridgeModule.resume()} />
      <YourBridgeView style={{ flex: 1, width: '100%' }} />
    </>
  );
}
```

### Sample project

A fully working example that wires together all the steps above is available in the samples folder: [ReactNativeSample](Android/SDKAndroid/SampleProjects).


