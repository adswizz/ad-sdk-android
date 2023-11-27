package com.adswizz.testSDKRequest

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.adswizz.testSDKRequest.data.Cities
import com.adswizz.testSDKRequest.data.Request
import com.adswizz.testSDKRequest.data.databaseCities
import com.adswizz.testSDKRequest.databinding.ActivityMainBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<Cities> = moshi.adapter(Cities::class.java)

        val jsonConfig = try {
            assets
                .open("cities.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (e: IOException) {
            ""
        }

        databaseCities = try {
            adapter.fromJson(jsonConfig)?.cities?.filter{
                it.population > 100000 && it.cou_name_en != "Russian Federation"
            }
        } catch (e: IOException) {
            null
        }

        println("*** Database cities: ${databaseCities?.size}")
    }

    fun selectLocation(){
        val intent = Intent(applicationContext, SelectActivity::class.java)
        intent.putExtra("Type", "Location")
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun makeRequest(){
        val intent = Intent(applicationContext, PlayerActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}