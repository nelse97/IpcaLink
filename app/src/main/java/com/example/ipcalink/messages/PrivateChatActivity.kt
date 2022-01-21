package com.example.ipcalink.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityPrivateChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.chatappfirebase.Models.Message
import com.example.ipcalink.calendar.CalendarHelper.getDate
import com.example.ipcalink.models.User
import com.example.ipcalink.models.UsersChats
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class PrivateChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivateChatBinding
    enum class FabState {
        MESSAGE, MEDIACONTENT
    }
    private lateinit var fabState: FabState
    private lateinit var authUserUid: String
    lateinit var chatType: String
    lateinit var chatId: String
    private lateinit var db: FirebaseFirestore
    var messagesList = mutableListOf<Message>()
    private var adapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //remove top bar better
        supportActionBar?.hide()

        //get current user uid
        authUserUid = FirebaseAuth.getInstance().currentUser!!.uid

        //instantiate firestore object
        db = FirebaseFirestore.getInstance()

        fabState = FabState.MEDIACONTENT

        //get intent extras from chats screen
        chatId = intent.getStringExtra("chatId").toString()
        val chatName = intent.getStringExtra("chatName").toString()
        chatType = intent.getStringExtra("chatType").toString()
        var photoUrl = ""
        if(intent.getStringExtra("photoUrl") != null) {
            photoUrl = intent.getStringExtra("photoUrl")!!
        }

        //load chat name
        binding.textViewTopChatName.text = chatName

        //load chat image
        if (photoUrl.isNotEmpty()) {
            Glide.with(this).load(photoUrl).into(binding.circleImageTopChat)
        } else {
            binding.circleImageTopChat.setImageResource(R.drawable.padrao)
        }

        binding.rvPrivateChat.layoutManager = LinearLayoutManager(this)
        binding.rvPrivateChat.setHasFixedSize(true)
        binding.rvPrivateChat.adapter = adapter

        binding.fabPrivateChat.setOnClickListener {
            if(fabState == FabState.MESSAGE) {
                sendTextMessage(binding.etMessagePrivateChat.text.toString())
                binding.etMessagePrivateChat.setText("")
            } else {

            }
        }

        binding.etMessagePrivateChat.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()) {
                    fabState = FabState.MESSAGE
                    binding.fabPrivateChat.setImageResource(R.drawable.ic_send_icon)
                } else {
                    fabState = FabState.MEDIACONTENT
                    binding.fabPrivateChat.setImageResource(R.drawable.ic_add_icon)
                }
            }
        })

        binding.buttonBackTopBar.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        messagesList.clear()
        //get list of all user chats
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { messages, e ->
                if (e != null) {
                    Toast.makeText(
                        this,
                        "Ocorreu um erro ao tentar listar todas as suas mensagens. Tente novamente mais tarde.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("PrivateChatActivity", e.message.toString())
                    return@addSnapshotListener
                } else {
                    //userExistingPrivateChats.clear()
                    messagesList.clear()
                    for (message in messages!!) {
                        val newMessage = message.toObject<Message>()
                        messagesList.add(newMessage)
                    }
                    adapter.notifyDataSetChanged()
                }

            }
    }

    override fun onStop() {
        super.onStop()
        db.clearPersistence()
    }

    inner class MessagesAdapter() : RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {

        private val SENDER_TYPE = 0
        private val RECIPIENT_TYPE = 1

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MessagesAdapter.MyViewHolder {

            val item = if (viewType == SENDER_TYPE) {
                LayoutInflater.from(this@PrivateChatActivity)
                    .inflate(R.layout.row_message_sender, parent, false)
            } else {
                LayoutInflater.from(this@PrivateChatActivity)
                    .inflate(R.layout.row_message_recipient, parent, false)
            }

            return MyViewHolder(item)
        }

        override fun onBindViewHolder(holder: MessagesAdapter.MyViewHolder, position: Int) {
            val message = messagesList[position]
            val date = getDate(message.timestamp!!.seconds * 1000, "hh:mm")
            holder.tvMessageBody.text = message.body
            holder.tvMessageTime.text = date
        }

        override fun getItemCount(): Int {
            return messagesList.size
        }

        inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val tvMessageBody: TextView = view.findViewById(R.id.tvRowChatMessage)
            val tvMessageTime: TextView = view.findViewById(R.id.tvRowChatMessageTime)
        }

        override fun getItemViewType(position: Int): Int {
            val message = messagesList[position]
            if(message.senderId == authUserUid) {
                return SENDER_TYPE
            }
            return RECIPIENT_TYPE
        }

    }

    private fun sendTextMessage(message: String) {
        var newMessage = Message()
        newMessage.senderId = authUserUid
        if(chatType == "private") {
            newMessage.unreadCount = 1
        }
        newMessage.fileUrl = ""
        newMessage.body = message
        newMessage.timestamp = Timestamp.now()

        db.collection("chats").document(chatId).collection("messages")
            .add(newMessage)
            .addOnCompleteListener {
                if(!it.isSuccessful) {
                    Toast.makeText(this, "Erro ao enviar a mensagem.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}