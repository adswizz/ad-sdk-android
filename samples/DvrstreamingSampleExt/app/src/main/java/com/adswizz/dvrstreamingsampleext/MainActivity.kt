package com.adswizz.dvrstreamingsampleext

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import com.ad.core.adBaseManager.AdBaseManager
import com.ad.core.streaming.AdManagerStreamingSettings
import com.ad.core.streaming.AdStreamManager
import com.ad.core.streaming.DvrBufferInfo
import com.adswizz.common.AdPlayer
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.dvrstreamingsampleext.databinding.ActivityMainBinding
import com.adswizz.sdk.AdswizzSDK
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var streamManager: AdswizzAdStreamManager? = null
    private var playerState = PlayerState.IDLE
    private lateinit var externalAdPlayer: ExternalAdPlayer

    private val dateFormat = SimpleDateFormat("dd/MM HH:mm:ss", Locale.US)

    private val streamListener = object: AdStreamManager.Listener {
        override fun willStartPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            playerState = PlayerState.PLAYING
            updateIcon()
            binding.buttonReset.isEnabled = true
        }

        override fun didFinishPlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            reset()
        }

        override fun didPausePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            playerState = PlayerState.PAUSED
            updateIcon()
        }

        override fun didResumePlayingUrl(adStreamManager: AdStreamManager, url: Uri) {
            updateIcon()
        }

        override fun adBreakStarted(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            println("dvrstreamingsample::adBreakStarted")
        }

        override fun adBreakEnded(adStreamManager: AdStreamManager, adBaseManager: AdBaseManager) {
            println("dvrstreamingsample::adBreakEnded")
        }

        override fun onMetadataChanged(adStreamManager: AdStreamManager, metadataItem: AdPlayer.MetadataItem) {
            println("dvrstreamingsample::onMetadataChanged $metadataItem")
        }

        override fun onError(adStreamManager: AdStreamManager, error: Error) {
            println("dvrstreamingsample::onError $error")
        }

        @SuppressLint("SetTextI18n")
        override fun onDvrMetadataReceived(adStreamManager: AdStreamManager, dvrBufferInfo: DvrBufferInfo?) {
            dvrBufferInfo?.let {
                println("dvrstreamingsample::onDvrMetadataReceived")
                binding.dvrStartTimestamp.text = it.start.toString()
                binding.dvrStartDateTime.text = dateFormat.format(Date(it.start))

                binding.dvrEndTimestamp.text = it.latest.toString()
                binding.dvrEndDateTime.text = dateFormat.format(Date(it.latest))

                binding.dvrBufferTimestamp.text = it.maxBufferSize.toString()
                val hours = it.maxBufferSize / 3600000
                val minutes = (it.maxBufferSize % 3600000) / 60000
                binding.dvrBufferTime.text = "${hours}h ${minutes}m"

                val actualWindowSize = it.latest - it.start

                binding.dvrPlaySlider.apply {
                    valueFrom = it.start.toFloat()
                    valueTo = it.latest.toFloat()
                    value = it.start + actualWindowSize / 2f
                }
                binding.dvrPlayTimestamp.editText?.setText(binding.dvrPlaySlider.value.toLong().toString())

                binding.dvrPlay0.setOnClickListener { _ ->
                    streamManager?.play(STREAM_URL, it.start)
                }

                binding.dvrPlay25.setOnClickListener { _ ->
                    streamManager?.play(STREAM_URL, it.start + actualWindowSize / 4)
                }

                binding.dvrPlay50.setOnClickListener { _ ->
                    streamManager?.play(STREAM_URL, it.start + actualWindowSize / 2)
                }

                binding.dvrPlay75.setOnClickListener { _ ->
                    streamManager?.play(STREAM_URL, it.start + actualWindowSize * 3 / 4)
                }

                binding.dvrPlayAction.isEnabled = true
                binding.dvrPlaySlider.isEnabled = true
                binding.dvrPlayTimestamp.isEnabled = true
                binding.dvrPlay0.isEnabled = true
                binding.dvrPlay25.isEnabled = true
                binding.dvrPlay50.isEnabled = true
                binding.dvrPlay75.isEnabled = true
            } ?: run {
                clearDvrInfo()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        AdswizzSDK.initialize(this.applicationContext)

        externalAdPlayer = ExternalAdPlayer(this)

        binding.buttonPlay.setOnClickListener {
            when (playerState) {
                PlayerState.IDLE -> {
                    val settings = AdManagerStreamingSettings.Builder()
                        .dvrEnabled(true)
                        .enqueueEnabled(false)
                        .adPlayerInstance(externalAdPlayer)
                        .build()
                    streamManager = AdswizzAdStreamManager(settings)
                    streamManager?.addListener(streamListener)
                    streamManager?.play(STREAM_URL)
                }
                PlayerState.PLAYING -> {
                    streamManager?.pause()
                }
                PlayerState.PAUSED -> {
                    streamManager?.play(STREAM_URL)
                }
            }
        }

        binding.buttonReset.setOnClickListener {
            reset()
        }

        binding.dvrPlayTimestamp.editText?.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }
            return@setOnEditorActionListener false
        }
        binding.dvrPlayTimestamp.editText?.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                binding.dvrPlayTimestamp.editText?.text?.let {
                    it.toString().toFloatOrNull()?.let { timestamp ->
                        val properTimestamp = timestamp.coerceIn(binding.dvrPlaySlider.valueFrom, binding.dvrPlaySlider.valueTo)
                        binding.dvrPlaySlider.value = properTimestamp
                        binding.dvrPlayTimestamp.editText?.setText(properTimestamp.toLong().toString())
                    }
                }
            }
        }

        binding.dvrPlaySlider.setLabelFormatter { return@setLabelFormatter dateFormat.format(it) }
        binding.dvrPlaySlider.addOnChangeListener { _, value, fromUser ->
            if(fromUser) {
                binding.dvrPlayTimestamp.editText?.setText(value.toLong().toString())
            }
        }

        binding.dvrPlayAction.setOnClickListener {
            streamManager?.play(STREAM_URL, binding.dvrPlaySlider.value.toLong())
        }

    }

    private fun clearDvrInfo() {
        binding.dvrStartTimestamp.text = ""
        binding.dvrStartDateTime.text = ""
        binding.dvrEndTimestamp.text = ""
        binding.dvrEndDateTime.text = ""
        binding.dvrBufferTimestamp.text = ""
        binding.dvrBufferTime.text = ""
        binding.dvrPlayAction.isEnabled = false
        binding.dvrPlaySlider.isEnabled = false
        binding.dvrPlayTimestamp.isEnabled = false
        binding.dvrPlay0.isEnabled = false
        binding.dvrPlay25.isEnabled = false
        binding.dvrPlay50.isEnabled = false
        binding.dvrPlay75.isEnabled = false
    }

    private fun reset() {
        streamManager?.apply {
            stop()
            removeListener(streamListener)
            cleanup()
        }
        streamManager = null
        playerState = PlayerState.IDLE
        clearDvrInfo()
        binding.buttonReset.isEnabled = false
        updateIcon()
    }

    private fun updateIcon() {
        binding.buttonPlay.text = if(playerState == PlayerState.PLAYING) "Pause" else "Play"
        binding.buttonPlay.setIconResource(if(playerState == PlayerState.PLAYING) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
    }

    companion object {
        const val STREAM_URL = "https://streaming.sdk.eks.devel.adswizz.com/addictalternative/playlist.m3u8"
    }

    enum class PlayerState {
        IDLE, PLAYING, PAUSED
    }
}