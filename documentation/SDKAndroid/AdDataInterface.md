# AdData interface

This interface exposes to the host app the following information:

```kotlin
interface AdData {

    /**
     * The ad ID from the VAST response
     */
    val id: String?

    /**
     * The duration of the ad taken from the VAST response
     */
    val duration: Double?

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
    val skipOffset: Double?

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
     * The companion selected by the SDK to be displayed
     * based on SDK settings
     */
    val selectedCompanion: CompanionVast?
    
    /**
     * The list of all companions from the VAST response
     */
    fun getAllCompanions(): List<CompanionVast>

    /**
     * Exposes whether the ad has a companion banner information or not.
     */
    var hasCompanion: Boolean

    /**
     * Expose ad type information
     */
    var adType: Ad.AdType
}
```