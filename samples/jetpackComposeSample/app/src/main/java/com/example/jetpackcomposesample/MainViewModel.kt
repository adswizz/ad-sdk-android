package com.example.jetpackcomposesample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.datasource.DataSpec.HttpMethod
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.ad.core.adManager.AdManagerSettings
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PlayState {
    PLAYING,
    PAUSED,
    STOPPED
}

enum class AdType {
    Video,
    Companion
}

class MainViewModel : ViewModel() {

    var adType = AdType.Companion
    private val _playingState = MutableStateFlow(PlayState.STOPPED)
    val playingState = _playingState.asStateFlow()

    private val _logs = MutableStateFlow(emptyList<String>())
    val logs = _logs.asStateFlow()

    private var zoneIdAndCompanion = Pair("17862", 17863)

    private var _selectedResourceText = MutableStateFlow("Static Resource")
    val selectedResourceText = _selectedResourceText.asStateFlow()

    private var currentManager: AdManager? = null

    fun playOrPauseContent() {
        when (_playingState.value) {
            PlayState.PLAYING -> {
                currentManager?.pause()
            }

            PlayState.PAUSED -> {
                currentManager?.resume()
            }

            else -> {
                if (adType == AdType.Companion)
                    requestCompanionAd()
                else {
                    requestVideoAd()
                }
            }
        }
    }

    fun resetContent() {
        currentManager?.reset()
        currentManager = null
        if (adType == AdType.Companion)
            requestCompanionAd()
        else {
            requestVideoAd()
        }
    }

    fun clearLogs() = viewModelScope.launch {
        _logs.emit(emptyList())
    }

    fun skipAd(){
        currentManager?.skipAd()
    }
    private fun requestCompanionAd() {
        AdswizzAdRequest.Builder()
            .withZones(listOf(AdswizzAdZone(zoneId = zoneIdAndCompanion.first)))
            .withCompanionZoneIds(listOf(zoneIdAndCompanion.second))
            .withHttpProtocol(AdswizzAdRequest.HttpProtocol.HTTPS)
            .withServer("demo.deliveryengine.adswizz.com").build {
                AdRequestConnection(it).requestAds { adManager, error ->
                    println("Errror : $error")
                    adManager ?: return@requestAds

                    adManager.setListener(object : AdManagerListener {
                        override fun onEventErrorReceived(
                            adManager: AdManager, ad: AdData?, error: Error
                        ) = Unit

                        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
                            if (event.type == AdEvent.Type.State.AllAdsCompleted) {
                                _playingState.tryEmit(PlayState.STOPPED)
                            }
                            if (event.type == AdEvent.Type.State.DidStartPlaying) {
                                _playingState.tryEmit(PlayState.PLAYING)
                            }
                            if (event.type == AdEvent.Type.State.DidPausePlaying) {
                                _playingState.tryEmit(PlayState.PAUSED)
                            }
                            if (event.type == AdEvent.Type.State.DidResumePlaying) {
                                _playingState.tryEmit(PlayState.PLAYING)
                            }
                        }
                    })

                    currentManager = adManager
                    adManager.prepare()
                    adManager.play()
                }

            }
    }

    private fun requestVideoAd(){
        AdswizzAdRequest.Builder()
            .withZones(listOf(AdswizzAdZone(zoneId ="17161")))
            .withServer("demo.deliveryengine.adswizz.com").build {
                AdRequestConnection(it).requestAds { adManager, _ ->
                    adManager ?: return@requestAds

                    adManager.adManagerSettings = AdManagerSettings.Builder().videoViewId(123).build()
                    adManager.setListener(object : AdManagerListener {
                        override fun onEventErrorReceived(
                            adManager: AdManager, ad: AdData?, error: Error
                        ) = Unit

                        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
                            if (event.type == AdEvent.Type.State.AllAdsCompleted) {
                                _playingState.tryEmit(PlayState.STOPPED)
                            }
                            if (event.type == AdEvent.Type.State.DidStartPlaying) {
                                _playingState.tryEmit(PlayState.PLAYING)
                            }
                            if (event.type == AdEvent.Type.State.DidPausePlaying) {
                                _playingState.tryEmit(PlayState.PAUSED)
                            }
                            if (event.type == AdEvent.Type.State.DidResumePlaying) {
                                _playingState.tryEmit(PlayState.PLAYING)
                            }
                        }
                    })

                    currentManager = adManager
                    adManager.prepare()
                    adManager.play()
                }

            }
    }

    fun addLog(companionViewTag: String, message: String) =
        viewModelScope.launch {
            val newList =
                _logs.value.toMutableList().also { it.add("Companion:$companionViewTag $message") }
            _logs.emit(newList)
        }

    fun onResourceSelected(resourceType: String) {
        zoneIdAndCompanion = if (resourceType.contains("HtmlResource", ignoreCase = true)) {
            _selectedResourceText.tryEmit("HtmlResource")
            "12561" to 12562
        } else {
            _selectedResourceText.tryEmit("Static Resource")
            Pair("17862", 17863)
        }
        currentManager?.reset()
        currentManager = null
    }
}