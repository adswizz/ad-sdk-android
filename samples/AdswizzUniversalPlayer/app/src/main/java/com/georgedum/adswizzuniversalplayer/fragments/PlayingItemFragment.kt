package com.georgedum.adswizzuniversalplayer.fragments

import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
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
    private var hasStarted = false
    private var playbackState = PlaybackStateCompat.STATE_NONE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayingitemLayoutBinding.inflate(inflater, container, false)
        binding.mediaButton.setOnClickListener{
            when (playbackState){
                PlaybackStateCompat.STATE_PAUSED -> MusicServiceConnections.transportControls.play()
                PlaybackStateCompat.STATE_STOPPED -> MusicServiceConnections.transportControls.playFromMediaId(
                    mediaItem.mediaId,
                    Bundle()
                )
                PlaybackStateCompat.STATE_NONE -> MusicServiceConnections.transportControls.playFromMediaId(
                    mediaItem.mediaId,
                    Bundle()
                )
                else -> {

                }
            }
            //MusicServiceConnections.transportControls.play()
        }
        binding.title.text = mediaItem.title
        binding.subtitle.text = mediaItem.subtitle
        binding.position.text = "00:00"
        binding.duration.text = "00:00"
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        MusicServiceConnections.playbackState.observe(viewLifecycleOwner){
            println("*** PlaybackState: $it ${it.extras?.getDouble("duration")}")
            val duration = (it.extras?.getDouble("duration") ?: 0).toLong()
            binding.position.text = String.format("%02d:%02d", it.position/60, it.position%60)
            binding.duration.text = String.format("%02d:%02d", duration/60, duration%60)
            playbackState = it.state
        }

        MusicServiceConnections.nowPlaying.observe(viewLifecycleOwner){
            //println("*** MediaMetaData: $it")
        }
        // Always true, but lets lint know that as well.
    }

    fun updateToMediaItem(media: MediaData){
        if (mediaItem.mediaId != media.mediaId){
            //do nothing
        }
    }

    override fun onStop() {
        println("**** FRAGMENT ONSTOP")
//        MusicServiceConnections.nowPlaying.removeObserver(viewLifecycleOwner)
        //MusicServiceConnections.transportControls.pause()
        super.onStop()
    }

    override fun onStart() {
        println("**** FRAGMENT ONSTART")
//        MusicServiceConnections.nowPlaying.removeObserver(this)
        //MusicServiceConnections.transportControls.play()
        super.onStart()
    }
}