package com.example.ipcalink.Calendar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ipcalink.AES.AES
import com.example.ipcalink.AES.AES.AesDecrypt
import com.example.ipcalink.Calendar.Extensions.daysOfWeekFromLocale
import com.example.ipcalink.Calendar.Extensions.dpToPx
import com.example.ipcalink.Calendar.Extensions.inputMethodManager
import com.example.ipcalink.Calendar.Extensions.makeInVisible
import com.example.ipcalink.Calendar.Extensions.makeVisible
import com.example.ipcalink.Calendar.Extensions.setTextColorRes
import com.example.ipcalink.MainActivity
import com.example.ipcalink.R
import com.example.ipcalink.databinding.CalendarDayBinding
import com.example.ipcalink.databinding.FragmentCalendarBinding
import com.example.ipcalink.models.Event
import com.example.ipcalink.models.Notification
import com.example.ipcalink.notifications.PushNotificationFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.yearMonth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.*
import java.time.LocalDateTime.ofInstant
import java.time.LocalTime.ofInstant
import java.time.OffsetDateTime.ofInstant
import java.time.OffsetTime.ofInstant
import java.time.ZonedDateTime.ofInstant
import java.time.format.DateTimeFormatter
import java.util.*
import javax.security.auth.Subject
import kotlin.time.Duration.Companion.hours
import com.google.firebase.firestore.FirebaseFirestoreException

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot


class CalendarFragment : Fragment() {

    /*private val eventsAdapter = EventsAdapter {
        AlertDialog.Builder(requireContext())
            .setMessage("Delete this event?")
            .setPositiveButton("Delete") { _, _ ->
                //deleteEvent(it)
            }
            .setNegativeButton("Close", null)
            .show()
    }*/

    /*private val inputDialog by lazy {
        val editText = AppCompatEditText(requireContext())
        val layout = FrameLayout(requireContext()).apply {
            // Setting the padding on the EditText only pads the input area
            // not the entire EditText so we wrap it in a FrameLayout.
            val padding = dpToPx(20, requireContext())
            setPadding(padding, padding, padding, padding)
            addView(editText, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Enter event title")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                saveEvent(editText.text.toString())
                // Prepare EditText for reuse.
                editText.setText("")
            }
            .setNegativeButton("Close", null)
            .create()
            .apply {
                setOnShowListener {
                    // Show the keyboard
                    editText.requestFocus()
                    context.inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                }
                setOnDismissListener {
                    editText.setText("")
                    // Hide the keyboard
                    context.inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
            }
    }*/

    private var _binding : FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val dbFirebase = Firebase.firestore

    private var adapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null

    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    private val eventsMap = mutableMapOf<LocalDate, List<Event>>()
    val events = mutableListOf<Event>()


    val myLocale = Locale("pt", "PT")
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", myLocale)

    private var control = 1


    private lateinit var registration : ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Hides top bar
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()


        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        adapter = EventsAdapter(null)
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = adapter

        insertingEventsIntoCalendar()

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()


        val endMonth = currentMonth.plusMonths(3)


        binding.calendar.apply {
            setup(currentMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }


        if (savedInstanceState == null) {
            binding.calendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }


        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day : CalendarDay
            val binding = CalendarDayBinding.bind(view)


            init {

                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }


        binding.calendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day

                val textView = container.binding.textViewDay
                val cardView = container.binding.cardView

                textView.text = day.date.dayOfMonth.toString()


                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        /*today -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.calendar_day_selected)
                            dotView.makeInVisible()
                        }*/
                        selectedDate -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.calendar_day_selected)
                            cardView.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                            cardView.setBackgroundResource(R.color.white)

                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            textView.setBackgroundResource(R.drawable.calendar_day_not_selected)
                            cardView.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                            cardView.setBackgroundResource(R.color.colorPrimary)
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.gray_155)
                    textView.setBackgroundResource(R.drawable.calendar_day_other_month)
                    cardView.makeInVisible()
                }
            }
        }

        binding.calendar.monthScrollListener = {

            if (binding.calendar.maxRowCount == 6) {

                binding.textViewYear.text = it.yearMonth.year.toString()
                binding.textViewMonth.text = monthTitleFormatter.format(it.yearMonth)
            } else {
                // In week mode, we show the header a bit differently.
                // We show indices with dates from different months since
                // dates overflow and cells in one index can belong to different
                // months/years.
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.textViewYear.text = firstDate.yearMonth.year.toString()
                    binding.textViewMonth.text = monthTitleFormatter.format(firstDate)

                } else {
                    binding.textViewMonth.text =
                        monthTitleFormatter.format(lastDate)
                    if (firstDate.year == lastDate.year) {
                        binding.textViewYear.text = firstDate.yearMonth.year.toString()
                    } else {
                        //binding.textViewYear.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                    }
                }
            }
            //selectDate(it.yearMonth.atDay(1))
        }

        binding.imageArrow.setImageResource(R.drawable.round_keyboard_arrow_up_black_36)
        binding.imageArrow.setColorFilter(Color.argb(255, 0, 78, 56))



        binding.imageArrow.setOnClickListener {

            control++

            val firstDate = binding.calendar.findFirstVisibleDay()?.date ?: return@setOnClickListener
            val lastDate = binding.calendar.findLastVisibleDay()?.date ?: return@setOnClickListener

            val oneWeekHeight = binding.calendar.daySize.height
            val oneMonthHeight = oneWeekHeight * 6

            val oldHeight = if(control %2 == 0) oneMonthHeight else oneWeekHeight
            val newHeight = if(control %2 == 0) oneWeekHeight else oneMonthHeight

            if(control %2 == 1) {
                binding.imageArrow.setImageResource(R.drawable.round_keyboard_arrow_up_black_36)
                binding.imageArrow.setColorFilter(Color.argb(255, 0, 78, 56))

            } else {
                binding.imageArrow.setImageResource(R.drawable.round_keyboard_arrow_down_black_36)
                binding.imageArrow.setColorFilter(Color.argb(255, 0, 78, 56))
            }

            // Animate calendar height changes.
            val animator = ValueAnimator.ofInt(oldHeight, newHeight)
            animator.addUpdateListener { animator ->
                binding.calendar.updateLayoutParams {
                    height = animator.animatedValue as Int
                }
            }

            // When changing from month to week mode, we change the calendar's
            // config at the end of the animation(doOnEnd) but when changing
            // from week to month mode, we change the calendar's config at
            // the start of the animation(doOnStart). This is so that the change
            // in height is visible. You can do this whichever way you prefer.

            animator.doOnStart {
                if (control %2 != 0) {
                    binding.calendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.ALL_MONTHS,
                        maxRowCount = 6,
                        hasBoundaries = true
                    )
                }
            }
            animator.doOnEnd {
                if (control %2 == 0) {
                    binding.calendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.FIRST_MONTH,
                        maxRowCount = 1,
                        hasBoundaries = false
                    )
                }

                if (control % 2 == 0) {
                    // We want the first visible day to remain
                    // visible when we change to week mode.
                    binding.calendar.scrollToDate(firstDate)
                } else {
                    // When changing to month mode, we choose current
                    // month if it is the only one in the current frame.
                    // if we have multiple months in one frame, we prefer
                    // the second one unless it's an outDate in the last index.
                    if (firstDate.yearMonth == lastDate.yearMonth) {
                        binding.calendar.scrollToMonth(firstDate.yearMonth)
                    } else {
                        // We compare the next with the last month on the calendar so we don't go over.
                        binding.calendar.scrollToMonth(minOf(firstDate.yearMonth.next, endMonth))
                    }
                }
            }
            animator.duration = 250
            animator.start()
        }

        binding.addButton.setOnClickListener {
            //dayOfWeak.removeRange(3, dayOfWeak.length)
            //month.removeRange(3, dayOfWeak.length)

            val dayOfWeek = selectedDate!!.dayOfWeek
            val date = selectedDate

            val intent = Intent(context, AddEventActivity::class.java)
            intent.putExtra("dayOfWeek", dayOfWeek.toString())
            intent.putExtra("date", date.toString())
            startActivity(intent)
        }
    }

    private fun selectDate(date: LocalDate) {

        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.calendar.notifyDateChanged(it) }
            binding.calendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }


    private fun saveEvent(value : QuerySnapshot) {

        eventsMap.clear()

        for(query in value){

            val event = Event.fromHash(query)

            val startDate = getDate(event.startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
            val endDate = getDate(event.endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

            val localStartDate = LocalDate.parse(DateFormater(startDate))
            binding.calendar.notifyDateChanged(localStartDate)


            localStartDate?.let {
                eventsMap[it] = eventsMap[it].orEmpty().plus(Event(event.id, event.title, event.description, event.sendDate, event.senderId, event.startDate, event.endDate, event.subject))
                updateAdapterForDate(it)
            }
        }
    }

    fun DateFormater(date: String): String {

        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS",  Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val dateToFormat = LocalDate.parse(date, inputFormatter)
        val formattedDate = outputFormatter.format(dateToFormat)

        return formattedDate
    }

    /*private fun deleteEvent(event: Event) {
        val date = event.calendarDate
        eventsMap[date] = eventsMap[date].orEmpty().minus(event)
        updateAdapterForDate(date)
    }*/


    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapterForDate(date: LocalDate) {

        //adapter!!.notifyItemRemoved(events.size - 1)
        //adapter!!.notifyItemRangeChanged(events.size - 1, events.size - 1)
        events.clear()


        events.addAll(this@CalendarFragment.eventsMap[date].orEmpty())
        //adapter!!.notifyItemInserted(events.size - 1)

        //eventsAdapter.apply {
        //}

        val month = monthTitleFormatter.format(date.month)
        binding.textViewMonth.text = month
        binding.textViewYear.text = date.year.toString()


        adapter?.notifyDataSetChanged()
    }


    /*private fun getNotifications(context: Context) {

        dbFirebase.collection("users").document("EJ1NUwpOoziRyiWWzNej").collection("events").addSnapshotListener { value, error ->

            if (error != null) {
                Log.w("ShowNotificationsFragment", "Listen failed.", error)
                return@addSnapshotListener
            }

            for(query in value!!){

                val event = Event.fromHash(query, context)

                val date = event.sendDate!!.removeRange(5, 19)

                list.add(Event(event.id, event.title, event.body, event.secretKey, event.iv, date, event.senderId, event.duration, event.subject))
            }

            binding.recyclerView2.adapter!!.notifyItemInserted(list.size - 1)
        }
    }*/


    inner class EventsAdapter(val onClick : ((Event) -> Unit)?) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_view_calendar, parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.itemView.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setMessage("Delete this event?")
                    .setPositiveButton("Delete") { _, _ ->
                        //deleteEvent(events[position])
                    }
                    .setNegativeButton("Close", null)
                    .show()
            }

            holder.v.apply {

                val startDate = events[position].startDate!!.toDate()
                val endDate = events[position].endDate!!.toDate()

                val startTime = startDate.hours.toString() + ":" + startDate.minutes.toString()
                val endTime = endDate.hours.toString() + ":" + endDate.minutes.toString()

                val textViewTitle = findViewById<TextView>(R.id.textViewTitle)
                textViewTitle.text = events[position].title
                val textViewDuration = findViewById<TextView>(R.id.textViewDuration)
                textViewDuration.text = "$startTime-$endTime"
                val textViewSubject = findViewById<TextView>(R.id.textViewSubject)
                textViewSubject.text = events[position].subject
            }
        }

        override fun getItemCount(): Int {
            return events.size
        }
    }

    private fun insertingEventsIntoCalendar() {

        registration = dbFirebase.collection("users").document("EJ1NUwpOoziRyiWWzNej").collection("events").addSnapshotListener { value, error ->

            if (error != null) {
                Log.w("ShowNotificationsFragment", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (value != null) {
                saveEvent(value)
            }

            /*for(query in value!!){

                val event = Event.fromHash(query)
                saveEvent(event)
                //val date = event.sendDate!!.removeRange(5, 19)

                //list.add(Notification(notification.id, notification.title, notification.body, notification.secretKey, notification.iv, date, notification.senderId))
            }*/
        }
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    override fun onDestroy() {
        super.onDestroy()

        registration.remove()
    }


    /*inner class EventsAdapter(val onClick: (Event) -> Unit) : RecyclerView.Adapter<EventsAdapter.EventsViewHolder>() {

        val events = mutableListOf<Event>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
            return EventsViewHolder(
                RowViewCalendarBinding.inflate(parent.context.layoutInflater, parent, false)
            )
        }

        override fun onBindViewHolder(viewHolder: EventsViewHolder, position: Int) {
            viewHolder.bind(events[position])
        }

        override fun getItemCount(): Int = events.size

        inner class EventsViewHolder(private val binding: RowViewCalendarBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                itemView.setOnClickListener {
                    onClick(events[bindingAdapterPosition])
                }
            }

            fun bind(event: Event) {
                binding.textViewTitle.text = event.text
                binding.textViewDuration.text = "Termina em 23m"
                binding.textViewSubject.text = "Web Design"
            }
        }
    }*/
}
