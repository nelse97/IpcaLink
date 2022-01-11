package com.example.ipcalink

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fullscreenContent.text = "IPCALink"
        supportActionBar?.hide()
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser != null){
            GlobalScope.launch (Dispatchers.Main){
                delay(2000)
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }else {
            GlobalScope.launch (Dispatchers.Main){
                delay(2000)
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }


    }
}