package com.georgedum.adswizzuniversalplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.android.uamp.MediaItemAdapter
import com.georgedum.adswizzuniversalplayer.databinding.ListMediaLayoutBinding
import com.georgedum.adswizzuniversalplayer.utils.Factory
import com.georgedum.adswizzuniversalplayer.viewmodels.ListMediaFragmentModel

class ListMediaFragment(val mediaId:String) : Fragment()  {

    private lateinit var binding: ListMediaLayoutBinding

    private val listMediaFragmentModel = ListMediaFragmentModel(mediaId)

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