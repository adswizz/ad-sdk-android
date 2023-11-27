package com.adswizz.testSDKRequest.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.io.IOException

@JsonClass(generateAdapter = true)
data class Coordinates(
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class City(
    val name: String,
    val cou_name_en: String,
    val population: Int,
    val coordinates: Coordinates
)

@JsonClass(generateAdapter = true)
data class Cities(
    val cities: List<City>
)



var databaseCities: List<City>? = null
