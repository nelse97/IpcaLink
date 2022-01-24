package com.example.ipcalink.messages

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityFilesBinding

class FilesActivity : AppCompatActivity() {

    lateinit var binding: ActivityFilesBinding
    lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //remove top bar better
        supportActionBar?.hide()

        chatId = intent.getStringExtra("chatId").toString()

        //initialize first fragment
        replaceFragment(MultimediaFilesFragment())

        binding.tvDocuments.setTypeface(null, Typeface.DEFAULT.style)
        binding.tvDocuments.setTextColor(resources.getColor(R.color.black))
        binding.tvMultimedia.setTypeface(null, Typeface.BOLD)
        binding.tvMultimedia.setTextColor(resources.getColor(R.color.colorPrimary))

        binding.tvMultimedia.setOnClickListener {
            //remove highlighted attributes documents textView
            binding.tvDocuments.setTypeface(null, Typeface.DEFAULT.style)
            binding.tvDocuments.setTextColor(resources.getColor(R.color.black))

            binding.tvMultimedia.setTypeface(null, Typeface.BOLD)
            binding.tvMultimedia.setTextColor(resources.getColor(R.color.colorPrimary))

            replaceFragment(MultimediaFilesFragment())
        }

        binding.tvDocuments.setOnClickListener {
            //remove highlighted attributes multimedia textView
            binding.tvMultimedia.setTypeface(null, Typeface.DEFAULT.style)
            binding.tvMultimedia.setTextColor(resources.getColor(R.color.black))

            binding.tvDocuments.setTypeface(null, Typeface.BOLD)
            binding.tvDocuments.setTextColor(resources.getColor(R.color.colorPrimary))

            replaceFragment(DocumentFilesFragment())
        }

        binding.ivBackButton.setOnClickListener {
            finish()
        }

    }

    private fun replaceFragment(fragment: Fragment?) {
        if (fragment != null) {
            val bundle = Bundle()
            bundle.putString("chatId", chatId)
            fragment.arguments = bundle
            val fm: FragmentManager = supportFragmentManager
            fm.beginTransaction().replace(R.id.filesMainFrameLayout, fragment).commit()
        }
    }
}