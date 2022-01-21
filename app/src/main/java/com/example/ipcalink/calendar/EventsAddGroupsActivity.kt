package com.example.ipcalink.calendar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityAddEventBinding
import com.example.ipcalink.databinding.ActivityEventsAddGroupsBinding
import com.example.ipcalink.databinding.FragmentCalendarBinding
import com.example.ipcalink.models.UsersChats
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList


class EventsAddGroupsActivity : AppCompatActivity() {

    private var _binding : ActivityEventsAddGroupsBinding? = null
    private val binding get() = _binding!!

    private var chatsAdapter: RecyclerView.Adapter<*>? = null
    private var chatsList : ArrayList<UsersChats> = ArrayList()
    private var selectedChatsIdsList : ArrayList<String> = ArrayList()
    private var selectedChatsNameList : ArrayList<String> = ArrayList()

    private var selectedChatsPhotoList : ArrayList<String> = ArrayList()
    private var layoutManager: LinearLayoutManager? = null

    private val dbFirebase = Firebase.firestore

    private val userUID = Firebase.auth.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        _binding = ActivityEventsAddGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    public override fun onStart() {
        super.onStart()

        //Hides top bar
        (this as AppCompatActivity?)!!.supportActionBar!!.hide()


        val ids = intent.getStringArrayListExtra("chatsIdsList")
        val names = intent.getStringArrayListExtra("chatsNameList")
        val photos = intent.getStringArrayListExtra("chatsPhotoList")

        if(ids != null && names != null && photos != null) {
            selectedChatsIdsList = ids
            selectedChatsNameList = names
            selectedChatsPhotoList = photos

        }

        insertingChats()


        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewGroups.layoutManager = layoutManager
        chatsAdapter = ChatsAdapter()
        binding.recyclerViewGroups.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewGroups.adapter = chatsAdapter
        binding.imageViewGoBack.setOnClickListener {
            finish()
        }


        binding.cardViewSaveEvent.setOnClickListener {

            val returnIntent = Intent(this, AddEventActivity::class.java)
            returnIntent.putExtra("selectedChatsPhotoList", selectedChatsPhotoList)
            returnIntent.putExtra("selectedChatsIdsList",   selectedChatsIdsList)
            returnIntent.putExtra("selectedChatsNameList",   selectedChatsNameList)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

    }

    inner class ChatsAdapter : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_view_events_add_group, parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {


            holder.v.apply {

                val toggleButton = findViewById<ToggleButton>(R.id.toggleButton)

                if(!selectedChatsIdsList.isNullOrEmpty()) {
                    for(s in selectedChatsIdsList) {
                        if(chatsList[position].chatId == s) {
                            toggleButton.toggle()
                        }
                    }
                }

                toggleButton.setOnCheckedChangeListener { _, isChecked ->
                    if(isChecked) {

                        var newChat = true

                        if(!selectedChatsIdsList.isNullOrEmpty()) {
                            for(s in selectedChatsIdsList) {
                                if(s == chatsList[position].chatId) {
                                    newChat = false
                                }
                            }
                        }

                        if(newChat) {
                            selectedChatsNameList.add(chatsList[position].chatName!!)
                            selectedChatsIdsList.add(chatsList[position].chatId!!)
                            selectedChatsPhotoList.add(chatsList[position].photoUrl!!)
                        }

                    } else {

                        var newChat = false

                        if(!selectedChatsIdsList.isNullOrEmpty()) {
                            for(s in selectedChatsIdsList) {
                                if(s == chatsList[position].chatId) {
                                    newChat = false
                                }
                            }
                        }


                        if(!newChat) {

                            selectedChatsNameList.remove(chatsList[position].chatName)
                            selectedChatsIdsList.remove(chatsList[position].chatId)
                            selectedChatsPhotoList.remove(chatsList[position].photoUrl)
                        }
                    }
                }

                val textViewGroupName = findViewById<TextView>(R.id.textViewGroupName)
                textViewGroupName.text = chatsList[position].chatName

            }
        }

        override fun getItemCount(): Int {
            return chatsList.size
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun insertingChats() {


        dbFirebase.collection("users").document(userUID!!).collection("chats").get().addOnCompleteListener {

            if (it.exception != null) {
                Log.w("ShowNotificationsFragment", "Listen failed.", it.exception)
                return@addOnCompleteListener
            }

            for (query in it.result!!) {

                val usersChats = UsersChats.fromHash(query)

                chatsList.add(UsersChats(usersChats.chatId, usersChats.chatName, usersChats.chatType, usersChats.photoUrl, usersChats.lastMessage,
                    usersChats.lastMessageSenderId, usersChats.lastMessageTimestamp))

            }

            chatsAdapter?.notifyDataSetChanged()

        }
    }

}