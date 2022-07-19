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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.companion.AdCompanionView
import com.ad.core.streaming.AdManagerStreamingSettings
import com.ad.core.streaming.AdStreamManager
import com.ad.core.streaming.DvrBufferInfo
import com.adswizz.common.AdPlayer
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.core.streaming.AfrConfig
import com.adswizz.sdk.AdswizzSDK
import com.adswizz.streamingsample.databinding.ActivityMainBinding
//import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

// Streaming app shows minimal server side functionality

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    companion object {
        const val SERVER_URL = "http://sdk.mobile.streaming.adswizz.com:8500/shakeDemo"
    }

    private var streamManager: AdswizzAdStreamManager? = null
    private var externalAdPlayer: ExternalAdPlayer? = null

    private val listener = object : AdStreamManager.Listener {
        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            binding.appLogs.addEntry("Will start playing url: $url")
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            binding.appLogs.addEntry("Did finish playing url: $url")
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            binding.appLogs.addEntry("Did pause playing url: $url")
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            binding.appLogs.addEntry("Did resume playing url: $url")
        }

        override fun onDvrMetadataReceived(
            adStreamManager: AdStreamManager,
            dvrBufferInfo: DvrBufferInfo?
        ) {
            // ignored
        }

        override fun adBreakStarted(
            adStreamManager: AdStreamManager, adBaseManager: AdBaseManager
        ) {
            binding.appLogs.addEntry("Ad break started: adBaseManager $adBaseManager")
        }

        override fun adBreakEnded(
            adStreamManager: AdStreamManager, adBaseManager: AdBaseManager
        ) {
            binding.appLogs.addEntry("Ad break ended: adBaseManager $adBaseManager")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            binding.appLogs.addEntry("Error - ${error.message} for adStreamManager: $adStreamManager")
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager, metadataItem: AdPlayer.MetadataItem
        ) {
            binding.appLogs.addEntry("Metadata received - adStreamManager: $adStreamManager - metadata count: ${metadataItem.value.count()}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //supportActionBar?.hide()

        val window = this.window
        getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_statusbar_color)

        val scroll = ScrollingMovementMethod()
        binding.appLogs.movementMethod = scroll

        binding.sdkVersion.text = "${getString(R.string.adswizz_version_prefix)}${com.adswizz.sdk.BuildConfig.VERSION_NAME}"

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

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuReplay -> {
                streamManager?.stop()
                streamManager?.removeListener(listener)
                binding.appLogs.text = ""

                streamManager = AdswizzAdStreamManager(null)
                streamManager?.play(SERVER_URL)
                streamManager?.addListener(listener)
            }
        }
        return super.onOptionsItemSelected(item)
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
        val entryLine = "$timeStamp $entry\n\n"

        ssBuilder.append(entryLine)

        val spanStart = ssBuilder.length - entryLine.length
        val spanEnd = spanStart + timeStamp.length
        ssBuilder.setSpan(RelativeSizeSpan(0.7f), spanStart, spanEnd, 0)
        ssBuilder.setSpan(ForegroundColorSpan(Color.DKGRAY), spanStart, spanEnd, 0)
        ssBuilder.setSpan(TypefaceSpan("sans-serif-condensed"), spanStart, spanEnd, 0)

        this.text = ssBuilder
    }
}
