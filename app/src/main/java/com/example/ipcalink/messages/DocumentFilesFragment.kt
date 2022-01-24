package com.example.ipcalink.messages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentDocumentFilesBinding
import com.google.firebase.firestore.FirebaseFirestore

class DocumentFilesFragment : Fragment() {

    private lateinit var binding: FragmentDocumentFilesBinding
    private lateinit var chatId: String
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDocumentFilesBinding.inflate(layoutInflater)
        chatId = requireArguments().getString("chatId").toString()




        // Inflate the layout for this fragment
        return binding.root
    }

}