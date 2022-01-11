package com.example.ipcalink

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ipcalink.databinding.ActivityMainBinding
import com.example.ipcalink.fragments.CalendarFragment
import com.example.ipcalink.fragments.MessagesFragment
import com.example.ipcalink.fragments.ProfileFragment
import com.example.ipcalink.fragments.ReminderFragment
import com.example.ipcalink.login.LoginActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val messagesFragment = MessagesFragment()
    private val calendarFragment = CalendarFragment()
    private val reminderFragment = ReminderFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        replaceFragment(messagesFragment)

        //load user profile image
        //TO DO

        //Bottom app bar top left and top right corner radius
        val bottomAppBar = binding.bottomAppBar
        val bottomBarBackground = bottomAppBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, 60f)
            .setTopLeftCorner(CornerFamily.ROUNDED, 60f)
            .build()

        binding.fab.setOnClickListener {

        }

        binding.ibMessages.setOnClickListener {

            replaceFragment(messagesFragment)
            disableAllUnderlines()
            replaceAllBottomNavIcons()
            binding.mainBottomNavMessagesUnderline.visibility = View.VISIBLE
            binding.ibMessages.setImageResource(R.drawable.ic_selected_messages_icon)
        }

        binding.ibCalendar.setOnClickListener {
            replaceFragment(calendarFragment, )
            disableAllUnderlines()
            replaceAllBottomNavIcons()
            binding.mainBottomNavCalendarUnderline.visibility = View.VISIBLE
            binding.ibCalendar.setImageResource(R.drawable.ic_selected_calendar_icon)
        }

        binding.ibReminder.setOnClickListener {
            replaceFragment(reminderFragment, )
            disableAllUnderlines()
            replaceAllBottomNavIcons()
            binding.mainBottomNavReminderUnderline.visibility = View.VISIBLE
            binding.ibReminder.setImageResource(R.drawable.ic_selected_reminder_icon)
        }

        binding.ibProfileImage.setOnClickListener {
            replaceFragment(profileFragment, )
            disableAllUnderlines()
            replaceAllBottomNavIcons()
            binding.mainBottomNavProfileUnderline.visibility = View.VISIBLE
        }


        /*binding.bottomNavigationBar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.menuIconMessages -> replaceFragment(messagesFragment)
                R.id.menuIconCalendar -> replaceFragment(calendarFragment)
                R.id.menuIconReminders -> replaceFragment(reminderFragment)
                R.id.menuIconProfile -> replaceFragment(profileFragment)
            }
            true
        }

        val badgeDrawable: BadgeDrawable =
            binding.mainBottomNavView.getOrCreateBadge(R.id.menuIconCalendar) // menu item id

        badgeDrawable.apply {
            number = 3
        }

        val badgeDrawable2: BadgeDrawable =
            binding.mainBottomNavView.getOrCreateBadge(R.id.menuIconProfile) // menu item id

        badgeDrawable2.apply {
            number = 4
        }

        val badgeDrawable3: BadgeDrawable =
            binding.mainBottomNavView.getOrCreateBadge(R.id.menuIconReminders) // menu item id

        badgeDrawable3.apply {
            number = 5
        }

        binding.mainBottomNavView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuIconMessages -> {
                    replaceFragment(messagesFragment)
                    binding.mainBottomNavView.getBadge(item.itemId)?.let { badgeDrawable ->
                        if(badgeDrawable.isVisible) {
                            binding.mainBottomNavView.removeBadge(item.itemId)
                        }
                    }
                    true
                }
                R.id.menuIconCalendar -> {
                    replaceFragment(calendarFragment)
                    binding.mainBottomNavView.getBadge(item.itemId)?.let { badgeDrawable ->
                        if(badgeDrawable.isVisible) {
                            binding.mainBottomNavView.removeBadge(item.itemId)
                        }
                    }
                    true
                }
                R.id.menuIconReminders -> {
                    replaceFragment(reminderFragment)
                    binding.mainBottomNavView.getBadge(item.itemId)?.let { badgeDrawable ->
                        if(badgeDrawable.isVisible) {
                            binding.mainBottomNavView.removeBadge(item.itemId)
                        }
                    }
                    true
                }
                R.id.menuIconProfile -> {
                    replaceFragment(profileFragment)
                    binding.mainBottomNavView.getBadge(item.itemId)?.let { badgeDrawable ->
                        if(badgeDrawable.isVisible) {
                            binding.mainBottomNavView.removeBadge(item.itemId)
                        }
                    }
                    true
                }
                else -> false
            }
        }*/
    }

    private fun replaceFragment(fragment: Fragment?) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameContainer, fragment)
            transaction.commit()

        }
    }

    private fun disableAllUnderlines() {
        binding.mainBottomNavMessagesUnderline.visibility = View.INVISIBLE
        binding.mainBottomNavCalendarUnderline.visibility = View.INVISIBLE
        binding.mainBottomNavReminderUnderline.visibility = View.INVISIBLE
        binding.mainBottomNavProfileUnderline.visibility = View.INVISIBLE
    }

    private fun enableAllUnderlines() {
        binding.mainBottomNavMessagesUnderline.visibility = View.VISIBLE
        binding.mainBottomNavCalendarUnderline.visibility = View.VISIBLE
        binding.mainBottomNavReminderUnderline.visibility = View.VISIBLE
        binding.mainBottomNavProfileUnderline.visibility = View.VISIBLE
    }

    private fun replaceAllBottomNavIcons() {
        binding.ibMessages.setImageResource(R.drawable.ic_unselected_messages_icon)
        binding.ibCalendar.setImageResource(R.drawable.ic_unselected_calendar_icon)
        binding.ibReminder.setImageResource(R.drawable.ic_unselected_reminder_icon)
    }

    fun signOut(view: View) {
        Firebase.auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


}