package com.example.ipcalink.models

import com.google.firebase.firestore.QueryDocumentSnapshot

class Subjects {

    var name: String = ""
    var abbreviation: String = ""
    var semester: String = ""

    constructor()

    constructor(
        name: String,
        abbreviation: String,
        semester: String
    ) {
        this.name = name
        this.abbreviation = abbreviation
        this.semester = semester
    }

    fun toHash(): HashMap<String, Any> {
        var hashMap = HashMap<String, Any>()
        hashMap["name"] = name
        hashMap["abbreviation"] = abbreviation
        hashMap["semester"] = semester
        return hashMap
    }

    companion object {
        fun fromHash(hashMap: QueryDocumentSnapshot): Subjects {
            return Subjects(
                hashMap["name"] as String,
                hashMap["abbreviation"] as String,
                hashMap["semester"] as String
            )
        }
    }
}