package com.georgedum.adswizzuniversalplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.georgedum.adswizzuniversalplayer.databinding.ListMediaLayoutBinding
import com.georgedum.adswizzuniversalplayer.databinding.PlayingitemLayoutBinding
import com.georgedum.adswizzuniversalplayer.models.MediaData
import com.georgedum.adswizzuniversalplayer.utils.MusicServiceConnections

class PlayingItemFragment(val mediaItem: MediaData) : Fragment() {
    private lateinit var binding: PlayingitemLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayingitemLayoutBinding.inflate(inflater, container, false)
        binding.mediaButton.setOnClickListener{
            MusicServiceConnections.transportControls.playFromMediaId(mediaItem.mediaId, Bundle())
        }
        binding.title.text = mediaItem.title
        binding.subtitle.text = mediaItem.subtitle
        binding.position.text = "00:00"
        binding.duration.text = "00:00"
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        MusicServiceConnections.playbackState.observe(requireActivity()){
            println("*** PlaybackState: $it")
        }

        MusicServiceConnections.nowPlaying.observe(requireActivity()){
            println("*** MediaMetaData: $it")
        }
        // Always true, but lets lint know that as well.
    }

    override fun onStop() {
        MusicServiceConnections.transportControls.pause()
        super.onStop()
    }
}