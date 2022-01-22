package com.example.ipcalink.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentProfileBinding
import com.example.ipcalink.login.LoginActivity
import com.example.ipcalink.models.Events
import com.example.ipcalink.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var _binding : FragmentProfileBinding
    private val binding get() = _binding!!
    private val dbFirebase = Firebase.firestore
    private val userID = Firebase.auth.uid
    lateinit var userInfo: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Inflate the layout for this fragment
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignOut.setOnClickListener {
            Firebase.auth.signOut()
            activity?.startActivity(Intent(activity, LoginActivity::class.java ))
            activity?.finish()
        }
        binding.buttonEdit.setOnClickListener {
            binding.buttonEdit.setVisibility(View.INVISIBLE)
            binding.buttonSave.setVisibility(View.VISIBLE)
            binding.textViewBio.setVisibility(View.INVISIBLE)
            binding.editTextBio.setVisibility(View.VISIBLE)
            binding.editTextBio.requestFocus()
        }
        binding.buttonSave.setOnClickListener {
            binding.buttonSave.setVisibility(View.INVISIBLE)
            binding.buttonEdit.setVisibility(View.VISIBLE)
            binding.editTextBio.setVisibility(View.INVISIBLE)
            binding.textViewBio.setVisibility(View.VISIBLE)
            userInfo.bio = binding.editTextBio.getText().toString()
            binding.textViewBio.text = userInfo.bio

            val userDb = dbFirebase.collection("users").document(userID!!)
            val userNewInfo = User(userInfo.userId,userInfo.name, userInfo.photoURl, userInfo.email, userInfo.bio, userInfo.isOnline!!, userInfo.lastSeen!!).toHash()

            userDb.update(userNewInfo).addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.d("", "Error adding event to chat: $userDb")
                } else {
                    Log.d("", "Event added to chat: $userDb")
                }
            }
        }

        dbFirebase.collection("users").document(userID!!).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.textViewName.text = document.getString("name")
                    binding.textViewEmail.text = document.getString("email")
                    binding.textViewBio.text = document.getString("bio")
                    binding.editTextBio.setText(document.getString("bio"))
                    userInfo = User.fromHashDoc(document)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }
}
