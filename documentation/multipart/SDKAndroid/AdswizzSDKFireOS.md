# AdswizzSDK for Fire OS
The SDK is compatible with FireOS which is a custom Android OS build. However there are some caveats due to some FireOS specific implementations of Android OS:

**Google Advertising ID** has been replaced by an Amazon Advertising ID thus the url request will be decorated with and IfaType equal to `fireOS` and 
the corresponding advertising id.

**Speech Recogniser** is not available in FireOS and thus the voice ads are not available.

**Wearable connections** are not available in Fire OS so connecting an FireOS device with a wearable device is not possible
and thus the ads that relies on shake me or voice detector features of the wearable device do not work.