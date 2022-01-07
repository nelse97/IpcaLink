package com.example.ipcalink.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.ipcalink.MainActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentBoarding3Binding

class Boarding3Fragment : Fragment() {

    private lateinit var binding : FragmentBoarding3Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBoarding3Binding.inflate(layoutInflater)

        //start main activity
        binding.buttonNext.setOnClickListener {

            val sp = activity?.getSharedPreferences("firstlogin", Activity.MODE_PRIVATE)
            val editor = sp?.edit()
            editor?.putBoolean("firstlogin", false)
            editor?.apply()

            activity?.startActivity(Intent(activity, MainActivity::class.java))
            activity?.finish()
        }

        return binding.root
    }
}