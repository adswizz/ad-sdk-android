package com.example.companionbannerjetpackcompose

import android.animation.ValueAnimator
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
    skipButton: @Composable () -> Unit = {}
) {

    AndroidView(
        modifier = Modifier.then(modifier),
        factory = {

            val container = FrameLayout(it)
            val skipButtonView = ComposeView(it)
                .apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent { skipButton() }
                    setPadding(0, 20, 20, 0)
                    layoutParams =
                        FrameLayout.LayoutParams(
                            ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                            ViewGroup.MarginLayoutParams.WRAP_CONTENT
                        )
                            .apply {
                                this.gravity = Gravity.END or Gravity.TOP
                            }
                }

            val adVideoView = AdVideoView(context = it).apply {
                this.setVideoViewId(videoViewID)
                this.setListener(object : AdVideoView.Listener {

                    override fun shouldOverrideVideoClickThrough(adVideoView: AdVideoView): Boolean =
                        false

                    override fun willLeaveApplication(adVideoView: AdVideoView) = Unit
                    override fun onVideoStarted(adVideoView: AdVideoView) = onVideoStarted()
                    override fun onVideoEnded(adVideoView: AdVideoView) {
                        clearContent()
                        onVideoEnded()
                    }

                })
            }
            container.addView(adVideoView)
            container.addView(skipButtonView)
            adVideoView.registerFriendlyObstruction(
                AdVideoFriendlyObstruction(
                    skipButtonView,
                    AdVideoFriendlyObstructionPurpose.CLOSE_AD
                )
            )
//            adVideoView.alpha = 0f
//
//            adVideoView.animate().alpha(1f)
//                .setDuration(3000)
//                .start()
//
//            ValueAnimator.ofFloat(0.0f, 1.0f).apply {
//                duration = 600
//                addUpdateListener {
//                    adVideoView.alpha = it.animatedValue as Float
//                }
//                start()
//            }
//            adVideoView.alpha = 0f
//            skipButtonView.startAnimation(AlphaAnimation(0.0f, 1.0f).apply { duration = 4000 })

            return@AndroidView container

        },
    )
}

data class ObstructionPoint(val x: Int, val y: Int, val color: Color)

@Composable
fun AdVideoViewScreen(isPlaying: Boolean, onSkipAd: () -> Unit) {
    val obstructions = remember {
        mutableStateListOf<ObstructionPoint>()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                visible = !isPlaying, enter = fadeIn(), exit = fadeOut()
            ) {
                Text(
                    modifier = Modifier
                        .background(Color.Blue.copy(alpha = 0.3f)),
                    text = "Video PlaceHolder",
                    textAlign = TextAlign.Center
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                visible = isPlaying, enter = fadeIn(), exit = fadeOut()
            ) {
                ComposableAdVideoView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Yellow.copy(0.2f))
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

        }




        Row(
            modifier = Modifier.padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Add/Remove Non Friendly obstructions")

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
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "plus"
                )

            }

            IconButton(onClick = {
                obstructions.removeLastOrNull()
            }) {
                Icon(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    painter = painterResource(id = R.drawable.ic_remove),
                    contentDescription = "minus"
                )
            }
        }


    }
}