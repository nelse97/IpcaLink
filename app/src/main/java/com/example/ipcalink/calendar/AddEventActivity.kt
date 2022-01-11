package com.example.ipcalink.calendar

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.ipcalink.encryption_algorithm.AES
import com.example.ipcalink.R
import com.example.ipcalink.databinding.ActivityAddEventBinding
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.example.ipcalink.models.Events
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


class AddEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEventBinding

    private val dbFirebase = Firebase.firestore

    private val myLocale = Locale("pt", "PT")

    var calendar = Calendar.getInstance()

    private val userUID = Firebase.auth.uid


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

        Locale.setDefault(myLocale)
        val config = baseContext.resources.configuration
        config.setLocale(myLocale)
        createConfigurationContext(config)

        val dateString = intent.getStringExtra("date")
        val chatId = intent.getStringExtra("chatId")
        val chatName = intent.getStringExtra("chatName")

        val date = LocalDate.parse(dateString)

        calendar.set(date.year, date.monthValue-1, date.dayOfMonth)

        val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
        binding.textViewStartDate.text = dateFormattedString
        binding.textViewEndDate.text = dateFormattedString


        binding.addButton.setOnClickListener {
            if(!binding.editTextTitle.text.isNullOrEmpty() &&
                !binding.editTextDecription.text.isNullOrEmpty() &&
                !binding.textViewStartDate.text.isNullOrEmpty() &&
                !binding.textViewEndDate.text.isNullOrEmpty() &&
                !binding.textViewStartTime.text.isNullOrEmpty() &&
                !binding.textViewEndTime.text.isNullOrEmpty()  ) {


                val title = binding.editTextTitle.text.toString()
                val description = binding.editTextDecription.text.toString()

                val startDateString = CalendarDateFormatter(binding.textViewStartDate.text.toString()) + " " + binding.textViewStartTime.text.toString()
                val endDateString = CalendarDateFormatter(binding.textViewEndDate.text.toString()) + " " + binding.textViewEndTime.text.toString()

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
                val sendDateString = format.format(calendar.time)

                val sendDateLong = Date.parse(sendDateString)
                val sendDate = Date(sendDateLong)
                val timeStampSend = Timestamp(sendDate)


                if(chatId != null && chatName != null)
                    saveEventToGroup(title, description, chatId, chatName, timeStampSend, userUID!!, timeStampStart, timeStampEnd)
                else
                    saveEventToUser(title, description, timeStampSend, timeStampStart, timeStampEnd)

            } else if (binding.editTextTitle.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira um título", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.editTextDecription.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira uma descrição", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.textViewStartDate.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira a data de inicio", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.textViewEndDate.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira a data de fim", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.textViewStartTime.text.isNullOrEmpty()) {
                val toast = Toast.makeText(this, "Por favor insira a hora de inicio", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (binding.textViewEndTime.text.isNullOrEmpty()) {
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

        val startTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            binding.textViewStartTime.text = SimpleDateFormat("HH:mm").format(calendar.time)
        }

        val endTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            binding.textViewEndTime.text = SimpleDateFormat("HH:mm").format(calendar.time)
        }

        binding.textViewStartTime.setOnClickListener {
            TimePickerDialog(
                this,
                startTimeSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.HOUR_OF_DAY,),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }


        binding.textViewEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                endTimeSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.HOUR_OF_DAY,),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
        // create an OnDateSetListener
        val startDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear - 1)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
                //val chosenDate = LocalDate.parse(dateFormattedString)
                binding.textViewStartDate.text = dateFormattedString

            }

        val endDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear - 1)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
                //val chosenDate = LocalDate.parse(dateFormattedString)
                binding.textViewEndDate.text = dateFormattedString
            }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        binding.textViewStartDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogTheme,
                startDateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.textViewEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogTheme,
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
    private fun saveEventToGroup(title: String, description: String, chatId : String, chatName : String, sendDate : Timestamp, senderId : String, startDate: Timestamp, endDate : Timestamp) {


        val eventChat =
            dbFirebase.collection("chats").
            document(chatId).
            collection("events").
            document()


        val event = Events(eventChat.id, chatId, chatName, title, description, sendDate, senderId, startDate, endDate).toHash()


        eventChat.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("", "Error adding event to chat: $eventChat")
            } else {
                Log.d("", "Event added to chat: $eventChat")
            }
        }


        val eventUser =
            dbFirebase.collection("users").
            document(userUID!!).
            collection("events").
            document()

        eventUser.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("", "Error adding event to user: $eventUser")
            } else {
                Log.d("", "Event added to user: $eventUser")
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

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveEventToUser(title: String, description: String, sendDate : Timestamp, startDate: Timestamp, endDate : Timestamp) {

        val eventUser =
            dbFirebase.collection("users").
            document(userUID!!).
            collection("events").
            document()

        val event = Events(eventUser.id, null, null, title, description, sendDate, null, startDate, endDate).toHash()

        eventUser.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("", "Error adding event to user: $eventUser")
            } else {
                Log.d("", "Event added to user: $eventUser")
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