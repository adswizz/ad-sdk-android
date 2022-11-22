package com.adswizz.videosamplefull

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.adManager.AdManagerSettings
import com.adswizz.videosamplefull.databinding.ActivityMainBinding
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.sdk.AdswizzSDK
import com.adswizz.videosamplefull.ExternalAdPlayer
import com.adswizz.videosamplefull.R
//import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

// Basic app shows minimal client side functionality

class MainActivity : AppCompatActivity() {

    companion object {
        const val SERVER_URL = "demo.deliveryengine.adswizz.com"
        const val ZONE_ID = "18022"
        //const val ZONE_ID = "14578"
        private var adManager: AdManager? = null
        private var externalAdPlayer: ExternalAdPlayer? = null
    }

    private lateinit var binding: ActivityMainBinding


    private enum class AdManagerState {
        Unknown, ReadyForPlay, Playing, Paused, AllAdsCompleted
    }

    private var adRequestConnection: AdRequestConnection? = null

    private var adManagerState: AdManagerState = AdManagerState.Unknown

    private val listener = object : AdManagerListener {
        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
            when (event.type) {
                AdEvent.Type.State.FirstAdWillInitialize -> {
                    println("Ad - first ad will initialize")
                }

                AdEvent.Type.State.Initialized -> {
                    println("Ad ${event.ad?.id} - initialized")
                }

                AdEvent.Type.State.Unknown -> {
                    println("Ad ${event.ad?.id} - in unknown state")
                }

                AdEvent.Type.State.PreparingForPlay -> {
                    println("Ad ${event.ad?.id} - preparing for play")
                }

                AdEvent.Type.State.ReadyForPlay -> {
                    adManagerState = AdManagerState.ReadyForPlay
                    println("Ad ${event.ad?.id} - ready for play")
                }

                AdEvent.Type.State.WillStartBuffering -> {
                    println("Ad ${event.ad?.id} - will start buffering")
                }

                AdEvent.Type.State.DidFinishBuffering -> {
                    println("Ad ${event.ad?.id} - did finish buffering")
                }

                AdEvent.Type.State.DidStartPlaying -> {
                    adManagerState = AdManagerState.Playing
                    println("Ad ${event.ad?.id} - did start playing")
                }

                AdEvent.Type.State.DidResumePlaying -> {
                    adManagerState = AdManagerState.Playing
                    println("Ad ${event.ad?.id} - did resume playing")
                }

                AdEvent.Type.State.DidPausePlaying -> {
                    adManagerState = AdManagerState.Paused
                    println("Ad ${event.ad?.id} - did pause playing")
                }

                AdEvent.Type.State.DidFinishPlaying -> {
                    println("Ad ${event.ad?.id} - did finish playing")
                }

                AdEvent.Type.State.Completed -> {
                    println("Ad ${event.ad?.id} - completed")
                }

                AdEvent.Type.State.AllAdsCompleted -> {
                    adManagerState = AdManagerState.AllAdsCompleted
                    //binding.toolbar.menu.findItem(R.id.menuPlay).setIcon(R.drawable.ic_play_arrow_white_24dp)
                    println("Ad - all ads completed")
                }

                AdEvent.Type.State.DidSkip -> {
                    println("Ad ${event.ad?.id} - did skip playing")
                }

                AdEvent.Type.State.NotUsed -> {
                    println("Ad ${event.ad?.id} - not used for playing")
                }

                AdEvent.Type.State.AdUpdated -> {
                    println("Ad ${event.ad?.id} - ad updated")
                }

                AdEvent.Type.Position.Start -> {
                    println("Ad ${event.ad?.id} - 0% start")
                }

                AdEvent.Type.Position.FirstQuartile -> {
                    println("Ad ${event.ad?.id} - 25% first quartile")
                }

                AdEvent.Type.Position.Midpoint -> {
                    println("Ad ${event.ad?.id} - 50% midpoint")
                }

                AdEvent.Type.Position.ThirdQuartile -> {
                    println("Ad ${event.ad?.id} - 75% third quartile")
                }

                AdEvent.Type.Position.Complete -> {
                    println("Ad ${event.ad?.id} - 100% complete")
                }
            }
        }

        override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
            println("Error - ${error.message} for ad: ${ad?.id}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        AdswizzSDK.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        supportActionBar?.hide()

        val window = this.window
        getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_statusbar_color)


        //Build the Ad Request with the needed parameters
        if (adManager == null) {
            externalAdPlayer = ExternalAdPlayer(this)
            AdswizzAdRequest.Builder()
                .withServer(SERVER_URL)
                .withZones(setOf(AdswizzAdZone(ZONE_ID)))
                .build() { adRequest ->
                    // Create the AdRequestConnection using the above adRequest
                    adRequestConnection = AdRequestConnection(adRequest)
                    adRequestConnection?.requestAds { adManager, error ->
                        handleResponse(error, adManager)
                    }
                }
        }
    }

    private fun handleResponse(
            error: Error?,
            adM: AdManager?
    ) {
        if (error != null) {
            println("RequestError: ${error.message}")
        }
        if (adM != null) {
            adManager = adM
            adManager?.adManagerSettings = AdManagerSettings.Builder()
                    .videoViewId(1)
                    .adPlayerInstance(externalAdPlayer!!)
                    .build()
            println("Received ad manager")
            adManager?.setListener(listener)
            adManager?.prepare()
            adManager?.play()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

}
