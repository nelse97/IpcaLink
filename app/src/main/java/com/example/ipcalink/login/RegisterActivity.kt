package com.example.ipcalink.login

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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

        binding.editTextTextPassword.setOnEditorActionListener{_, _, _ -> binding.editTextTextPasswordConfirm.requestFocus()}

        //enter performs click on register button
        binding.editTextTextPasswordConfirm.setOnEditorActionListener { _, _, _ -> binding.buttonRegister.performClick() }

        //register button
        binding.buttonRegister.setOnClickListener {

            val email : String = binding.editTextEmail.text.toString()
            val password : String = binding.editTextTextPassword.text.toString()
            val confirmpassword : String = binding.editTextTextPasswordConfirm.text.toString()

            //error verifications
            when {
                binding.editTextEmail.text.isEmpty() -> {
                    binding.editTextEmail.error = "Preencha este campo"
                }
                binding.editTextTextPassword.text.isEmpty() -> {
                    binding.editTextTextPassword.error = "Preencha este campo"
                }
                binding.editTextTextPasswordConfirm.text.isEmpty() -> {
                    binding.editTextTextPasswordConfirm.error = "Preencha este campo"
                }
                binding.editTextTextPassword.text.length < 6 -> {
                    binding.editTextTextPassword.error = "Password tem que ter pelo menos 6 caracteres"
                }
                confirmpassword != password -> {
                    binding.editTextTextPasswordConfirm.error = "Passwords não coencidem"
                }
                !email.endsWith("ipca.pt",true)->{
                    binding.editTextEmail.error = "O registo tem que ser feito com um email do IPCA."
                }
                else -> {
                    //creates user in firebase aunth
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                //email verification
                                sendVerificationEmail()
                                finish()

                            } else {
                                //check firebase exeptions
                                var exeption = ""
                                exeption = try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthWeakPasswordException) {
                                    "Senha fraca"
                                } catch (e: FirebaseAuthUserCollisionException) {
                                    "Já exite um utilizador com este email registado"
                                }/*catch (e:Exeption){
                                    "Erro ao registar o utilizador: "+ e.message
                                }*/

                                //show firebase exeption
                                binding.textViewErro.visibility = View.VISIBLE
                                binding.textViewErro.text = exeption
                            }
                        }
                }
            }
        }
    }

    //send verification email
    private fun sendVerificationEmail(){

        val user = FirebaseAuth.getInstance().currentUser

        user!!.sendEmailVerification()
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(baseContext, "Verifique o seu email",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }


}