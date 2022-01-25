package com.example.ipcalink.messages

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityNewMessageBinding
import com.example.ipcalink.models.Chats
import com.example.ipcalink.models.User
import com.example.ipcalink.models.PrivateUserChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class NewMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewMessageBinding
    var searchedUsersList = mutableListOf<User>()
    private lateinit var db: FirebaseFirestore
    private lateinit var newChatsAdapter: NewMessageActivity.NewChatAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var authUserUid: String
    private var currentUserInfo = User()
    var userExistingPrivateChats = ArrayList<String>()
    private var newChatID = ""
    private var existingChatInfo = PrivateUserChat()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //remove top bar better
        supportActionBar?.hide()

        userExistingPrivateChats =
            intent.getStringArrayListExtra("existingChats") as ArrayList<String>

        for (e in userExistingPrivateChats) {
            Log.d("existingId", e)
        }

        linearLayoutManager = LinearLayoutManager(this)

        //instatiate firestore object
        db = FirebaseFirestore.getInstance()

        //get current user uid
        authUserUid = FirebaseAuth.getInstance().currentUser!!.uid

        //get current user username
        db.collection("users").document(authUserUid).get()
            .addOnSuccessListener {
                    currentUserInfo = it.toObject<User>()!!
                Log.d("userInfoEmail", currentUserInfo.email)
            }

        binding.rvNewChat.layoutManager = linearLayoutManager

        newChatsAdapter = NewChatAdapter {
            if (userExistingPrivateChats.contains(it.userId)) {
                openExistingChat(it.userId)
            } else {
                createNewPrivateChat(it, currentUserInfo)
            }
        }

        binding.rvNewChat.adapter = newChatsAdapter

        binding.textView10.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.length > 2) {
                    if((s[0] == 'a' || s[0] == 'A') && s[1].isDigit()) {
                        searchUsersByEmail(s.toString())
                    } else {
                        var searchQuery = s.toString()
                        searchQuery = searchQuery.substring(0, 1).uppercase(Locale.getDefault()) + searchQuery.substring(1)
                            .lowercase(Locale.getDefault())
                        //searchQuery.replaceFirstChar { if (it.isLowerCase()) it.uppercase(Locale.getDefault()) else it.toString() }
                        searchUsersByName(searchQuery)
                    }
                }
            }
        })

        binding.ibBackButton.setOnClickListener {
            finish()
        }
    }

    private fun openExistingChat(userId: String) {

        db.collection("users").document(authUserUid).collection("chats")
            .whereEqualTo("chatUserId", userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (document in documentSnapshot) {
                    val existingChat = document.toObject<PrivateUserChat>()
                    existingChatInfo = existingChat
                }
                val intent = Intent(this, PrivateChatActivity::class.java)
                intent.putExtra("chatId", existingChatInfo.chatId)
                intent.putExtra("chatName",existingChatInfo.chatName)
                intent.putExtra("chatType", existingChatInfo.chatType)
                intent.putExtra("chatPhotoUrl", existingChatInfo.photoUrl)
                intent.putExtra("receiverUserId", userId)
                startActivity(intent)
                finish()
            }
    }

    inner class NewChatAdapter(private val clickListener: (User) -> Unit) :
        RecyclerView.Adapter<NewChatAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val row = LayoutInflater.from(baseContext)
                .inflate(R.layout.new_private_chat_row, parent, false)
            return MyViewHolder(row)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val user = searchedUsersList[position]

            //set image chat row
            if (user.photoUrl.isNotEmpty()) {
                try {
                    Glide.with(baseContext).load(user.photoUrl).into(holder.newChatImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                holder.newChatImage.setImageResource(R.drawable.padrao)
            }

            /*
            if (user.isOnline == true) {
                holder.newChatIsOnline.visibility = View.VISIBLE
            } else {
                holder.newChatIsOnline.visibility = View.GONE
            }*/

            holder.newChatUsername.text = user.name
            holder.newChatEmail.text = user.email

            holder.itemView.setOnClickListener {
                clickListener(searchedUsersList[position])
            }

        }

        override fun getItemCount(): Int {
            return searchedUsersList.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var newChatImage: CircleImageView = itemView.findViewById(R.id.rowNewPrivateChatIv)
            var newChatUsername: TextView = itemView.findViewById(R.id.rowNewPrivateChatUsername)
            var newChatEmail: TextView = itemView.findViewById(R.id.rowNewPrivateChatEmail)
        }

    }

    fun searchUsersByEmail(searchQuery: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("email", searchQuery)
            .whereNotEqualTo("email", currentUserInfo.email)
            .get()
            .addOnSuccessListener { documents ->
                searchedUsersList.clear()
                for (document in documents) {
                    val searchedUser: User? = null
                    searchedUsersList.add(document.toObject())
                }
                newChatsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting searched users: ", exception)
            }
    }

    fun searchUsersByName(searchQuery: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("name", searchQuery)
            .get()
            .addOnSuccessListener { documents ->
                searchedUsersList.clear()
                for (document in documents) {
                    val searchedUser: User = document.toObject()
                    if(searchedUser.email != currentUserInfo.email) {
                        searchedUsersList.add(document.toObject())
                    }
                }
                newChatsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting searched users: ", exception)
            }
    }

    fun createNewPrivateChat(receiverUser: User, currentUserInfo: User) {
        //create main chat document
        newChatID = db.collection("chats").document().id

        //create objects
        val newChat = Chats(newChatID, "", "private", "", "", "")
        val senderUserChat = PrivateUserChat(
            newChatID, receiverUser.name, "private", receiverUser.photoUrl, "", receiverUser.userId,
            null
        )
        //create new chat information with sender info
        val receiverUserChat = PrivateUserChat(
            newChatID, this.currentUserInfo.name, "private", this.currentUserInfo.photoUrl, "",
            authUserUid, null
        )

        //create references
        val mainChat = db.collection("chats").document(newChatID)
        val mainChatSenderInfo =
            db.collection("chats").document(newChatID).collection("users").document(
                this.currentUserInfo.userId
            )
        val mainChatReceiverInfo = db.collection("chats").document(newChatID).collection("users")
            .document(receiverUser.userId)

        val senderChat =
            db.collection("users").document(authUserUid).collection("chats").document(newChatID)
        val receiverChat =
            db.collection("users").document(receiverUser.userId).collection("chats")
                .document(newChatID)


        // Get a new write batch and commit all write operations
        db.runBatch { batch ->
            // Set the main chat branch
            batch.set(mainChat, newChat)

            // Set the sender branch
            batch.set(senderChat, senderUserChat)

            // Set the main branch
            batch.set(receiverChat, receiverUserChat)

            //set the users info on main branch
            batch.set(mainChatSenderInfo, currentUserInfo)
            batch.set(mainChatReceiverInfo, receiverUser)

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, PrivateChatActivity::class.java)
                intent.putExtra("chatId", newChatID)
                intent.putExtra("chatName", receiverUser.name)
                intent.putExtra("chatType", senderUserChat.chatType)
                intent.putExtra("chatPhotoUrl", receiverUser.photoUrl)
                intent.putExtra("receiverUserId", receiverUser.userId)
                startActivity(intent)
                finish()
            } else {
                Log.d("Error adding main chat!", task.exception.toString())
            }
        }
    }

}