package com.example.vrescieandroid.data

data class Chat(
    val conversationId: String,
    val memberIds: List<String>,
    val memberNames: List<String>,
    val lastMessage: String?
)



