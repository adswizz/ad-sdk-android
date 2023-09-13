package com.georgedum.adswizzuniversalplayer.viewmodels

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.georgedum.adswizzuniversalplayer.models.MediaData
import com.georgedum.adswizzuniversalplayer.utils.MusicServiceConnections

class ListMediaFragmentModel(private val mediaId: String) {

    private val _mediaItems = MutableLiveData<List<MediaData>>()
    val mediaItems: LiveData<List<MediaData>> = _mediaItems

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: List<MediaBrowserCompat.MediaItem>) {
            val itemsList = children.map { child ->
                val subtitle = child.description.subtitle ?: ""
                MediaData(
                    child.mediaId!!,
                    child.description.title.toString(),
                    subtitle.toString(),
                    "",
                    child.description.iconUri ?: Uri.parse(""),
                    child.isBrowsable
                )
            }
            _mediaItems.postValue(itemsList)
        }
    }

    init{
        MusicServiceConnections.subscribe(mediaId, subscriptionCallback)
    }

}