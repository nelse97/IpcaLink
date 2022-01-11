package com.example.ipcalink

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.ipcalink.databinding.ActivityPrivateChatBinding
import java.lang.Exception

class PrivateChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivateChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatId = intent.getStringExtra("chatId").toString()
        val chatName = intent.getStringExtra("chatName").toString()
        val chatType = intent.getStringExtra("chatType").toString()
        val photoUrl = intent.getStringExtra("photoUrl").toString()

        binding.textViewTopChatName.text = chatName
        if (photoUrl.isNotEmpty()) {
            try {
                Glide.with(this).load(photoUrl).into(binding.circleImageTopChat)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            binding.circleImageTopChat.setImageResource(R.drawable.padrao)
        }

        binding.buttonBackTopBar.setOnClickListener {
            finish()
        }
    }
}