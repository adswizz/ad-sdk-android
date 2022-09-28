package com.adswizz.precachesample

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.adManager.AdManagerSettings
import com.ad.core.cache.CachePolicy
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.player.CacheManager
import com.adswizz.precachesample.databinding.ActivityMainBinding
import com.adswizz.sdk.AdswizzSDK

class MainActivity : AppCompatActivity(), CacheManager.Listener {
    companion object {
        const val SERVER_URL = "demo.deliveryengine.adswizz.com"
        const val ZONE_ID = "13396"
        const val ZONE_ID_2 = "11993"
    }

    private lateinit var binding: ActivityMainBinding

    private var adManager: AdManager? = null
    private var adManagerSettings: AdManagerSettings? = null
    private var adRequestConnection: AdRequestConnection? = null
    private var count = 0

    private val listener = object : AdManagerListener {
        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
            when (event.type) {
                AdEvent.Type.State.FirstAdWillInitialize -> {
                    binding.appLogs.addEntry("Ad - first ad will initialize")
                }

                AdEvent.Type.State.Initialized -> {
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - initialized")
                    count++
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
                    binding.appLogs.addEntry("Ad ${event.ad?.id} - 0%  start")
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
            }
        }

        override fun onEventErrorReceived(adManager: AdManager, ad: AdData?, error: Error) {
            binding.appLogs.addEntry("Error - ${error.message} for ad: ${ad?.id}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        supportActionBar?.hide()

        val window = this.window
        getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_statusbar_color)

        val scroll = ScrollingMovementMethod()
        binding.appLogs.movementMethod = scroll

        AdswizzSDK.initialize(this)
        binding.sdkVersion.text = String.format(getString(R.string.adswizz_version_prefix, com.adswizz.sdk.BuildConfig.VERSION_NAME))

        adManagerSettings = AdManagerSettings.Builder()
            .addCachePolicy(CachePolicy.ASSETS)
            .enqueueEnabled(true)
            .build()

        CacheManager.addGlobalListener(this)
        //Build the Ad Request with the needed parameters
        AdswizzAdRequest.Builder()
            .withServer(SERVER_URL)
            .withZones(setOf(AdswizzAdZone(ZONE_ID), AdswizzAdZone(ZONE_ID_2)))
            .build { adRequest ->
                // Create the AdRequestConnection using the above adRequest
                adRequestConnection = AdRequestConnection(adRequest)
                adRequestConnection?.requestAds { adManager, error ->
                    // Handle the response from server
                    handleResponse(error, adManager)
                }

//                playButton.setOnClickListener {
//                    adManager?.play()
//                }
//                replayButton.setOnClickListener {
//                    adManager?.reset()
//                    adManager = null
//
//                    appLogs.text = ""
//
//                    adRequestConnection.requestAds { adManager, error ->
//                        handleResponse(error, adManager)
//                    }
//                }
            }
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
            this.adManager?.adManagerSettings = adManagerSettings
            binding.appLogs.addEntry("Received ad manager")
            adManager.setListener(listener)
            adManager.prepare()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuPlay -> {
                adManager?.play()
            }
            R.id.menuReplay -> {
                adManager?.reset()
                adManager = null

                binding.appLogs.text = ""

                adRequestConnection?.requestAds { adManager, error ->
                    handleResponse(error, adManager)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        AdswizzSDK.cleanup()
        super.onDestroy()
    }

    fun TextView.addEntry(entry: String) {
        val strBuilder = StringBuilder(this.text)
        strBuilder.append(entry)
        strBuilder.append("\n\n")
        this.text = strBuilder.toString()
    }

    override fun onDownloadCompleted(assetUri: String) {
        binding.appLogs.addEntry("Download completed for: $assetUri")
        count--

        if (count == 0) {
            adManager?.play() // play when all files have downloaded
        }
    }

    override fun onDownloadFailed(assetUri: String, error: Error) {
        binding.appLogs.addEntry("Download failed for: $assetUri")
    }

    override fun onDownloadStarted(assetUri: String) {
        binding.appLogs.addEntry("Download started for: $assetUri")
    }

}