package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Chats {

    var chatId: String? = null
    var chatName: String? = null
    var chatType: String? = null
    var notificationName: String? = null
    var notificationKey: String? = null
    var photoUrl: String? = null

    constructor(
        chatId: String?,
        chatName: String?,
        chatType: String?,
        notificationName: String?,
        notificationKey: String?,
        photoUrl: String?
    ) {
        this.chatId = chatId
        this.chatName = chatName
        this.chatType = chatType
        this.notificationName = notificationName
        this.notificationKey = notificationKey
        this.photoUrl = photoUrl
    }


    fun toHash(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()

        hashMap["chatId"] = chatId!!
        hashMap["chatName"] = chatName!!
        hashMap["chatType"] = chatType!!
        hashMap["notificationName"] = notificationName!!
        hashMap["notificationKey"] = notificationKey!!
        hashMap["photoUrl"] = photoUrl!!

        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): Chats {

            return Chats(
                hashMap["chatId"] as String,
                hashMap["chatName"] as String,
                hashMap["chatType"] as String,
                hashMap["notificationName"] as String,
                hashMap["notificationKey"] as String,
                hashMap["photoUrl"] as String
            )
        }
    }
}