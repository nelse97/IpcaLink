package com.example.ipcalink.models

import android.content.Context
import android.util.Base64
import com.example.ipcalink.AES.AES
import com.example.ipcalink.encryptedSharedPreferences.ESP
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import javax.crypto.spec.SecretKeySpec


class Event {

    var id : String? = null
    var title : String? = null
    var description : String? = null
    var sendDate : String? = null
    var senderId : String? = null
    var startDate : Timestamp? = null
    var endDate : Timestamp? = null
    var subject : String? = null

    constructor(
        id: String?,
        title: String?,
        description: String?,
        sendDate: String?,
        senderId: String?,
        startDate: Timestamp?,
        endDate: Timestamp?,
        subject: String?
    ) {
        this.id = id
        this.title = title
        this.description = description
        this.sendDate = sendDate
        this.senderId = senderId
        this.startDate = startDate
        this.endDate = endDate
        this.subject = subject
    }


    fun toHash() : HashMap<String, Any>{
        val hashMap = HashMap<String, Any>()

        hashMap["id"] = id!!
        hashMap["title"] = title!!
        hashMap["description"] = description!!
        hashMap["sendDate"] = sendDate!!
        hashMap["senderId"] = senderId!!
        hashMap["startDate"] = startDate!!
        hashMap["endDate"] = endDate!!
        hashMap["subject"] = subject!!


        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): Event {

            return Event(
                hashMap["id"] as String,
                hashMap["title"] as String,
                hashMap["description"] as String,
                hashMap["sendDate"] as String,
                hashMap["senderId"] as String,
                hashMap["startDate"] as Timestamp,
                hashMap["endDate"] as Timestamp,
                hashMap["subject"] as String
            )
        }
    }
}