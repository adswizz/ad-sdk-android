# AdswizzSDK for Fire OS
The SDK is compatible with FireOS, which is a custom Android OS build. However, there are some caveats, due to some FireOS specific implementations of Android OS:

**Google Advertising ID** has been replaced by an Amazon Advertising ID; thus, the URL request will be decorated with an IfaType equal to `fireOS` and 
the corresponding advertising ID.

**Speech Recognizer** is not available in FireOS; thus Voice Ads are not available.

**Wearable connections** are not available in Fire OS, so connecting an FireOS device with a wearable device is not possible; thus, ads that rely on Shake Me or the voice detector features of the wearable device do not work.