package com.example.ipcalink.messages

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappfirebase.Models.Message
import com.example.ipcalink.R
import com.example.ipcalink.calendar.CalendarHelper.getDate
import com.example.ipcalink.databinding.ActivityPrivateChatBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class PrivateChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivateChatBinding

    enum class FabState {
        MESSAGE, MEDIACONTENT
    }

    enum class MessageType {
        MEDIA, DOCUMENT
    }

    private lateinit var fabState: FabState
    private lateinit var authUserUid: String
    lateinit var chatType: String
    lateinit var chatId: String
    lateinit var receiverUserId: String
    private lateinit var db: FirebaseFirestore
    private lateinit var dbUpdateLastMessage: FirebaseFirestore
    var messagesList = mutableListOf<Message>()
    private lateinit var adapter: MessagesAdapter
    private val REQUEST_TAKE_GALLERY_PHOTO = 2
    private val REQUEST_TAKE_CAMERA_PHOTO = 3
    private val REQUEST_DOCUMENT = 4
    lateinit var imageUri: Uri
    lateinit var documentUri: Uri
    lateinit var imageBytes: Bitmap
    lateinit var uploadTask: UploadTask
    var currentTimestamp: String? = null
    lateinit var map: HashMap<*, *>
    var manager: DownloadManager? = null
    private var fbaClicked = false
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }

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

        dbUpdateLastMessage = FirebaseFirestore.getInstance()

        fabState = FabState.MEDIACONTENT

        //get intent extras from chats screen
        chatId = intent.getStringExtra("chatId").toString()
        receiverUserId = intent.getStringExtra("receiverUserId").toString()
        val chatName = intent.getStringExtra("chatName").toString()
        chatType = intent.getStringExtra("chatType").toString()
        var photoUrl = ""
        if (intent.getStringExtra("chatPhotoUrl") != null) {
            photoUrl = intent.getStringExtra("chatPhotoUrl")!!
        }

        //load chat name
        binding.textViewTopChatName.text = chatName

        //load chat image
        if (photoUrl.isNotEmpty()) {
            Glide.with(this).load(photoUrl).into(binding.circleImageTopChat)
        } else {
            binding.circleImageTopChat.setImageResource(R.drawable.padrao)
        }

        //set LinearLayoutManager
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true

        binding.rvPrivateChat.layoutManager = linearLayoutManager

        adapter = MessagesAdapter()

        binding.rvPrivateChat.adapter = adapter

        binding.fabPrivateChat.setOnClickListener {
            if (fabState == FabState.MESSAGE) {
                sendMessage(binding.etMessagePrivateChat.text.toString(), null, "")
                binding.etMessagePrivateChat.setText("")
            } else {
                //opens 4 fba buttons
                //binding.fabPrivateChat.visibility = View.GONE
                setVisibility()
                setAnimation()
                fbaClicked = !fbaClicked
            }
        }

        //to collapse the fba from 4 to 1
        /*binding.llMainPrivateChat.setOnClickListener {
            binding.llFabOptions.visibility = View.GONE
            binding.fabPrivateChat.visibility = View.VISIBLE
        }*/

        binding.fabGallery.setOnClickListener {
            selectImage()
        }

        binding.fabCamera.setOnClickListener {
            selectCamera()
        }

        binding.fabDocument.setOnClickListener {
            selectDocument()
        }


        binding.ibProfileQuickView.setOnClickListener {
            val intent = Intent(this, PrivateChatProfileQuickView::class.java)
            intent.putExtra("userId", receiverUserId)
            intent.putExtra("chatId", chatId)
            startActivity(intent)
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

    private fun setAnimation() {
        if (!fbaClicked) {
            binding.fabPrivateChat.startAnimation(rotateOpen)
        } else {
            binding.fabPrivateChat.startAnimation(rotateClose)
        }
    }

    private fun setVisibility() {
        if (!fbaClicked) {
            binding.fabGallery.visibility = View.VISIBLE
            binding.fabDocument.visibility = View.VISIBLE
            binding.fabCamera.visibility = View.VISIBLE
        } else {
            binding.fabGallery.visibility = View.GONE
            binding.fabDocument.visibility = View.GONE
            binding.fabCamera.visibility = View.GONE
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
                    messagesList.clear()
                    for (message in messages!!) {
                        val newMessage = message.toObject<Message>()
                        messagesList.add(newMessage)
                    }
                    //adapter.notifyItemInserted(adapter.itemCount - 1)
                    adapter.notifyDataSetChanged()
                    binding.rvPrivateChat.scrollToPosition(adapter.itemCount - 1)
                }
            }
    }

    override fun onStop() {
        super.onStop()
        db.clearPersistence()
    }

    inner class MessagesAdapter() :
        RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {

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
            val date = getDate(message.timestamp!!.seconds * 1000, "HH:mm")
            holder.tvMessageBody.text = message.body
            holder.tvMessageTime.text = date

            if (message.photoUrl != "") {
                // Create a reference to a file from a Google Cloud Storage URI
                holder.tvMessageBody.visibility = View.GONE
                holder.tvMessageIv.visibility = View.VISIBLE
                Glide.with(this@PrivateChatActivity)
                    .load(message.photoUrl)
                    .into(holder.tvMessageIv)
            } else {
                holder.tvMessageIv.setImageURI(null)
                holder.tvMessageIv.visibility = View.GONE
                holder.tvMessageBody.visibility = View.VISIBLE
            }

            if (message.documentUrl != "") {
                holder.tvMessageIconDownload.visibility = View.VISIBLE
                holder.tvMessageIconDownload.setOnClickListener {
                    downloadFile(this@PrivateChatActivity, message.documentUrl, message.body)
                }
            } else {
                holder.tvMessageIconDownload.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int {
            return messagesList.size
        }

        inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMessageBody: TextView = view.findViewById(R.id.tvRowChatMessage)
            val tvMessageTime: TextView = view.findViewById(R.id.tvRowChatMessageTime)
            val tvMessageIv: ImageView = view.findViewById(R.id.tvRowChatIv)
            val tvMessageIconDownload: ImageView = view.findViewById(R.id.tvRowChatDownloadIcon)
        }

        override fun getItemViewType(position: Int): Int {
            val message = messagesList[position]
            if (message.senderId == authUserUid) {
                return SENDER_TYPE
            }
            return RECIPIENT_TYPE
        }

    }

    private fun sendMessage(message: String, photoUrl: String?, documentUrl: String?) {
        val newMessage = Message()

        newMessage.senderId = authUserUid
        if (chatType == "private") {
            newMessage.unreadCount = 1
        }

        if (!photoUrl.isNullOrEmpty()) {
            newMessage.photoUrl = photoUrl
        } else {
            newMessage.photoUrl = ""
        }

        if (!documentUrl.isNullOrEmpty()) {
            newMessage.documentUrl = documentUrl
        } else {
            newMessage.documentUrl = ""
        }

        newMessage.body = message
        newMessage.timestamp = Timestamp.now()

        db.collection("chats").document(chatId).collection("messages")
            .add(newMessage)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(this, "Erro ao enviar a mensagem.", Toast.LENGTH_SHORT).show()
                } else {
                    saveLastMessage(message, newMessage.timestamp!!)
                }
            }
    }

    private fun saveLastMessage(lastMessage: String, lastMessageTimestamp: Timestamp) {
        val senderChat =
            dbUpdateLastMessage.collection("users").document(authUserUid).collection("chats")
                .document(chatId)

        Log.d("authID", authUserUid)
        val receiverChat =
            dbUpdateLastMessage.collection("users").document(receiverUserId).collection("chats")
                .document(chatId)

        Log.d("receiverId", receiverUserId)
        dbUpdateLastMessage.runBatch { batch ->
            // Update last message on sender chat
            batch.update(senderChat, "lastMessage", lastMessage)
            // Update last message timestamp on sender chat
            batch.update(senderChat, "lastMessageTimestamp", lastMessageTimestamp)

            // Update last message on receiver chat
            batch.update(receiverChat, "lastMessage", lastMessage)
            // Update last message timestamp on receiver chat
            batch.update(receiverChat, "lastMessageTimestamp", lastMessageTimestamp)
        }.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("Error saving lastMessage", task.exception.toString())
            }
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Selecionar Imagem"),
            REQUEST_TAKE_GALLERY_PHOTO
        )
    }

    private fun selectDocument() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "application/pdf/*"
        startActivityForResult(
            Intent.createChooser(intent, "Selecionar Documento"),
            REQUEST_DOCUMENT
        )
    }

    private fun selectCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_TAKE_CAMERA_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_GALLERY_PHOTO && resultCode == RESULT_OK) {

            imageUri = data?.data!!
            uploadGalleryImage()

        } else if (requestCode == REQUEST_TAKE_CAMERA_PHOTO && resultCode == RESULT_OK) {

            val capturedImgBitmap = data!!.extras!!.get("data") as Bitmap
            uploadCameraImage(capturedImgBitmap)

        } else if (requestCode == REQUEST_DOCUMENT && resultCode == RESULT_OK) {

            var fileName: String? = null

            //get document filename with extension
            data!!.data?.let { returnUri ->
                contentResolver.query(returnUri, null, null, null, null)
            }?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
            }

            documentUri = data.data!!
            uploadDocument(documentUri, fileName!!)

        }
    }

    private fun uploadDocument(documentUri: Uri, fileName: String) {

        val storageReference =
            FirebaseStorage.getInstance().getReference("chats/$chatId/documents/$fileName")

        uploadTask = storageReference.putFile(documentUri)

        val urlTask: Task<Uri?> = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                sendMessage(fileName!!, "", downloadUri.toString())
            } else {
                Log.d("ErrDownUrl", task.exception.toString())
            }
        }
    }

    private fun uploadGalleryImage() {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        val storageReference =
            FirebaseStorage.getInstance().getReference("chats/$chatId/media/$fileName")

        uploadTask = storageReference.putFile(imageUri)

        val urlTask: Task<Uri?> = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                sendMessage("", downloadUri.toString(), "")
            } else {
                Log.d("ErrDownUrl", task.exception.toString())
            }
        }

    }

    fun uploadCameraImage(capturedImage: Bitmap) {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        val storageReference =
            FirebaseStorage.getInstance().getReference("chats/$chatId/media/$fileName")

        val baos = ByteArrayOutputStream()
        capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        uploadTask = storageReference.putBytes(data)

        val urlTask: Task<Uri?> = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                sendMessage("", downloadUri.toString(), "")
            } else {
                Log.d("ErrDownUrl", task.exception.toString())
            }
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