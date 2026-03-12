# Using a proxy server to route all network connections

## Configuring `AdswizzSDK` to route all requests through a proxy endpoint

When enabled by a publisher, all HTTP(S) requests initiated by the SDK are routed to a publisher-provided forward proxy endpoint. 
The proxy then performs the original request on behalf of the SDK and returns the response to the SDK.

This behavior is configured by setting the `proxyConfig` parameter in `AdswizzSDKConfig` during `AdswizzSDK` initialization.


```kotlin
data class AdswizzSDKConfig(
    /**
     * installationId provided by an Adswizz engineer or PIM. This is how it can be set in the manifest:
     * &lt; meta-data android:name="com.adswizz.core.installationId" android:value="ADD_YOUR_INSTALLATION_ID_HERE" /&gt;
     */
    val installationId: String? = null,
    /**
     * playerId provided by an Adswizz engineer or PIM. This is how it can be set in the manifest:
     * &lt; meta-data android:name="com.adswizz.core.playerId" android:value="ADD_YOUR_PLAYER_ID_HERE" /&gt;
     */
    val playerId: String? = null,

    /**
     * proxyConfig used to proxy requests (8.2.+)
     */
    val proxyConfig: ProxyConfig? = null
)

```

The `ProxyConfig` object  defines how the SDK routes network requests through a forward proxy. Depending on sthe selected strategy, the SDK modify the original request
bybadding specific headers or URI parameters to comply with the proxy server's routing strategy.

```kotlin
data class ProxyConfig(
    /**
     * Base url of the publisher proxy (e.g. https://proxy.com). Must be https in production builds
     * Must be a valid absolute URL. SHOULD use HTTPS in production environments.
     * May include a custom port (e.g., `https://proxy.com:8443`).
     *
     */
    val baseURL: String,
    /**
     *   Optional path appended to [baseURL] to form the final proxy endpoint.
    */
    val path: String = "",
    
    /** Optional headers appended to every proxied request.
    * - Typically used for proxy authentication or routing metadata.
    * - Values in this map override existing request headers with the same name.
    */
    val additionalHeaders: Map<String, String>,

    /** Determines how the original destination URL is embedded into the proxied request.*/
    val strategy: ProxyStrategy = ProxyStrategy.HEADER_ORIGINAL_URL,

    /** The header name used to transmit the original request URL when using [ProxyStrategy.HEADER_ORIGINAL_URL]*/
    val originalUrlHeaderName: String? = "X-Adswizz-Proxy-Original-URL",

    /**
     *  The query parameter name used to embed the original request URL when using
     * [ProxyStrategy.QUERY_PARAM_ORIGINAL_URL].
    */
    val originalUrlQueryParam: String? = "target_uri",

    /** The HTTP header name used to attach a unique identifier to each proxied request.
    *
    * - Default: `"X-Adswizz-Proxy-Request-Id"`.
    * - If null or empty, the request-id header is not included
    */
    val proxyRequestIdHeaderName: String? = "X-Adswizz-Proxy-Request-Id"
)
```

The `baseUrl` and `path` properties are used are used across all strategies to define the host and path for proxy requests.
`additionalHeaders` are appended to HTTP headers already set internally by the SDK. Use custom, specific header names to avoid collisions with reserved headers from the original request.
The header specified by `proxyRequestIdHeaderName` is automatically populated with a unique UUID to identify each proxied request
The default value is `X-Adswizz-Proxy-Request-Id`



### `ProxyStrategy.HEADER_ORIGINAL_URL` strategy 
This strategy adds a dedicated header containing the original URL that the proxy server should forward the request to.
This header is defined by the `originalUrlHeaderName` property of the `ProxyConfig` object. By default, it is set to `X-Adswizz-Proxy-Original-URL``.
The `originalUrlQueryParam` property is not used in this strategy

### `ProxyStrategy.QUERY_PARAM_ORIGINAL_URL` strategy 
This strategy appends a query parameter containing the original URL, encoded in a URL-safe format.
The parameter name is defined by the `originalUrlQueryParam` property of the ProxyConfig object. The default value is `target_uri`.


### `ProxyStrategy.REWRITE_DOMAIN_ONLY` strategy
This strategy rewrites part of the `path` property using specific hostname or subdomain values when predefined string markers (macros) are detected in the path. 
Two macros are now available: HOSTNAME and SUBDOMAIN.

```kotlin
enum class ProxyMacros(val value: String) {
    SUBDOMAIN("{SUBDOMAIN}"),
    HOSTNAME("{HOSTNAME}")
}
```
Ex.:
If the original URI is:
 `https://demo.deliveryengine.adswizz.com/.....`
	If the path property is `/somepath/{SUBDOMAIN}`, the SDK expands the macro and rewrites the path to `/somepath/demo.deliveryengine`		
	If the path property is `/somepath/{HOSTNAME}`, the SDK expands the macro and rewrites the path to `/somepath/demo.deliveryengine.adswizz.com`
	
We recommend the second approach, as it preserves the full host and subdomain structure and avoids potential issues related to different top-level domains (TLDs).




