package com.example.ipcalink.calendar

import android.annotation.SuppressLint
import com.example.ipcalink.calendar.Extensions.myLocale
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

object CalendarHelper {


    //This function split the date and return only the Hours in text
    fun getHours(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("T").split(dateTime)
        val strArray2 = Pattern.compile(":").split(strArray[1])

        return strArray2[0].toString()
    }


    //This function split the date and return only the Minutes in text
    fun getMinutes(dateTime: String): String {

        // Split the date
        val strArray = Pattern.compile("T").split(dateTime)
        val strArray2 = Pattern.compile(":").split(strArray[1])

        return strArray2[1].toString()
    }

    //This function picks up the milliSeconds corresponding to a date
    //and then transforms it into a string date
    @SuppressLint("SimpleDateFormat")
    fun getDate(milliSeconds: Long, dateFormat: String?): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }


    fun DateFormater(date: String): String {

        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS",  Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val dateToFormat = LocalDate.parse(date, inputFormatter)
        val formattedDate = outputFormatter.format(dateToFormat)

        return formattedDate
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
}