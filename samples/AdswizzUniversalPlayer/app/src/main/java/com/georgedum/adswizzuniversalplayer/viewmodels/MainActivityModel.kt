package com.georgedum.adswizzuniversalplayer.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.georgedum.adswizzuniversalplayer.models.MediaData
import com.georgedum.adswizzuniversalplayer.utils.MusicServiceConnections

class MainActivityModel {

    interface ItemClickInterface{
        fun onItemClick(item: MediaData)
    }

    private var listener: ItemClickInterface? = null

    val rootMediaId: LiveData<String> =
        Transformations.map(MusicServiceConnections.isConnected) { isConnected ->
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