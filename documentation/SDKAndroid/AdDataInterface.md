# AdData interface

This interface exposes to the host app the following information:

```kotlin
/**
 *  AdData exposes the ad data attributes of the VAST protocol.
 */
interface AdData {
    /**
     * Unique id of the ad instance. While two request on the same zone id can return
     * the same ad with the same id, the instanceId will be different.
     */
    val instanceId: String

    /**
     * The ad ID from the VAST response
     */
    val id: String?

    /**
     * The duration of the ad taken from the VAST response
     */
    val duration: Duration?

    /**
     * The media file that was chosen be used for the ad
     */
    val mediaUrlString: String?

    /**
     * The ad format: normal, extension or silence extension
     */
    val adFormat: AdFormat

    /**
     * The minimum time that has to pass until an ad can be skipped. If null, the ad can be skipped immediately.
     */
    val skipOffset: Duration?

    /**
     * Ad extensions from VAST response
     */
    val extensions: List<VastExtension>

    /**
     * Ad parameters from the VAST response
     */
    val adParameters: AdParameters?

    /**
     * Creative extensions from the VAST response
     */
    val creativeExtensions: List<CreativeExtension>

    /**
     * The media file width field value from the VAST response
     */
    val width: Int?

    /**
     * The media file height field value from the VAST response
     */
    val height: Int?

    /**
     * The pricing field from VAST response
     */
    val pricing: Pricing?

    /**
     * The companion selected by the SDK to be displayed based on [AdCompanionOptions] settings
     */
    val selectedCompanion: CompanionVast?

    /**
     * The list of all companions from the VAST response
     */
    fun getAllCompanions(): List<CompanionVast>

    /**
     * Exposes whether the ad has a companion banner information or not.
     */
    val hasCompanion: Boolean

    /**
     * Expose ad type information
     */
    var adType: Ad.AdType

    /**
     * The advertiser field from VAST response.
     * The [Advertiser.id] is an (optional) identifier for the advertiser, provided by the ad server. Can be used for internal analytics. This is only available in newer VAST versions such as 4.1, 4.2, etc.
     * The [Advertiser.value] is a string that provides the name of the advertiser as defined by the ad serving party. Recommend using the domain of the advertiser.
     *
     * The Advertiser format should be as follows
     * <Advertiser id="the_advertiser_id">YourAdvertiserValue</Advertiser>
     * <Advertiser>YourAdvertiserValueWithoutId</Advertiser>
     * @since 7.8.5
     */
    val advertiser: Advertiser?

    /**
     * The ad title field from VAST response.
     * `return` empty string if missing
     * @since 8.0.1
     */
    val adTitle: String

    /**
     * The ad description field from VAST response.
     * `return` null if missing
     * @since 8.0.1
     */
    val adDescription: String?
}
```