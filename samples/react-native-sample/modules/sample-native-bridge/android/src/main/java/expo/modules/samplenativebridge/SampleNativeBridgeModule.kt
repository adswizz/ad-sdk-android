package expo.modules.samplenativebridge

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.functions.Queues
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.adManager.AdManagerSettings
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone

class AdEventRecord(
    @Field val type: String = "",
    @Field val message: String = ""
) : Record {
    fun toEventBody(): Map<String, Any?> = mapOf("type" to type, "message" to message)
}

class SampleNativeBridgeModule : Module() {

    companion object {
        const val SERVER_URL = "demo.deliveryengine.adswizz.com"
        const val ZONE_ID = "17862"
        const val COMPANION_ZONE_ID = 17863

    }

    private var adRequestConnection: AdRequestConnection? = null
    private var adManager: AdManager? = null

    override fun definition() = ModuleDefinition {
        Name("SampleNativeBridge")

        Events("onAdEvent")

        AsyncFunction("requestAds") { promise: expo.modules.kotlin.Promise ->

            // cleanup the previous adManager instance
            this@SampleNativeBridgeModule.adManager?.reset()
            this@SampleNativeBridgeModule.adManager = null

            AdswizzAdRequest.Builder()
                .withServer(SERVER_URL)
                .withHttpProtocol(AdswizzAdRequest.HttpProtocol.HTTP)
                .withZones(listOf(AdswizzAdZone(ZONE_ID)))
                .withCompanionZoneIds(listOf(COMPANION_ZONE_ID))
                .build { adRequest ->
                    // Create the AdRequestConnection using the above adRequest
                    adRequestConnection = AdRequestConnection(adRequest)
                    adRequestConnection?.requestAds { adManager, error ->
                        if (adManager != null && error == null) {
                            this@SampleNativeBridgeModule.adManager = adManager
                            adManager.prepare()
                            promise.resolve(true)
                        } else {
                            promise.resolve(false)
                        }
                    }
                }
        }

        AsyncFunction("play") {
            adManager?.play()
        }.runOnQueue(Queues.MAIN)

        AsyncFunction("pause") {
            adManager?.pause()
        }.runOnQueue(Queues.MAIN)

        AsyncFunction("resume") {
            adManager?.resume()
        }.runOnQueue(Queues.MAIN)

        AsyncFunction("observe") { promise: expo.modules.kotlin.Promise ->
            adManager?.setListener(object : AdManagerListener {
                override fun onEventReceived(adManager: AdManager, event: AdEvent) {
                    sendEvent(
                        "onAdEvent",
                        AdEventRecord(
                            type = event.type?.toString() ?: "UNKNOWN",
                            message = "adId: ${event.ad?.instanceId} hasCompanion: ${event.ad?.hasCompanion}"
                        ).toEventBody()
                    )
                }

                override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
                    sendEvent(
                        "onAdEvent",
                        AdEventRecord(
                            type = "ERROR",
                            message = error.message ?: error.toString()
                        ).toEventBody()
                    )
                }

            })
            promise.resolve(null)
        }

        View(SampleNativeBridgeView::class) {}
    }
}
