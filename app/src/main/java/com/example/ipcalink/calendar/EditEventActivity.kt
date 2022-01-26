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
import com.example.ipcalink.calendar.CalendarHelper.DateFormaterCalendarIngToCalendarPt
import com.example.ipcalink.calendar.CalendarHelper.getDate
import com.example.ipcalink.calendar.CalendarHelper.getHours
import com.example.ipcalink.calendar.CalendarHelper.getMinutes
import com.example.ipcalink.databinding.ActivityAddEventBinding
import com.example.ipcalink.models.Events
import com.example.ipcalink.models.UsersChats
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

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
    private var oldChatIdList : ArrayList<String> = ArrayList()
    private var chatIdToRemoveList : ArrayList<String> = ArrayList()

    private val userChatsList : ArrayList<UsersChats> = ArrayList()
    private val chatsWithEventsList : ArrayList<UsersChats> = ArrayList()
    private var eventToEdit : ArrayList<Events> = ArrayList()

    private var chatsAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null

    private var lastSavedStartDate : String = "14:00"
    private var lastSavedEndDate : String = "16:00"
    private lateinit var chatId : String

    private lateinit var eventIdString : String


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        _binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Locale.setDefault(myLocale)
        val config = baseContext.resources.configuration
        config.setLocale(myLocale)
        createConfigurationContext(config)

        //Hides top bar
        (this as AppCompatActivity?)!!.supportActionBar!!.hide()


        binding.textViewEvento.text = "Editar Evento"


        layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewGroups.layoutManager = layoutManager
        chatsAdapter = ChatsAdapter()
        binding.recyclerViewGroups.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewGroups.adapter = chatsAdapter


        eventIdString = intent.getStringExtra("eventId")!!
        chatId = intent.getStringExtra("chatId")!!

        if(chatId.isNotEmpty()) {
            searchChat {
                searchChatsWithEvents(eventIdString) {
                    val startDate = getDate(eventToEdit[0].startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                    val endDate = getDate(eventToEdit[0].endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

                    val localStartDate = LocalDate.parse(DateFormater(startDate))
                    val localendDate = LocalDate.parse(DateFormater(endDate))
                    calendar1.set(localStartDate.year, localStartDate.monthValue-1, localStartDate.dayOfMonth)
                    calendar2.set(localendDate.year, localendDate.monthValue-1, localendDate.dayOfMonth)

                    val startDateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())

                    val endDateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar2.time.toString())

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
        } else {
            binding.textView7.visibility = View.GONE
            binding.addGroupsButton.visibility = View.GONE
            binding.recyclerViewGroups.visibility = View.GONE

            searchUserEvent(eventIdString) {

                val startDate = getDate(eventToEdit[0].startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                val endDate = getDate(eventToEdit[0].endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

                val localStartDate = LocalDate.parse(DateFormater(startDate))
                val localendDate = LocalDate.parse(DateFormater(endDate))
                calendar1.set(localStartDate.year, localStartDate.monthValue-1, localStartDate.dayOfMonth)
                calendar2.set(localendDate.year, localendDate.monthValue-1, localendDate.dayOfMonth)

                val startDateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())

                val endDateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar2.time.toString())

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

    @SuppressLint("SimpleDateFormat", "ResourceType", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onStart() {
        super.onStart()

        println("testeeeee2")

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

                //for a event to be edited the start date must be inferior to the end date
                if(startDate.time < endDate.time) {
                    //if chats ids list is not empty the event will be saved in a chat or more
                    if(!chatsIdsList.isNullOrEmpty()) {


                        val orderedChatsIdsList : ArrayList<String> = ArrayList()
                        val orderedOldChatsIdsList : ArrayList<String> = ArrayList()


                        for(chatId in chatsIdsList.sortedDescending()) {
                            orderedChatsIdsList.add(chatId)
                        }

                        for(oldChatId in oldChatIdList.sortedDescending()) {
                            orderedOldChatsIdsList.add(oldChatId)
                        }
                        println("orderedChatsIdsList")
                        println(orderedChatsIdsList)
                        println("orderedOldChatsIdsList")
                        println(orderedOldChatsIdsList)

                        if(orderedChatsIdsList == orderedOldChatsIdsList) {
                            for(chatId in chatsIdsList) {
                                println("Entrou")
                                editEventToGroups(chatId, eventIdString, title, description, timeStampSend, timeStampStart, timeStampEnd, userUID!!)
                            }
                            finish()
                        } else {
                            val newChatIdList = chatsIdsList
                            for(id in oldChatIdList) {
                                if(!newChatIdList.contains(id)) {
                                    chatIdToRemoveList.add(id)
                                }
                            }
                            for(chatId in chatsIdsList) {
                                setEventToGroups(chatId, eventIdString, title, description, timeStampSend, timeStampStart, timeStampEnd, userUID!!)
                            }

                            if(!chatIdToRemoveList.isNullOrEmpty()) {
                                for(chatId in chatIdToRemoveList) {
                                    deleteEventFromGroup(chatId, eventIdString)
                                }
                            }
                            finish()
                        }
                    } else {
                        editEventToUser(eventIdString, title, description, timeStampSend, timeStampStart, timeStampEnd)
                        println("oldChatIdList")
                        println(oldChatIdList)
                        if(!oldChatIdList.isNullOrEmpty()) {
                            for(chatId in oldChatIdList) {
                                deleteEventFromGroup(chatId, eventIdString)
                            }
                        }
                        finish()
                    }
                } else {
                    val toast = Toast.makeText(this, "Por favor insira uma data de fim de evento maior que a de inicio", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
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
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

                calendar1.set(Calendar.YEAR, year)
                calendar1.set(Calendar.MONTH, monthOfYear)
                calendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar1.time.toString())
                binding.textViewStartDate.text = dateFormattedString

            }

        val endDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

                calendar2.set(Calendar.YEAR, year)
                calendar2.set(Calendar.MONTH, monthOfYear)
                calendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormattedString = DateFormaterCalendarIngToCalendarPt(calendar2.time.toString())
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

    @SuppressLint("SetTextI18n")
    private fun customTimePicker(time : String) {


        if(time == "startTime") {
            binding.titleTimePicker.text = "Inicio"
        } else {
            binding.titleTimePicker.text = "Fim"
        }

        binding.CardViewTimePicker.visibility = View.VISIBLE

        binding.editTextTitle.visibility = View.GONE
        binding.editTextDecription.visibility = View.GONE
        binding.textViewStartTime.visibility = View.GONE
        binding.textViewEndTime.visibility = View.GONE
        binding.textViewStartDate.visibility = View.GONE
        binding.textViewEndDate.visibility = View.GONE
        binding.toggleButton.visibility = View.GONE
        binding.addGroupsButton.visibility = View.GONE
        binding.textView8.visibility = View.INVISIBLE
        binding.CardViewTimePicker.visibility = View.VISIBLE


        val hours = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",
            "15", "16", "17", "18", "19", "20", "21", "22", "23")

        val minutes = arrayOf("00", "15", "30", "45")



        val bottomSheetDialog1 = BottomSheetDialog(this, R.style.SheetDialog)

        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = hours.size - 1
        binding.hourPicker.displayedValues = hours
        bottomSheetDialog1.show()

        val bottomSheetDialog2 = BottomSheetDialog(this, R.style.SheetDialog)


        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = minutes.size - 1
        binding.minutePicker.displayedValues = minutes
        bottomSheetDialog2.show()


        binding.cardViewCancel.setOnClickListener {
            binding.CardViewTimePicker.visibility = View.GONE
            binding.editTextTitle.visibility = View.VISIBLE
            binding.editTextDecription.visibility = View.VISIBLE
            binding.textViewStartTime.visibility = View.VISIBLE
            binding.textViewEndTime.visibility = View.VISIBLE
            binding.textViewStartDate.visibility = View.VISIBLE
            binding.textViewEndDate.visibility = View.VISIBLE
            binding.toggleButton.visibility = View.VISIBLE

            if(!chatId.isNullOrEmpty()) {
                binding.addGroupsButton.visibility = View.VISIBLE
            }
            binding.textView8.visibility = View.VISIBLE
        }


        binding.cardViewSave.setOnClickListener {

            binding.CardViewTimePicker.visibility = View.GONE
            binding.editTextTitle.visibility = View.VISIBLE
            binding.editTextDecription.visibility = View.VISIBLE
            binding.textViewStartTime.visibility = View.VISIBLE
            binding.textViewEndTime.visibility = View.VISIBLE
            binding.textViewStartDate.visibility = View.VISIBLE
            binding.textViewEndDate.visibility = View.VISIBLE
            binding.toggleButton.visibility = View.VISIBLE

            if(!chatId.isNullOrEmpty()) {
                binding.addGroupsButton.visibility = View.VISIBLE
            }
            binding.textView8.visibility = View.VISIBLE

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
    private fun editEventToGroups(chatId : String, eventIdString : String, title: String, description: String, sendDate : Timestamp, startDate: Timestamp, endDate : Timestamp, senderID : String) {


        val eventChat =
            dbFirebase.collection("chats").
            document(chatId).
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
    }


    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setEventToGroups(chatId : String, eventIdString : String, title: String, description: String, sendDate : Timestamp, startDate: Timestamp, endDate : Timestamp, senderID : String) {


        val eventChat =
            dbFirebase.collection("chats").
            document(chatId).
            collection("events").
            document(eventIdString)


        val event = Events(eventChat.id, title, description, sendDate, userUID, startDate, endDate).toHash()


        eventChat.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("", "Error setting event to chat: $eventChat")
            } else {
                Log.d("", "Event setting to chat: $eventChat")
            }
        }
    }

    private fun deleteEventFromGroup(chatId : String, eventIdString : String) {

        val eventChat =
            dbFirebase.collection("chats").
            document(chatId).
            collection("events").
            document(eventIdString)


        eventChat.delete().addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("", "Error deleting event from chat: $eventChat")
            } else {
                Log.d("", "Event deleted from chat: $eventChat")
            }
        }
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

        eventUser.set(event).addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("", "Error editing user event: $eventUser")
            } else {
                Log.d("", "User event edited: $eventUser")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 1001) {
            if(resultCode == RESULT_OK) {

                val photos = data?.getStringArrayListExtra("selectedChatsPhotoList")
                val ids = data?.getStringArrayListExtra("selectedChatsIdsList")
                val names = data?.getStringArrayListExtra("selectedChatsNameList")
                //val oldIds = data?.getStringArrayListExtra("oldSelectedChatsIdsList")

                chatsPhotoList = photos!!
                chatsIdsList = ids!!
                chatsNameList = names!!
                //oldChatIdList = oldIds!!


                chatsAdapter?.notifyDataSetChanged()

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
        }.addOnCompleteListener {
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

                            chatsWithEventsList.add(userChat)

                            eventToEdit.add(event)

                            //if(calendarSharedPreferences(this).control == "firstTime") {
                            chatsPhotoList.add(userChat.photoUrl!!)
                            chatsIdsList.add(userChat.chatId!!)
                            chatsNameList.add(userChat.chatName!!)
                            oldChatIdList.add(userChat.chatId!!)
                            chatsAdapter?.notifyDataSetChanged()
                            //}
                        }
                    }
                }
            }.addOnCompleteListener {
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
        }.addOnCompleteListener {
            callback()
        }
    }
}