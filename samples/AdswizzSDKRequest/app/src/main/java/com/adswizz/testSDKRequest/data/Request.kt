package com.adswizz.testSDKRequest.data

import androidx.lifecycle.MutableLiveData
import com.ad.core.AdvertisementSettings
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.streaming.AfrConfig

object Request {
    class LocationConfig(
        val location: String,
        val coordinates: Coordinates
    )

    val locationLive = MutableLiveData<LocationConfig>()

    var requestType: String = ""
    var serverUrl: String = ""
    var location: Coordinates = locationLive.value?.coordinates ?: Coordinates(44.43225, 26.10626)
    var sdkVersion: String = ""
    var playerId: String = ""
    var sessionId: String = ""
    var ifaString: String = ""
    var ifaType: AdvertisementSettings.IfaType = AdvertisementSettings.IfaType.UNKNOWN
    var ifaTypeString: String = ""
    var zoneId: String = ""
    var zoneMaxAds: String = ""
    var zoneMaxDur: String = ""
    var afrServerUrl: String = ""
    var afrProtocol: AdswizzAdRequest.HttpProtocol = AdswizzAdRequest.HttpProtocol.HTTPS
    var afrCompanionZones: String = ""

}