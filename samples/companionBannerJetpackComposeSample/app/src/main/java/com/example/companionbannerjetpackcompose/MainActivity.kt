package com.example.companionbannerjetpackcompose

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.companion.AdCompanionView
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.sdk.AdswizzSDK
import com.example.companionbannerjetpackcompose.ui.theme.CompanionBannerJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAdswizzSDK(this)
        setContent {
            CompanionBannerJetpackComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    ComposableCompanionView(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Cyan)
                            .padding(10.dp)

                    )
                }
            }
        }
    }
}

private fun setupAdswizzSDK(context: Context){
    AdswizzSDK.initialize(context.applicationContext){
        AdswizzAdRequest.Builder()
            .withZones(setOf(AdswizzAdZone(zoneId = "17012")))
            .withCompanionZones("17013")
            .withServer("demo.deliveryengine.adswizz.com")
            .build {
                AdRequestConnection(it)
                    .requestAds { adManager, _ ->
                        adManager ?: return@requestAds

                        adManager.setListener(object : AdManagerListener {
                            override fun onEventErrorReceived(
                                adManager: AdManager,
                                ad: AdData?,
                                error: Error
                            ) = Unit

                            override fun onEventReceived(adManager: AdManager, event: AdEvent) {
                                println("------- ev ${event.ad?.hasCompanion}")
                            }
                        })

                        adManager.prepare()
                        adManager.play()
                    }

            }
    }
}


@Composable
fun ComposableCompanionView(modifier: Modifier){

    AndroidView(modifier = Modifier.then(modifier), factory = {
        AdCompanionView(context = it)
            .apply {
                setBackgroundColor(it.getColor(R.color.purple_500))
                listener = object : AdCompanionView.Listener{
                    override fun didDisplayAd(adCompanionView: AdCompanionView) {
                        println("------ did display")
                    }

                    override fun didEndDisplay(adCompanionView: AdCompanionView) {
                    }

                    override fun didFailToDisplayAd(
                        adCompanionView: AdCompanionView,
                        error: Error
                    ) {
                    }

                    override fun onRenderProcessGone(
                        adCompanionView: AdCompanionView,
                        didCrash: Boolean
                    ) {
                    }

                    override fun shouldOverrideClickThrough(
                        adCompanionView: AdCompanionView,
                        uri: Uri
                    ): Boolean {
                        return false
                    }

                    override fun willLeaveApplication(adCompanionView: AdCompanionView) {
                    }

                    override fun willLoadAd(adCompanionView: AdCompanionView) {
                    }
                }
            }
    })

}