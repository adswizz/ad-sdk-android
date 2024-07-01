package com.example.companionbannerjetpackcompose

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
        setContent { App() }
    }
}

var isSDKInitialised = false

private fun setupAdswizzSDK(context: Context) {
    AdswizzSDK.initialize(context.applicationContext) {
        isSDKInitialised = true
        Toast.makeText(context, "AdswizzSDK is initialised", Toast.LENGTH_SHORT).show()
    }
}

private fun requestCompanionAd() {
    AdswizzAdRequest.Builder().withZones(setOf(AdswizzAdZone(zoneId = "17012")))
        .withCompanionZones("17013").withServer("demo.deliveryengine.adswizz.com").build {
            AdRequestConnection(it).requestAds { adManager, _ ->
                    adManager ?: return@requestAds

                    adManager.setListener(object : AdManagerListener {
                        override fun onEventErrorReceived(
                            adManager: AdManager, ad: AdData?, error: Error
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

@Composable
fun ComposableCompanionView(modifier: Modifier) {

    AndroidView(modifier = Modifier.then(modifier), factory = {
        AdCompanionView(context = it).apply {
                setBackgroundColor(it.getColor(R.color.purple_500))
                listener = object : AdCompanionView.Listener {
                    override fun didDisplayAd(adCompanionView: AdCompanionView) {
                        println("------ did display")
                    }

                    override fun didEndDisplay(adCompanionView: AdCompanionView) {
                    }

                    override fun didFailToDisplayAd(
                        adCompanionView: AdCompanionView, error: Error
                    ) {
                    }

                    override fun onRenderProcessGone(
                        adCompanionView: AdCompanionView, didCrash: Boolean
                    ) {
                    }

                    override fun shouldOverrideClickThrough(
                        adCompanionView: AdCompanionView, uri: Uri
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    CompanionBannerJetpackComposeTheme {

        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = { Text(text = backStackEntry?.destination?.route.orEmpty()) }
            )
        }) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = "main",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                composable("main") {
                    MainScreen(onDetailsClick = { navController.navigate("details") })
                }

                composable("details") {
                    DetailsScreen()
                }
            }


        }

    }

}


@Composable
fun MainScreen(onDetailsClick: () -> Unit) {

    Column(
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ComposableCompanionView(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )
        Button(onClick = {
            if (isSDKInitialised) requestCompanionAd()
        }) {
            Text(text = "Play companion Ad")
        }

        Button(onClick = onDetailsClick) {
            Text(text = "Go to details")
        }
    }

}


@Composable
fun DetailsScreen() {
    Text(text = "Hello details")
    ComposableCompanionView(
        modifier = Modifier
            .fillMaxSize()
    )
}