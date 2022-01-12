package com.example.ipcalink.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ipcalink.R
import com.example.ipcalink.calendar.AddEventActivity
import com.example.ipcalink.calendar.Extensions
import com.example.ipcalink.calendar.Extensions.makeInVisible
import com.example.ipcalink.calendar.Extensions.makeVisible
import com.example.ipcalink.calendar.Extensions.setTextColorRes
import com.example.ipcalink.databinding.CalendarDayBinding
import com.example.ipcalink.databinding.FragmentCalendarBinding
import com.example.ipcalink.models.Chats
import com.example.ipcalink.models.Events
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.model.OutDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.Size
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.yearMonth
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    /*// TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalendarFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/

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

    private var eventsAdapter: RecyclerView.Adapter<*>? = null
    private var chatsAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null

    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    private val eventsMap = mutableMapOf<LocalDate, List<Events>>()
    private val eventsList = mutableListOf<Events>()

    private var chatsList : ArrayList<Chats> = ArrayList()

    private var currentChatId : String? = null
    private var currentChatName : String? = null

    private val userUID = Firebase.auth.uid

    val myLocale = Locale("pt", "PT")
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM", myLocale)

    private var imageArrowControl = 1
    private var imageHamburgerControl = 1

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
        binding.recyclerViewEvents.layoutManager = layoutManager
        eventsAdapter = EventsAdapter(null)
        binding.recyclerViewEvents.itemAnimator = null
        binding.recyclerViewEvents.adapter = eventsAdapter


        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewGroupChats.layoutManager = layoutManager
        chatsAdapter = ChatsAdapter()
        binding.recyclerViewGroupChats.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewGroupChats.adapter = chatsAdapter

        binding.recyclerViewGroupChats.visibility = View.GONE

        val daysOfWeek = Extensions.daysOfWeekFromLocale()

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(60)
        val endMonth = currentMonth.plusMonths(60)


        // Setup custom day size to fit two months on the screen.
        val dm = DisplayMetrics()
        //val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager

        binding.calendar.apply {

            //daySize = Size(140, 95)

            // We want the immediately following/previous month to be
            // partially visible so we multiply the total width by 0.73
            val monthWidth = (dm.widthPixels * 0.73).toInt()
            val dayWidth = monthWidth / 7
            val dayHeight = (dayWidth * 1.73).toInt() // We don't want a square calendar.
            daySize = Size(dayWidth, dayHeight)

            // Add margins around our card view.
            val horizontalMargin = Extensions.dpToPx(20, requireContext())
            val verticalMargin = Extensions.dpToPx(0, requireContext())
            setMonthMargins(start = horizontalMargin, end = horizontalMargin, top = verticalMargin, bottom = verticalMargin)

            setup(startMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        searchingEvents()

        binding.imageHamburger.setOnClickListener {
            imageHamburgerControl++

            if(imageHamburgerControl %2 == 0) {
                binding.recyclerViewGroupChats.visibility = View.VISIBLE
                insertingChats()
            } else {
                chatsList.clear()
                binding.recyclerViewGroupChats.visibility = View.GONE
                searchingEvents()
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
                val cardView1 = container.binding.cardView1
                val cardView2 = container.binding.cardView2

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        /*today -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.calendar_day_selected)
                            cardView.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                            cardView.setBackgroundResource(R.color.white)
                        }*/
                        selectedDate -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.calendar_day_selected)
                            cardView1.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                            cardView2.setBackgroundResource(R.color.white)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            textView.setBackgroundResource(R.drawable.calendar_day_not_selected)
                            cardView1.isVisible = eventsMap[day.date].orEmpty().isNotEmpty()
                            cardView2.setBackgroundResource(R.color.colorPrimary)
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.gray_155)
                    textView.setBackgroundResource(R.drawable.calendar_day_not_selected)
                    cardView1.makeInVisible()
                }
            }
        }

        binding.calendar.monthScrollListener = {

            binding.calendar.apply {

                //daySize = Size(140, 95)

                // We want the immediately following/previous month to be
                // partially visible so we multiply the total width by 0.73
                val monthWidth = (dm.widthPixels * 0.73).toInt()
                val dayWidth = monthWidth / 7
                val dayHeight = (dayWidth * 1.73).toInt() // We don't want a square calendar.
                daySize = Size(dayWidth, dayHeight)

                // Add margins around our card view.
                val horizontalMargin = Extensions.dpToPx(20, requireContext())
                val verticalMargin = Extensions.dpToPx(0, requireContext())
                setMonthMargins(start = horizontalMargin, end = horizontalMargin, top = verticalMargin, bottom = verticalMargin)
            }

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

        binding.imageArrow.setImageResource(R.drawable.round_keyboard_arrow_up_black_36)
        binding.imageArrow.setColorFilter(Color.argb(255, 0, 78, 56))



        binding.imageArrow.setOnClickListener {

            imageArrowControl++

            val firstDate = binding.calendar.findFirstVisibleDay()?.date ?: return@setOnClickListener
            val lastDate = binding.calendar.findLastVisibleDay()?.date ?: return@setOnClickListener

            val oneWeekHeight = binding.calendar.daySize.height
            val oneMonthHeight = oneWeekHeight * 6

            val oldHeight = if(imageArrowControl %2 == 0) oneMonthHeight else oneWeekHeight
            val newHeight = if(imageArrowControl %2 == 0) oneWeekHeight else oneMonthHeight

            if(imageArrowControl %2 == 1) {
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
                if (imageArrowControl %2 != 0) {
                    binding.calendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.ALL_MONTHS,
                        outDateStyle = OutDateStyle.END_OF_ROW,
                        maxRowCount = 6,
                        hasBoundaries = true
                    )
                }
            }
            animator.doOnEnd {
                if (imageArrowControl %2 == 0) {
                    binding.calendar.updateMonthConfiguration(
                        inDateStyle = InDateStyle.FIRST_MONTH,
                        outDateStyle = OutDateStyle.END_OF_ROW,
                        maxRowCount = 1,
                        hasBoundaries = false
                    )
                }

                if (imageArrowControl % 2 == 0) {
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

        /*binding.addButton.setOnClickListener {

            //val dayOfWeek = selectedDate!!.dayOfWeek
            val date = selectedDate

            val intent = Intent(context, AddEventActivity::class.java)
            //intent.putExtra("dayOfWeek", dayOfWeek.toString())
            intent.putExtra("date", date.toString())
            intent.putExtra("chatId", currentChatId)
            intent.putExtra("chatName", currentChatName)

            startActivity(intent)
        }*/
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

                val chat = Chats.fromHash(query)

                /*if(firstChat) {
                    currentChatId = chat.chatId!!
                    currentChatName = chat.chatName!!
                }

                firstChat = false*/

                chatsList.add(
                    Chats(chat.chatId, chat.chatName, chat.chatType, chat.notificationName,
                    chat.notificationKey, chat.photoUrl, chat.lastMessage,
                    chat.lastMessageSenderId, chat.lastMessageTimestamp)
                )

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

        binding.calendar.notifyCalendarChanged()
        eventsMap.clear()

        for(query in value){

            val event = Events.fromHash(query)

            val startDate = getDate(event.startDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")
            val endDate = getDate(event.endDate!!.seconds * 1000, "yyyy-MM-dd'T'HH:mm:ss.SSS")

            val localStartDate = LocalDate.parse(DateFormater(startDate))
            binding.calendar.notifyDateChanged(localStartDate)


            localStartDate?.let {
                eventsMap[it] = eventsMap[it].orEmpty().plus(Events(event.id, event.chatId, event.chatName, event.title, event.description, event.sendDate, event.senderId, event.startDate, event.endDate))
                updateAdapterForDate(it)
            }
        }


        binding.calendar.post {
            // Show today's events initially.
            selectDate(today)
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
        eventsList.clear()


        eventsList.addAll(this@CalendarFragment.eventsMap[date].orEmpty())
        //adapter!!.notifyItemInserted(events.size - 1)

        //eventsAdapter.apply {
        //}

        val month = monthTitleFormatter.format(date.month)
        binding.textViewMonth.text = month
        binding.textViewYear.text = date.year.toString()


        eventsAdapter?.notifyDataSetChanged()
    }


    inner class EventsAdapter(val onClick : ((Events) -> Unit)?) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

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
                textViewChatName.text = eventsList[position].chatName
            }
        }

        override fun getItemCount(): Int {
            return eventsList.size
        }
    }

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
                currentChatId = chatsList[position].chatId!!
                currentChatName = chatsList[position].chatName!!
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

    fun getHours(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("T").split(dateTime)
        val strArray2 = Pattern.compile(":").split(strArray[1])

        return strArray2[0].toString()
    }


    /*
        This function split the date and return only the Minutes in text
        @date = default date
     */
    fun getMinutes(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("T").split(dateTime)
        val strArray2 = Pattern.compile(":").split(strArray[1])

        return strArray2[1].toString()
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

        dbFirebase.collection("users").document(userUID!!).collection("events").whereEqualTo("chatId", currentChatId).addSnapshotListener { value, error ->

            if (error != null) {
                Log.w("ShowNotificationsFragment", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (value != null) {
                saveEvent(value)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
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
}