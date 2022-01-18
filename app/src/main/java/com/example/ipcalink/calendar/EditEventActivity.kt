package com.example.ipcalink.calendar

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ipcalink.R
import com.example.ipcalink.calendar.CalendarHelper.DateFormater
import com.example.ipcalink.calendar.CalendarHelper.getDate
import com.example.ipcalink.calendar.CalendarHelper.getHours
import com.example.ipcalink.calendar.CalendarHelper.getMinutes
import com.example.ipcalink.databinding.ActivityAddEventBinding
import com.example.ipcalink.models.Events
import com.example.ipcalink.models.UsersChats
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration.Companion.hours

class EditEventActivity : AppCompatActivity() {

    private var _binding: ActivityAddEventBinding? = null
    private val binding get() = _binding!!

    private val dbFirebase = Firebase.firestore

    private val myLocale = Locale("pt", "PT")

    private var calendar1 = Calendar.getInstance()
    private var calendar2 = Calendar.getInstance()

    private val userUID = Firebase.auth.uid

    private var chatsPhotoList : ArrayList<String> = ArrayList()
    private var chatsIdsList : ArrayList<String> = ArrayList()
    private var chatsNameList : ArrayList<String> = ArrayList()

    private val userChatsList : ArrayList<UsersChats> = ArrayList()
    private val chatsWithEventsList : ArrayList<UsersChats> = ArrayList()
    private var eventToEdit : ArrayList<Events> = ArrayList()

    private var chatsAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null

    private var lastSavedStartDate : String = "14:00"
    private var lastSavedEndDate : String = "16:00"

    //lateinit var timePicker: SupportedDatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        _binding = ActivityAddEventBinding.inflate(layoutInflater)
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

        binding.textViewEvento.text = "Editar Evento"


        layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewGroups.layoutManager = layoutManager
        chatsAdapter = ChatsAdapter()
        binding.recyclerViewGroups.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewGroups.adapter = chatsAdapter

        val eventIdString = intent.getStringExtra("eventId")
        //val chatId = intent.getStringExtra("chatId")

        searchChat {
            searchChatsWithEvents(eventIdString!!) {
                if(!chatsWithEventsList.isNullOrEmpty()) {

                    val startDate = getDate(eventToEdit[0].startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                    val endDate = getDate(eventToEdit[0].endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

                    val localStartDate = LocalDate.parse(DateFormater(startDate))
                    val localendDate = LocalDate.parse(DateFormater(endDate))
                    calendar1.set(localStartDate.year, localStartDate.monthValue-1, localStartDate.dayOfMonth)
                    calendar2.set(localendDate.year, localendDate.monthValue-1, localendDate.dayOfMonth)

                    val startDateFormattedString =
                        CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())

                    val endDateFormattedString =
                        CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar2.time.toString())

                    val startHour = getHours(startDate)
                    val startMinute = getMinutes(startDate)
                    val startTime = "$startHour:$startMinute"

                    val endHour = getHours(endDate)
                    val endMinute = getMinutes(endDate)
                    val endTime = "$endHour:$endMinute"

                    binding.textViewStartDate.text = startDateFormattedString
                    binding.textViewEndDate.text = endDateFormattedString
                    binding.textViewStartTime.text = startTime
                    binding.textViewEndTime.text = endTime
                    binding.editTextTitle.setText(eventToEdit[0].title)
                    binding.editTextDecription.setText(eventToEdit[0].description)
                } else {
                    searchUserEvent(eventIdString) {

                        binding.textView7.visibility = View.GONE
                        binding.addGroupsButton.visibility = View.GONE
                        binding.recyclerViewGroups.visibility = View.GONE

                        val startDate = getDate(eventToEdit[0].startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                        val endDate = getDate(eventToEdit[0].endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

                        val localStartDate = LocalDate.parse(DateFormater(startDate))
                        val localendDate = LocalDate.parse(DateFormater(endDate))
                        calendar1.set(localStartDate.year, localStartDate.monthValue-1, localStartDate.dayOfMonth)
                        calendar2.set(localendDate.year, localendDate.monthValue-1, localendDate.dayOfMonth)

                        val startDateFormattedString =
                            CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())

                        val endDateFormattedString =
                            CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar2.time.toString())

                        val startHour = getHours(startDate)
                        val startMinute = getMinutes(startDate)
                        val startTime = "$startHour:$startMinute"

                        val endHour = getHours(endDate)
                        val endMinute = getMinutes(endDate)
                        val endTime = "$endHour:$endMinute"

                        binding.textViewStartDate.text = startDateFormattedString
                        binding.textViewEndDate.text = endDateFormattedString
                        binding.textViewStartTime.text = startTime
                        binding.textViewEndTime.text = endTime
                        binding.editTextTitle.setText(eventToEdit[0].title)
                        binding.editTextDecription.setText(eventToEdit[0].description)
                    }
                }
            }
        }

        /*if(chatsWithEventsList.isNullOrEmpty()) {
            searchUserEvent(eventIdString!!) {
                val startDate = getDate(eventToEdit[0].startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                val endDate = getDate(eventToEdit[0].endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

                val localStartDate = LocalDate.parse(DateFormater(startDate))
                val localendDate = LocalDate.parse(DateFormater(endDate))
                calendar1.set(localStartDate.year, localStartDate.monthValue-1, localStartDate.dayOfMonth)
                calendar2.set(localendDate.year, localendDate.monthValue-1, localendDate.dayOfMonth)

                val startDateFormattedString =
                    CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())

                val endDateFormattedString =
                    CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())

                val startHour = getHours(startDate)
                val startMinute = getMinutes(startDate)
                val startTime = "$startHour:$startMinute"

                val endHour = getHours(endDate)
                val endMinute = getMinutes(endDate)
                val endTime = "$endHour:$endMinute"

                binding.textViewStartDate.text = startDateFormattedString
                binding.textViewEndDate.text = endDateFormattedString
                binding.textViewStartTime.text = startTime
                binding.textViewEndTime.text = endTime
                binding.editTextTitle.setText(eventToEdit[0].title)
                binding.editTextDecription.setText(eventToEdit[0].description)
            }
        }*/



        binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.textViewStartTime.text = "00:00"
                binding.textViewEndTime.text = "23:59"
            } else {
                binding.textViewStartTime.text = lastSavedStartDate
                binding.textViewEndTime.text = lastSavedEndDate
            }
        }

        binding.addGroupsButton.setOnClickListener {

            val intent = Intent(this, EventsAddGroupsActivity::class.java)
            intent.putExtra("chatsIdsList", chatsIdsList)
            intent.putExtra("chatsNameList", chatsNameList)
            intent.putExtra("chatsPhotoList", chatsPhotoList)
            startActivityForResult(intent, 1001)
        }

        binding.imageViewGoBack.setOnClickListener {
            finish()
        }


        binding.cardViewSaveEvent.setOnClickListener {
            if(!binding.editTextTitle.text.isNullOrEmpty() &&
                !binding.editTextDecription.text.isNullOrEmpty() &&
                !binding.textViewStartDate.text.isNullOrEmpty() &&
                !binding.textViewEndDate.text.isNullOrEmpty() &&
                !binding.textViewStartTime.text.isNullOrEmpty() &&
                !binding.textViewEndTime.text.isNullOrEmpty()  ) {



                val title = binding.editTextTitle.text.toString()
                val description = binding.editTextDecription.text.toString()

                val startDateString = CalendarHelper.CalendarDateFormatter(binding.textViewStartDate.text.toString()) + " " + binding.textViewStartTime.text.toString()
                val endDateString = CalendarHelper.CalendarDateFormatter(binding.textViewEndDate.text.toString()) + " " + binding.textViewEndTime.text.toString()


                val startDateLong = Date.parse(startDateString)
                val startDate = Date(startDateLong)
                val timeStampStart = Timestamp(startDate)


                val endDateLong = Date.parse(endDateString)
                val endDate = Date(endDateLong)
                val timeStampEnd = Timestamp(endDate)


                //Send Date needs to be formatted in this way 14/11/2021 16:38
                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone(ZoneId.of("UTC"))
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss",  myLocale)
                val sendDateString = format.format(calendar.time)

                val sendDateLong = Date.parse(sendDateString)
                val sendDate = Date(sendDateLong)
                val timeStampSend = Timestamp(sendDate)


                if(startDate.time <= endDate.time) {
                    if(!chatsWithEventsList.isNullOrEmpty()) {
                        for(chat in chatsWithEventsList) {
                            editEventToGroups(chat, eventIdString!!, title, description, timeStampSend, timeStampStart, timeStampEnd, userUID!!)
                        }
                    } else {
                        editEventToUser(eventIdString!!, title, description, timeStampSend, timeStampStart, timeStampEnd)
                    }
                } else {
                    val toast = Toast.makeText(this, "Por favor insira uma data de fim de evento maior que a de inicio", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }

                finish()


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

                calendar1.set(Calendar.YEAR, year)
                calendar1.set(Calendar.MONTH, monthOfYear)
                calendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                val dateFormattedString =
                    CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())
                //val chosenDate = LocalDate.parse(dateFormattedString)
                binding.textViewStartDate.text = dateFormattedString

            }

        val endDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                calendar2.set(Calendar.YEAR, year)
                calendar2.set(Calendar.MONTH, monthOfYear)
                calendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormattedString =
                    CalendarHelper.DateFormaterCalendarIngToCalendarPt(calendar2.time.toString())
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
                calendar1.get(Calendar.YEAR),
                calendar1.get(Calendar.MONTH),
                calendar1.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.textViewEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogTheme,
                endDateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar2.get(Calendar.YEAR),
                calendar2.get(Calendar.MONTH),
                calendar2.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun customTimePicker(time : String) {

        binding.CardViewTimePicker.visibility = View.VISIBLE


        val hours = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",
            "15", "16", "17", "18", "19", "20", "21", "22", "23")

        val minutes = arrayOf("00", "15", "30", "45")



        val bottomSheetDialog1 = BottomSheetDialog(this, R.style.SheetDialog)

        //val values = resources.getStringArray(R.array.time)
        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = hours.size - 1
        binding.hourPicker.displayedValues = hours
        //mBottomSheetDialog.setContentView()
        bottomSheetDialog1.show()

        val bottomSheetDialog2 = BottomSheetDialog(this, R.style.SheetDialog)


        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = minutes.size - 1
        binding.minutePicker.displayedValues = minutes
        //mBottomSheetDialog.setContentView(it)
        bottomSheetDialog2.show()

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
            val minuteStr = if (binding.minutePicker.value == 0) "0${binding.minutePicker.value}" else "${binding.minutePicker.value * 15}"

            println(minuteStr)

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
    private fun editEventToGroups(chat : UsersChats, eventIdString : String, title: String, description: String, sendDate : Timestamp, startDate: Timestamp, endDate : Timestamp, senderID : String) {


        val eventChat =
            dbFirebase.collection("chats").
            document(chat.chatId!!).
            collection("events").
            document(eventIdString)


        val event = Events(eventChat.id, title, description, sendDate, userUID, startDate, endDate).toHash()


        eventChat.update(event).addOnCompleteListener {
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
    private fun editEventToUser(eventIdString : String, title: String, description: String, sendDate : Timestamp, startDate: Timestamp, endDate : Timestamp) {

        val eventUser =
            dbFirebase.collection("users").
            document(userUID!!).
            collection("events").
            document(eventIdString)

        val event = Events(eventUser.id, title, description, sendDate, userUID, startDate, endDate).toHash()

        eventUser.update(event).addOnCompleteListener {
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

    /*fun getDate(milliSeconds: Long, dateFormat: String?): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }*/

    /*override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val hourStr = if (hourOfDay < 10) "0${hourOfDay}" else "$hourOfDay"
        val minuteStr = if (minute < 10) "0${minute}" else "$minute"
        binding.textViewStartTime.text = "${hourStr}:${minuteStr}"
    }*/

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1001) {
            if(resultCode == RESULT_OK) {

                val photos = data?.getStringArrayListExtra("selectedChatsPhotoList")
                val ids = data?.getStringArrayListExtra("selectedChatsIdsList")
                val names = data?.getStringArrayListExtra("selectedChatsNameList")

                if(!photos.isNullOrEmpty() && !ids.isNullOrEmpty() && !names.isNullOrEmpty()) {
                    chatsPhotoList = photos
                    chatsIdsList = ids
                    chatsNameList = names

                    ChatsAdapter().notifyDataSetChanged()
                } else {
                    chatsPhotoList = ArrayList()
                    chatsIdsList = ArrayList()
                    chatsNameList = ArrayList()
                }
            }
        }
    }


    inner class ChatsAdapter : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_add_event_goups, parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                val groupImage = findViewById<ImageView>(R.id.imageViewGroup)
                groupImage.setBackgroundResource(R.drawable.image)
                val chatName = findViewById<TextView>(R.id.textViewGroupName)
                chatName.text = chatsNameList[position]
            }
        }

        override fun getItemCount(): Int {
            return chatsIdsList.size
        }
    }

    /*fun getChats(chatsIds :  ArrayList<String>?) {
        for (chatId in chatsIds!!) {
            dbFirebase.collection("chats").document(chatId).collection("events").get().addOnCompleteListener {

                if (it.exception != null) {
                    Log.w("ShowNotificationsFragment", "Listen failed.", it.exception)
                    return@addOnCompleteListener
                }

                for (query in it.result!!) {

                    val usersChats = UsersChats.fromHash(query)

                    chatsList.add(UsersChats(usersChats.chatId, usersChats.chatName, usersChats.chatType, usersChats.photoUrl, usersChats.lastMessage,
                        usersChats.lastMessageSenderId, usersChats.lastMessageTimestamp))

                }

                chatsAdapter?.notifyDataSetChanged()

            }
        }
    }*/

    fun searchChat(callback : ()-> Unit) {
        dbFirebase.collection("users").document(userUID!!).collection("chats").get().addOnCompleteListener {

            if (it.exception != null) {
                Log.w("EditEventsActivity", "Listen failed.", it.exception)
                return@addOnCompleteListener
            }

            for (query in it.result!!) {

                val usersChats = UsersChats.fromHash(query)

                userChatsList.add(usersChats)

            }
            callback()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchChatsWithEvents(eventIdString : String, callback : ()-> Unit) {
        for (userChat in userChatsList) {
            dbFirebase.collection("chats").document(userChat.chatId!!).collection("events").get().addOnSuccessListener { documents ->


                documents.let {

                    for (document in documents) {
                        if(document.id == eventIdString) {
                            val event = Events.fromHash(document)
                            chatsPhotoList.add(userChat.photoUrl!!)
                            chatsIdsList.add(userChat.chatId!!)
                            chatsNameList.add(userChat.chatName!!)

                            chatsWithEventsList.add(userChat)
                            println("chatsWithEventsList")
                            println(chatsWithEventsList)
                            eventToEdit.add(event)

                            chatsAdapter?.notifyDataSetChanged()
                        }
                    }
                }
                callback()
            }
        }
    }

    private fun searchUserEvent(eventIdString : String, callback : ()-> Unit) {

        dbFirebase.collection("users").document(userUID!!).collection("events").get().addOnSuccessListener { documents ->



            documents.let {

                for (document in documents) {
                    if(document.id == eventIdString) {
                        val event = Events.fromHash(document)
                        eventToEdit.add(event)
                    }
                }
            }
            callback()
        }
    }
}