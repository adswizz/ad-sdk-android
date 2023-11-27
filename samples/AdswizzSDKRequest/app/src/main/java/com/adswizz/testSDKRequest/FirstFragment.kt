package com.adswizz.testSDKRequest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BuildCompat
import com.ad.core.AdvertisementSettings
import com.adswizz.sdk.BuildConfig
import com.adswizz.testSDKRequest.data.Request
import com.adswizz.testSDKRequest.databinding.FragmentMainBinding
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val ifaMap = listOf(
        Pair(AdvertisementSettings.IfaType.GOOGLE_PLAY_SERVICES , "093201-dkasjdkla-fbhfrjrjhjrkhjkr-83393"),
        Pair(AdvertisementSettings.IfaType.AMAZON_DEVICE , "klsd0-sadfsa-98ddsj90caja-83393"),
        Pair(AdvertisementSettings.IfaType.RANDOM , UUID.randomUUID().toString()),
        Pair(AdvertisementSettings.IfaType.UNKNOWN , "")
    )

    private var ifaMapIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateDefault()
        binding.apply {
            btnSelectLocation.setOnClickListener{
                (activity as? MainActivity)?.selectLocation()
            }
            btnRequest.setOnClickListener {
                populateRequest()
                (activity as? MainActivity)?.makeRequest()
            }
            btnClientside.setOnClickListener{
                btnClientside.setTextColor(0xFFFF0000.toInt())
                btnServerside.setTextColor(0xFFFFFFFF.toInt())
                btnPodcasting.setTextColor(0xFFFFFFFF.toInt())
                clientSettings.visibility = View.VISIBLE
                clientSettingsLabel.visibility = View.VISIBLE
                afrSettings.visibility = View.GONE
                afrSettingsLabel.visibility = View.GONE
                Request.requestType = "ClientSide"
            }
            btnServerside.setOnClickListener {
                btnClientside.setTextColor(0xFFFFFFFF.toInt())
                btnServerside.setTextColor(0xFFFF0000.toInt())
                btnPodcasting.setTextColor(0xFFFFFFFF.toInt())
                clientSettings.visibility = View.GONE
                clientSettingsLabel.visibility = View.GONE
                afrSettings.visibility = View.VISIBLE
                afrSettingsLabel.visibility = View.VISIBLE
                Request.requestType = "ServerSide"
            }
            btnPodcasting.setOnClickListener {
                btnClientside.setTextColor(0xFFFFFFFF.toInt())
                btnServerside.setTextColor(0xFFFFFFFF.toInt())
                btnPodcasting.setTextColor(0xFFFF0000.toInt())
                clientSettings.visibility = View.GONE
                clientSettingsLabel.visibility = View.GONE
                afrSettings.visibility = View.VISIBLE
                afrSettingsLabel.visibility = View.VISIBLE
                Request.requestType = "PodcastingSide"
            }

            btnRegenSessId.setOnClickListener {
                val sessid = "${System.currentTimeMillis()}.${abs(Random.nextLong() % 999999)}"
                sessionId.setText(sessid)
            }

            btnCycle.setOnClickListener{
                val ifaPair = ifaMap.get(ifaMapIndex%ifaMap.size)
//                val ifa = if (ifaPair.second.length>15){
//                    ifaPair.second.substring(0..14) + "..."
//                }
//                else{
//                    ifaPair.second
//                }
//                val ifaString = "$ifa"
                this.ifa.setText(ifaPair.second)
                this.ifatype.setText(ifaPair.first.toString())
                ifaMapIndex++
            }
        }

        Request.locationLive.observe(viewLifecycleOwner){
            binding.location.setText(it.location)
        }
    }

    private fun populateRequest() : Boolean{
        Request.serverUrl = binding.url.text.toString()
        //test url is ok

        Request.sdkVersion = binding.sdkVersion.text.toString()
        Request.playerId = binding.playerId.text.toString()
        Request.sessionId = binding.sessionId.text.toString()
        Request.ifaString = binding.ifa.text.toString()
        Request.ifaTypeString = binding.ifatype.text.toString()

        Request.zoneId = binding.clientZoneId.text.toString()
        return true
    }

    private fun populateDefault(){
        Request.requestType = "ClientSide"

        binding.apply {
            val sessid = "${System.currentTimeMillis()}.${abs(Random.nextLong() % 999999)}"
            this.sessionId.setText(sessid)

            val ifaPair = ifaMap.get(ifaMapIndex%ifaMap.size)
            this.ifa.setText(ifaPair.second)
            this.ifatype.setText(ifaPair.first.toString())
            ifaMapIndex++


            playerId.setText("ArtemisKotlinPlayerId")
            sdkVersion.setText(BuildConfig.VERSION_NAME)


            /*
                set default client- side
             */
            btnClientside.setTextColor(0xFFFF0000.toInt())
            clientSettings.visibility = View.VISIBLE
            clientSettingsLabel.visibility = View.VISIBLE
            afrSettings.visibility = View.GONE
            afrSettingsLabel.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}