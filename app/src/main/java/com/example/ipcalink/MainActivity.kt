package com.example.ipcalink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import java.util.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ipcalink.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /*var notificationReceiver : NotificationReceiver? = null

    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            //Get the message head
            intent?.extras?.getString(MyFirebaseMessagingService.NOTIFICATION_HEAD)?.let { it1 ->
                intent?.extras?.getString(MyFirebaseMessagingService.NOTIFICATION_BODY)?.let { it2->
                    //here the message is being configured
                    alertNotificatio(this@MainActivity, it1, it2)
                }
            }
        }
    }*/

   /* override fun onResume() {
        super.onResume()
        notificationReceiver = NotificationReceiver()

        //activates the Notification Receiver
        this.registerReceiver(notificationReceiver, IntentFilter(MyFirebaseMessagingService.BROADCAST_NEW_NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()

        //deactivates the Notification Receiver
        notificationReceiver?.let {
            this.unregisterReceiver(it)
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.HomeFragment,
                R.id.PushNotificationFragment,
                R.id.ShowNotificationFragment,
                R.id.CalendarFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
