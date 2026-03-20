# Using a Proxy Server to route all SDK network traffic

## Configuring `AdswizzSDK` to route all requests through a proxy endpoint

When enabled by a publisher, all HTTP(S) requests initiated by the SDK are routed to a publisher-provided forward proxy endpoint.
The proxy executes the original request on behalf of the SDK and forwards the response back to the SDK.

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
	 * [ProxyConfig] provided to SDK in order to use a forward proxy pattern.</br>
	 * When provided during SDK initialization, this configuration applies globally to all eligible SDK network requests
	 */
    val proxyConfig: ProxyConfig? = null
)

```

The `ProxyConfig` object defines how the SDK routes network requests through a forward proxy. Depending on the selected strategy, the SDK modify the original request
by adding specific headers or URI parameters to comply with the proxy server's routing strategy.

```kotlin

/**
 * Configuration object that defines how the SDK routes network requests
 * through a forward proxy.
 *
 * When provided during SDK initialization, this configuration applies
 * globally to all eligible SDK network requests
 * (e.g., tracking pixels, media file downloads).
 *
 * The proxy behavior is determined by the selected [ProxyURLConfig.strategy]
 * @param defaultProxyURLConfig - default [ProxyURLConfig] used for all network requests
 * @param proxyURLMappings - a custom mapping that can configure proxy data for a specific domain.
 */

data class ProxyConfig(
	val defaultProxyURLConfig: ProxyURLConfig,
	val proxyURLMappings: List<ProxyURLMapping> = emptyList()
)
```

The `ProxyURLMapping` defines a mapping between an original request domain and a proxy configuration.
- `originalDomainMatch` parameter will be used to select any URL that matches the domain.
- `proxyURLConfig` will be used to load the configuration for any URL that is matching the originalDomainMatch value.

```kotlin
data class ProxyURLMapping(
	val originalDomainMatch: String,
	val proxyURLConfig: ProxyURLConfig
)
```

The `ProxyURLConfig` object defines the specific configuration for mapping network requests to a proxy server. It determines where the requests are sent and how the original destination is communicated to the proxy.
```kotlin
data class ProxyURLConfig(
    /**
     * Base url of the publisher proxy (e.g. https://proxy.com). Must be https in production builds
     * Must be a valid absolute URL. SHOULD use HTTPS in production environments.
     * May include a custom port (e.g., `https://proxy.com:8443`).
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
    val additionalHeaders: Map<String, String> = emptyMap(),

    /** Determines how the original destination URL is embedded into the proxied request.*/
    val strategy: ProxyStrategy = ProxyStrategy.HEADER_ORIGINAL_URL,

    /** The header name used to transmit the original request URL when using [ProxyStrategy.HEADER_ORIGINAL_URL]*/
    val originalUrlHeaderName: String? = "X-Adswizz-Proxy-Original-URL",
    /**
     *  The query parameter name used to embed the original request URL when using
     * [ProxyStrategy.QUERY_PARAM_ORIGINAL_URL].
     */
    val originalUrlQueryParam: String? = "target_uri"
)
```

The `baseUrl` and `path` properties are used across all strategies to define the host and path for proxy requests.
`additionalHeaders` are appended to HTTP headers already set internally by the SDK. Use custom, specific header names to avoid collisions with reserved headers from the original request.


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
E.g.:
If the original URI is:
 `https://demo.deliveryengine.adswizz.com/.....`
	If the path property is `/somepath/{SUBDOMAIN}`, the SDK expands the macro and rewrites the path to `/somepath/demo.deliveryengine`		
	If the path property is `/somepath/{HOSTNAME}`, the SDK expands the macro and rewrites the path to `/somepath/demo.deliveryengine.adswizz.com`
	
We recommend the second approach, as it preserves the full host and subdomain structure and avoids potential issues related to different top-level domains (TLDs).




