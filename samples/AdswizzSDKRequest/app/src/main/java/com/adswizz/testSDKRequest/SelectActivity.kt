package com.adswizz.testSDKRequest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.adswizz.testSDKRequest.data.City
import com.adswizz.testSDKRequest.data.Coordinates
import com.adswizz.testSDKRequest.data.Request
import com.adswizz.testSDKRequest.data.databaseCities
import com.adswizz.testSDKRequest.databinding.ActivityMainBinding
import com.adswizz.testSDKRequest.databinding.SelectActivityBinding

class SelectActivity: AppCompatActivity() {
    private lateinit var binding: SelectActivityBinding

    private var selectedCity = ""
    private var selectedCoordinates : Coordinates? = null

    private var items: MutableList<Item> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var map = mutableMapOf<String, MutableList<City>>()
        databaseCities?.forEach {
            if ( map.containsKey(it.cou_name_en)){
                map[it.cou_name_en]?.add(it)
            }
            else{
                map[it.cou_name_en] = mutableListOf(it)
            }
        }

        map.forEach {entry ->
            items.add(Item(entry.key, true))
            entry.value.forEach { city->
                items.add( Item(city.name, false) {
                    selectedCity = it
                    selectedCoordinates = city.coordinates
                    binding.title.text = "$it ( ${city.coordinates.lat} x ${city.coordinates.lon} )"
                    Request.locationLive.postValue(Request.LocationConfig(it, city.coordinates))
                })
            }
        }

        val adapter = ItemsAdapter(items)
        binding.list.adapter = adapter

        binding.select.setOnClickListener{
            finish()
        }
        //get the type
        //for the moment only locations
    }
}