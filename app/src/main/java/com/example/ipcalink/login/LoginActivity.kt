package com.example.ipcalink.login

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.InputType.*
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.FcmToken
import com.example.ipcalink.MainActivity
import com.example.ipcalink.R
import com.example.ipcalink.calendar.Extensions.myLocale
import com.example.ipcalink.databinding.ActivityLoginBinding
import com.example.ipcalink.models.Chats
import com.example.ipcalink.models.IpcaUser
import com.example.ipcalink.models.Subjects
import com.example.ipcalink.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class LoginActivity : AppCompatActivity() {

    private lateinit var user: IpcaUser
    var docid = ""
    val schoolYear = detectSchoolYear()
    val semester = detectSemester()

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp = getSharedPreferences("firstlogin", Activity.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean("firstlogin", true)
        editor.apply()

        //remove top bar
        supportActionBar?.hide()

        //set notification bar to right color
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                this.window.statusBarColor = getColor(R.color.background_color)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                this.window.statusBarColor = getColor(R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                this.window.statusBarColor = getColor(R.color.white)
            }
        }

        auth = Firebase.auth

        binding.editTextTextPassword.inputType = 129

        //password visibility button
        var passwordToggle = true
        binding.passwordToggle.setOnClickListener {
            passwordToggle = if (passwordToggle) {

                binding.editTextTextPassword.inputType = 145
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off_black_24dp)
                false

            } else {
                binding.editTextTextPassword.inputType = 129
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_black_24dp)
                true
            }
        }

        binding.editTextEmail.setOnEditorActionListener { _, _, _ -> binding.editTextTextPassword.requestFocus() }

        //enter performs login button click
        binding.editTextTextPassword.setOnEditorActionListener { _, _, _ -> binding.buttonLogin.performClick() }

        //login button
        binding.buttonLogin.setOnClickListener {

            when {
                binding.editTextEmail.text.isEmpty() -> {
                    binding.editTextEmail.error = "Preencha este campo"
                }
                binding.editTextTextPassword.text.isEmpty() -> {
                    binding.editTextTextPassword.error = "Prencha este campo"
                }
                else -> {

                    val email: String = binding.editTextEmail.text.toString()
                    val password: String = binding.editTextTextPassword.text.toString()

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                val user = auth.currentUser

                                // Sign in success, update UI with the signed-in user's information
                                if (user!!.isEmailVerified) {

                                    //create/update firestoredatabase values
                                    createdata()

                                    val sp = getSharedPreferences("firstlogin", Activity.MODE_PRIVATE)
                                    val firstlogin = sp.getBoolean("firstlogin", true)

                                    if (firstlogin) {
                                        startActivity(Intent(this@LoginActivity, BoardingActivity::class.java))
                                        finish()
                                    } else {
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish()
                                    }
                                } else {
                                    FirebaseAuth.getInstance().signOut()
                                    binding.editTextEmail.error = "Email não verificado"
                                }

                            } else {
                                //error notice
                                binding.editTextEmail.error =
                                    "Password errada ou utilizador não existe"
                            }
                        }

                }
            }

        }

        //register button
        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        //reset password
        binding.textViewPasswordReset.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createUser() {

        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone(ZoneId.of("UTC"))
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", myLocale)
        val sendDateString = format.format(calendar.time)

        val sendDateLong = Date.parse(sendDateString)
        val sendDate = Date(sendDateLong)
        val timeStampSend = Timestamp(sendDate)

        val newUser = User(auth.currentUser!!.uid, user.name, "", auth.currentUser!!.email!!, "", timeStampSend,true )
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnCompleteListener {
                if (!it.result!!.exists()) {
                    db.collection("users")
                        .document(auth.currentUser!!.uid)
                        .set(newUser)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createdata() {

        user = IpcaUser("", auth.currentUser!!.email!!, "", "", auth.currentUser!!.uid)


        db.collection("ipca")
            .whereEqualTo("email", auth.currentUser!!.email)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    docid = document.id
                    user = document.toObject()
                }
                createUser()
                if (!documents.isEmpty) {
                    callback()
                }

            }.addOnCompleteListener {
                createUser()
            }


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            FcmToken.fcmToken = task.result

        }).addOnSuccessListener {
            val userUID = Firebase.auth.uid
            verifyFcmToken(FcmToken.fcmToken!!, userUID!!)
        }

    }

    private fun saveFcmToken(fcmToken: String, userUID: String) {

        val hashMap = HashMap<String, Any>()
        hashMap["fcmToken"] = fcmToken

        db.collection("users").document(userUID).collection("fcmTokens").document().set(hashMap)
    }

    private fun verifyFcmToken(fcmToken: String, userUID: String) {

        db.collection("users").document(userUID).collection("fcmTokens")
            .whereEqualTo("fcmToken", fcmToken).get().addOnCompleteListener {
                if (it.result!!.isEmpty) {
                    saveFcmToken(fcmToken, userUID)
                }
            }
    }

    private fun detectSchoolYear(): String {

        var sdf = SimpleDateFormat("MM")
        val currentTime = sdf.format(Date())
        var schoolYear = ""

        when {
            currentTime.toInt() >= 9 -> {

                sdf = SimpleDateFormat("yy")
                val year1 = sdf.format(Date()).toString()
                val year2 = sdf.format(Date()).toInt() + 1

                schoolYear = "$year1/$year2"

            }
            currentTime.toInt() <= 2 -> {

                sdf = SimpleDateFormat("yy")
                val year1 = sdf.format(Date()).toInt() - 1
                val year2 = sdf.format(Date()).toString()

                schoolYear = "$year1/$year2"

            }
            else -> {
                return null.toString()
            }
        }


        return schoolYear
    }

    private fun detectSemester(): String {

        val sdf = SimpleDateFormat("MM")
        val currentTime = sdf.format(Date())

        return if (currentTime.toInt() in 3..8) {
            "2º"
        } else {
            "1º"
        }
    }

    private fun callback() {
        if (schoolYear != "null") {
            db.collection("ipca")
                .document(docid)
                .collection("cursos")
                .whereEqualTo("schoolYear", schoolYear)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("ipca")
                            .document(docid)
                            .collection("cursos")
                            .document(document.id)
                            .collection("disciplinas")
                            .get()
                            .addOnSuccessListener { subdocs ->
                                for (subdoc in subdocs) {
                                    val subject: Subjects = subdoc.toObject()

                                    val ipcaChat = Chats("", subject.name, "subject", "", "", "")
                                    val docRef = db.collection("chatsIpca")
                                    docRef.whereEqualTo("chatName", ipcaChat.chatName).get()
                                        .addOnCompleteListener {
                                            println(1)
                                            if (it.result!!.isEmpty) {
                                                println(2)
                                                docRef.document()
                                                    .set(ipcaChat)
                                                    .addOnSuccessListener {
                                                        docRef.whereEqualTo(
                                                            "chatName",
                                                            ipcaChat.chatName
                                                        )
                                                            .get()
                                                            .addOnSuccessListener { subsubdocs ->
                                                                for (subsubdoc in subsubdocs) {
                                                                    docRef.document(subsubdoc.id)
                                                                        .update(
                                                                            "chatId",
                                                                            subsubdoc.id
                                                                        ).addOnCompleteListener {
                                                                            db.collection("users")
                                                                                .document(auth.currentUser!!.uid)
                                                                                .collection("chats")
                                                                                .document(ipcaChat.chatId!!)
                                                                                .set(ipcaChat)
                                                                                .addOnCompleteListener {
                                                                                    docRef.document(
                                                                                        subsubdoc.id
                                                                                    ).collection(
                                                                                        "users"
                                                                                    ).document(
                                                                                        auth.currentUser!!.uid
                                                                                    ).set(user)
                                                                                }
                                                                        }
                                                                }
                                                            }
                                                    }
                                            } else {
                                                println(3)
                                                docRef.whereEqualTo("chatName", ipcaChat).get()
                                                    .addOnSuccessListener { subsubdocs ->
                                                        println(4)
                                                        for (subsubdoc in subsubdocs) {
                                                            println(5)
                                                            db.collection("users")
                                                                .document(auth.currentUser!!.uid)
                                                                .collection("chats")
                                                                .document(ipcaChat.chatId!!)
                                                                .set(ipcaChat)
                                                                .addOnCompleteListener {
                                                                    docRef.document(
                                                                        subsubdoc.id
                                                                    ).collection(
                                                                        "users"
                                                                    ).document(
                                                                        auth.currentUser!!.uid
                                                                    ).set(user)

                                                                }
                                                        }
                                                    }
                                            }
                                        }
                                }
                            }
                    }
                }
        }
    }
}