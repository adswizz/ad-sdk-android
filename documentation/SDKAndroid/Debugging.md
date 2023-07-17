# Debugging the AdswizzSDK

The AdswizzSDK uses an internal logger called **_AdLogger_** which routes different debug messages either trough default Log system or trough 
a custom **_Logger__** interface implemented by the AdswizzSDK consumer. 
If AdswizzSDK.logger property is set with a **_Logger__* implementation then the messages are sent back trough `log` callback 


```kotlin
interface Logger {
    fun log(type: LogType, tag: String, message: String)
}

......
object AdswizzSDK {
	...
	var logger: Logger? = null
	...
}

```

