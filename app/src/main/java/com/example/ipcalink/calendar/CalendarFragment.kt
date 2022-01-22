package com.example.ipcalink.calendar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.example.ipcalink.calendar.Extensions.daysOfWeekFromLocale
import com.example.ipcalink.R
import com.example.ipcalink.calendar.CalendarHelper.DateFormater
import com.example.ipcalink.calendar.CalendarHelper.getDate
import com.example.ipcalink.calendar.Extensions.dpToPx
import com.example.ipcalink.calendar.Extensions.makeInVisible
import com.example.ipcalink.calendar.Extensions.makeVisible
import com.example.ipcalink.calendar.Extensions.setTextColorRes
import com.example.ipcalink.databinding.CalendarDayBinding
import com.example.ipcalink.databinding.FragmentCalendarBinding
import com.example.ipcalink.models.Events
import com.example.ipcalink.models.UsersChats
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.kizitonwose.calendarview.model.OutDateStyle
import com.kizitonwose.calendarview.utils.Size
import java.time.temporal.ChronoUnit


class CalendarFragment : Fragment() {

    private var _binding : FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val dbFirebase = Firebase.firestore

    private var eventsAdapter: RecyclerView.Adapter<*>? = null
    private var chatsAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null

    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    private val eventsMap = mutableMapOf<LocalDate, List<Events>>()
    private val eventsList = mutableListOf<Events>()

    private var chatsList : ArrayList<UsersChats> = ArrayList()

    private val userUID = Firebase.auth.uid

    val myLocale = Locale("pt", "PT")
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", myLocale)

    /*private var imageArrowControl = 1
    private var imageHamburgerControl = 1*/


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


        //When the user enters the calendar fragment, the current chat id and name
        //shared preferences has to be reseted, because the user is going to be seeing the calendar globally
        calendarSharedPreferences(requireContext()).currentChatId = null
        calendarSharedPreferences(requireContext()).currentChatName = null


        //setting up the events recycler view
        setUpRecyclerView()


        //setting up the chats recycler view
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewGroupChats.layoutManager = layoutManager
        chatsAdapter = ChatsAdapter()
        binding.recyclerViewGroupChats.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewGroupChats.adapter = chatsAdapter


        binding.recyclerViewGroupChats.visibility = View.GONE


        //Get the days of the week from my current locale
        val daysOfWeek = daysOfWeekFromLocale()

        //Get my current month and then define my start and end month
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(60)
        val endMonth = currentMonth.plusMonths(60)


        val dm = DisplayMetrics()

        // Setup the day size for the days of my calendar.
        binding.calendar.apply {

            // Here i calculate
            val monthWidth = (dm.widthPixels * 0.73).toInt()
            val dayWidth = monthWidth / 7
            val dayHeight = (dayWidth * 1.73).toInt() // We don't want a square calendar.
            daySize = Size(dayWidth, dayHeight)

            // Add margins around our card view.
            val horizontalMargin = dpToPx(5, requireContext())
            val verticalMargin = dpToPx(0, requireContext())
            setMonthMargins(start = horizontalMargin, end = horizontalMargin, top = verticalMargin, bottom = verticalMargin)

            //Setup the days of the week and the start/end month
            setup(startMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        //Search the events of a user
        //to then fill the calendar with them
        searchingEvents()

        //If the toogle button has the state 'on' the calendar shows the groups in witch the users belongs
        //so that they can filter the events that appear on the calendar
        //If the toogle button has the state 'off' the calendar shows all of the events
        binding.toggleHamburger.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.recyclerViewGroupChats.visibility = View.VISIBLE
                insertingChats()
            } else {
                chatsList.clear()
                calendarSharedPreferences(requireContext()).currentChatId = null
                calendarSharedPreferences(requireContext()).currentChatName = null
                binding.recyclerViewGroupChats.visibility = View.GONE
                searchingEvents()
            }
        }

        //This class deals with the clicks done by the user on the calendar days
        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day : CalendarDay
            val binding = CalendarDayBinding.bind(view)


            init {
                //if a user clicks on a day and that day belongs to the current month, the day is selected
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }

        //This object
        binding.calendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day

                val textViewDay  = container.binding.textViewDay
                val cardView1 = container.binding.cardView1
                val cardView2 = container.binding.cardView2
                val textView6 = binding.textView6

                textViewDay.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textViewDay.makeVisible()
                    when (day.date) {
                        today -> {
                            if(today != selectedDate) {
                                textViewDay.setTextColorRes(R.color.black)
                                textViewDay.setBackgroundResource(R.drawable.calendar_today)
                                cardView1.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                                cardView2.setBackgroundResource(R.color.colorPrimary)
                            } else {
                                textViewDay.setTextColorRes(R.color.white)
                                textViewDay.setBackgroundResource(R.drawable.calendar_day_selected)
                                cardView1.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                                cardView2.setBackgroundResource(R.color.white)
                            }

                        }
                        selectedDate -> {
                            textViewDay.setTextColorRes(R.color.white)
                            textViewDay.setBackgroundResource(R.drawable.calendar_day_selected)
                            cardView1.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                            cardView2.setBackgroundResource(R.color.white)

                            if(eventsMap[day.date] != null) {
                                textView6.visibility = View.GONE
                            } else {
                                textView6.visibility = View.VISIBLE
                            }
                        }
                        else -> {
                            textViewDay.setTextColorRes(R.color.black)
                            textViewDay.setBackgroundResource(R.drawable.calendar_day_not_selected)
                            cardView1.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                            cardView2.setBackgroundResource(R.color.colorPrimary)
                        }
                    }
                } else {
                    textViewDay.setTextColorRes(R.color.gray_155)
                    textViewDay.setBackgroundResource(R.drawable.calendar_day_not_selected)
                    cardView1.makeInVisible()
                }
            }
        }

        //when the user scrolls to a new month this code is called
        binding.calendar.monthScrollListener = {

            //if the calendar is in month mode, then i define the year and month value normally
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
                        /*binding.textViewMonth.text =
                            monthTitleFormatter.format(firstDate) + "-" + monthTitleFormatter.format(lastDate)*/
                        binding.textViewYear.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                    }
                }
            }
        }

        /*binding.imageArrow.setImageResource(R.drawable.round_keyboard_arrow_up_black_36)
        binding.imageArrow.setColorFilter(Color.argb(255, 0, 78, 56))*/


        //If the user clicks on the calendar arrow then depending on the state of the toogle button
        //the calendar will enter week mode or month mode
        binding.toggleButtonArrow.setOnClickListener {

            val firstDate = binding.calendar.findFirstVisibleDay()?.date ?: return@setOnClickListener
            val lastDate = binding.calendar.findLastVisibleDay()?.date ?: return@setOnClickListener

            val oneWeekHeight = binding.calendar.daySize.height
            val oneMonthHeight = oneWeekHeight * 6

            val oldHeight = if(binding.toggleButtonArrow.isChecked) oneMonthHeight else oneWeekHeight
            val newHeight = if(binding.toggleButtonArrow.isChecked) oneWeekHeight else oneMonthHeight


            // Animate calendar height changes.
            val animator = ValueAnimator.ofInt(oldHeight, newHeight)
            //When the values oldHeight and newHeight change, the code inside this listener is going to be executed
            animator.addUpdateListener { animator ->
                //Updating layout parameters of the calendar with the new height
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
                if (!binding.toggleButtonArrow.isChecked) {
                    binding.calendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.ALL_MONTHS,
                        outDateStyle = OutDateStyle.END_OF_ROW,
                        maxRowCount = 6,
                        hasBoundaries = true
                    )
                }
            }
            animator.doOnEnd {
                if (binding.toggleButtonArrow.isChecked) {
                    binding.calendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.FIRST_MONTH,
                        outDateStyle = OutDateStyle.END_OF_ROW,
                        maxRowCount = 1,
                        hasBoundaries = false
                    )
                }

                if (binding.toggleButtonArrow.isChecked) {
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

        binding.fabAdd.setOnClickListener {

            //val dayOfWeek = selectedDate!!.dayOfWeek
            val date = selectedDate

            val intent = Intent(context, AddEventActivity::class.java)
            //intent.putExtra("dayOfWeek", dayOfWeek.toString())
            intent.putExtra("date", date.toString())

            startActivity(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun insertingChats() {


        dbFirebase.collection("users").document(userUID!!).collection("chats").addSnapshotListener { value, error ->

            if (error != null) {
                Log.w("ShowNotificationsFragment", "Listen failed.", error)
                return@addSnapshotListener
            }

            //var firstChat = true

            for (query in value!!) {

                val usersChats = UsersChats.fromHash(query)

                /*if(firstChat) {
                    currentChatId = chat.chatId!!
                    currentChatName = chat.chatName!!
                }

                firstChat = false*/

                chatsList.add(UsersChats(usersChats.chatId, usersChats.chatName, usersChats.chatType, usersChats.photoUrl, usersChats.lastMessage,
                    usersChats.lastMessageSenderId, usersChats.lastMessageTimestamp))

            }
            chatsAdapter?.notifyDataSetChanged()


            //searchingEvents()
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

            val event = Events.fromHash(query)

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
                    eventsMap[it] = eventsMap[it].orEmpty().plus(Events(event.id, event.title, event.description, event.sendDate, event.senderId, event.startDate, event.endDate))
                    updateAdapterForDate(it)
                }

                date = date.plusDays(1)

                i++
            }

            /*val event = Events.fromHash(query)

            val startDate = getDate(event.startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
            val endDate = getDate(event.endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

            val localStartDate = LocalDate.parse(DateFormater(startDate))
            binding.calendar.notifyDateChanged(localStartDate)


            localStartDate?.let {
                eventsMap[it] = eventsMap[it].orEmpty().plus(Events(event.id, event.title, event.description, event.sendDate, event.senderId, event.startDate, event.endDate))
                updateAdapterForDate(it)
            }*/
        }

        binding.calendar.notifyCalendarChanged()

        binding.calendar.post {
            // Show today's events initially.
            selectDate(today)
        }

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
        eventsList.clear()


        eventsList.addAll(this@CalendarFragment.eventsMap[date].orEmpty())
        //adapter!!.notifyItemInserted(events.size - 1)

        //eventsAdapter.apply {
        //}

        /*val month = monthTitleFormatter.format(date.month)
        binding.textViewMonth.text = month
        binding.textViewYear.text = date.year.toString()*/


        eventsAdapter?.notifyDataSetChanged()
    }


    /*inner class EventsAdapter(val onClick : ((Events) -> Unit)?) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_view_calendar_events, parent, false)
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

                val startDate = getDate(eventsList[position].startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                val endDate = getDate(eventsList[position].endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")


                val startHour = getHours(startDate)
                val startMinute = getMinutes(startDate)

                val endHour = getHours(endDate)
                val endMinute = getMinutes(endDate)

                val startTime = "$startHour:$startMinute"
                val endTime = "$endHour:$endMinute"


                val textViewTitle = findViewById<TextView>(R.id.textViewTitle)
                textViewTitle.text = eventsList[position].title
                val textViewDuration = findViewById<TextView>(R.id.textViewDuration)
                textViewDuration.text = "$startTime-$endTime"
                val textViewChatName = findViewById<TextView>(R.id.textViewChatName)
                textViewChatName.text = currentChatName
            }
        }

        override fun getItemCount(): Int {
            return eventsList.size
        }
    }*/

    inner class ChatsAdapter : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_view_calendar_chat_groups, parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.setOnClickListener {

                calendarSharedPreferences(requireContext()).currentChatId = chatsList[position].chatId!!
                calendarSharedPreferences(requireContext()).currentChatName = chatsList[position].chatName!!
                searchingEventsFromAChat()
            }

            holder.v.apply {

                val groupName = findViewById<TextView>(R.id.textViewGroupName)
                groupName.text = chatsList[position].chatName
            }
        }

        override fun getItemCount(): Int {
            return chatsList.size
        }
    }



    private fun searchingEvents() {

        registration = dbFirebase.collection("users").document(userUID!!).collection("events").addSnapshotListener { value, error ->

            if (error != null) {
                Log.w("ShowNotificationsFragment", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (value != null) {
                saveEvent(value)
            }
        }
    }

    private fun searchingEventsFromAChat() {

        dbFirebase.collection("chats").document(calendarSharedPreferences(requireContext()).currentChatId!!)
            .collection("events").addSnapshotListener { value, error ->

                if (error != null) {
                    Log.w("ShowNotificationsFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (value != null) {
                    saveEvent(value)
                }
            }
    }

    private fun setUpRecyclerView() {

        binding.recyclerViewEvents.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))


        eventsAdapter = RecyclerViewAdapter(eventsList, eventsMap, binding, requireContext())
        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerViewEvents.adapter = eventsAdapter

        //When the user does a swipe to the left this code is executed
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.recyclerViewEvents.adapter as RecyclerViewAdapter
                adapter.removeAt(viewHolder.adapterPosition, calendarSharedPreferences(requireContext()).currentChatId, calendarSharedPreferences(requireContext()).currentChatName)
            }


        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewEvents)
    }


    override fun onDestroy() {
        super.onDestroy()

        registration.remove()
    }
}

