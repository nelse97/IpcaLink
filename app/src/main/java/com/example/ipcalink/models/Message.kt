package com.example.ipcalink.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.type.DateTime
import java.sql.Time

class Message {

    var senderId   : String = ""
    var senderName : String = ""
    var fileUrl : String = ""
    var text : String = ""
    var timeStamp : Timestamp? = null
    var unreadCount : Int? = null
    var visible : Boolean? = null


    constructor(
        senderId        : String,
        senderName    : String,
        fileUrl       : String,
        text      : String,
        timeStamp : Timestamp,
        unreadCount : Int,
        visible : Boolean
    ) {
        this.senderId      = senderId
        this.senderName  = senderName
        this.fileUrl     = fileUrl
        this.text = text
        this.timeStamp = timeStamp
        this.unreadCount = unreadCount
        this.visible = visible
    }

    fun toHash() : HashMap<String, Any>{
        var hashMap = HashMap<String, Any>()
        hashMap.put("senderId"   , senderId)
        hashMap.put("senderName", senderName)
        hashMap.put("fileUrl"  , fileUrl)
        hashMap.put("text", text)
        hashMap.put("timeStamp", timeStamp as Any)
        hashMap.put("unreadCount", unreadCount as Any)
        hashMap.put("visible", visible as Any)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): Message {
            val message = Message(
                hashMap["senderId"    ] as String,
                hashMap["senderName"   ] as String,
                hashMap["fileUrl"   ] as String,
                hashMap["text"   ] as String,
                hashMap["timeStamp"   ] as Timestamp,
                hashMap["unreadCount"   ] as Int,
                hashMap["visible"   ] as Boolean,
            )
            return message
        }
    }

}