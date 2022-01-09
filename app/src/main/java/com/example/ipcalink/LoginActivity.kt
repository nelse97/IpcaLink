package com.example.ipcalink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.example.ipcalink.encryption_algorithm.AES
import com.example.ipcalink.databinding.ActivityLoginBinding
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth : FirebaseAuth

    private val dbFirebase = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    public override fun onStart() {
        super.onStart()

        val buttonLog = binding.Login

        // Initialize Firebase Auth
        auth = Firebase.auth


        buttonLog.setOnClickListener {

            val email = binding.Email.text.toString()
            val password = binding.Password.text.toString()

            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {

                //This needs to be execute when a user creates a group

                //Here i generate an encrypted key and save it in encrypted shared preferences
                val secretKey = AES.generateAESKey()
                val secretKeyString = Base64.encodeToString(secretKey, Base64.DEFAULT)

                val keySet = ESP(this).keysPref
                keySet.add("ka4vgKgo8QzsVkdn5brt - $secretKeyString")

                ESP(this).keysPref = keySet


                Toast.makeText(this, "User Logged in Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error Logging in the account", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getUserInfo(email : String) {

        var userInfo : String? = null

        dbFirebase.collection("users").
        whereEqualTo("email", email).get().addOnCompleteListener {
            for (query in it.result!!) {
                if (it.isSuccessful) {
                    userInfo = query.data.toString()
                    Log.d("Successfully found user", userInfo!!)
                } else {
                    it.exception?.let {
                        throw it
                    }
                }
            }
        }
    }

}