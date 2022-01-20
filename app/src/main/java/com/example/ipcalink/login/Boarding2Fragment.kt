package com.example.ipcalink.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentBoarding2Binding

class Boarding2Fragment : Fragment() {

    private lateinit var binding : FragmentBoarding2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBoarding2Binding.inflate(layoutInflater)

        //navegate next fragment
        binding.buttonNext.setOnClickListener {
            view?.findNavController()?.navigate(R.id.navigation_boarding3)
        }

        return binding.root
    }
}