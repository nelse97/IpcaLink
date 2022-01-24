package com.example.ipcalink.messages

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ipcalink.R
import com.example.ipcalink.databinding.FragmentMessagesBinding
import com.example.ipcalink.fragments.SchoolMessagesFragment

class MessagesFragment : Fragment() {

    private val privateMessagesFragment = PrivateMessagesFragment()
    private val schoolMessagesFragment = SchoolMessagesFragment()
    private lateinit var binding: FragmentMessagesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMessagesBinding.inflate(layoutInflater)

        //initialize private messages fragment
        replaceFragment(privateMessagesFragment)

        binding.tvPrivateMessagesTopBar.setTypeface(null, Typeface.BOLD)
        binding.tvSchoolMessagesTopBar.setTypeface(null, Typeface.NORMAL)

        binding.fabAddPrivateMessage.setOnClickListener {
            startActivity(Intent(context, NewMessageActivity::class.java))
        }

        binding.llPrivateMessages.setOnClickListener {
            replaceFragment(privateMessagesFragment)

            //remove highlighted attributes from school chat view
            binding.tvSchoolMessagesTopBar.setTypeface(null, Typeface.NORMAL)
            binding.tvSchoolMessagesTopBar.setTextColor(resources.getColor(R.color.black))
            binding.underlineSchoolMessagesHighlight.visibility = View.INVISIBLE

            //add highlighted attributes to private chat view
            binding.tvPrivateMessagesTopBar.setTypeface(null, Typeface.BOLD)
            binding.tvPrivateMessagesTopBar.setTextColor(resources.getColor(R.color.colorPrimary))
            binding.underlinePrivateMessagesHighlight.visibility = View.VISIBLE
        }

        binding.llSchoolMessages.setOnClickListener {
            replaceFragment(schoolMessagesFragment)

            //remove highlighted attributes from private chat view
            binding.tvPrivateMessagesTopBar.setTypeface(null, Typeface.NORMAL)
            binding.tvPrivateMessagesTopBar.setTextColor(resources.getColor(R.color.black))
            binding.underlinePrivateMessagesHighlight.visibility = View.INVISIBLE

            //add highlighted attributes to school chat view
            binding.tvSchoolMessagesTopBar.setTypeface(null, Typeface.BOLD)
            binding.tvSchoolMessagesTopBar.setTextColor(resources.getColor(R.color.colorPrimary))
            binding.underlineSchoolMessagesHighlight.visibility = View.VISIBLE
        }

        // return the fragment layout
        return binding.root
    }

    private fun replaceFragment(fragment: Fragment?) {
        if (fragment != null) {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.messagesFrameContainer, fragment)
            transaction.commit()
        }
    }
}