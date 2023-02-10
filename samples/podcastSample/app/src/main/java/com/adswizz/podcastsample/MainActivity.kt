package com.adswizz.podcastsample

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.podcast.AdPodcastManager
import com.ad.core.podcast.AdPodcastManagerDownloadListener
import com.ad.core.podcast.DownloadState
import com.ad.core.podcast.DownloadableAsset
import com.ad.core.video.VideoResizeMode
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.podcast.AdswizzAdPodcastManager
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.core.streaming.AfrConfig
import com.adswizz.podcastsample.databinding.ActivityMainBinding
import com.adswizz.sdk.AdswizzSDK
import java.io.File

class MainActivity : AppCompatActivity() {

    private val podcastUri: Uri =
        Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3")

    private var downloadedLocation: Uri? = null
    private var downloadingAsset: DownloadableAsset? = null

    private lateinit var binding: ActivityMainBinding

    private lateinit var podcastManager: AdswizzAdPodcastManager
    private var playerState = PlayerState.IDLE
    private val logAdapter = LogAdapter()
    private lateinit var saveLocation: Uri

    private val podcastListener = object : AdPodcastManager.Listener {
        override fun willStartPlayingUrl(
            adPodcastManager: AdPodcastManager,
            url: Uri,
            adBaseManager: AdBaseManager?
        ) {
            playerState = PlayerState.PLAYING
            updateIcon()
            binding.buttonReset.isEnabled = true
            binding.podcastSlider.isEnabled = true
            logAdapter.add("Playback started for $url")
        }

        override fun didFinishPlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            reset()
            logAdapter.add("Playback finished")
        }

        override fun didPausePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            playerState = PlayerState.PAUSED
            updateIcon()
            logAdapter.add("Pause playback")
        }

        override fun didResumePlayingUrl(adPodcastManager: AdPodcastManager, url: Uri) {
            playerState = PlayerState.PLAYING
            updateIcon()
            logAdapter.add("Resume playback")
        }

        override fun adBreakStarted(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
            logAdapter.add("Ad break started")
        }

        override fun adBreakEnded(
            adPodcastManager: AdPodcastManager,
            adBaseManager: AdBaseManager
        ) {
            logAdapter.add("Ad break ended")
        }

        override fun onError(adPodcastManager: AdPodcastManager, error: Error) {
            logAdapter.add("An error occurred during playback: ${error.message}")
        }

        override fun onPlayHeadReport(
            adPodcastManager: AdPodcastManager,
            elapsedSec: Double,
            durationSec: Double
        ) {
            binding.podcastSlider.valueTo = durationSec.toFloat()
            binding.podcastSlider.value = elapsedSec.toFloat()
            binding.playHead.text =
                "%02d:%02d".format(elapsedSec.toInt() / 60, elapsedSec.toInt() % 60)
            binding.duration.text =
                "%02d:%02d".format(durationSec.toInt() / 60, durationSec.toInt() % 60)
        }
    }

    private val downloadListener = object : AdPodcastManagerDownloadListener {
        override fun didDownload(
            adPodcastManager: AdPodcastManager,
            bytesDownloaded: Long,
            bytesExpected: Long,
            asset: DownloadableAsset
        ) {
            downloadingAsset = asset
            binding.downloadProgress.progress = (bytesDownloaded * 100 / bytesExpected).toInt()
            updateDownloadState()
        }

        override fun didFinishDownload(
            adPodcastManager: AdPodcastManager,
            asset: DownloadableAsset
        ) {
            downloadedLocation = asset.location
            downloadingAsset = null
            binding.downloadProgress.progress = 100
            updateDownloadState()
            logAdapter.add("Download finished!")
        }

        override fun didReceive(
            adPodcastManager: AdPodcastManager,
            error: Error,
            asset: DownloadableAsset
        ) {
            downloadingAsset = null
            if(asset.state == DownloadState.failed) {
                logAdapter.add("An error occurred while downloading! (${error.message})")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        AdswizzSDK.initialize(this)
        AdswizzSDK.afrConfig = AfrConfig(
            protocol = AdswizzAdRequest.HttpProtocol.HTTP,
            server = "demo.deliveryengine.adswizz.com",
            endpoint = "www/delivery/afr.php"
        )
        
        podcastManager = AdswizzAdPodcastManager()
        podcastManager.addListener(podcastListener)
        podcastManager.addDownloadListener(downloadListener)

        saveLocation = Uri.parse("${applicationContext.filesDir}/${podcastUri.lastPathSegment}")
        if(File(saveLocation.toString()).exists()) {
            downloadedLocation = saveLocation
            updateDownloadState()
        }

        binding.buttonPlay.setOnClickListener {
            when (playerState) {
                PlayerState.IDLE -> {
                    val localPodcast = downloadedLocation
                    if(localPodcast != null) {
                        logAdapter.add("Will play saved podcast")
                        podcastManager.play(localPodcast)
                    } else {
                        logAdapter.add("Will play online podcast")
                        podcastManager.play(podcastUri)
                    }
                }
                PlayerState.PLAYING -> {
                    podcastManager.pause()
                }
                PlayerState.PAUSED -> {
                    podcastManager.resume()
                }
            }
        }

        binding.buttonReset.setOnClickListener {
            reset()
        }

        binding.podcastSlider.setLabelFormatter {
            "%02d:%02d".format(
                it.toInt() / 60,
                it.toInt() % 60
            )
        }
        binding.podcastSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                podcastManager.seekTo(value.toDouble())
            }
        }

        binding.log.adapter = logAdapter

        binding.podcastDownload.setOnClickListener {

            binding.podcastDownload.isEnabled = false
            binding.downloadProgress.visibility = View.VISIBLE
            binding.downloadProgress.progress = 0
            binding.podcastStopDownload.isEnabled = true
            podcastManager.startDownloadPodcast(podcastUri, saveLocation) { success, exception ->
                if(!success) {
                    logAdapter.add("Can't download podcast because ${exception?.message}")
                }
            }
        }

        binding.podcastStopDownload.setOnClickListener {
            downloadingAsset?.let {
                podcastManager?.stopDownloadPodcast(it.location)
            }
        }

        binding.podcastRemoveDownload.setOnClickListener {
            downloadedLocation?.let {
                binding.downloadProgress.progress = 0
                podcastManager?.removePodcast(it)
                downloadedLocation = null
                updateDownloadState()
            }
        }
    }

    override fun onDestroy() {
        podcastManager.cleanup()
        super.onDestroy()
    }

    private fun reset() {
        podcastManager.stop()
        playerState = PlayerState.IDLE
        binding.buttonReset.isEnabled = false
        binding.podcastSlider.value = 0f
        binding.podcastSlider.isEnabled = false
        binding.playHead.text = "00:00"
        binding.duration.text = "00:00"
        updateIcon()
        logAdapter.clear()
    }

    private fun updateIcon() {
        when (playerState) {
            PlayerState.IDLE -> {
                binding.buttonPlay.text = getString(R.string.play)
                binding.buttonPlay.setIconResource(android.R.drawable.ic_media_play)
            }
            PlayerState.PLAYING -> {
                binding.buttonPlay.text = getString(R.string.pause)
                binding.buttonPlay.setIconResource(android.R.drawable.ic_media_pause)
            }
            PlayerState.PAUSED -> {
                binding.buttonPlay.text = getString(R.string.resume)
                binding.buttonPlay.setIconResource(android.R.drawable.ic_media_play)
            }
        }
    }

    private fun updateDownloadState() {
        when {
            downloadingAsset != null -> {
                binding.podcastDownload.isEnabled = false
                binding.podcastStopDownload.isEnabled = true
                binding.podcastRemoveDownload.isEnabled = false
            }
            downloadedLocation != null -> {
                binding.podcastDownload.isEnabled = false
                binding.podcastStopDownload.isEnabled = false
                binding.podcastRemoveDownload.isEnabled = true
            }
            else -> {
                binding.podcastDownload.isEnabled = true
                binding.podcastStopDownload.isEnabled = false
                binding.podcastRemoveDownload.isEnabled = false
            }
        }
    }

    enum class PlayerState {
        IDLE, PLAYING, PAUSED
    }
}