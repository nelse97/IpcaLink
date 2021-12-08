package com.example.ipcalink.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ipcalink.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonRegister.setOnClickListener {

            val email : String = binding.editTextEmail.text.toString()
            val password : String = binding.editTextTextPassword.text.toString()
            val confirmpassword : String = binding.editTextTextPasswordConfirm.text.toString()

            if (binding.editTextEmail.text.isEmpty()){
                binding.editTextEmail.error = "Preencha este campo"
            }else if (binding.editTextTextPassword.text.isEmpty()){
                binding.editTextTextPassword.error = "Preencha este campo"
            }else if (binding.editTextTextPasswordConfirm.text.isEmpty()){
                binding.editTextTextPasswordConfirm.error = "Preencha este campo"
            }else if(binding.editTextTextPassword.text.length < 6) {
                binding.editTextTextPassword.error = "password tem que ter pelo menos 6 caracteres"
            }else{

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->


                        if (confirmpassword == password){
                            if (task.isSuccessful) {
                                Toast.makeText(baseContext, "Registado",
                                    Toast.LENGTH_SHORT).show()
                                finish()

                            }else {
                                binding.editTextEmail.error = "Email inválido"
                            }
                        } else{
                            binding.editTextTextPasswordConfirm.error = "Passwords não coencidem"
                        }
                    }
            }
        }

    }
}