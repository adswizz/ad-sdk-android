package com.example.jetpackcomposesample

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ad.core.video.AdVideoFriendlyObstruction
import com.ad.core.video.AdVideoFriendlyObstructionPurpose
import com.ad.core.video.AdVideoView

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
            return@AndroidView container
        },
    )
}

@Composable
fun AdVideoViewScreen(isPlaying: Boolean, onSkipAd: () -> Unit) {

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
                    })

            }
        }

    }
}