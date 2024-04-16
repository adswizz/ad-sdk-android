package com.adswizz.basicsampleext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ad.core.adBaseManager.AdEvent
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.sdk.extensions.streaming.observeAdEvents
import com.adswizz.sdk.extensions.streaming.observeStreamEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerSideViewModel(application: Application) : AndroidViewModel(application)  {
    private var adStreamManager: AdswizzAdStreamManager? = null

    val liveState = MutableLiveData<String>()

    val SERVER_STREAMING_URL = "http://sdk.mobile.streaming.adswizz.com:8500/shakeDemo"

    private var adManagerState =  AdManagerState.Unknown


    fun setup() {
        adStreamManager = AdswizzAdStreamManager(null)
        viewModelScope.launch {
            val manager = adStreamManager ?: return@launch
            manager.observeAdEvents(viewModelScope)
                .mapNotNull { it.value }
                .collect { adEvent ->
                    withContext(Dispatchers.Main) {
                        adManagerState = when (adEvent.type) {
                            AdEvent.Type.State.ReadyForPlay ->
                                AdManagerState.ReadyForPlay

                            AdEvent.Type.State.DidStartPlaying ->
                                AdManagerState.Playing

                            AdEvent.Type.State.DidPausePlaying ->
                                AdManagerState.Paused

                            AdEvent.Type.State.AllAdsCompleted ->
                                AdManagerState.AllAdsCompleted

                            else -> AdManagerState.Unknown
                        }
                    }
                }
        }

        viewModelScope.launch {
            val manager = adStreamManager ?: return@launch
            manager.observeStreamEvents(this)
                .collect { res ->
                    withContext(Dispatchers.Main) {
                        liveState.value = res.toString()
                    }
                }
        }
    }

    fun resume(){
        println("*** resume stream $adManagerState")
        if (adManagerState == AdManagerState.Paused) {
            adStreamManager?.resume()
        }
    }

    fun reset(){
        adStreamManager?.stop()
    }

    fun play(){
        if ( adManagerState == AdManagerState.Paused ){
            adStreamManager?.resume()
        }
        else {
            adStreamManager?.play(SERVER_STREAMING_URL)
        }
    }

    fun pause(){
        adStreamManager?.pause()
    }
}