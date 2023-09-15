package com.georgedum.adswizzuniversalplayer.fragments

import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.georgedum.adswizzuniversalplayer.R
import com.georgedum.adswizzuniversalplayer.databinding.ListMediaLayoutBinding
import com.georgedum.adswizzuniversalplayer.databinding.PlayingitemLayoutBinding
import com.georgedum.adswizzuniversalplayer.models.MediaData
import com.georgedum.adswizzuniversalplayer.utils.MusicServiceConnections

class PlayingItemFragment(val mediaItem: MediaData) : Fragment() {
    private lateinit var binding: PlayingitemLayoutBinding
    private var playbackState = PlaybackStateCompat.STATE_NONE
    private var started = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayingitemLayoutBinding.inflate(inflater, container, false)
        binding.mediaButton.setOnClickListener{
            when (playbackState){
                PlaybackStateCompat.STATE_PAUSED -> {
                    if ( started ){
                        MusicServiceConnections.transportControls.playFromMediaId(
                            mediaItem.mediaId,
                            Bundle()
                        )
                    }
                    else {
                        MusicServiceConnections.transportControls.play()
                    }
                    binding.mediaButton.setImageResource(R.drawable.ic_pause_black_24dp)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    MusicServiceConnections.transportControls.playFromMediaId(
                        mediaItem.mediaId,
                        Bundle()
                    )
                    binding.mediaButton.setImageResource(R.drawable.ic_pause_black_24dp)
                }
                PlaybackStateCompat.STATE_NONE -> {
                    MusicServiceConnections.transportControls.playFromMediaId(
                        mediaItem.mediaId,
                        Bundle()
                    )
                    binding.mediaButton.setImageResource(R.drawable.ic_pause_black_24dp)
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    started = false
                    MusicServiceConnections.transportControls.pause()
                    binding.mediaButton.setImageResource(R.drawable.ic_play_arrow_black_24dp)

                }
                else -> {

                }
            }
        }
        if (mediaItem.title.isNotEmpty()) {
            binding.title.text = mediaItem.title
        }
        if (mediaItem.subtitle.isNotEmpty()) {
            binding.subtitle.text = mediaItem.subtitle
        }
        binding.position.text = "00:00"
        binding.duration.text = "00:00"
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        MusicServiceConnections.playbackState.observe(viewLifecycleOwner){
            val duration = (it.extras?.getDouble("duration") ?: 0).toLong()
            binding.position.text = String.format("%02d:%02d", it.position/60, it.position%60)
            binding.duration.text = String.format("%02d:%02d", duration/60, duration%60)
            playbackState = it.state
        }

        MusicServiceConnections.nowPlaying.observe(viewLifecycleOwner){
            it.getText(MediaMetadataCompat.METADATA_KEY_TITLE)?.let { str ->
                if (str.isNotEmpty()){ binding.title.text = str }
            }
            it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)?.let{ str ->
                if (str.isNotEmpty()){ binding.subtitle.text = str }
            }
        }

        MusicServiceConnections.sessionEvents.observe(viewLifecycleOwner){
        }
        // Always true, but lets lint know that as well.
    }

    override fun onDestroy() {
        MusicServiceConnections.transportControls.pause()
        super.onDestroy()
    }
    fun updateToMediaItem(media: MediaData){
        if (mediaItem.mediaId != media.mediaId){
            //do nothing
        }
    }
}