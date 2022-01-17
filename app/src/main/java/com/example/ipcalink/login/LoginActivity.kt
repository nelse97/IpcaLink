package com.example.ipcalink.login

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.MainActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityLoginBinding
import com.example.ipcalink.models.IpcaUser
import com.example.ipcalink.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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

        auth = Firebase.auth

        binding.editTextTextPassword.inputType = 129

        //password visibility button
        var password_togle = true
        binding.passwordToggle.setOnClickListener {
            password_togle = if (password_togle){

                binding.editTextTextPassword.inputType = 145
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off_black_24dp)
                false

            }else{
                binding.editTextTextPassword.inputType = 129
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_black_24dp)
                true
            }
        }

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
                                println(0)

                                //create/update firestoredatabase values
                                createdata()

                                // Sign in success, update UI with the signed-in user's information
                                checkIfEmailisVerified()
                            } else {
                                //error notice
                                binding.editTextEmail.error = "Password errada ou utilizador não existe"
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

    //check email verification and first login
    private fun checkIfEmailisVerified(){

        val user = auth.currentUser

        if(user!!.isEmailVerified){

            val sp = getSharedPreferences("firstlogin", Activity.MODE_PRIVATE)
            val firstlogin = sp.getBoolean("firstlogin",true)

            if(firstlogin){
                startActivity(Intent(this@LoginActivity, BoardingActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        } else{
            FirebaseAuth.getInstance().signOut()
            binding.editTextEmail.error = "Email não verificado"
        }
    }

    private fun createdata(){

        val db = Firebase.firestore
        println(1)

        db.collection("ipca")
            .whereEqualTo("email", auth.currentUser!!.email.toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    println(2)
                    val user = document.toObject<IpcaUser>()
                    println(user.email)
                }
            }


    }
}