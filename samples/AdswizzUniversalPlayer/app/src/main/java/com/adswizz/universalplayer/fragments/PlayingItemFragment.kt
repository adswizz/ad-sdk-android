package com.adswizz.universalplayer.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adswizz.universalplayer.R
import com.adswizz.universalplayer.databinding.PlayingitemLayoutBinding
import com.adswizz.universalplayer.models.MediaData
import com.adswizz.universalplayer.utils.MusicServiceConnections

class PlayingItemFragment() : Fragment() {
    companion object{
        const val MEDIA_ITEM_ARG = "com.adswizz.universalplayer.fragments.PlayingItemFragment.MEDIA_ITEM"
    }

    private lateinit var binding: PlayingitemLayoutBinding
    private lateinit var mediaItem: MediaData
    private var playbackState = PlaybackStateCompat.STATE_NONE
    private var started = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlayingitemLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bundle = arguments?.getBundle(MEDIA_ITEM_ARG) ?: return
        mediaItem = MediaData(bundle.getString("mediaId") ?: "",
            bundle.getString("title") ?: "",
            bundle.getString("subtitle") ?: "",
            bundle.getString("uri") ?: "",
            Uri.parse(bundle.getString("albumArtUri") ?: ""),
            bundle.getBoolean("browsable"))
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
                    MusicServiceConnections.transportControls.pause()
                    binding.mediaButton.setImageResource(R.drawable.ic_play_arrow_black_24dp)

                }
                else -> {

                }
            }
            started = false
        }
        if (mediaItem.title.isNotEmpty()) {
            binding.title.text = mediaItem.title
        }
        if (mediaItem.subtitle.isNotEmpty()) {
            binding.subtitle.text = mediaItem.subtitle
        }
        binding.position.text = "00:00"
        binding.duration.text = "00:00"

        MusicServiceConnections.playbackState.observe(viewLifecycleOwner){
            val duration = (it.extras?.getDouble("duration") ?: 0).toLong()
            binding.position.text = String.format("%02d:%02d", it.position/60, it.position%60)
            binding.duration.text = String.format("%02d:%02d", duration/60, duration%60)
            playbackState = it.state
        }

        MusicServiceConnections.nowPlaying.observe(viewLifecycleOwner){
            if ( !started) {
                it.getText(MediaMetadataCompat.METADATA_KEY_TITLE)?.let { str ->
                    if (str.isNotEmpty()) {
                        binding.title.text = str
                    }
                }
                it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)?.let { str ->
                    if (str.isNotEmpty()) {
                        binding.subtitle.text = str
                    }
                }
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