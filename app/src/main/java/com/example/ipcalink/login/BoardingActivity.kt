package com.example.ipcalink.login

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ipcalink.MainActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityBoardingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class BoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //remove top bar
        supportActionBar?.hide()

        //set notification bar to right color
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                this.window.statusBarColor = getColor(R.color.background_color)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                this.window.statusBarColor = getColor(R.color.white)}
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                this.window.statusBarColor = getColor(R.color.white)}
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_boarding)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_boarding1, R.id.navigation_boarding2, R.id.navigation_boarding3
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //skip button/start main activity
        binding.buttonSkip.setOnClickListener {

            val sp = getSharedPreferences("firstlogin", Activity.MODE_PRIVATE)
            val editor = sp.edit()
            editor.putBoolean("firstlogin", false)
            editor.apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}