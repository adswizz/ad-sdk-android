package com.adswizz.basicsampleext

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.adswizz.basicsampleext.databinding.ActivityMainBinding
import com.adswizz.sdk.AdswizzSDK
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

// Basic app shows minimal client side functionality

enum class SelectedManager{
    Client,
    Server,
    Podcast
}


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var selectedManager = SelectedManager.Client


    private val clientSideViewModel: ClientSideViewModel by viewModels()
    private val serverSideModel: ServerSideViewModel by viewModels()
    private val podcastModel: PodcastViewModel by viewModels()

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val window = this.window
        getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_statusbar_color)

        val scroll = ScrollingMovementMethod()
        binding.appLogs.movementMethod = scroll

        AdswizzSDK.initialize(this){
            clientSideViewModel.setupManager()
            serverSideModel.setup()
            podcastModel.setup()
        }

        binding.sdkVersion.text = "${getString(R.string.adswizz_version_prefix)}${com.adswizz.sdk.BuildConfig.VERSION_NAME}"

        clientSideViewModel.liveState.observe(this){
            binding.appLogs.addEntry("Event: $it")
        }

        serverSideModel.liveState.observe(this){
            binding.appLogs.addEntry("Stream event: $it")
        }

        podcastModel.liveState.observe(this){
            binding.appLogs.addEntry("Podcast event: $it")
        }

        binding.btnSelectModule.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text){
                    "ClientSide" -> {
                        selectedManager = SelectedManager.Client
                        clientSideViewModel.resume()
                    }
                    "ServerSide" -> {
                        selectedManager = SelectedManager.Server
                        serverSideModel.resume()
                    }
                    "Podcast" -> {
                        selectedManager = SelectedManager.Podcast
                        podcastModel.resume()
                    }
                    else -> Unit
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab?.text){
                    "ClientSide" -> clientSideViewModel.pause()
                    "ServerSide" -> serverSideModel.pause()
                    "Podcast" ->podcastModel.pause()
                    else -> Unit
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                when (tab?.text){
                    "ClientSide" -> println("**** Reselect ClientSide ****")
                    "ServerSide" -> println("**** Reselect ServerSide ****")
                    "Podcast" -> println("**** Reselect Podcast ****")
                    else -> Unit
                }
            }

        })
        //Build the Ad Request with the needed parameters
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuPlay -> {
                if ( selectedManager == SelectedManager.Server){
                    serverSideModel.play()
                }
                else if ( selectedManager == SelectedManager.Podcast){
                    podcastModel.play()
                }
                else {
                    clientSideViewModel.play()
                }
            }
            R.id.menuReplay -> {
                if ( selectedManager == SelectedManager.Client){
                    clientSideViewModel.reset()
                }
                else if (selectedManager == SelectedManager.Podcast){
                    podcastModel.reset()
                }
                binding.appLogs.text = ""
                binding.appLogs.scrollTo(0,0)
            }
            R.id.menuPause -> {
                if ( selectedManager == SelectedManager.Server) {
                    serverSideModel.pause()
                }
                else if ( selectedManager == SelectedManager.Podcast) {
                    podcastModel.pause()
                }
                else {
                    clientSideViewModel.pause()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        AdswizzSDK.cleanup()
        super.onDestroy()
    }

    fun TextView.addEntry(entry: String) {
        val ssBuilder = SpannableStringBuilder(this.text)

        val timeStamp: String = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
        val entryLine = "$timeStamp $entry\n"

        ssBuilder.append(entryLine)

        val spanStart = ssBuilder.length - entryLine.length
        val spanEnd = spanStart + timeStamp.length
        ssBuilder.setSpan(RelativeSizeSpan(0.7f), spanStart, spanEnd, 0)
        ssBuilder.setSpan(ForegroundColorSpan(Color.DKGRAY), spanStart, spanEnd, 0)
        ssBuilder.setSpan(TypefaceSpan("sans-serif-condensed"), spanStart, spanEnd, 0)

        this.text = ssBuilder
    }
}
