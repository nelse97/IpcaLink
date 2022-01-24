package com.example.ipcalink.login

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
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
                this.window.statusBarColor = getColor(R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                this.window.statusBarColor = getColor(R.color.white)
            }
        }

        //enter performs click on send email button
        binding.editTextEmailAddress.setOnEditorActionListener { _, _, _ -> binding.buttonSendEmail.performClick() }

        //send email button
        binding.buttonSendEmail.setOnClickListener {

            auth = Firebase.auth

            val email = binding.editTextEmailAddress.text.toString()

            //check erros
            when {
                binding.editTextEmailAddress.text.isEmpty() -> {
                    binding.editTextEmailAddress.error = "Campo de email vazio"
                }
                !email.endsWith("ipca.pt", true) -> {
                    binding.editTextEmailAddress.error = "O email não pertence ao IPCA."
                }
                else -> {
                    //send email
                    auth
                        .sendPasswordResetEmail(email)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    baseContext, "Verifique o seu email",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    baseContext, "Email não registado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                }

            }
        }
    }
}