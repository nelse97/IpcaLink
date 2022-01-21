package com.example.chatappfirebase.Models

import com.google.firebase.Timestamp

class Message {

    var senderId: String? = null
    var body: String? = null
    var fileUrl: String? = null
    var timestamp: Timestamp? = null
    var unreadCount: Int? = null

    constructor()

    constructor(senderId: String, body: String, fileUrl: String, timestamp: Timestamp, unreadCount: Int) {
        this.senderId = senderId
        this.body = body
        this.fileUrl = fileUrl
        this.timestamp = timestamp
        this.unreadCount = unreadCount
    }

}