package com.example.ipcalink.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ipcalink.PrivateChatActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentPrivateMessagesBinding
import com.example.ipcalink.models.User
import com.example.ipcalink.models.UserChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PrivateMessagesFragment : Fragment() {

    private lateinit var binding: FragmentPrivateMessagesBinding
    var userChats = mutableListOf<UserChat>()
    private lateinit var db: FirebaseFirestore
    private lateinit var chatsAdapter: PrivateChatsAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var authUserUid: String
    var userExistingPrivateChats = ArrayList<String>()
    var c: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentPrivateMessagesBinding.inflate(layoutInflater)

        //instantiate firestore object
        db = FirebaseFirestore.getInstance()

        //db.firestoreSettings.isPersistenceEnabled() = true

        //get current user uid
        authUserUid = FirebaseAuth.getInstance().currentUser!!.uid

        linearLayoutManager = LinearLayoutManager(activity)
        binding.rvPrivateChats.layoutManager = linearLayoutManager

        chatsAdapter = PrivateChatsAdapter {
            val intent = Intent(activity, PrivateChatActivity::class.java)
            intent.putExtra("chatId", it.chatId)
            intent.putExtra("chatName", it.chatName)
            intent.putExtra("chatType", it.chatType)
            intent.putExtra("chatPhotoUrl", it.photoUrl)
            startActivity(intent)
        }

        //bind adapter to private chats recycler view
        binding.rvPrivateChats.adapter = chatsAdapter

        // return the fragment layout
        return binding.root
    }

    private fun verifyCurrentPrivateChats() {
        for (userChat in userChats) {
            if (userChat.chatType == "private") {
                db.collection("chats").document(userChat.chatId).collection("users")
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        for (document in documentSnapshot) {
                            val chatUser = document.toObject<User>()
                            if(chatUser.userId != authUserUid) {
                                userExistingPrivateChats.add(chatUser.userId)
                            }
                        }
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //get list of all user chats
        db.collection("users").document(authUserUid).collection("chats")
            .addSnapshotListener { chats, e ->
                if (e != null) {
                    Toast.makeText(
                        activity,
                        "Ocorreu um erro ao tentar listar todos os seus chats. Tente novamente mais tarde.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("chatsFragment", e.message.toString())
                    return@addSnapshotListener
                }
                userExistingPrivateChats.clear()
                userChats.clear()
                for (chat in chats!!) {
                    val newChat = chat.toObject<UserChat>()
                    userChats.add(newChat)
                }
                Log.d("PrivateMessages", userChats.size.toString())
                if(userChats.size == 0) {
                    noChatsShowNotice()
                } else {
                    noChatsHideNotice()
                    //get a list of the users current private chats
                    verifyCurrentPrivateChats()
                    chatsAdapter.notifyDataSetChanged()
                }

            }
    }

    override fun onStop() {
        super.onStop()
        db.clearPersistence()
    }

    inner class PrivateChatsAdapter (private val clickListener: (UserChat) -> Unit) :
        RecyclerView.Adapter<PrivateChatsAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val row = LayoutInflater.from(activity).inflate(R.layout.chat_row, parent, false)
            return MyViewHolder(row)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val userChat = userChats[position]

            //set image chat row
            if (userChat.photoUrl.isNotEmpty()) {
                try {
                    Glide.with(activity!!).load(userChat.photoUrl).into(holder.chatImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                holder.chatImage.setImageResource(R.drawable.padrao)
            }

            holder.chatTitle.text = userChat.chatName
            holder.chatLastMessage.text = userChat.lastMessage

            holder.chatRowTime.text = getDateTime(userChat.lastMessageTimestamp.toString().toLong())
            holder.itemView.setOnClickListener {
                clickListener(userChats[position])
            }

        }

        override fun getItemCount(): Int {
            return userChats.size
        }

        inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            var chatImage: CircleImageView = itemView.findViewById(R.id.rowChatIv)
            var chatUnreadMessagesBackground: ImageView = itemView.findViewById(R.id.rowChatUnreadMessagesBackground)
            var chatUnreadMessagesCount: TextView = itemView.findViewById(R.id.chatRowUnreadMessagesCount)
            var isOnline: ImageView = itemView.findViewById(R.id.ivIsOnlineRowChat)
            var chatTitle: TextView = itemView.findViewById(R.id.rowChatTitle)
            var chatLastMessage: TextView = itemView.findViewById(R.id.rowChatLastMessage)
            var chatRowTime: TextView = itemView.findViewById(R.id.tvChatRowTime)
        }

    }

    private fun checkIfChatIsOnline(userChatId: String, ) {
        TODO("Not yet implemented")
    }

    private fun getDateTime(s: Long): String? {
        try {
            val sdf = SimpleDateFormat("hh:mm")
            val netDate = Date(s)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    private fun noChatsShowNotice() {
        binding.rvPrivateChats.visibility = View.INVISIBLE
        binding.tvNoChats.visibility = View.VISIBLE
    }

    private fun noChatsHideNotice() {
        binding.rvPrivateChats.visibility = View.VISIBLE
        binding.tvNoChats.visibility = View.INVISIBLE
    }
}