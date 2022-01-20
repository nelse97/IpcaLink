package com.example.ipcalink.calendar


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.ipcalink.R
import com.example.ipcalink.calendar.CalendarHelper.DateFormater
import com.example.ipcalink.calendar.CalendarHelper.getDate
import com.example.ipcalink.calendar.CalendarHelper.getHours
import com.example.ipcalink.calendar.CalendarHelper.getMinutes
import com.example.ipcalink.databinding.FragmentCalendarBinding
import com.example.ipcalink.models.Events
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kizitonwose.calendarview.CalendarView
import kotlinx.coroutines.NonDisposableHandle.parent
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class RecyclerViewAdapter internal constructor(rl: MutableList<Events>, map : MutableMap<LocalDate, List<Events>>, b : FragmentCalendarBinding, c : Context) : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    private val userUID = Firebase.auth.uid
    private val dbFirebase = Firebase.firestore


    private val binding = b

    private val recyclerMap = map
    private val context = c

    private var recyclerList = rl

    inner class RecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        var title : TextView? = itemView.findViewById(R.id.textViewTitle)
        var duration : TextView? = itemView.findViewById(R.id.textViewDuration)
        var chatName : TextView? = itemView.findViewById(R.id.textViewChatName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.RecyclerViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.row_view_calendar_events,
            parent, false
        )
        return RecyclerViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.RecyclerViewHolder, position: Int) {

        val currentItem: Events = recyclerList[position]


        /*holder.itemView.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setMessage("Delete this event?")
                .setPositiveButton("Delete") { _, _ ->

                    deleteEvent(recyclerList[position], holder.itemView.context)
                }
                .setNegativeButton("Close", null)
                .show()
        }*/

        holder.itemView.setOnClickListener {

            val intent = Intent(context, EditEventActivity::class.java)
            //intent.putExtra("dayOfWeek", dayOfWeek.toString())
            intent.putExtra("eventId", recyclerList[position].id)


            startActivity(context, intent, null)
        }


        holder.itemView.apply {

            val startDate = getDate(recyclerList[position].startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
            val endDate = getDate(recyclerList[position].endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")


            val startHour = getHours(startDate)
            val startMinute = getMinutes(startDate)

            val endHour = getHours(endDate)
            val endMinute = getMinutes(endDate)

            val startTime = "$startHour:$startMinute"
            val endTime = "$endHour:$endMinute"



            holder.title?.text = currentItem.title
            holder.duration?.text = "$startTime-$endTime"
            holder.chatName?.text = calendarSharedPreferences(holder.itemView.context).currentChatName
        }
    }

    override fun getItemCount(): Int {

        return recyclerList.size
    }

    fun removeAt(position: Int, currentChatId : String?, currentChatName : String?) {

        val event = recyclerList[position]
        deleteEvent(event, currentChatId, currentChatName)

    }



    private fun deleteEvent(event: Events, currentChatId : String?, currentChatName : String?) {

        binding.calendar.notifyCalendarChanged()

        val startDate = getDate(event.startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val endDate = getDate(event.endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")


        val localStartDate = LocalDate.parse(DateFormater(startDate))
        val localendDate = LocalDate.parse(DateFormater(endDate))

        val dayDiff = ChronoUnit.DAYS.between(localStartDate, localendDate)

        var date = localStartDate

        var i = 0

        while (i <= dayDiff) {
            binding.calendar.notifyDateChanged(date)

            date?.let {
                recyclerMap[date] = recyclerMap[date].orEmpty().minus(Events(event.id, event.title, event.description, event.sendDate, event.senderId, event.startDate, event.endDate))
                updateAdapterForDate(it)
            }

            date = date.plusDays(1)

            i++
        }

        AlertDialog.Builder(context)
            .setMessage("Delete this event?")
            .setPositiveButton("Confirm") { _, _ ->
                deleteEvents(event, currentChatId, currentChatName)
            }
            .setNegativeButton("Cancel") { _, _ ->
                recyclerMap.clear()

                date = localStartDate
                i = 0

                while (i <= dayDiff) {
                    binding.calendar.notifyDateChanged(date)

                    date?.let {
                        recyclerMap[date] = recyclerMap[date].orEmpty().plus(Events(event.id, event.title, event.description, event.sendDate, event.senderId, event.startDate, event.endDate))
                        updateAdapterForDate(it)
                    }

                    date = date.plusDays(1)

                    i++
                }
            }.show()

        //deleteEvents(event, currentChatId, currentChatName)
    }

    private fun deleteEvents(event : Events, currentChatId : String?, currentChatName : String?) {
        if(!currentChatId.isNullOrEmpty() && !currentChatName.isNullOrEmpty()) {
            dbFirebase.collection("chats").document(currentChatId).collection("events").document(event.id!!)
                .delete().addOnCompleteListener {
                    if (!it.isSuccessful) {
                        return@addOnCompleteListener
                    } else {
                        println("Success")
                        binding.calendar.notifyCalendarChanged()
                    }
                }
        } else {
            dbFirebase.collection("users").document(userUID!!).collection("events").document(event.id!!)
                .delete().addOnCompleteListener {

                    if (!it.isSuccessful) {
                        return@addOnCompleteListener
                    } else {
                        println("Success")
                        binding.calendar.notifyCalendarChanged()
                    }
                }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapterForDate(date: LocalDate) {

        recyclerList.clear()


        recyclerList.removeAll(recyclerMap[date].orEmpty())

        //val month = monthTitleFormatter.format(date.month)
        //binding.textViewMonth.text = month
        //binding.textViewYear.text = date.year.toString()


        notifyDataSetChanged()
    }
}