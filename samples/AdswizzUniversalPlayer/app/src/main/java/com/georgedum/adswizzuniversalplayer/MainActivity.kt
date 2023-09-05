package com.georgedum.adswizzuniversalplayer

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.Manifest
import android.net.Uri
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.adswizz.sdk.AdswizzSDK
import com.georgedum.adswizzuniversalplayer.databinding.ActivityMainBinding
import com.georgedum.adswizzuniversalplayer.fragments.ListMediaFragment
import com.georgedum.adswizzuniversalplayer.fragments.PlayingItemFragment
import com.georgedum.adswizzuniversalplayer.models.MediaData
import com.georgedum.adswizzuniversalplayer.utils.Factory
import com.georgedum.adswizzuniversalplayer.utils.MusicServiceConnections
import com.georgedum.adswizzuniversalplayer.viewmodels.MainActivityModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val viewModel = Factory.getMainModelInstance()

    private val clickable = object : MainActivityModel.ItemClickInterface{
        override fun onItemClick(item: MediaData) {
            if ( item.browsable ){
                navigateToMediaItem(item.mediaId)
            }
            else{
                navigateToDetailScreen(item)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)
        MusicServiceConnections.initializeWithAppContext(this)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
            println("Show permission")
        }
        ActivityCompat.requestPermissions(this,  listOf(Manifest.permission.POST_NOTIFICATIONS).toTypedArray(), 1024)

        viewModel.rootMediaId.observe(this,
            Observer<String> { rootMediaId ->
                rootMediaId?.let { navigateToMediaItem(it) }
            })
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        //Uri.parse(null) - java.lang.NullPointerException
//        println("Uri nasol 1 : ${Uri.parse("http:/ceva")}")
//        println("Uri nasol 2 : ${Uri.parse("http/altceva")}")

//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
//        AdswizzSDK.initialize(applicationContext,null)
//        mediaBrowser = MediaBrowserCompat(
//            this,
//            ComponentName(this, AdswizzMediaBrowserService::class.java),
//            connectionCallbacks,
//            null )// optional Bundle


    }

    private fun navigateToMediaItem(mediaId: String) {
        val fragment = ListMediaFragment(mediaId)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragmentContainer, fragment, mediaId
        )
        transaction.addToBackStack(mediaId)
        transaction.commit()
    }

    private fun navigateToDetailScreen(item:MediaData){
        val fragment = PlayingItemFragment(item)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragmentContainer, fragment, item.mediaId
        )
        transaction.addToBackStack(item.mediaId)
        transaction.commit()

    }

    public override fun onStart() {
        super.onStart()
        viewModel.setInterface(clickable)
        //mediaBrowser.connect()
    }

    public override fun onStop() {
        super.onStop()
        viewModel.setInterface(null)
        // (see "stay in sync with the MediaSession")
        //MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        //mediaBrowser.disconnect()
    }

    fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)
        // Grab the view for the play/pause button


        // Display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            println("On Metadata Changed $metadata")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            println("On Playback Changed: $state")
        }
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
//            println("ConnectionCallback:: onConnected")
            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@MainActivity, // Context
                    token
                ).apply {
                    registerCallback(controllerCallback)
                }

                // Save the controller
                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            }
            println("Media browser root: ${mediaBrowser.root}")
            val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback(){
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    children.forEach {
                        println("Subscription: $parentId ${it.mediaId}")
                        if (it.isBrowsable){
                            println("Browsable: ${it.isBrowsable}")
                            it.mediaId?.let{ ID ->
                                mediaBrowser.subscribe(ID, this)
                            }
                        }
                    }
                    super.onChildrenLoaded(parentId, children)
                }

            }
            mediaBrowser.subscribe(mediaBrowser.root, subscriptionCallback)
            // Finish building the UI
            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            println("ConnectionCallback:: onConnectionSuspended")

            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            println("ConnectionCallback:: onConnectionFailed")

            // The Service has refused our connection
        }
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