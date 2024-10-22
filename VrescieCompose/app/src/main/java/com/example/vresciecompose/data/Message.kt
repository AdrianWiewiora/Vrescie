package com.example.vresciecompose.data

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val messageSeen: Boolean = false
) {
    constructor() : this("", "", 0)
}

