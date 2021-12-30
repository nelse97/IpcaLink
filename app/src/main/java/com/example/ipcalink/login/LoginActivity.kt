package com.example.ipcalink.login

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.MainActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

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

        binding.editTextTextPassword.setOnEditorActionListener { _, _, _ -> binding.buttonLogin.performClick() }

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
                                // Sign in success, update UI with the signed-in user's information
                                checkIfEmailisVerified()
                            } else {
                                binding.editTextEmail.error = "Password errada ou utilizador não existe"
                                /*Toast.makeText(
                                        baseContext, "Authentication failed.",
                                        Toast.LENGTH_SHORT
                                    ).show()*/
                            }
                        }

                }
            }

        }
        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
        binding.textViewPasswordReset.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
        }
    }

    private fun checkIfEmailisVerified(){

        val user = FirebaseAuth.getInstance().currentUser

        if(user!!.isEmailVerified){
            startActivity(Intent(this@LoginActivity, BoardingActivity::class.java))
            finish()
        } else{
            FirebaseAuth.getInstance().signOut()
            binding.editTextEmail.error = "Email não verificado"
        }
    }
}