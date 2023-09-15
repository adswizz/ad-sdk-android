package com.adswizz.universalplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import android.view.Menu
import android.view.MenuItem
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.adswizz.universalplayer.databinding.ActivityMainBinding
import com.adswizz.universalplayer.fragments.ListMediaFragment
import com.adswizz.universalplayer.fragments.PlayingItemFragment
import com.adswizz.universalplayer.models.MediaData
import com.adswizz.universalplayer.utils.Factory
import com.adswizz.universalplayer.utils.MusicServiceConnections
import com.adswizz.universalplayer.viewmodels.MainActivityModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val viewModel = Factory.getMainModelInstance()

    private var currentFragment: Fragment? = null

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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Adswizz UAMP (SDK ${BuildConfig.SDK_VERSION})"

        MusicServiceConnections.initializeWithAppContext(this)
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            println("Show permission")
        }
//        ActivityCompat.requestPermissions(
//            this,
//            listOf(Manifest.permission.POST_NOTIFICATIONS).toTypedArray(),
//            1024
//        )

        viewModel.rootMediaId.observe(this,
            Observer<String> { rootMediaId ->
                rootMediaId?.let { navigateToMediaItem(it) }
            })

        MusicServiceConnections.playbackState.observe(this) {
            if (it.state == PlaybackState.STATE_PLAYING) {
                MusicServiceConnections.nowPlaying.value?.let {
                    if (currentFragment is PlayingItemFragment) {
                        (currentFragment as PlayingItemFragment).updateToMediaItem(
                            MediaData(
                                it.getString(
                                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID
                                ),
                                it.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                                it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
                                "",
                                Uri.parse(""),
                                false
                            )
                        )
                    } else {
                        navigateToDetailScreen(
                            MediaData(
                                it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                                it.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                                it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
                                "",
                                Uri.parse(""),
                                false
                            )
                        )
                    //update interface to currentPlayingItem
                    }
                }
                //if ( currentFragment is PlayingItemFragment)
            }
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

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            requestPermissions()
        }, 0L)

    }

    private fun requestPermissions() {

        val permissionsList = mutableListOf(
            // artemis only
            Manifest.permission.WAKE_LOCK,
            // permission needed by sdk for decorate & permission action
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.WRITE_CALENDAR,
            // sdk
            Manifest.permission.INTERNET, Manifest.permission.VIBRATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        //app only
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            permissionsList.add(Manifest.permission.FOREGROUND_SERVICE)

        val neededPermissionsList = mutableListOf<String>()

        for (i in 0 until permissionsList.count()) {
            if (ContextCompat.checkSelfPermission(this, permissionsList[i]) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsList[i])) {
                    // Show an explanation to the user! After the user
                    // sees the explanation, try again to request the permission.
                    Snackbar.make(
                        binding.root,
                        "The app needs the permission ${permissionsList[i]}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    neededPermissionsList.add(permissionsList[i])
                }
            }
        }

        if (neededPermissionsList.size > 0) {
            val array = arrayOfNulls<String>(neededPermissionsList.size)
            ArrayList(neededPermissionsList).toArray(array)
            ActivityCompat.requestPermissions(this, array, 1000)
        }
    }

    private fun navigateToMediaItem(mediaId: String) {
        val fragment = ListMediaFragment(mediaId)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragmentContainer, fragment, mediaId
        )
        transaction.addToBackStack(mediaId)
        transaction.commit()
        currentFragment = fragment
    }

    private fun navigateToDetailScreen(item: MediaData){
        val fragment = PlayingItemFragment(item)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragmentContainer, fragment, item.mediaId
        )
        transaction.addToBackStack(item.mediaId)
        transaction.commit()
        currentFragment = fragment
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount>1) {
            super.onBackPressed()
        }
    }

    public override fun onDestroy() {
        val intent = Intent(this, AdswizzMediaBrowserService::class.java)
        stopService(intent)
        super.onDestroy()
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