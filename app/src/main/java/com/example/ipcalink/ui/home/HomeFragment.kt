package com.example.ipcalink.ui.home

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ipcalink.StartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentHomeBinding
import java.io.FileDescriptor


class HomeFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    //Realtime Database
    private val realDatabase = FirebaseDatabase.getInstance("https://fir-demo-b169d-default-rtdb.europe-west1.firebasedatabase.app")
    //Firestore Database
    private val dbFirebase = Firebase.firestore

    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private val list : ArrayList<String> = ArrayList()

    private var bitmap : Bitmap? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Hides top bar
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        auth = Firebase.auth

        mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = mLayoutManager
        mAdapter = Adapter()
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.adapter = mAdapter


        binding.buttonAdd.visibility = View.GONE

        binding.buttonAdd.setOnClickListener {
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val storage = Firebase.storage
            val storageRef = storage.reference
            val filename = "${UUID.randomUUID()}.jpg"
            val mountainImagesRef = storageRef.child("images/${Firebase.auth.currentUser?.uid}/$filename")

            val uploadTask = mountainImagesRef.putBytes(data)
            uploadTask.continueWithTask {task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                mountainImagesRef.downloadUrl
            }
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads

            }.addOnSuccessListener { task ->
                storageRef.child("images/${Firebase.auth.currentUser?.uid}/$filename").downloadUrl.addOnSuccessListener {
                    // Got the download URL for 'users/me/profile.png'

                    val downloadUri = it.toString()

                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${downloadUri}")

                }.addOnFailureListener {
                    // Handle any errors
                }
            }
        }

        binding.buttonSelect.setOnClickListener {

            //val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val intent  = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_REQUEST)
        }

        binding.buttonLogOut.setOnClickListener {
            logOut()
        }

        //database.reference.child("ProgrammingKnowledge").child("Android").setValue("abcd")

        /*val map : HashMap<String, Any> = HashMap()
        map["Name"] = "Jorge"
        map["Email"] = "jorgemiguelsa12@zonmail.pt"

        realDatabase.reference.child("ProgrammingKnowledge").child("MultipleValues").updateChildren(map)*/

        /*
        val map : HashMap<String, Any> = HashMap()
        map["n1"] = "Java"
        map["n2"] = "Kotlin"
        map["n3"] = "Flutter"
        map["n4"] = "React Native"
        */

        //database.reference.child("Languages").updateChildren(map)

        /*val dbRef = realDatabase.reference.child("Information")

        // Read from the database
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                list.clear()

                for (snapshot in dataSnapshot.children){
                    val info: Information = snapshot.getValue(Information::class.java)!!
                    val txt = info.getName() + " : " + info.getEmail()
                    list.add(txt)
                }

                mAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Toast.makeText(this@MainActivity, "Canceled", Toast.LENGTH_SHORT).show()
            }
        })*/


        /*dbFirebase.collection("cities").document("JSR").addSnapshotListener { value, error ->

        }*/

        /*dbFirebase.collection("cities").whereEqualTo("capital" , false).get().addOnCompleteListener {
            for (query in it.result!!){
                if (it.isSuccessful) {
                    Log.d("Document", query.id + "=>" + query.data)
                }
            }
        }*/

        /*val docRef : DocumentReference = dbFirebase.collection("cities").document("SF")

        docRef.get().addOnCompleteListener {
            val doc = it.result

            if (doc!!.exists()) {
                Log.d("Document", doc.data.toString())
            } else {
                Log.d("Document", "No data")
            }
        }*/

        /*val ref : DocumentReference = dbFirebase.collection("cities").document("JSR")
        ref.update("Capitol", true)*/

        /*val data : HashMap<String, Any> = HashMap()
        data["name"] = "Tokyo"
        data["capital"] = "Japan"

        dbFirebase.collection("cities").add(data).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "City added successfully", Toast.LENGTH_SHORT).show()
            }
        }*/

        /*val data : HashMap<String, Any> = HashMap()
        data["Capitol"] = false

        dbFirebase.collection("cities").document("JSR").set(data, SetOptions.merge()).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Data added to cities successfully", Toast.LENGTH_SHORT).show()
            }
        }*/

        /*val city : HashMap<String, Any> = HashMap()
        city["name"] = "fghfgh"
        city["state"] = "asdvbfddasf"
        city["county"] = "India"

        dbFirebase.collection("cities").document("JSR").set(city).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "City added successfully", Toast.LENGTH_SHORT).show()
            }
        }*/

        /*val curso : HashMap<String, Any> = HashMap()
        curso["ano"] = "2"
        curso["nome"] = "ammmmmmmmm"
        curso["semestre"] = "1"*/


        /*val db = dbFirebase.collection("ipca").document("cKWkNSInqwLG9EK00SjM").collection("cursos").document("6uTrC0LKdNOJkFOiPuud")

        db.update(curso)

        db.get().addOnCompleteListener {
            val doc = it.result

            val ano = doc?.get("ano")

            if (doc!!.exists()) {
                Log.d("Document", ano.toString())
            } else {
                Log.d("Document", "No data")
            }
        }*/

        /*dbFirebase.collection("ipca").
        document("cKWkNSInqwLG9EK00SjM").collection("cursos").
        whereEqualTo("nome", "am").get().addOnCompleteListener {
            for (query in it.result!!){
                if (it.isSuccessful) {
                    Log.d("Document", query.id + "=>" + query.data)
                }
            }
        }*/

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                IMAGE_REQUEST -> {
                    val imageUri = data?.data

                    println("#######imageUri###########")
                    println(imageUri)

                    val parcelFileDescriptor =
                        imageUri?.let { requireContext().contentResolver.openFileDescriptor(it, "r") }
                    val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
                    val bm = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                    parcelFileDescriptor!!.close()

                    bm?.let {
                        binding.buttonAdd.visibility = View.VISIBLE
                        bitmap = bm
                    }
                }
            }
        } else {
            println("Data VALUE")
            println(data?.extras?.get("data"))
            println("resultCode")
            println(resultCode)
        }
    }

    private fun logOut() {
        auth.signOut()
        Toast.makeText(context, "User Signed Out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(context, StartActivity::class.java))
        //finish()
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {
                val textViewLabel = findViewById<TextView>(R.id.label)
                textViewLabel.text = list[position]
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    companion object {
        const val IMAGE_REQUEST = 1001
        const val CAMERA_PIC_REQUEST = 1002
    }
}