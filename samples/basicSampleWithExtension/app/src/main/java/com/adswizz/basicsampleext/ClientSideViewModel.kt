package com.adswizz.basicsampleext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ad.core.adManager.AdManager
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.sdk.extensions.admanager.observeAdEvents
import com.adswizz.sdk.extensions.adrequest.requestAdManagerWith
import com.adswizz.sdk.extensions.result.AdswizzResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientSideViewModel(application: Application) : AndroidViewModel(application) {

    val SERVER_URL = "demo.deliveryengine.adswizz.com"
    val ZONE_ID = "13396"

    private var adManager: AdManager? = null

    val liveState = MutableLiveData<String>()

    private var adManagerState =  AdManagerState.Unknown

    fun setupManager(){
        requestAdManagerWith({
            zones = setOf(AdswizzAdZone(ZONE_ID))
            server = SERVER_URL
        } ){
            if ( it.value != null ){
                adManager = it.value
                adManager?.prepare()
                viewModelScope.launch {
                    adManager?.observeAdEvents()?.collect{  res ->
                        if (res is AdswizzResult.Success){
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

            }
        }
    }

    fun play(){
        //depending on state
        when (adManagerState) {
            AdManagerState.Unknown, AdManagerState.ReadyForPlay -> {
                adManager?.play()
            }

            AdManagerState.Paused -> {
                adManager?.resume()
            }

            AdManagerState.AllAdsCompleted -> {
                adManager?.reset()
                adManager?.prepare()
                adManager?.play()
            }
            else -> Unit
        }
    }

    fun resume(){
        if (adManagerState == AdManagerState.Paused) {
            adManager?.resume()
        }
    }

    fun pause(){
        adManager?.pause()
    }

    fun reset(){
        adManager?.reset()
        adManager?.prepare()
        adManagerState = AdManagerState.Unknown
    }
}