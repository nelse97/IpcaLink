package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class IpcaUser {

    var name        : String = ""
    var email       : String = ""
    var type        : String = ""
    var gender      : String = ""
    var userId      : String = ""

    constructor()

    constructor(
        name        : String,
        email       : String,
        type        : String,
        gender      : String,
        userId         : String
    ) {
        this.name       = name
        this.email      = email
        this.type       = type
        this.gender     = gender
        this.userId        = userId
    }

    fun toHash() : HashMap<String, Any>{
        val hashMap = HashMap<String, Any>()
        hashMap["name"]     = name
        hashMap["email"]    = email
        hashMap["gender"]   = gender
        hashMap["type"]     = type
        hashMap["userId"]   = userId
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): IpcaUser {
            return IpcaUser(
                hashMap["name"]     as String,
                hashMap["email"]    as String,
                hashMap["gender"]   as String,
                hashMap["type"]     as String,
                hashMap["userId"]   as String
            )
        }
    }
}