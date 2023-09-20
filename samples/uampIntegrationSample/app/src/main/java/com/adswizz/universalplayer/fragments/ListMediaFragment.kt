package com.adswizz.universalplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.adswizz.universalplayer.MediaItemAdapter
import com.adswizz.universalplayer.databinding.ListMediaLayoutBinding
import com.adswizz.universalplayer.utils.Factory
import com.adswizz.universalplayer.viewmodels.ListMediaFragmentModel

class ListMediaFragment() : Fragment()  {

    companion object{
        const val MEDIA_ID_ARG = "com.adswizz.universalplayer.fragments.ListMediaFragment.MEDIA_ID"
    }


    private var mediaId: String = ""

    private lateinit var binding: ListMediaLayoutBinding

    private lateinit var listMediaFragmentModel: ListMediaFragmentModel

    private val listAdapter = MediaItemAdapter { clickedItem ->
        Factory.getMainModelInstance().mediaItemClicked(clickedItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListMediaLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaId = arguments?.getString(MEDIA_ID_ARG) ?: return

        listMediaFragmentModel = ListMediaFragmentModel(mediaId)
        // Always true, but lets lint know that as well.

        listMediaFragmentModel.mediaItems.observe(viewLifecycleOwner,
            Observer { list ->
//                binding.loadingSpinner.visibility =
//                    if (list?.isNotEmpty() == true) View.GONE else View.VISIBLE
                listAdapter.submitList(list)
            })


        // Set the adapter
        binding.mediaList.adapter = listAdapter
    }
}

