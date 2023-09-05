package com.georgedum.adswizzuniversalplayer.models

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class MediaData(val mediaId: String,
                     val title: String,
                     val subtitle: String,
                     val albumArtUri: Uri,
                     val browsable: Boolean){

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<MediaData>() {
            override fun areItemsTheSame(
                oldItem: MediaData,
                newItem: MediaData
            ): Boolean =
                oldItem.mediaId == newItem.mediaId

            override fun areContentsTheSame(oldItem: MediaData, newItem: MediaData) =
                oldItem.mediaId == newItem.mediaId && oldItem.title == newItem.title && oldItem.browsable == newItem.browsable

        }
    }
}
