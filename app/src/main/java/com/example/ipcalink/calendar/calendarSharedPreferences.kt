package com.example.ipcalink.calendar

import android.content.Context
import android.content.SharedPreferences


lateinit var sharedPreferences : SharedPreferences


class calendarSharedPreferences {

    constructor(context: Context){
        sharedPreferences =
            context.
            getSharedPreferences("CALENDAR_PREF", Context.MODE_PRIVATE)
    }

    var currentChatId : String?
        get() = sharedPreferences.getString("CURRENT_CHAT_ID", "")
        set(value) {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString("CURRENT_CHAT_ID", value)
            editor.apply()
        }

    var currentChatName : String?
        get() = sharedPreferences.getString("CURRENT_CHAT_NAME", "")
        set(value) {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString("CURRENT_CHAT_NAME", value)
            editor.apply()
        }

}