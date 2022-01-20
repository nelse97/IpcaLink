package com.example.ipcalink.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.type.DateTime
import java.util.*
import kotlin.collections.HashMap

class User {

    lateinit var userId: String
    lateinit var name: String
    lateinit var photoUrl: String
    lateinit var email: String
    lateinit var bio: String
    @field:JvmField // use this annotation if your Boolean field is prefixed with 'is'
    var isOnline: Boolean? = null
    lateinit var lastSeen: Timestamp

    constructor()

    constructor(
        userId        : String,
        name    : String,
        photoUrl    : String,
        email       : String,
        bio: String,
        lastSeen: Timestamp,
        isOnline: Boolean
    ) {
        this.userId      = userId
        this.name  = name
        this.photoUrl = photoUrl
        this.email     = email
        this.bio = bio
        this.lastSeen = lastSeen
        this.isOnline = isOnline
    }

    fun toHash() : HashMap<String, Any>{
        var hashMap = HashMap<String, Any>()
        hashMap.put("userId", userId)
        hashMap.put("name"   , name)
        hashMap.put("photoUrl", photoUrl)
        hashMap.put("email"  , email)
        hashMap.put("bio"  , bio)
        isOnline?.let { hashMap.put("isOnline"  , it) }
        hashMap.put("lastSeen"  , lastSeen)
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
                hashMap["lastSeen"   ] as Timestamp,
                hashMap["isOnline"   ] as Boolean
            )
            return user
        }
    }
}