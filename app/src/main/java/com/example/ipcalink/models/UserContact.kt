package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class UserContact {

    var userId   : String = ""
    var photoUrl : String = ""
    var bio : String = ""
    var name : String = ""


    constructor(
        userId        : String,
        photoUrl    : String,
        bio       : String,
        name      : String
    ) {
        this.userId      = userId
        this.photoUrl  = photoUrl
        this.bio     = bio
        this.name = name
    }

    fun toHash() : HashMap<String, Any>{
        var hashMap = HashMap<String, Any>()
        hashMap.put("userId"   , userId)
        hashMap.put("photoUrl", photoUrl)
        hashMap.put("bio"  , bio)
        hashMap.put("name", name)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): UserContact {
            val userContact = UserContact(
                hashMap["userId"    ] as String,
                hashMap["photoUrl"   ] as String,
                hashMap["bio"   ] as String,
                hashMap["name"   ] as String
            )
            return userContact
        }
    }

}