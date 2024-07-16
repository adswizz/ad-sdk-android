package com.example.companionbannerjetpackcompose

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ad.core.video.AdVideoFriendlyObstruction
import com.ad.core.video.AdVideoFriendlyObstructionPurpose
import com.ad.core.video.AdVideoView
import kotlin.random.Random

@Composable
fun ComposableAdVideoView(
    modifier: Modifier,
    videoViewID: Int,
    onVideoStarted: () -> Unit = {},
    onVideoEnded: () -> Unit = {},
    skipButton : @Composable () -> Unit = {}
) {

    AndroidView(modifier = Modifier.then(modifier), factory = {

        val container = FrameLayout(it)
        val skipButtonView = ComposeView(it)
            .apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent { skipButton() }
                setPadding(0, 20, 20, 0)
                layoutParams =
                    FrameLayout.LayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                        .apply {
                            this.gravity = Gravity.END or Gravity.TOP
                        }
            }

        val adVideoView = AdVideoView(context = it, ).apply {
            this.setVideoViewId(videoViewID)
            this.setListener(object : AdVideoView.Listener {

                override fun shouldOverrideVideoClickThrough(adVideoView: AdVideoView): Boolean =
                    false

                override fun willLeaveApplication(adVideoView: AdVideoView) = Unit
                override fun onVideoStarted(adVideoView: AdVideoView)  = onVideoStarted()
                override fun onVideoEnded(adVideoView: AdVideoView) {
                    clearContent()
                    onVideoEnded()
                }

            })
        }
        container.addView(adVideoView)
        container.addView(skipButtonView)
        adVideoView.registerFriendlyObstruction(AdVideoFriendlyObstruction(skipButtonView, AdVideoFriendlyObstructionPurpose.CLOSE_AD))

        return@AndroidView container

    },
 )
}

data class ObstructionPoint(val x: Int, val y: Int, val color: Color)

@Composable
fun AdVideoViewScreen(onSkipAd: () -> Unit) {
    val obstructions = remember {
        mutableStateListOf<ObstructionPoint>()
    }

    Column(
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            ComposableAdVideoView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                videoViewID = 123,
                skipButton = {
                    Button(onClick = onSkipAd) {
                        Text(text = "Skip Ad")
                    }
                }
            )

            obstructions.forEach {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .offset(x = it.x.dp, it.y.dp)
                        .background(it.color)
                )
            }
        }


        Text(text = "Add Non Friendly obstructions")

        Row {
            IconButton(onClick = {
                obstructions.add(
                    ObstructionPoint(
                        x = Random.nextInt(0, 400),
                        y = Random.nextInt(0, 400),
                        color = Color(
                            red = Random.nextInt(0, 255),
                            green = Random.nextInt(0, 255),
                            blue = Random.nextInt(0, 255)
                        )
                    )
                )
            }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "plus"
                )
            }

            IconButton(onClick = {
                obstructions.removeLastOrNull()
            }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "minus"
                )
            }
        }


    }
}