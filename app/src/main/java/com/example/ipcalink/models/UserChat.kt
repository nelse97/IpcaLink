package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.type.DateTime

class UserChat {

    var chatId   : String = ""
    var chatName : String = ""
    var chatType : String = ""
    var photoUrl : String = ""
    var lastMessageTimestamp : String = ""
    var lastMessageSenderId : String = ""
    var lastMessage: String = ""

    constructor()

    constructor(
        chatId        : String,
        chatName    : String,
        chatType    : String,
        photoUrl       : String,
        lastMessageTimestamp: String,
        lastMessageSenderId: String,
        lastMessage: String
    ) {
        this.chatId      = chatId
        this.chatName  = chatName
        this.chatType = chatType
        this.photoUrl     = photoUrl
        this.lastMessageTimestamp = lastMessageTimestamp
        this.lastMessageSenderId = lastMessageSenderId
        this.lastMessage = lastMessage
    }

    fun toHash() : HashMap<String, Any>{
        var hashMap = HashMap<String, Any>()
        hashMap.put("chatId"   , chatId)
        hashMap.put("chatName", chatName)
        hashMap.put("chatType", chatType)
        hashMap.put("photoUrl"  , photoUrl)
        hashMap.put("lastMessageTimestamp", lastMessageTimestamp)
        hashMap.put("lastMessageSenderId", lastMessageSenderId)
        hashMap.put("lastMessage", lastMessage)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): UserChat {
            val userChat = UserChat(
                hashMap["chatId"] as String,
                hashMap["chatName"] as String,
                hashMap["chatType"] as String,
                hashMap["photoUrl"] as String,
                hashMap["lastMessageTimestamp"] as String,
                hashMap["lastMessageSenderId"] as String,
                hashMap["lastMessage"] as String
            )
            return userChat
        }
    }

}