package com.example.ipcalink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ipcalink.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    public override fun onStart() {
        super.onStart()

        val buttonReg = binding.buttonRegister

        // Initialize Firebase Auth
        auth = Firebase.auth

        buttonReg.setOnClickListener {

            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            if(password.isEmpty()) {
                Toast.makeText(this@RegisterActivity, "Empty Credentials", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this@RegisterActivity, "Password too short", Toast.LENGTH_SHORT).show()
            } else {
                registration(email, password)
            }
        }
    }


    private fun registration(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
                Toast.makeText(this, "Account Registered Successfully", Toast.LENGTH_SHORT).show()


                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error Registering the Account", Toast.LENGTH_SHORT).show()
            }
        }
    }
}