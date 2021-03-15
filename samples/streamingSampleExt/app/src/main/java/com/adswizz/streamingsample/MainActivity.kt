package com.adswizz.streamingsample

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdPlayer
import com.ad.core.streaming.AdManagerStreamingSettings
import com.ad.core.streaming.AdStreamManager
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.core.streaming.AfrConfig
import com.adswizz.sdk.AdswizzSDK
import kotlinx.android.synthetic.main.activity_main.*

// Streaming app shows minimal server side functionality

class MainActivity : AppCompatActivity() {

    companion object {
        const val SERVER_URL = "http://sdk.mobile.streaming.adswizz.com:8500/shakeDemo"
    }

    private var streamManager: AdswizzAdStreamManager? = null
    private var externalExoPlayer: ExternalExoPlayer? = null

    private val listener = object : AdStreamManager.Listener {
        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            appLogs.addEntry("Will start playing url: $url")
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            appLogs.addEntry("Did finish playing url: $url")
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            appLogs.addEntry("Did pause playing url: $url")
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            appLogs.addEntry("Did resume playing url: $url")
        }

        override fun adBreakStarted(
            adStreamManager: AdStreamManager, adBaseManager: AdBaseManager
        ) {
            appLogs.addEntry("Ad break started: adBaseManager $adBaseManager")
        }

        override fun adBreakEnded(
            adStreamManager: AdStreamManager, adBaseManager: AdBaseManager
        ) {
            appLogs.addEntry("Ad break ended: adBaseManager $adBaseManager")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            appLogs.addEntry("Error - ${error.message} for adStreamManager: $adStreamManager")
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager, metadataItem: AdPlayer.MetadataItem
        ) {
            appLogs.addEntry("Metadata received - adStreamManager: $adStreamManager - metadata count: ${metadataItem.value.count()}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportActionBar?.hide()

        val window = this.window
        getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_statusbar_color)

        val scroll = ScrollingMovementMethod()
        appLogs.movementMethod = scroll

        AdswizzSDK.initialize(this)
        AdswizzSDK.afrConfig = AfrConfig(
            protocol = AdswizzAdRequest.HttpProtocol.HTTP,
            server = "demo.deliveryengine.adswizz.com/",
            endpoint = "www/delivery/afr.php"
        )

        externalExoPlayer = ExternalExoPlayer(this)
        val adManagerStreamingSettingsBuilder = AdManagerStreamingSettings.Builder()
        externalExoPlayer?.let {
            adManagerStreamingSettingsBuilder.adPlayerInstance(it)
        }
        streamManager = AdswizzAdStreamManager(adManagerStreamingSettingsBuilder.build())
        streamManager?.addListener(listener)
        streamManager?.play(SERVER_URL)

        replayButton.setOnClickListener {
            streamManager?.stop()
            streamManager?.removeListener(listener)
            appLogs.text = ""

            streamManager = AdswizzAdStreamManager(null)
            streamManager?.play(SERVER_URL)
            streamManager?.addListener(listener)
        }
    }

    override fun onDestroy() {
        streamManager?.cleanup()
        streamManager = null
        externalExoPlayer?.cleanup()
        externalExoPlayer = null
        AdswizzSDK.cleanup()
        super.onDestroy()
    }

    fun TextView.addEntry(entry: String) {
        val strBuilder = StringBuilder(this.text)
        strBuilder.append(entry)
        strBuilder.append("\n")
        this.text = strBuilder.toString()
    }
}
