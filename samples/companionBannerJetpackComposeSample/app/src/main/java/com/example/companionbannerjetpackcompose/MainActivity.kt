package com.example.companionbannerjetpackcompose

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ad.core.companion.AdCompanionView
import com.adswizz.sdk.AdswizzSDK
import com.example.companionbannerjetpackcompose.ui.theme.CompanionBannerJetpackComposeTheme
import com.example.companionbannerjetpackcompose.ui.theme.Purple40

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

@Composable
fun ComposableCompanionView(
    modifier: Modifier,
    willLoadAd: (String) -> Unit = {},
    didDisplayAd: (String) -> Unit = {},
    didEndDisplay: (String) -> Unit = {},
    willLeaveApplication: (String) -> Unit = {},
    didFailToDisplayAd: (String, Error) -> Unit = { _, _ -> Unit },
    onRenderProcessGone: (String, Boolean) -> Unit = { _, _ -> Unit },
    shouldOverrideClickThrough: (String, Uri) -> Boolean = { _, _ -> false },
) {

    AndroidView(modifier = Modifier.then(modifier), factory = {
        AdCompanionView(context = it).apply {
            setBackgroundColor(it.getColor(R.color.purple_500))
            this.listener = object : AdCompanionView.Listener {
                override fun didDisplayAd(adCompanionView: AdCompanionView) =
                    didDisplayAd(this@apply.toString())

                override fun didEndDisplay(adCompanionView: AdCompanionView) =
                    didEndDisplay(this@apply.toString())

                override fun didFailToDisplayAd(adCompanionView: AdCompanionView, error: Error) =
                    didFailToDisplayAd(this@apply.toString(), error)

                override fun onRenderProcessGone(
                    adCompanionView: AdCompanionView,
                    didCrash: Boolean
                ) = onRenderProcessGone(this@apply.toString(), didCrash)

                override fun shouldOverrideClickThrough(
                    adCompanionView: AdCompanionView,
                    uri: Uri
                ): Boolean = shouldOverrideClickThrough(this@apply.toString(), uri)

                override fun willLeaveApplication(adCompanionView: AdCompanionView) =
                    willLeaveApplication(this@apply.toString())

                override fun willLoadAd(adCompanionView: AdCompanionView) =
                    willLoadAd(this@apply.toString())

            }
        }
    })

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    var canNavigateBack by remember { mutableStateOf(false) }
    var showNavigation by remember { mutableStateOf(true) }
    navController.addOnDestinationChangedListener { controller, _, _ ->
        canNavigateBack = controller.previousBackStackEntry != null && controller.currentBackStackEntry?.destination?.route != "AdVideoViewScreen"
        showNavigation = controller.currentBackStackEntry?.destination?.route == "CompanionMainScreen"
                || controller.currentBackStackEntry?.destination?.route == "AdVideoViewScreen"
    }

    val state by viewModel.playingState.collectAsState()

    val logs by viewModel.logs.collectAsState()

    val selectedResourceText by viewModel.selectedResourceText.collectAsState()

    CompanionBannerJetpackComposeTheme {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple40),
                    navigationIcon = {
                        if (canNavigateBack) {
                            IconButton(onClick = {
                                navController.navigateUp()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "back"
                                )
                            }
                        }
                    },
                    actions = {

                        if (backStackEntry?.destination?.route == "CompanionAdvanced") {
                            IconButton(onClick = { viewModel.clearLogs() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "",
                                    tint = Color.Blue
                                )
                            }
                        }

                        IconButton(onClick = {
                            viewModel.playOrPauseContent()
                        }) {
                            Icon(
                                painter = painterResource(id = if (state == PlayState.PLAYING) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = "",
                                tint = Color.Blue
                            )
                        }
                        IconButton(onClick = { viewModel.resetContent() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_replay),
                                contentDescription = "",
                                tint = Color.Blue
                            )
                        }
                    },
                    title = { Text(text = backStackEntry?.destination?.route.orEmpty()) })
            },
            bottomBar = {

                AnimatedVisibility(visible = showNavigation) {
                    BottomAppBar {
                        NavigationBarItem(
                            selected = backStackEntry?.destination?.route == "CompanionMainScreen",
                            label = { Text(text = "AdCompanionView") },
                            onClick = {
                                navController.navigate("CompanionMainScreen") {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "CompanionView"
                                )
                            })
                        NavigationBarItem(
                            selected = backStackEntry?.destination?.route == "AdVideoViewScreen",
                            label = { Text(text = "AdVideoView") },
                            onClick = {
                                navController.navigate("AdVideoViewScreen") {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "VideoView"
                                )
                            })
                    }
                }
            })
        { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = "CompanionMainScreen",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                composable("CompanionMainScreen") {
                    CompanionScreen(
                        onDetailsClick = { navController.navigate("FullScreenCompanion") },
                        onAnimationScreen = { navController.navigate("CompanionAdvanced") },
                    )
                }
                composable("AdVideoViewScreen") {
                    AdVideoViewScreen()
                }

                composable("FullScreenCompanion") {
                    CompanionDetailsScreen()
                }
                composable("CompanionAdvanced") {
                    CompanionAnimationScreen(
                        logs = logs,
                        selectedResource = selectedResourceText,
                        onCompanionEvent = viewModel::addLog,
                        onResourceSelected = viewModel::onResourceSelected
                    )
                }
            }

        }

    }

}