package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.type.DateTime

class UserChat {

    var chatId   : String = ""
    var chatName : String = ""
    var photoUrl : String = ""


    constructor(
        chatId        : String,
        chatName    : String,
        photoUrl       : String
    ) {
        this.chatId      = chatId
        this.chatName  = chatName
        this.photoUrl     = photoUrl
    }

    fun toHash() : HashMap<String, Any>{
        var hashMap = HashMap<String, Any>()
        hashMap.put("chatId"   , chatId)
        hashMap.put("chatName", chatName)
        hashMap.put("photoUrl"  , photoUrl)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): UserChat {
            val userChat = UserChat(
                hashMap["chatId"    ] as String,
                hashMap["chatName"   ] as String,
                hashMap["photoUrl"   ] as String
            )
            return userChat
        }
    }

}