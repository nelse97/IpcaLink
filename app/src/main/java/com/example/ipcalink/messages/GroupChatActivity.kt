package com.example.ipcalink.messages

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ablanco.zoomy.ZoomListener
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import com.example.chatappfirebase.Models.Message
import com.example.ipcalink.R
import com.example.ipcalink.calendar.CalendarHelper
import com.example.ipcalink.databinding.ActivityGroupChatBinding
import com.example.ipcalink.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GroupChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityGroupChatBinding
    private lateinit var authUserUid: String
    private lateinit var db: FirebaseFirestore
    private lateinit var dbUpdateLastMessage: FirebaseFirestore
    lateinit var chatId: String
    lateinit var chatName: String
    lateinit var photoUrl: String
    enum class FabState {
        MESSAGE, MEDIACONTENT
    }
    private lateinit var fabState: FabState
    private val REQUEST_TAKE_GALLERY_PHOTO = 2
    private val REQUEST_TAKE_CAMERA_PHOTO = 3
    private val REQUEST_DOCUMENT = 4
    lateinit var imageUri: Uri
    lateinit var documentUri: Uri
    lateinit var uploadTask: UploadTask
    private var fbaClicked = false
    var messagesList = mutableListOf<Message>()
    var groupUsersList = mutableListOf<User>()
    private lateinit var adapter: GroupMessagesAdapter
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
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //remove top bar better
        supportActionBar?.hide()

        //get current user uid
        authUserUid = FirebaseAuth.getInstance().currentUser!!.uid

        //instantiate firestore object
        db = FirebaseFirestore.getInstance()

        dbUpdateLastMessage = FirebaseFirestore.getInstance()

        //get intent extras from chats screen
        chatId = intent.getStringExtra("chatId").toString()
        chatName = intent.getStringExtra("chatName").toString()
        if (!intent.getStringExtra("chatPhotoUrl").isNullOrEmpty()) {
            photoUrl = intent.getStringExtra("chatPhotoUrl")!!
            Glide.with(this).load(photoUrl).into(binding.circleImageTopChat)
        }

        binding.textViewTopChatName.text = chatName

        //get group chat users info
        dbUpdateLastMessage.collection("chats").document(chatId).collection("users")
            .get()
            .addOnSuccessListener { users ->
                for (user in users!!) {
                    val newUser = user.toObject<User>()
                    groupUsersList.add(newUser)
                    Log.d("email", newUser.email)
                }
            }

        //set LinearLayoutManager
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true

        binding.rvGroupChat.layoutManager = linearLayoutManager

        binding.rvGroupChat.adapter = adapter

        binding.fabPrivateChat.setOnClickListener {
            if (fabState == FabState.MESSAGE) {
                sendMessage(binding.etMessageGroupChat.text.toString(), null, "")
                binding.etMessageGroupChat.setText("")
            } else {
                //opens 4 fba buttons
                //binding.fabPrivateChat.visibility = View.GONE
                setVisibility()
                setAnimation()
                fbaClicked = !fbaClicked
            }
        }

        binding.fabGallery.setOnClickListener {
            selectImage()
        }

        binding.fabCamera.setOnClickListener {
            selectCamera()
        }

        binding.fabDocument.setOnClickListener {
            selectDocument()
        }

        binding.etMessageGroupChat.addTextChangedListener(object :
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
    }

    private fun sendMessage(message: String, photoUrl: String?, documentUrl: String?) {
        val newMessage = Message()

        newMessage.senderId = authUserUid
        newMessage.unreadCount = 1

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

        val groupUsersFirestoreRefs = mutableListOf<DocumentReference>()

        for(user in groupUsersList) {
            groupUsersFirestoreRefs.add(dbUpdateLastMessage.collection("users").document(user.userId)
                .collection("chats").document(chatId))
        }

        dbUpdateLastMessage.runBatch { batch ->
            // Update last message on sender chat
            for(ref in groupUsersFirestoreRefs) {
                batch.update(ref, "lastMessage", lastMessage)
                batch.update(ref, "lastMessageTimestamp", lastMessageTimestamp)
            }

        }.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("Error saving lastMessage", task.exception.toString())
            }
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
                    return@addSnapshotListener
                } else {
                    messagesList.clear()
                    for (message in messages!!) {
                        val newMessage = message.toObject<Message>()
                        messagesList.add(newMessage)
                    }
                    //adapter.notifyItemInserted(adapter.itemCount - 1)
                    adapter.notifyDataSetChanged()
                    binding.rvGroupChat.scrollToPosition(adapter.itemCount - 1)
                }
            }
    }

    override fun onStop() {
        super.onStop()
        db.clearPersistence()
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

    private fun setAnimation() {
        if (!fbaClicked) {
            binding.fabPrivateChat.startAnimation(rotateOpen)
        } else {
            binding.fabPrivateChat.startAnimation(rotateClose)
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

    inner class GroupMessagesAdapter() :
        RecyclerView.Adapter<GroupChatActivity.GroupMessagesAdapter.MyViewHolder>() {

        private val SENDER_TYPE = 0
        private val RECIPIENT_TYPE = 1

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): GroupMessagesAdapter.MyViewHolder {

            val item = if (viewType == SENDER_TYPE) {
                LayoutInflater.from(this@GroupChatActivity)
                    .inflate(R.layout.row_message_sender, parent, false)
            } else {
                LayoutInflater.from(this@GroupChatActivity)
                    .inflate(R.layout.row_message_recipient, parent, false)
            }

            return MyViewHolder(item)
        }

        override fun onBindViewHolder(holder: GroupMessagesAdapter.MyViewHolder, position: Int) {
            val message = messagesList[position]
            val date = CalendarHelper.getDate(message.timestamp!!.seconds * 1000, "HH:mm")
            holder.tvMessageBody.text = message.body
            holder.tvMessageTime.text = date

            if (message.photoUrl != "") {

                // Create a reference to a file from a Google Cloud Storage URI
                holder.tvMessageBody.visibility = View.GONE
                holder.tvMessageIv.visibility = View.VISIBLE
                Glide.with(this@GroupChatActivity)
                    .load(message.photoUrl)
                    .into(holder.tvMessageIv)


                val builder: Zoomy.Builder = Zoomy.Builder(this@GroupChatActivity)
                    .target(holder.tvMessageIv)
                    .tapListener {
                        //View tapped, do stuff
                    }
                    .longPressListener {
                        //View long pressed, do stuff
                    }.doubleTapListener {
                        //View double tapped, do stuff
                    }
                    .zoomListener(object : ZoomListener {
                        override fun onViewStartedZooming(view: View) {
                        }

                        override fun onViewEndedZooming(view: View) {
                            //View ended zooming
                        }
                    })

                builder.register()
            } else {
                holder.tvMessageIv.setImageURI(null)
                holder.tvMessageIv.visibility = View.GONE
                holder.tvMessageBody.visibility = View.VISIBLE
            }

            if (message.documentUrl != "") {
                holder.tvMessageIconDownload.visibility = View.VISIBLE
                holder.tvMessageIconDownload.setOnClickListener {
                    downloadFile(this@GroupChatActivity, message.documentUrl, message.body)
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
}