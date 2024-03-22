package com.adswizz.basicsample

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adswizz.core.podcast.AdswizzAdPodcastManager
import com.adswizz.sdk.extensions.podcast.AdswizzPodcastEvent
import com.adswizz.sdk.extensions.podcast.observeAdEvents
import com.adswizz.sdk.extensions.podcast.observePodcastEvents
import com.adswizz.sdk.extensions.result.AdswizzResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PodcastViewModel(application: Application) : AndroidViewModel(application)  {
    private var podcastManager: AdswizzAdPodcastManager? = null

    val liveState = MutableLiveData<String>()

    private val podcastUri: Uri =
        Uri.parse("http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3")

    private var jobListenAdEvents: Job? = null
    private var jobListenPodcastEvents: Job? = null

    fun setup(){
        podcastManager = AdswizzAdPodcastManager()

        jobListenAdEvents = viewModelScope.launch {
            podcastManager?.observeAdEvents()?.collect { res ->
                if (res is AdswizzResult.Success) {
                    withContext(Dispatchers.Main) {
                        liveState.value = res.value.type.value
                    }
                }
            }
        }

        jobListenPodcastEvents = viewModelScope.launch {
            podcastManager?.observePodcastEvents()?.collect{ res->
                if ( !(res is AdswizzPodcastEvent.OnPlayHeadReport)) {
                    withContext(Dispatchers.Main) {
                        liveState.value = res.toString()
                    }
                }
            }
        }
    }

    fun play(){
        podcastManager?.play(podcastUri)
    }

    fun pause(){
        podcastManager?.pause()
    }

    fun resume(){
        podcastManager?.resume()
    }

    fun reset(){
        podcastManager?.stop()
        runBlocking(Dispatchers.IO) {
            podcastManager?.cleanup()
            jobListenAdEvents?.cancel()
            jobListenPodcastEvents?.cancel()
        }
        setup()
    }
}