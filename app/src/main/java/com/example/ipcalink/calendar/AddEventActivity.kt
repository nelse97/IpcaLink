package com.example.ipcalink.calendar

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ipcalink.encryption_algorithm.AES
import com.example.ipcalink.R
import com.example.ipcalink.calendar.Extensions.myLocale
import com.example.ipcalink.databinding.ActivityAddEventBinding
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.example.ipcalink.models.Events
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ibotta.android.support.pickerdialogs.SupportedDatePickerDialog
import com.ibotta.android.support.pickerdialogs.SupportedTimePickerDialog
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

    private var calendar = Calendar.getInstance()

    private val userUID = Firebase.auth.uid

    private var lastSavedStartDate : String = "14:00"
    private var lastSavedEndDate : String = "16:00"

    lateinit var timePicker: SupportedDatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    @SuppressLint("SimpleDateFormat", "ResourceType")
    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onStart() {
        super.onStart()

        /*Locale.setDefault(myLocale)

        Locale.setDefault(myLocale)
        val config = baseContext.resources.configuration
        config.setLocale(myLocale)
        createConfigurationContext(config)*/

        //Hides top bar
        (this as AppCompatActivity?)!!.supportActionBar!!.hide()

        val dateString = intent.getStringExtra("date")
        //val chatId = intent.getStringExtra("chatId")

        val date = LocalDate.parse(dateString)

        calendar.set(date.year, date.monthValue-1, date.dayOfMonth)

        val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
        binding.textViewStartDate.text = dateFormattedString
        binding.textViewEndDate.text = dateFormattedString
        binding.textViewStartTime.text = lastSavedStartDate
        binding.textViewEndTime.text = lastSavedEndDate

        binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            println("teste")
            if(isChecked) {
                binding.textViewStartTime.text = "00:00"
                binding.textViewEndTime.text = "23:59"
            } else {
                binding.textViewStartTime.text = lastSavedStartDate
                binding.textViewEndTime.text = lastSavedEndDate
            }
        }



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


                if(!calendarSharedPreferences(this).currentChatId.isNullOrEmpty() && !calendarSharedPreferences(this).currentChatName.isNullOrEmpty()) {
                    saveEventToGroup(calendarSharedPreferences(this).currentChatId!!, title, description, timeStampSend, userUID!!, timeStampStart, timeStampEnd)
                } else {
                    saveEventToUser(title, description, timeStampSend, timeStampStart, timeStampEnd)
                }


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


        binding.textViewStartTime.setOnClickListener {
            customTimePicker("startTime")
        }

        binding.textViewEndTime.setOnClickListener {
            customTimePicker("endTime")
        }

        // create an OnDateSetListener
        val startDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar.time.toString())
                //val chosenDate = LocalDate.parse(dateFormattedString)
                binding.textViewStartDate.text = dateFormattedString

            }

        val endDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
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

    private fun customTimePicker(time : String) {

        binding.CardViewTimePicker.visibility = View.VISIBLE


        val hours = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",
            "15", "16", "17", "18", "19", "20", "21", "22", "23")

        val minutes = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
            "31", "32", "32", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45",
            "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59")



        val BottomSheetDialog1 = BottomSheetDialog(this, R.style.SheetDialog)

        //val values = resources.getStringArray(R.array.time)
        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = hours.size - 1
        binding.hourPicker.displayedValues = hours
        //mBottomSheetDialog.setContentView()
        BottomSheetDialog1.show()

        val BottomSheetDialog2 = BottomSheetDialog(this, R.style.SheetDialog)


        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = minutes.size - 1
        binding.minutePicker.displayedValues = minutes
        //mBottomSheetDialog.setContentView(it)
        BottomSheetDialog2.show()

        /*var hourStr = "00"
        var minuteStr = "00"

        binding.hourPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            hourStr = if (newVal < 10) "0${newVal}" else "$newVal"
        }

        binding.minutePicker.setOnValueChangedListener { picker, oldVal, newVal ->
            minuteStr = if (newVal < 10) "0${newVal}" else "$newVal"
        }*/

        binding.cardViewCancel.setOnClickListener {
            binding.CardViewTimePicker.visibility = View.GONE
        }


        binding.cardViewSave.setOnClickListener {

            val hourStr = if (binding.hourPicker.value < 10) "0${binding.hourPicker.value}" else "${binding.hourPicker.value}"
            val minuteStr = if (binding.minutePicker.value < 10) "0${binding.minutePicker.value}" else "${binding.minutePicker.value}"

            if(time == "startTime") {
                binding.CardViewTimePicker.visibility = View.GONE
                lastSavedStartDate = "$hourStr:$minuteStr"
                binding.textViewStartTime.text = "$hourStr:$minuteStr"
            } else {
                binding.CardViewTimePicker.visibility = View.GONE
                lastSavedEndDate = "$hourStr:$minuteStr"
                binding.textViewEndTime.text = "$hourStr:$minuteStr"
            }
        }


    }



    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveEventToGroup(chatId : String, title: String, description: String, sendDate : Timestamp, senderId : String, startDate: Timestamp, endDate : Timestamp) {


        val eventChat =
            dbFirebase.collection("chats").
            document(chatId).
            collection("events").
            document()


        val event = Events(eventChat.id, title, description, sendDate, senderId, startDate, endDate).toHash()


        eventChat.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("", "Error adding event to chat: $eventChat")
            } else {
                Log.d("", "Event added to chat: $eventChat")
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

        val event = Events(eventUser.id, title, description, sendDate, userUID, startDate, endDate).toHash()

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

    /*override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "$hourOfDay"
        val minuteStr = if (minute < 10) "0${minute}" else "$minute"
        binding.textViewStartTime.text = "${hourStr}:${minuteStr}"
    }*/
}