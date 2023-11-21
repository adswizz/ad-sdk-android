package com.adswizz.universalplayer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.adswizz.universalplayer.models.MediaData
import com.adswizz.universalplayer.utils.MusicServiceConnections

class MainActivityModel {

    interface ItemClickInterface{
        fun onItemClick(item: MediaData)
    }

    private var listener: ItemClickInterface? = null

    val rootMediaId: LiveData<String?> =
        MusicServiceConnections.isConnected.map { isConnected ->
            if (isConnected) {
                MusicServiceConnections.rootMediaId
            } else {
                null
            }
        }

    fun setInterface(callback: ItemClickInterface?){
        listener = callback
    }


    fun mediaItemClicked(item: MediaData){
        listener?.onItemClick(item)
    }
}