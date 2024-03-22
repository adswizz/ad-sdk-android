package com.adswizz.basicsample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.adswizz.core.streaming.AdswizzAdStreamManager
import com.adswizz.sdk.extensions.result.AdswizzResult
import com.adswizz.sdk.extensions.streaming.observeAdEvents
import com.adswizz.sdk.extensions.streaming.observeStreamEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerSideViewModel(application: Application) : AndroidViewModel(application)  {
    private var adStreamManager: AdswizzAdStreamManager? = null

    val liveState = MutableLiveData<String>()

    val SERVER_STREAMING_URL = "http://sdk.mobile.streaming.adswizz.com:8500/shakeDemo"

    private var adManagerState =  AdManagerState.Unknown


    fun setup(){
        adStreamManager = AdswizzAdStreamManager(null)
        viewModelScope.launch {
            adStreamManager?.observeAdEvents()?.collect { res ->
                if (res is AdswizzResult.Success) {
                    withContext(Dispatchers.Main) {
                        when (res.value.type.value){
                            "ready_for_play" -> adManagerState = AdManagerState.ReadyForPlay
                            "did_start_playing" -> adManagerState = AdManagerState.Playing
                            "all_ads_completed" -> adManagerState = AdManagerState.AllAdsCompleted
                            "did_pause_playing" -> adManagerState = AdManagerState.Paused
                            else -> Unit
                        }
                        liveState.value = res.value.type.value
                    }
                }
            }
        }
        viewModelScope.launch {
            adStreamManager?.observeStreamEvents()?.collect{  res ->
                withContext(Dispatchers.Main) {
                    liveState.value = res.toString()
                }
            }
        }
    }

    fun resume(){
        if (adManagerState == AdManagerState.Paused) {
            adStreamManager?.resume()
        }
    }

    fun reset(){
        adStreamManager?.stop()
    }

    fun play(){
        adStreamManager?.play(SERVER_STREAMING_URL)
    }

    fun pause(){
        adStreamManager?.pause()
    }
}