package com.example.ipcalink.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

class PrivateUserChat {
    var chatId : String? = null
    var chatName : String? = null
    var chatType : String? = null
    var photoUrl : String? = null
    var lastMessage : String? = null
    var chatUserId : String? = null
    var lastMessageTimestamp : Timestamp? = null

    constructor(
            chatId: String?,
            chatName: String?,
            chatType: String?,
            photoUrl: String?,
            lastMessage: String?,
            senderId: String?,
            lastMessageTimestamp: Timestamp?
    ) {
        this.chatId = chatId
        this.chatName = chatName
        this.chatType = chatType
        this.photoUrl = photoUrl
        this.lastMessage = lastMessage
        this.chatUserId = senderId
        this.lastMessageTimestamp = lastMessageTimestamp
    }


    constructor()


    fun toHash() : HashMap<String, Any>{
        val hashMap = HashMap<String, Any>()

        hashMap["chatId"] = chatId!!
        hashMap["chatName"] = chatName!!
        hashMap["chatType"] = chatType!!
        hashMap["photoUrl"] = photoUrl!!
        hashMap["lastMessage"] = lastMessage!!
        hashMap["senderId"] = chatUserId!!
        hashMap["lastMessageTimestamp"] = lastMessageTimestamp!!

        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot) : PrivateUserChat {
            return PrivateUserChat(
                    hashMap["chatId"] as String,
                    hashMap["chatName"] as String,
                    hashMap["chatType"] as String,
                    hashMap["photoUrl"] as String,
                    hashMap["lastMessage"] as String,
                    hashMap["lastMessageSenderId"] as String,
                    hashMap["lastMessageTimestamp"] as Timestamp?
            )
        }
    }
}