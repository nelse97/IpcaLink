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
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityNewMessageBinding
import com.example.ipcalink.models.Chats
import com.example.ipcalink.models.User
import com.example.ipcalink.models.UsersChats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class NewMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewMessageBinding
    var searchedUsersList = mutableListOf<User>()
    private lateinit var db: FirebaseFirestore
    private lateinit var newChatsAdapter: NewMessageActivity.NewChatAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var authUserUid: String
    private var currentUserPhotoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //remove top bar better
        supportActionBar?.hide()

        linearLayoutManager = LinearLayoutManager(this)

        //instantiate firestore object
        db = FirebaseFirestore.getInstance()

        //get current logged in user photo url
        if (FirebaseAuth.getInstance().currentUser!!.photoUrl != null) {
            currentUserPhotoUrl = FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()
        }


        //get current user uid
        authUserUid = FirebaseAuth.getInstance().currentUser!!.uid

        binding.rvNewChat.layoutManager = linearLayoutManager

        newChatsAdapter = NewChatAdapter {
            createNewPrivateChat(it)
        }

        binding.rvNewChat.adapter = newChatsAdapter

        binding.textView10.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.length > 2) {
                    searchedUsersList.clear()
                    searchUsers(s.toString())
                }
            }
        })

        binding.ibBackButton.setOnClickListener {
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

    fun searchUsers(searchQuery: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("email", searchQuery)
            .get()
            .addOnSuccessListener { documents ->
                searchedUsersList.clear()
                for (document in documents) {
                    val searchedUser: User? = null
                    searchedUsersList.add(document.toObject<User>())
                }
                newChatsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting searched users: ", exception)
            }
    }

    fun createNewPrivateChat(user: User) {
        //create main chat document
        val newChatID = db.collection("chats").document().id
        val newChat = Chats(newChatID, "", "private", "", "", "")

        //add new chat to chats collection
        db.collection("chats").document(newChatID).set(newChat).addOnCompleteListener { newChat ->
            if (newChat.isSuccessful) {
                createSenderChat(newChatID, user.photoUrl)
                createReceiverChat(newChatID, user.userId)
            } else {
                Log.d(
                    "Error adding main chat!",
                    newChat.exception.toString()
                )
            }
        }
    }

    fun createSenderChat(newChatID: String, receiverPhotoUrl: String) {
        //create new chat information with receiver info
        val newSenderUserChat =
            UsersChats(newChatID, "", "private", receiverPhotoUrl, "", "", null)
        //create new chat on sending user branch
        db.collection("users").document(authUserUid).collection("chats")
            .document(newChatID).set(newSenderUserChat)
            .addOnCompleteListener { addNewSenderChat ->
                if (addNewSenderChat.isSuccessful) {

                }
            }


    }

    fun createReceiverChat(newChatID: String, receiverUserId: String) {
        //create new chat information with sender info
        val newReceiverUserChat = UsersChats(
            newChatID,
            "",
            "private",
            currentUserPhotoUrl,
            "",
            "",
            null
        )
        db.collection("users").document(receiverUserId).collection("chats")
            .document(newChatID)
            .set(newReceiverUserChat)
            .addOnCompleteListener { addNewReceiverChat ->
                if (addNewReceiverChat.isSuccessful) {
                    val intent = Intent(this, ChatRoomActivity::class.java)
                    intent.putExtra("chatId", newChatID)
                    startActivity(intent)
                    finish()
                } else {
                    Log.d(
                        "Error adding receiver chat!",
                        addNewReceiverChat.exception.toString()
                    )
                }
            }
    }

}