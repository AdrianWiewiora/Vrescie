package com.example.vrescieandroid.data

data class Message(val senderId: String, val text: String, val timestamp: Long) {
    constructor() : this("", "", 0)
}
