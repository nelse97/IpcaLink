package com.example.ipcalink.messages

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappfirebase.Models.Message
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentDocumentFilesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.io.File

class DocumentFilesFragment : Fragment() {

    private lateinit var binding: FragmentDocumentFilesBinding
    private lateinit var chatId: String
    private lateinit var db: FirebaseFirestore
    private var adapter = DocumentsAdapter()
    var messagesList = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDocumentFilesBinding.inflate(layoutInflater)
        chatId = requireArguments().getString("chatId").toString()

        binding.rvDocumentFiles.layoutManager = LinearLayoutManager(context)
        binding.rvDocumentFiles.adapter = adapter

        db = FirebaseFirestore.getInstance()

        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { messages, e ->
                if (e != null) {
                    Log.d("DocumentListing", e.message.toString())
                    return@addSnapshotListener
                } else {
                    //userExistingPrivateChats.clear()
                    messagesList.clear()
                    for (message in messages!!) {
                        val newMessage = message.toObject<Message>()
                        if (newMessage.documentUrl != "") {
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

    inner class DocumentsAdapter() :
        RecyclerView.Adapter<DocumentFilesFragment.DocumentsAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val row =
                LayoutInflater.from(activity).inflate(R.layout.card_view_document, parent, false)
            return MyViewHolder(row)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val document = messagesList[position]
            holder.documentName.text = document.body
            holder.docDownloadIcon.setOnClickListener {
                downloadFile(activity!!, document.documentUrl, document.body)
            }

        }

        override fun getItemCount(): Int {
            return messagesList.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val documentName: TextView = itemView.findViewById(R.id.tvCardDocument)
            val docDownloadIcon: ImageView = itemView.findViewById(R.id.ivCardDocDownload)
        }

    }

    fun downloadFile(baseActivity: Context, url: String?, title: String?): Long {
        val direct = File(Environment.getExternalStorageDirectory().toString() + "/your_folder")
        if (!direct.exists()) {
            direct.mkdirs()
        }
        val extension = url?.substring(url.lastIndexOf("."))
        val downloadReference: Long
        val dm: DownloadManager =
            baseActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, title)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle(title)
        Toast.makeText(baseActivity, "Download iniciado..", Toast.LENGTH_SHORT).show()
        downloadReference = dm.enqueue(request)
        return downloadReference
    }

}