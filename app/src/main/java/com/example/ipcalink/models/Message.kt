package com.example.chatappfirebase.Models

import com.google.firebase.Timestamp

class Message {

    var senderId: String? = null
    var body: String? = null
    var photoUrl: String? = null
    var documentUrl: String? = null
    var timestamp: Timestamp? = null
    var unreadCount: Int? = null

    constructor()

    constructor(senderId: String, body: String, photoUrl: String, documentUrl: String, timestamp: Timestamp, unreadCount: Int) {
        this.senderId = senderId
        this.body = body
        this.photoUrl = photoUrl
        this.documentUrl = documentUrl
        this.timestamp = timestamp
        this.unreadCount = unreadCount
    }

}