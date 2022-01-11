package com.example.ipcalink

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.FcmToken.fcmToken
import com.example.ipcalink.databinding.ActivityStartBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    public override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val register = binding.buttonRegister
        val login = binding.buttonLogin


        //val navController = findNavController(R.id.nav_host_fragment_content_main)
        //appBarConfiguration = AppBarConfiguration(navController.graph)
        //setupActionBarWithNavController(navController, appBarConfiguration)

        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val intent = Intent(this@StartActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        } else {
            register.setOnClickListener {
                val intent = Intent(this@StartActivity, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }
            login.setOnClickListener {
                val intent = Intent(this@StartActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}