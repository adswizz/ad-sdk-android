# Integrating AdswizzSDKPinpointAnalytics
## Prerequisites

*   Android Studio ( latest version )
*   Minimum Kotlin version 1.8.22 

## Dependencies
**_AdswizzSDKPinpointAnalytics** (com.adswizz:adswizz-sdk-pinpoint-analytics) depends on the following frameworks:
* com.amazonaws:aws-android-sdk-core

The **_AdswizzSDKPinpointAnalytics_** for Android depends on *Amazon’s AWSCore* as an external library in order to implement the AWS Pinpoint service used by the [AdswizzSDK Analytics](/Android/SDKAndroid/AdswizzAnalytics).<br>

### From Maven 
The recommended version for aws library is 2.72.0 or later

```gradle
dependencies {
    implementation 'com.amazonaws:aws-android-sdk-core:2.72.0'
}
```

**_AdswizzSDKPinpointAnalytics_** resides in the same GitHub repo as **_AdswizzSDK_** so the same repo url can be used to download the artifacts.

```gradle
maven {
    url "https://raw.githubusercontent.com/adswizz/ad-sdk-android/master/releases"
}
```

## AdswizzSDKPinpointAnalytics integration
**_AdswizzSDKPinpointAnalytics_** doesn't need to be initialized. In order to pipe the Analytics events from **_AdswizzSDK_** to the AWS Pinpoint service, your application needs to create an **_AdswizzPinpointConnector_** object and add it to the **_AdswizzSDK_** list of connectors. 
To create an instance of **_AdswizzPinpointConnector_**, first you will create a configuration object.

### AWSPinpointConfig
The **_AdswizzPinpointConfiguration_** is used to instantiate the **_AdswizzPinpointConnector_**. The configuration requires the information listed below.

**Required**:
* region - the `region` to connect to the AWS Pinpoint service account
* projectId - the `projectId`  to connect to the AWS Pinpoint service account
* identityPoolId - the `identityPoolId`  to connect to the AWS Pinpoint service account

**Optional**:
* batchSize - the `batchSize` determines how many Analytics events will be send to the AWS Pinpoint service in one batch. The default value is 50
* minimumSendInterval - the `minimumSendInterval` indicates the minimum time interval between sending event batches to the AWS Pinpoint service. Measured in seconds. The default value is 30 seconds.

### AdswizzPinpointConnector
You are now ready to create the **_AdswizzPinpointConnector_**. Here is how you can do that in your application.

```kotlin
import com.adswizz.analytics.AWSPinpointConfig
import com.adswizz.analytics.AWSPinpointConnector

...

override fun onCreate(savedInstanceState: Bundle?) {

	...
		
		AdswizzSDK.initialize(this.applicationContext)

	...
	
	   AWSPinpointConnector(
			this.applicationContext,
			AWSPinpointConfig(
				awsProjectId,
				identityPoolId,
				awsRegion,
				awsBatchSize,
				awsMinimumSendingInterval
			)
		).also {
			AdswizzSDK.analytics?.add(it)
		}
	
	...
}

```

You can add or remove the connector anytime, not only on initialisation. As soon as the connector is removed the analytics events are not sent to aws storage anymore.
And that's it! You have successfully added the AWS Pinpoint service to your **_AdswizzSDK_** integration in your application. **_AdswizzSDK_** will send the Analytics events it generates to the **_AdswizzPinpointConnector_** object. When it accumulates enough events (matching the `batchSize`), the **_AdswizzPinpointConnector_** will send them to the AWS Pinpoint service.

> **Note**
> By using Charles as proxy, you may notice that the API calls to the AWS Pinpoint service will return HTTP code 202 instead of 200. That is the expected behaviour. HTTP status code 202 means that the requests have been accepted for processing but the processing has not yet been completed (reported by the AWS servers).
