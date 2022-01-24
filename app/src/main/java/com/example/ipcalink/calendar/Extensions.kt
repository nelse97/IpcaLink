package com.example.ipcalink.calendar

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*

object Extensions {

    //Here i define a variable of type locale so i can translate the calendar
    val myLocale = Locale("pt", "PT")

    //This function makes a view Visible
    fun View.makeVisible() {
        visibility = View.VISIBLE
    }

    //This function makes a view Invisible
    fun View.makeInVisible() {
        visibility = View.INVISIBLE
    }

    //This function makes a view Gone
    fun View.makeGone() {
        visibility = View.GONE
    }

    //This function transforms a dp metric into a pixel metric
    fun dpToPx(dp: Int, context: Context): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()

    //this function inflates a view
    internal fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return context.layoutInflater.inflate(layoutRes, this, attachToRoot)
    }


    internal val Context.layoutInflater: LayoutInflater
        get() = LayoutInflater.from(this)

    internal val Context.inputMethodManager
        get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    internal fun Boolean?.orFalse(): Boolean = this ?: false


    //This function takes the id of a drawable and gets the object correspondent to that drawable
    internal fun Context.getDrawableCompat(@DrawableRes drawable: Int) =
        ContextCompat.getDrawable(this, drawable)

    //This function takes the id of a color and gets the object correspondent to that color
    internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

    //This function takes the id of a color and gets the object correspondent and then sets the text with that color
    internal fun TextView.setTextColorRes(@ColorRes color: Int) =
        setTextColor(context.getColorCompat(color))

    //This function returns the days of the week from my locale
    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    //This function defines the corner radius of a gradient Drawable
    fun GradientDrawable.setCornerRadius(
        topLeft: Float = 0F,
        topRight: Float = 0F,
        bottomRight: Float = 0F,
        bottomLeft: Float = 0F
    ) {
        cornerRadii = arrayOf(
            topLeft, topLeft,
            topRight, topRight,
            bottomRight, bottomRight,
            bottomLeft, bottomLeft
        ).toFloatArray()
    }
}