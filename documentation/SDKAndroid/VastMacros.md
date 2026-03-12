# Vast Macros

The **_AdswizzSDK_** fully supports various VAST macros categories such as: generic, ad break, client, publisher, capabilities, player, state, error, verification and regulation info.</br>
For more information about the existing macros, please visit the [VAST4.2](https://iabtechlab.com/wp-content/uploads/2019/06/VAST_4.2_final_june26.pdf) specs page.

## How to pass macros to the SDK 

Most of these macros are automatically filled by the SDK, but in some cases the **_AdswizzSDK_** needs additional information in order to compute the macro values.</br>
As of version 8.2.0, the **_AdswizzSDK_** added the support to query the host app information needed to expand new macros through `AdMacroProvider` interface.

### AdMacroProvider definition

```kotlin

/**
 *  The AdMacroProvider interface is used to provide various macros that cannot be automatically computed by the SDK.
 *  The methods can be selectively overridden depending on the integration scenario.
 */
interface AdMacroProvider {

    /**
     * This method is used to provide values for the `CONTENTID` macro.</br>
     * `CONTENTID` is content identifier for the content asset into which the ad is being loaded or inserted.</br>
     * Applicable to ad request & all tracking pixels.</br>
     * default values is `null`
     */
    fun getContentId(): String? = null

    /**
     * This method is used to provide values for the `CONTENTURI` macro.</br>
     * `CONTENTURI` is the URI of the main media content asset into which the ad is being loaded or inserted</br>
     * Applicable to ad request & all tracking pixels.</br>
     * default values is `null`
     */
    fun getContentUri(): String? = null

    /**
     * This method is used to provide values for the `MEDIAPLAYHEAD` macro.</br>
     * `MEDIAPLAYHEAD` is playhead position of the video or audio content (NOT the ad creative).</br>
     * Applicable to ad request & all tracking pixels.</br>
     * default values is `null`
     */
    fun getMediaPlayhead(): Duration? = null

}
```
### AdMacroProvider usage

You can set your implementation of AdMacroProvider interface to AdswizzAdRequest.Builder as follows:
```kotlin
val adMacroProviderImpl = object : AdMacroProvider {
    override fun getContentId(): String? = yourContentIdInternal
    override fun getContentUri(): String? = yourContentUriInternal
    override fun getMediaPlayhead(): Duration? = yourMediaPlayheadInternal
}

val adRequestBuilder =
    AdswizzAdRequest.Builder()
        ... 
        .withAdMacroProvider(adMacroProviderImpl)
        .build()
```

Another way to pass your AdMacroProvider implementation is through AdManager instance obtained after the ad request:
```kotlin

val adRequestConnection = AdRequestConnection(request)

adRequestConnection.requestAds { adManager, error ->
    adManager?.adMacroProvider = adMacroProviderImpl
}

```
> **Note**
> The AdMacroProvider instance set to the AdswizzAdRequest.Builder is also passed automatically to the resulted AdManager instance.  Setting a new instance to the `adManager.adMacroProvider` property overrides the previous one inherited from the ad request.


## Supported macros in the SDK
The following macros are supported:
- TIMESTAMP
- CACHEBUSTING
- CONTENTPLAYHEAD
- MEDIAPLAYHEAD
- BREAKPOSITION
- BLOCKEDADCATEGORIES
- ADCOUNT
- TRANSACTIONID
- ADTYPE
- IFA
- IFATYPE
- CLIENTUA
- DEVICEUA
- SERVERSIDE
- APPBUNDLE
- VASTVERSIONS
- PLAYERCAPABILITIES
- PLAYERSTATE
- ADPLAYHEAD
- ASSETURI
- ADSERVINGID
- LIMITADTRACKING
- CONTENTID
- CONTENTURI
<br>and partially supports:
- ERRORCODE - only some of the error codes are currently covered
