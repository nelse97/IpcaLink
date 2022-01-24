package com.example.ipcalink.messages

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ipcalink.databinding.ActivityPrivateChatProfileQuickViewBinding
import com.example.ipcalink.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class PrivateChatProfileQuickView : AppCompatActivity() {

    lateinit var userId: String
    lateinit var chatId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityPrivateChatProfileQuickViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivateChatProfileQuickViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //remove top bar better
        supportActionBar?.hide()

        userId = intent.getStringExtra("userId").toString()

        chatId = intent.getStringExtra("chatId").toString()

        db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnCompleteListener {
                val user = it.result!!.toObject<User>()
                binding.profileQuickViewName.text = user!!.name
                binding.profileQuickViewEmail.text = user!!.email
                if (user.photoUrl.isNotEmpty()) {
                    Glide.with(this@PrivateChatProfileQuickView)
                        .load(user.photoUrl)
                        .into(binding.profileQuickViewIv)
                }
            }

        binding.ivBackButton.setOnClickListener {
            finish()
        }

        binding.llViewFiles.setOnClickListener {
            val intent = Intent(this, FilesActivity::class.java)
            intent.putExtra("chatId", chatId)
            startActivity(intent)
        }
    }
}