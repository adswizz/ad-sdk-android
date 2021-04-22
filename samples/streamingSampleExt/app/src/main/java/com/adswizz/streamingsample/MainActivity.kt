package com.adswizz.streamingsample

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdPlayer
import com.ad.core.companion.AdCompanionView
import com.ad.core.streaming.AdManagerStreamingSettings
import com.ad.core.streaming.AdStreamManager
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.core.streaming.AfrConfig
import com.adswizz.sdk.AdswizzSDK
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

// Streaming app shows minimal server side functionality

class MainActivity : AppCompatActivity() {

    companion object {
        const val SERVER_URL = "http://sdk.mobile.streaming.adswizz.com:8500/shakeDemo"
    }

    private var streamManager: AdswizzAdStreamManager? = null
    private var externalAdPlayer: ExternalAdPlayer? = null

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

        sdkVersion.text = "${getString(R.string.adswizz_version_prefix)}${com.adswizz.sdk.BuildConfig.VERSION_NAME}"

        val companion = findViewById<AdCompanionView>(R.id.companionView)
        companion.setBackgroundColor(Color.parseColor("#00000000"))

        AdswizzSDK.initialize(this)
        AdswizzSDK.afrConfig = AfrConfig(
            protocol = AdswizzAdRequest.HttpProtocol.HTTP,
            server = "demo.deliveryengine.adswizz.com/",
            endpoint = "www/delivery/afr.php",
            companionZone = "12193"
        )

        externalAdPlayer = ExternalAdPlayer(this)
        val adManagerStreamingSettingsBuilder = AdManagerStreamingSettings.Builder()
        externalAdPlayer?.let {
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
        externalAdPlayer?.cleanup()
        externalAdPlayer = null
        AdswizzSDK.cleanup()
        super.onDestroy()
    }

    fun TextView.addEntry(entry: String) {
        val ssBuilder = SpannableStringBuilder(this.text)

        val timeStamp: String = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        val entryLine = "$timeStamp $entry\n"

        ssBuilder.append(entryLine)

        val spanStart = ssBuilder.length - entryLine.length
        val spanEnd = spanStart + timeStamp.length
        ssBuilder.setSpan(RelativeSizeSpan(0.7f), spanStart, spanEnd, 0)
        ssBuilder.setSpan(ForegroundColorSpan(Color.DKGRAY), spanStart, spanEnd, 0)
        ssBuilder.setSpan(TypefaceSpan("sans-serif-condensed"), spanStart, spanEnd, 0)

        this.text = ssBuilder
    }
}
