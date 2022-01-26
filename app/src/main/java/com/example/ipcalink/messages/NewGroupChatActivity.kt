package com.example.ipcalink.messages

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityNewGroupChatBinding
import com.example.ipcalink.models.Chats
import com.example.ipcalink.models.PrivateUserChat
import com.example.ipcalink.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class NewGroupChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityNewGroupChatBinding
    private var currentUserInfo = User()
    private lateinit var horizontalLinearLayoutManager: LinearLayoutManager
    private lateinit var verticalLinearLayoutManager: LinearLayoutManager
    private lateinit var authUserUid: String
    private var newGroupChatID = ""
    lateinit var uploadTask: UploadTask
    var searchedUsersList = mutableListOf<User>()
    var selectedSearchedUsersList = mutableListOf<User>()
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NewGroupChatAdapter
    private lateinit var selectedUsersAdapter: SelectedUsersAdapter
    private val REQUEST_TAKE_GALLERY_PHOTO = 2
    lateinit var imageUri: Uri
    var photoUrl = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNewGroupChatBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //remove top bar better
        supportActionBar?.hide()

        horizontalLinearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        verticalLinearLayoutManager = LinearLayoutManager(this)

        //instatiate firestore object
        db = FirebaseFirestore.getInstance()

        //get current user uid
        authUserUid = FirebaseAuth.getInstance().currentUser!!.uid



        binding.ibBackButton.setOnClickListener {
            finish()
        }

        //get current user info
        db.collection("users").document(authUserUid).get()
            .addOnSuccessListener {
                currentUserInfo = it.toObject<User>()!!
                Log.d("userInfoEmail", currentUserInfo.email)
            }

        binding.circleImageView6.setOnClickListener {
            selectImage()
        }

        adapter = NewGroupChatAdapter {
            if(!selectedSearchedUsersList.contains(it)) {
                selectedSearchedUsersList.add(it)
            } else {
                selectedSearchedUsersList.remove(it)
            }
            selectedUsersAdapter.notifyDataSetChanged()
        }

        selectedUsersAdapter = SelectedUsersAdapter {
            selectedSearchedUsersList.remove(it)
            selectedUsersAdapter.notifyDataSetChanged()
        }

        binding.fabAddGroupChat.setOnClickListener {
            if(binding.etGroupChatName.text.isNullOrEmpty()) {
                Toast.makeText(this@NewGroupChatActivity, "Deve dar um nome ao seu novo chat de grupo.", Toast.LENGTH_SHORT).show()
            } else if(selectedSearchedUsersList.size == 0) {
                Toast.makeText(this@NewGroupChatActivity, "Deve selecionar pelo menos um utilizador.", Toast.LENGTH_SHORT).show()
            } else {
                createNewGroupChat()
            }
        }

        binding.rvSelectedUsers.layoutManager = horizontalLinearLayoutManager
        binding.rvSelectedUsers.adapter = selectedUsersAdapter

        binding.rvSearchedUsers.layoutManager = verticalLinearLayoutManager
        binding.rvSearchedUsers.adapter = adapter

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
    }

    inner class NewGroupChatAdapter(private val clickListener: (User) -> Unit) :
        RecyclerView.Adapter<NewGroupChatActivity.NewGroupChatAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val row = LayoutInflater.from(baseContext)
                .inflate(R.layout.new_group_chat_row, parent, false)
            return MyViewHolder(row)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val user = searchedUsersList[position]

            //set image chat row
            if (user.photoUrl.isNotEmpty()) {
                try {
                    Glide.with(baseContext).load(user.photoUrl).into(holder.newGroupChatImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                holder.newGroupChatImage.setImageResource(R.drawable.padrao)
            }

            /*
            if (user.isOnline == true) {
                holder.newChatIsOnline.visibility = View.VISIBLE
            } else {
                holder.newChatIsOnline.visibility = View.GONE
            }*/

            holder.newGroupChatUsername.text = user.name
            holder.newGroupChatEmail.text = user.email

            holder.itemView.setOnClickListener {
                clickListener(searchedUsersList[position])
                holder.newGroupChatIsCheck.isChecked = !holder.newGroupChatIsCheck.isChecked
            }

        }

        override fun getItemCount(): Int {
            return searchedUsersList.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var newGroupChatImage: CircleImageView = itemView.findViewById(R.id.rowNewGroupChatIv)
            var newGroupChatUsername: TextView = itemView.findViewById(R.id.rowNewGroupChatUsername)
            var newGroupChatEmail: TextView = itemView.findViewById(R.id.rowNewGroupChatEmail)
            var newGroupChatIsCheck: CheckBox = itemView.findViewById(R.id.userSelectedCheckBox)
        }

    }

    inner class SelectedUsersAdapter(private val clickListener: (User) -> Unit) :
        RecyclerView.Adapter<NewGroupChatActivity.SelectedUsersAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val row = LayoutInflater.from(baseContext)
                .inflate(R.layout.selected_user_row, parent, false)
            return MyViewHolder(row)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val user = selectedSearchedUsersList[position]

            //set image chat row
            if (user.photoUrl.isNotEmpty()) {
                try {
                    Glide.with(baseContext).load(user.photoUrl).into(holder.newGroupChatSelectedUserImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                holder.newGroupChatSelectedUserImage.setImageResource(R.drawable.padrao)
            }

            holder.newGroupChatSelecterUsername.text = user.name

            holder.itemView.setOnClickListener {
                clickListener(selectedSearchedUsersList[position])
            }

        }

        override fun getItemCount(): Int {
            return selectedSearchedUsersList.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var newGroupChatSelectedUserImage: CircleImageView = itemView.findViewById(R.id.ivSelectedUserHorizontal)
            var newGroupChatSelecterUsername: TextView = itemView.findViewById(R.id.tvSelectedUserName)
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
                    searchedUsersList.add(document.toObject())
                }
                adapter.notifyDataSetChanged()
                //adapter.notifyItemInserted(adapter.itemCount - 1)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting searched users: ", exception)
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
                adapter.notifyDataSetChanged()
                //adapter.notifyItemInserted(adapter.itemCount - 1)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting searched users: ", exception)
            }
    }

    fun createNewGroupChat() {
        //create main chat document
        newGroupChatID = db.collection("chats").document().id

        val groupUsersFirestoreRefs = mutableListOf<DocumentReference>()
        val mainChatUsersFirestoreRefs = mutableListOf<DocumentReference>()

        //create objects
        val newChat = Chats(newGroupChatID, binding.etGroupChatName.text.toString(), "group", "", "", photoUrl)

        val userChat = PrivateUserChat(
            newGroupChatID, binding.etGroupChatName.text.toString(), "group", photoUrl, "", "",
            null
        )

        //create main references
        val mainChat = db.collection("chats").document(newGroupChatID)

        //create references to save chats to users chats branch
        selectedSearchedUsersList.add(currentUserInfo)
        for(selectedUser in selectedSearchedUsersList) {
            groupUsersFirestoreRefs.add(db.collection("users").document(selectedUser.userId)
                .collection("chats").document(newGroupChatID))
        }


        // Get a new write batch and commit all write operations
        db.runBatch { batch ->
            // Set the main chat branch
            batch.set(mainChat, newChat)

            // Set the users chats branches
            for(reference in groupUsersFirestoreRefs) {
                batch.set(reference, userChat)
            }

            //set the users in main branch
            for(selectedUser in selectedSearchedUsersList) {
                val mainChatUserDocRef = db.collection("chats").document(newGroupChatID).collection("users")
                    .document(selectedUser.userId)
                batch.set(mainChatUserDocRef, selectedUser)
            }

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, GroupChatActivity::class.java)
                intent.putExtra("chatId", newGroupChatID)
                intent.putExtra("chatName", binding.etGroupChatName.text.toString())
                intent.putExtra("chatPhotoUrl", photoUrl)
                startActivity(intent)
                finish()
            } else {
                Log.d("Error adding main chat!", task.exception.toString())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_GALLERY_PHOTO && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            binding.circleImageView6.setImageURI(imageUri)
            uploadGalleryImage()
        }
    }

    private fun uploadGalleryImage() {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        val storageReference =
            FirebaseStorage.getInstance().getReference("chats/$newGroupChatID/media/$fileName")

        uploadTask = storageReference.putFile(imageUri)

        val urlTask: Task<Uri?> = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                photoUrl = task.result.toString()
                Log.d("photo", photoUrl)
            } else {
                Log.d("ErrDownUrl", task.exception.toString())
            }
        }

    }

}