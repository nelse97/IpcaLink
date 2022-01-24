package com.example.ipcalink.messages

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappfirebase.Models.Message
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentMultimediaFilesBinding
import com.example.ipcalink.models.FirebaseImage
import com.example.ipcalink.models.PrivateUserChat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import de.hdodenhof.circleimageview.CircleImageView
import androidx.recyclerview.widget.GridLayoutManager




class MultimediaFilesFragment : Fragment() {

    private lateinit var binding: FragmentMultimediaFilesBinding
    private lateinit var chatId: String
    private lateinit var db: FirebaseFirestore
    private var adapter = MediaFilesAdapter()
    var messagesList = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMultimediaFilesBinding.inflate(layoutInflater)
        chatId = requireArguments().getString("chatId").toString()

        db = FirebaseFirestore.getInstance()

        val layoutManager = GridLayoutManager(activity, 3)
        binding.rvMultimediaFiles.layoutManager = layoutManager
        binding.rvMultimediaFiles.adapter = adapter

        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { messages, e ->
                if (e != null) {
                    Toast.makeText(
                        activity,
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
                        if(newMessage.photoUrl != "") {
                            messagesList.add(newMessage)
                        }
                    }
                    //adapter.notifyItemInserted(adapter.itemCount - 1)
                    adapter.notifyDataSetChanged()
                }
            }


        // Inflate the layout for this fragment
        return binding.root
    }

    inner class MediaFilesAdapter() :
        RecyclerView.Adapter<MultimediaFilesFragment.MediaFilesAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val row = LayoutInflater.from(activity).inflate(R.layout.card_view_multimedia, parent, false)
            return MyViewHolder(row)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val image = messagesList[position]

            try {
                Glide.with(activity!!).load(image.photoUrl).into(holder.image)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return messagesList.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var image: ImageView = itemView.findViewById(R.id.idIvMultimediaRow)
        }

    }

}