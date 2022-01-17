package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class IpcaUser {

    var name        : String = ""
    var email       : String = ""
    var type        : String = ""
    var gender      : String = ""

    constructor()

    constructor(
        name      : String,
        email    : String,
        type       : String,
        gender    : String,
    ) {
        this.name       = name
        this.email      = email
        this.type       = type
        this.gender     = gender
    }

    fun toHash() : HashMap<String, Any>{
        var hashMap = HashMap<String, Any>()
        hashMap.put("nome"      , name)
        hashMap.put("email"     , email)
        hashMap.put("genero"    , gender)
        hashMap.put("tipo"      , type)
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): IpcaUser {
            val user = IpcaUser(
                hashMap["nome"]     as String,
                hashMap["email"]     as String,
                hashMap["genero"]     as String,
                hashMap["tipo"]     as String
            )
            return user
        }
    }
}