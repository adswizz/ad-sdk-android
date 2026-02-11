package com.adswizz.basicadstreamplayeradaptersample

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.adswizz.common.AdMetadataItem
import com.adswizz.common.AdStreamPlayerAdapter
import com.adswizz.player.toAdMetadataList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@UnstableApi
class ExternalAdStreamPlayerAdapter(context: Context) : AdStreamPlayerAdapter, Player.Listener {

    val player = ExoPlayer.Builder(context).build()

    override val currentPlayhead: Duration
        get() = player.currentPosition.toDuration(DurationUnit.MILLISECONDS)

    override val metadataSharedFlow: MutableSharedFlow<List<AdMetadataItem>> =
        MutableSharedFlow()

    init {
        player.addListener(this)
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    fun loadStream(url: Uri){
        player.addMediaItem(MediaItem.fromUri(url))
    }


    override fun onMetadata(metadata: Metadata) {
        scope.launch { metadataSharedFlow.emit(metadata.toAdMetadataList()) }
    }

    fun cleanup(){
        player.release()
        scope.cancel()
    }
}
