package com.adswizz.basicsample

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
import androidx.media3.exoplayer.ExoPlayer
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.adManager.AdManagerSettings
import com.adswizz.basicsample.databinding.ActivityMainBinding
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.sdk.AdswizzSDK
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val SERVER_URL = "demo.deliveryengine.adswizz.com"
        const val ZONE_ID = "17862"
        const val COMPANION_ID = "17863"
    }

    private enum class AdManagerState {
        Unknown, ReadyForPlay, Playing, Paused, AllAdsCompleted
    }

    private lateinit var binding: ActivityMainBinding

    private var adManager: AdManager? = null
    private var adRequestConnection: AdRequestConnection? = null

    private lateinit var player: ExoPlayer
    private lateinit var externalMediaPlayerState: ExternalMediaPlayerState

    private var adManagerState: AdManagerState = AdManagerState.Unknown

    private val listener = object : AdManagerListener {
        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
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
                    adManagerState = AdManagerState.ReadyForPlay
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - ready for play")
                }

                AdEvent.Type.State.WillStartBuffering -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - will start buffering")
                }

                AdEvent.Type.State.DidFinishBuffering -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did finish buffering")
                }

                AdEvent.Type.State.DidStartPlaying -> {
                    adManagerState = AdManagerState.Playing
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did start playing")
                }

                AdEvent.Type.State.DidResumePlaying -> {
                    adManagerState = AdManagerState.Playing
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did resume playing")
                }

                AdEvent.Type.State.DidPausePlaying -> {
                    adManagerState = AdManagerState.Paused
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did pause playing")
                }

                AdEvent.Type.State.DidFinishPlaying -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - did finish playing")
                }

                AdEvent.Type.State.Completed -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - completed")
                }

                AdEvent.Type.State.AllAdsCompleted -> {
                    adManagerState = AdManagerState.AllAdsCompleted
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
                else -> Unit
            }
        }

        override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
            binding.appLogs.addEntry("Error - ${error.message} for ad: ${ad?.id}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val window = this.window
        getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_statusbar_color)

        val scroll = ScrollingMovementMethod()
        binding.appLogs.movementMethod = scroll



        binding.sdkVersion.text = "${getString(R.string.adswizz_version_prefix)}${com.adswizz.sdk.BuildConfig.VERSION_NAME}"

        //Build the Ad Request with the needed parameters
        player = ExoPlayer.Builder(this).build()
        externalMediaPlayerState = ExternalMediaPlayerState(this, player)
        AdswizzSDK.initialize(this) {
            AdswizzAdRequest.Builder()
                .withServer(SERVER_URL)
                .withZones(listOf(AdswizzAdZone(ZONE_ID)))
                .build { adRequest ->
                    // Create the AdRequestConnection using the above adRequest
                    adRequestConnection = AdRequestConnection(adRequest)
                    adRequestConnection?.requestAds { adManager, error ->
                        handleResponse(error, adManager)
                    }
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuPlay -> {
                when (adManagerState) {
                    AdManagerState.Unknown, AdManagerState.ReadyForPlay -> {
                        if (adManagerState == AdManagerState.Unknown) {
                            binding.appLogs.addEntry("Recommendation: just to be sure that the ad playback will start immediately wait for the <<AdEvent.Type.State.ReadyForPlay>> event")
                        }
                        player.playWhenReady = true
                    }
                    AdManagerState.Paused -> {
                        player.playWhenReady = true
                    }
                    else -> Unit
                }
            }
            R.id.menuReplay -> {
                externalMediaPlayerState.resetPlayer()

                adManager?.reset()
                adManager = null
                adManagerState = AdManagerState.Unknown

                binding.appLogs.text = ""
                binding.appLogs.addEntry("Make new ad request")


                adRequestConnection?.requestAds { adManager, error ->
                    handleResponse(error, adManager)
                }
            }
            R.id.menuPause -> {
                player.playWhenReady = false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleResponse(
        error: Error?,
        adManager: AdManager?
    ) {
        if (error != null) {
            binding.appLogs.addEntry("RequestError: ${error.message}")
        }
        if (adManager != null) {
            this.adManager = adManager
            binding.appLogs.addEntry("Received ad manager")
            adManager.adManagerSettings = AdManagerSettings.Builder().timeoutIntervalForResource(2.5).build()
            adManager.setListener(listener)
            adManager.adManagerSettings = AdManagerSettings.Builder().mediaPlayerStateInstance(externalMediaPlayerState).build()
            adManager.ads.forEachIndexed { idx, ad ->
                externalMediaPlayerState.loadMediaItem(ad.instanceId, Uri.parse(ad.mediaUrlString), idx )
            }

            adManager.prepare()
            player.prepare()
        }
    }

    override fun onDestroy() {
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
