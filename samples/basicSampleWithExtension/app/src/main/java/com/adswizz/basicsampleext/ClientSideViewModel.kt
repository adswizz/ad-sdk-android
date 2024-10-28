package com.adswizz.basicsampleext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ad.core.adBaseManager.AdEvent
import com.ad.core.adManager.AdManager
import com.adswizz.core.adFetcher.AdswizzAdZone
import com.adswizz.sdk.extensions.admanager.actions.AdManagerAction
import com.adswizz.sdk.extensions.admanager.actions.postAction
import com.adswizz.sdk.extensions.admanager.observeAdEvents
import com.adswizz.sdk.extensions.adrequest.requestAdManagerWith
import com.adswizz.sdk.extensions.result.AdswizzResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientSideViewModel(application: Application) : AndroidViewModel(application) {

    val SERVER_URL = "demo.deliveryengine.adswizz.com"
    val ZONE_ID = "17862"

    private var adManager: AdManager? = null

    val liveState = MutableLiveData<String>()

    private var adManagerState =  AdManagerState.Unknown

    fun setupManager(){

        viewModelScope.launch {
            val manager =  requestAdManagerWith {
                zones = setOf(AdswizzAdZone(ZONE_ID))
                server = SERVER_URL
            }.value ?: return@launch
            adManager = manager


            manager.observeAdEvents(this)
                .mapNotNull { it.value }
                .onStart { manager.postAction(AdManagerAction.Prepare) }
                .collect { res ->
                    withContext(Dispatchers.Main) {
                        when (res.type) {
                            AdEvent.Type.State.ReadyForPlay -> adManagerState = AdManagerState.ReadyForPlay
                            AdEvent.Type.State.DidStartPlaying -> adManagerState = AdManagerState.Playing
                            AdEvent.Type.State.AllAdsCompleted -> adManagerState = AdManagerState.AllAdsCompleted
                            AdEvent.Type.State.DidPausePlaying -> adManagerState = AdManagerState.Paused
                            else -> Unit
                        }
                        liveState.value = res.type.value
                    }
            }
        }
    }

    fun play(){
        //depending on state
        viewModelScope.launch {
            val adManager = adManager ?: return@launch

            when (adManagerState) {
                AdManagerState.Unknown, AdManagerState.ReadyForPlay ->
                    adManager.postAction(AdManagerAction.Play)

                AdManagerState.Paused -> adManager.postAction(AdManagerAction.Resume)

                AdManagerState.Playing ->
                    adManager.postAction(AdManagerAction.Pause)

                AdManagerState.AllAdsCompleted -> {
                    adManager.postAction(AdManagerAction.Reset)
                    adManager.postAction(AdManagerAction.Prepare)
                    adManager.postAction(AdManagerAction.Play)
                }
            }
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