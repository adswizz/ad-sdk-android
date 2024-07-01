package com.example.companionbannerjetpackcompose

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    AdswizzAdRequest.Builder().withZones(setOf(AdswizzAdZone(zoneId = "17862")))
        .withCompanionZones("17863").withServer("demo.deliveryengine.adswizz.com").build {
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
                    MainScreen(
                        onDetailsClick = { navController.navigate("details") },
                        onAnimationScreen = { navController.navigate("animation_screen") }
                    )
                }

                composable("details") {
                    DetailsScreen()
                }
                composable("animation_screen") {
                    AnimationScreen()
                }
            }


        }

    }

}


@Composable
fun MainScreen(onDetailsClick: () -> Unit, onAnimationScreen: () -> Unit) {

    var showCompanionView by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (showCompanionView) {
            ComposableCompanionView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp), contentAlignment = Alignment.Center
            ) {
                Text(text = "Companion PlaceHolder")
            }
        }
        Button(onClick = {
            if (isSDKInitialised) requestCompanionAd()
        }) {
            Text(text = "Play companion Ad")
        }

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = showCompanionView, onCheckedChange = {
                showCompanionView = it
            })
            Text(text = "Show Companion View")
        }

        Button(onClick = onDetailsClick) {
            Text(text = "Go to details")
        }

        Button(onClick = onAnimationScreen) {
            Text(text = "Go to animation")
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

@Composable
fun AnimationScreen() {

    var width by remember {
        mutableFloatStateOf(1f)
    }

    var height by remember {
        mutableFloatStateOf(1f)
    }
    val modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.5f)
    Column {
        Box(modifier = modifier){
            ComposableCompanionView(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth(width)
                    .fillMaxHeight(height)
            )
        }
        Column(modifier = modifier){

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Change width")
            Slider(value = width, valueRange = 0f..1f, onValueChange = {
                width = it
            })
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Change height")
            Slider(value = height, valueRange = 0f..1f, onValueChange = {
                height = it
            })
        }
    }


}