package com.example.ipcalink

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.example.ipcalink.encryption_algorithm.AES
import com.example.ipcalink.databinding.ActivityLoginBinding
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    /*private lateinit var binding: ActivityLoginBinding
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

        val buttonLog = binding.buttonLogin

        // Initialize Firebase Auth
        auth = Firebase.auth


        buttonLog.setOnClickListener {

            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {

                //This needs to be execute when a user creates a group

                //Here i generate an encrypted key and save it in encrypted shared preferences
                /*val secretKey = AES.generateAESKey()
                val secretKeyString = Base64.encodeToString(secretKey, Base64.DEFAULT)

                val keySet = ESP(this).keysPref
                keySet.add("ka4vgKgo8QzsVkdn5brt - $secretKeyString")

                ESP(this).keysPref = keySet


                Toast.makeText(this, "User Logged in Successfully", Toast.LENGTH_SHORT).show()*/
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    FcmToken.fcmToken = task.result

                    // Log and toast
                    Log.d(ContentValues.TAG, "O FCM é ${FcmToken.fcmToken}")
                    Toast.makeText(this, "O FCM é ${FcmToken.fcmToken}", Toast.LENGTH_SHORT).show()
                }).addOnSuccessListener {
                    val userUID = Firebase.auth.uid
                    saveFcmToken(FcmToken.fcmToken!!, userUID!!)
                }

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error Logging in the account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveFcmToken(fcmToken : String, userUID : String) {

        val hashMap = HashMap<String, Any>()
        hashMap["fcmToken"] = fcmToken

        dbFirebase.collection("users").document(userUID).collection("fcmTokens").document().set(hashMap)
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
    }*/

}