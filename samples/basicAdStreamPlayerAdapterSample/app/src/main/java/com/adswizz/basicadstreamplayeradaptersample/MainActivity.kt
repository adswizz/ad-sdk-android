package com.adswizz.basicadstreamplayeradaptersample

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
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdBaseManagerListener
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.streaming.AdManagerStreamingSettings
import com.ad.core.streaming.AdStreamManager
import com.ad.core.streaming.DvrBufferInfo
import com.adswizz.basicadstreamplayeradaptersample.databinding.ActivityMainBinding
import com.adswizz.common.AdMetadataItem
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.core.streaming.AfrConfig
import com.adswizz.sdk.AdswizzSDK
import java.text.SimpleDateFormat
import java.util.Date

@UnstableApi
class MainActivity : AppCompatActivity() {

    private var streamManager: AdswizzAdStreamManager? = null
    private var externalAdPlayer: ExternalAdStreamPlayerAdapter? = null
    private var adBreakManager: AdBaseManager? = null

    private lateinit var binding: ActivityMainBinding

    private val adManagerListener = object : AdBaseManagerListener {
        override fun onEventReceived(adBaseManager: AdBaseManager, event: AdEvent) {
            when (event.type) {
                AdEvent.Type.State.FirstAdWillInitialize -> {
                    binding.appLogs.addEntry("Ad - first ad will initialize")
                }

                AdEvent.Type.State.Initialized -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - initialized")
                }

                AdEvent.Type.State.Unknown -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - in unknown state")
                }

                AdEvent.Type.State.PreparingForPlay -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - preparing for play")
                }

                AdEvent.Type.State.ReadyForPlay -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - ready for play")
                }

                AdEvent.Type.State.WillStartBuffering -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - will start buffering")
                }

                AdEvent.Type.State.DidFinishBuffering -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did finish buffering")
                }

                AdEvent.Type.State.DidStartPlaying -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did start playing")
                }

                AdEvent.Type.State.DidResumePlaying -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did resume playing")
                }

                AdEvent.Type.State.DidPausePlaying -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did pause playing")
                }

                AdEvent.Type.State.DidFinishPlaying -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did finish playing")
                }

                AdEvent.Type.State.Completed -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - completed")
                }

                AdEvent.Type.State.AllAdsCompleted -> {
                    binding.appLogs.addEntry("Ad - all ads completed")
                }

                AdEvent.Type.State.DidSkip -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did skip playing")
                }

                AdEvent.Type.State.NotUsed -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - not used for playing")
                }

                AdEvent.Type.State.AdUpdated -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - ad updated")
                }

                AdEvent.Type.Position.Start -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - 0% start")
                }

                AdEvent.Type.Position.FirstQuartile -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - 25% first quartile")
                }

                AdEvent.Type.Position.Midpoint -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - 50% midpoint")

                }

                AdEvent.Type.Position.ThirdQuartile -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - 75% third quartile")

                }

                AdEvent.Type.Position.Complete -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - 100% complete")
                }

                AdEvent.Type.Other.Error -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} ERROR ${event.extraAdData}")
                }
                else -> Unit
            }
        }

        override fun onEventErrorReceived(
            adBaseManager: AdBaseManager,
            event: AdEvent,
            error: Error
        ) {
            binding.appLogs.addEntry("Error - ${error.message} for ad: ${event.ad?.id}")
        }
    }

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
            adBreakManager = adBaseManager
            adBaseManager.setListener(adManagerListener)
        }

        override fun adBreakEnded(
            adStreamManager: AdStreamManager, adBaseManager: AdBaseManager
        ) {
            binding.appLogs.addEntry("Ad break ended: adBaseManager $adBaseManager")
            adBreakManager?.removeAdBaseManagerListener()
            adBreakManager = null
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            binding.appLogs.addEntry("Error - ${error.message} for adStreamManager: $adStreamManager")
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager, metadataItem: AdMetadataItem
        ) {
            binding.appLogs.addEntry("Metadata received - adStreamManager: $adStreamManager - metadata count: ${metadataItem.value.count()}")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val innerPadding = insets.getInsets(
                // Notice we're using systemBars, not statusBar
                WindowInsetsCompat.Type.systemBars()
                        // Notice we're also accounting for the display cutouts
                        or WindowInsetsCompat.Type.displayCutout()
                // If using EditText, also add
                // "or WindowInsetsCompat.Type.ime()"
                // to maintain focus when opening the IME
            )
            v.setPadding(
                innerPadding.left,
                innerPadding.top,
                innerPadding.right,
                innerPadding.bottom)
            insets
        }

        AdswizzSDK.initialize(this)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val scroll = ScrollingMovementMethod()
        binding.appLogs.movementMethod = scroll

        binding.sdkVersion.text = "${getString(R.string.adswizz_version_prefix)}${com.adswizz.sdk.BuildConfig.VERSION_NAME}"

        binding.companionView.setBackgroundColor(Color.parseColor("#00000000"))

        AdswizzSDK.afrConfig = AfrConfig(
            protocol = AdswizzAdRequest.HttpProtocol.HTTP,
            server = "demo.deliveryengine.adswizz.com/",
            endpoint = "www/delivery/afr.php",
            companionZone = "12193"
        )

        externalAdPlayer = ExternalAdStreamPlayerAdapter(this)
        val adManagerStreamingSettingsBuilder = AdManagerStreamingSettings.Builder()
            .adStreamPlayerAdapterInstanceInstance(externalAdPlayer!!)
            .build()
        streamManager = AdswizzAdStreamManager(adManagerStreamingSettingsBuilder)
        val streamUrl = AdswizzAdStreamManager.decorateUrl("https://streaming.sdk.eks.devel.adswizz.com/shakeDemo")
        streamManager?.addListener(listener)
        externalAdPlayer?.loadStream(streamUrl)
        externalAdPlayer?.player?.prepare()
        externalAdPlayer?.player?.play()
    }

    override fun onDestroy() {
        streamManager?.cleanup()
        streamManager = null
        externalAdPlayer?.cleanup()
        externalAdPlayer = null
        AdswizzSDK.cleanup()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuReplay -> {
                externalAdPlayer?.player?.stop()
                externalAdPlayer?.player?.clearMediaItems()
                streamManager?.cleanup()
                streamManager?.removeListener(listener)
                binding.appLogs.text = ""
                val adManagerStreamingSettingsBuilder = AdManagerStreamingSettings.Builder()
                    .adStreamPlayerAdapterInstanceInstance(externalAdPlayer!!)
                    .build()
                streamManager = AdswizzAdStreamManager(adManagerStreamingSettingsBuilder)
                val streamUrl = AdswizzAdStreamManager.decorateUrl("https://streaming.sdk.eks.devel.adswizz.com/shakeDemo")
                streamManager?.addListener(listener)
                externalAdPlayer?.loadStream(streamUrl)
                externalAdPlayer?.player?.prepare()
                externalAdPlayer?.player?.play()
            }
        }
        return super.onOptionsItemSelected(item)
    }

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