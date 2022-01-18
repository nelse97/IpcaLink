package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class User {

    var userId     : String = ""
    var name       : String = ""
    var email      : String = ""
    var bio        : String = ""
    var isOnline   : Boolean? = null
    var lastSeen   : String = ""
    var photoURl   : String = ""

    constructor()


    constructor(
        userId      : String,
        name        : String,
        photoURl    : String,
        email       : String,
        bio         : String,
        isOnline    : Boolean,
        lastSeen    : String
    ) {
        this.userId    = userId
        this.name      = name
        this.photoURl  = photoURl
        this.email     = email
        this.bio       = bio
        this.isOnline  = isOnline
        this.lastSeen  = lastSeen
    }

    fun toHash() : HashMap<String, Any>{
        var hashMap = HashMap<String, Any>()
        hashMap.put("userId", userId)
        hashMap.put("name"   , name)
        hashMap.put("photoUrl", photoURl)
        hashMap.put("email"  , email)
        hashMap.put("bio"  , bio)
        hashMap.put("isOnline", isOnline as Any)
        hashMap.put("lastSeen", lastSeen)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): User {
            val user = User(
                hashMap["userId"] as String,
                hashMap["name"    ] as String,
                hashMap["photoUrl"   ] as String,
                hashMap["email"   ] as String,
                hashMap["bio"   ] as String,
                hashMap["isOnline"   ] as Boolean,
                hashMap["lastSeen"   ] as String
            )
            return user
        }
    }
}