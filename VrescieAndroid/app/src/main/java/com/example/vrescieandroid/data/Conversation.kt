package com.example.vrescieandroid.data

data class Conversation(val userId1: String, val userId2: String) {
    constructor() : this("", "")
}
