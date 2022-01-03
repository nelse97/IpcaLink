package com.example.ipcalink.Calendar

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.AES.AES
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityAddEventBinding
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.example.ipcalink.models.Event
import com.example.ipcalink.notifications.PushNotificationFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.time.temporal.TemporalQueries.localDate
import kotlin.collections.ArrayList


class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding

    private val dbFirebase = Firebase.firestore

    private val myLocale = Locale("pt", "PT")

    var calendar = Calendar.getInstance()

    //private lateinit var encryptedTitle : String
    //private lateinit var encryptedDescription : String
    private lateinit var ivString : String
    private var secretKeyString = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onStart() {
        super.onStart()

        Locale.setDefault(myLocale)

        val dateString = intent.getStringExtra("date")

        val date = LocalDate.parse(dateString)

        calendar.set(date.year, date.monthValue-1, date.dayOfMonth)

        val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
        binding.editTextStartDate.setText(dateFormattedString)
        binding.editTextEndDate.setText(dateFormattedString)


        binding.addButton.setOnClickListener {
            if(!binding.editTextTitle.text.isNullOrEmpty() &&
                !binding.editTextDecription.text.isNullOrEmpty() &&
                !binding.editTextStartDate.text.isNullOrEmpty() &&
                !binding.editTextEndDate.text.isNullOrEmpty() &&
                !binding.editTextStartTime.text.isNullOrEmpty() &&
                !binding.editTextEndTime.text.isNullOrEmpty()  ) {


                val title = binding.editTextTitle.text.toString()
                val description = binding.editTextDecription.text.toString()

                val startDateString = CalendarDateFormatter(binding.editTextStartDate.text.toString()) + " " + binding.editTextStartTime.text.toString()
                val endDateString = CalendarDateFormatter(binding.editTextEndDate.text.toString()) + " " + binding.editTextEndTime.text.toString()

                println(startDateString)

                val startDateLong = Date.parse(startDateString)
                val startDate = Date(startDateLong)
                val timeStampStart = Timestamp(startDate)


                val endDateLong = Date.parse(endDateString)
                val endDate = Date(endDateLong)
                val timeStampEnd = Timestamp(endDate)


                //Send Date needs to be formatted in this way 14/11/2021 16:38
                val calendar = Calendar.getInstance()
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                val sendDate = format.format(calendar.time)


                GlobalScope.launch(Dispatchers.IO) {
                    //EventEncryption(this@AddEventActivity, title, description)


                    sendEventToFirebase(title, description, sendDate, "gh", timeStampStart, timeStampEnd, "Web Design")
                }

            } else if (binding.editTextTitle.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira um título", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.editTextDecription.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira uma descrição", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.editTextStartDate.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira a data de inicio", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.editTextEndDate.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira a data de fim", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.editTextStartTime.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira a hora de inicio", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.editTextEndTime.text.isNullOrEmpty()) {
                val toast =
                    Toast.makeText(this, "Por favor insira a hora de fim", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else {
                val toast = Toast.makeText(this, "Campos vazios", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }

        // create an OnDateSetListener
        val startDateSetListener =
            DatePickerDialog.OnDateSetListener { _, _, _, _ ->

                calendar.set(Calendar.YEAR, date.year)
                calendar.set(Calendar.MONTH, date.monthValue - 1)
                calendar.set(Calendar.DAY_OF_MONTH, date.dayOfMonth)

                val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
                //val chosenDate = LocalDate.parse(dateFormattedString)
                binding.editTextStartDate.setText(dateFormattedString)
            }

        val endDateSetListener =
            DatePickerDialog.OnDateSetListener { _, _, _, _ ->

                calendar.set(Calendar.YEAR, date.year)
                calendar.set(Calendar.MONTH, date.monthValue - 1)
                calendar.set(Calendar.DAY_OF_MONTH, date.dayOfMonth)

                val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
                //val chosenDate = LocalDate.parse(dateFormattedString)
                binding.editTextEndDate.setText(dateFormattedString)
            }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        binding.editTextStartDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyAppTheme,
                startDateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.editTextEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyAppTheme,
                endDateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun sendEventToFirebase(title: String, body: String, sendDate : String, senderId : String, startDate: Timestamp, endDate : Timestamp, subject : String) {

        delay(1500)


        val eventChat =
            dbFirebase.collection("chats").
            document("S77po7vNGjtKja2Rinyb").
            collection("events").
            document()


        val event = Event(eventChat.id, title, body, sendDate, senderId, startDate, endDate, subject).toHash()


        eventChat.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d(PushNotificationFragment.TAG, "Error adding event to chat: $eventChat")
            } else {
                Log.d(PushNotificationFragment.TAG, "Event added to chat: $eventChat")
            }
        }

        val notificationUser =
            dbFirebase.collection("users").
            document("EJ1NUwpOoziRyiWWzNej").
            collection("events").
            document()

        notificationUser.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d(PushNotificationFragment.TAG, "Error adding event to user: $notificationUser")
            } else {
                Log.d(PushNotificationFragment.TAG, "Event added to user: $notificationUser")
            }
        }

         /*val startDate = getDate(timeStampStart.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
         val endDate = getDate(timeStampEnd.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

         val even = Event(eventChat.id, title, body, sendDate, senderId, startDate, endDate, subject).toHash()

         //Return to Profile view activity the edited user
         returnIntent.putExtra("event", startDate)
         returnIntent.putExtra("end_date", endDate)
         setResult(Activity.RESULT_OK, returnIntent)*/

        finish()

    }

    fun EventEncryption(context : Context, title: String, description : String) {
        //I generate a random iv
        val iv = AES.GeneratingRandomIv()


        //I have to get all the key and search for the key that corresponds to the group
        val keys = ESP(context).keysPref
        val correspondingGroupId = "ka4vgKgo8QzsVkdn5brt"


        //I search for the key that corresponds to the group
        for (k in keys){
            if (k.contains(correspondingGroupId)){
                val key = k.removePrefix("$correspondingGroupId - ")
                secretKeyString = key
            }
        }

        //I get the key as bytes array and then rebuild the Secret key from the bytes array
        val secretKeyBytes = Base64.decode(secretKeyString, Base64.DEFAULT)

        val secretKey : SecretKey =
            SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.size, "AES")


        ivString = Base64.encodeToString(iv, Base64.DEFAULT)

        //I encrypt the data that the user is sending
        //encryptedTitle = AES.AesEncrypt(title, iv, secretKey)
        //encryptedDescription = AES.AesEncrypt(description, iv, secretKey)
    }

    fun CalendarDateFormatter(date: String): String {

        val inputFormatter =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy",  myLocale)
        val outputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH)
        val dateToFormat = LocalDate.parse(date, inputFormatter)
        val formattedDate = outputFormatter.format(dateToFormat)

        return formattedDate
    }

    fun DateFormaterCalendarIngToCalendarPt(date: String): String {

        val inputFormatter =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", myLocale)
        val dateToFormat = LocalDate.parse(date, inputFormatter)
        val formattedDate = outputFormatter.format(dateToFormat)

        return formattedDate
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }
}