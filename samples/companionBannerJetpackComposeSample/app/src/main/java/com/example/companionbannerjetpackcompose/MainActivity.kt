package com.example.companionbannerjetpackcompose

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ad.core.companion.AdCompanionView
import com.adswizz.sdk.AdswizzSDK
import com.example.companionbannerjetpackcompose.ui.theme.CompanionBannerJetpackComposeTheme
import com.example.companionbannerjetpackcompose.ui.theme.Purple40
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    navController.addOnDestinationChangedListener { controller, _, _ ->
        canNavigateBack = controller.previousBackStackEntry != null
    }

    val state by viewModel.playingState.collectAsState()

    val logs by viewModel.logs.collectAsState()

    val selectedResourceText by viewModel.selectedResourceText.collectAsState()

    CompanionBannerJetpackComposeTheme {

        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Purple40),
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = {
                            navController.navigateUp()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack, contentDescription = "back"
                            )
                        }
                    }
                },
                actions = {

                    if (backStackEntry?.destination?.route == "Advanced") {
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
        }) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = "MainScreen",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                composable("MainScreen") {
                    MainScreen(
                        onDetailsClick = { navController.navigate("FullScreenCompanion") },
                        onAnimationScreen = { navController.navigate("Advanced") },
                    )
                }

                composable("FullScreenCompanion") {
                    DetailsScreen()
                }
                composable("Advanced") {
                    AnimationScreen(
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
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Companion PlaceHolder")
            }
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
            Text(text = "Go to Full Screen")
        }

        Button(onClick = onAnimationScreen) {
            Text(text = "Advanced")
        }
    }

}


@Composable
fun DetailsScreen() {
    Text(text = "Hello details")
    ComposableCompanionView(
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun AnimationScreen(
    logs: List<String>,
    selectedResource: String,
    onCompanionEvent: (String, String) -> Unit,
    onResourceSelected: (String) -> Unit
) {

    var width by remember { mutableFloatStateOf(1f) }
    var height by remember { mutableFloatStateOf(1f) }
    var shouldShowResourceDialog by remember { mutableStateOf(false) }

    val modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.5f)
    Column {
        Box(modifier = modifier) {
            ComposableCompanionView(
                modifier = Modifier
                    .fillMaxWidth(width)
                    .fillMaxHeight(height)
                    .animateContentSize(),
                didDisplayAd = { onCompanionEvent(it, "DidDisplay") },
                didEndDisplay = { onCompanionEvent(it, "DidEndDisplay") },
                willLoadAd = { onCompanionEvent(it, "WillLoadAd") },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Change width")
        Slider(
            modifier = Modifier.padding(horizontal = 10.dp),
            value = width,
            valueRange = 0f..1f,
            onValueChange = {
                width = it
            })
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Change height")
        Slider(
            modifier = Modifier.padding(horizontal = 10.dp),
            value = height,
            valueRange = 0f..1f,
            onValueChange = {
                height = it
            })

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { shouldShowResourceDialog = true }) {
                Text(text = "$selectedResource Selected")
            }
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            items(logs) {
                Text(
                    text = it,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 10.dp, bottom = 5.dp)
                        .background(Color.Black)
                        .padding(1.dp)
                        .background(Color.White)
                )
            }
        }

        if (shouldShowResourceDialog) {
            SelectResourceDialog(onDismissRequest = {
                shouldShowResourceDialog = false
                onResourceSelected(it)
            })
        }
    }
}

@Composable
fun SelectResourceDialog(onDismissRequest: (String) -> Unit) {

    val resources = listOf("Static Resource", "HtmlResource")
    var selectedOption by remember { mutableStateOf(resources[0]) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onDismissRequest(selectedOption) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "Select Companion Resource",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center,
            )
            resources.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = {
                                selectedOption = text
                                scope.launch {
                                    delay(250)
                                    onDismissRequest(text)
                                }
                            }
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        onClick = {
                            selectedOption = text
                            scope.launch {
                                delay(250)
                                onDismissRequest(text)
                            }
                        }
                    )
                    Text(text = text)
                }
            }


        }
    }
}

