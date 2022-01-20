package com.example.ipcalink.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot


class Events {

    var id : String? = null
    var title : String? = null
    var description : String? = null
    var sendDate : Timestamp? = null
    var senderId : String? = null
    var startDate : Timestamp? = null
    var endDate : Timestamp? = null

    constructor(
        id: String?,
        title: String?,
        description: String?,
        sendDate: Timestamp?,
        senderId: String?,
        startDate: Timestamp?,
        endDate: Timestamp?
    ) {
        this.id = id
        this.title = title
        this.description = description
        this.sendDate = sendDate
        this.senderId = senderId
        this.startDate = startDate
        this.endDate = endDate
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


        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): Events {

            return Events(
                hashMap["id"] as String,
                hashMap["title"] as String,
                hashMap["description"] as String,
                hashMap["sendDate"] as Timestamp,
                hashMap["senderId"] as String?,
                hashMap["startDate"] as Timestamp,
                hashMap["endDate"] as Timestamp
            )
        }
    }
}