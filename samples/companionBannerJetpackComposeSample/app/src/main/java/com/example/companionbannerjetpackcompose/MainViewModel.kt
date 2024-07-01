package com.example.companionbannerjetpackcompose

import androidx.lifecycle.ViewModel
import com.ad.core.adBaseManager.AdData
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adFetcher.AdRequestConnection
import com.ad.core.adManager.AdManager
import com.ad.core.adManager.AdManagerListener
import com.adswizz.core.adFetcher.AdswizzAdRequest
import com.adswizz.core.adFetcher.AdswizzAdZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlayState {
    PLAYING,
    PAUSED,
    STOPPED
}

class MainViewModel : ViewModel() {

    private val _playingState = MutableStateFlow(PlayState.STOPPED)
    val playingState = _playingState.asStateFlow()


    private var currentManager: AdManager? = null

    fun playOrPauseContent() {
        if (_playingState.value == PlayState.PLAYING){
            currentManager?.pause()
        }
        else if (_playingState.value == PlayState.PAUSED){
            currentManager?.resume()
        }
        else {
            requestCompanionAd()
        }
    }

    fun resetContent(){
        currentManager?.reset()
        currentManager = null
        requestCompanionAd()
    }

    private fun requestCompanionAd() {
        AdswizzAdRequest.Builder().withZones(setOf(AdswizzAdZone(zoneId = "17862")))
            .withCompanionZones("17863").withServer("demo.deliveryengine.adswizz.com").build {
                AdRequestConnection(it).requestAds { adManager, _ ->
                    adManager ?: return@requestAds

                    adManager.setListener(object : AdManagerListener {
                        override fun onEventErrorReceived(
                            adManager: AdManager, ad: AdData?, error: Error
                        ) = Unit

                        override fun onEventReceived(adManager: AdManager, event: AdEvent) {
                            if (event.type == AdEvent.Type.State.AllAdsCompleted){
                                _playingState.tryEmit(PlayState.STOPPED)
                            }
                            if (event.type == AdEvent.Type.State.DidStartPlaying){
                                _playingState.tryEmit(PlayState.PLAYING)
                            }
                            if (event.type == AdEvent.Type.State.DidPausePlaying){
                                _playingState.tryEmit(PlayState.PAUSED)
                            }
                            if (event.type == AdEvent.Type.State.DidResumePlaying){
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
}