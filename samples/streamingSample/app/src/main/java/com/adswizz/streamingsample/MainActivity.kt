package com.adswizz.streamingsample

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.adBaseManager.AdPlayer
import com.ad.core.streaming.AdStreamManager
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.core.streaming.AfrConfig
import com.adswizz.sdk.AdswizzSDK
import com.adswizz.streamingsample.databinding.ActivityMainBinding

// Streaming app shows minimal server side functionality

class MainActivity : AppCompatActivity() {

    companion object {
        const val SERVER_URL = "http://sdk.mobile.streaming.adswizz.com:8500/shakeDemoAAC/playlist.m3u8"
    }

    private lateinit var binding: ActivityMainBinding

    private var streamManager: AdswizzAdStreamManager? = null
    private val logAdapter = LogAdapter()

    private val listener = object : AdStreamManager.Listener {
        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            logAdapter.add("Will start playing url: $url")
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            logAdapter.add("Did finish playing url: $url")
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            logAdapter.add("Did pause playing url: $url")
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            logAdapter.add("Did resume playing url: $url")
        }

        override fun adBreakStarted(
            adStreamManager: AdStreamManager, adBaseManager: AdBaseManager
        ) {
            logAdapter.add("Ad break started: adBaseManager $adBaseManager")
        }

        override fun adBreakEnded(
            adStreamManager: AdStreamManager, adBaseManager: AdBaseManager
        ) {
            logAdapter.add("Ad break ended: adBaseManager $adBaseManager")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            logAdapter.add("Error - ${error.message} for adStreamManager: $adStreamManager")
        }

        override fun onMetadataChanged(
            adStreamManager: AdStreamManager, metadataItem: AdPlayer.MetadataItem
        ) {
            logAdapter.add("Metadata received: $metadataItem")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_statusbar_color)

        AdswizzSDK.initialize(this)
        AdswizzSDK.afrConfig = AfrConfig(
            protocol = AdswizzAdRequest.HttpProtocol.HTTP,
            server = "demo.deliveryengine.adswizz.com",
            endpoint = "www/delivery/afr.php"
        )

        streamManager = AdswizzAdStreamManager(null)
        streamManager?.addListener(listener)
        streamManager?.play(SERVER_URL)

        binding.log.adapter = logAdapter
        logAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.log.smoothScrollToPosition(logAdapter.itemCount - 1)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_reload -> {
            streamManager?.stop()
            streamManager?.removeListener(listener)

            logAdapter.clearAll()

            streamManager = AdswizzAdStreamManager(null)
            streamManager?.play(SERVER_URL)
            streamManager?.addListener(listener)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        streamManager?.cleanup()
        AdswizzSDK.cleanup()
        super.onDestroy()
    }
}
