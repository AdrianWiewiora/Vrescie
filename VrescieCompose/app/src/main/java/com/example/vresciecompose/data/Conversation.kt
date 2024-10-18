package com.example.vresciecompose.data

data class Conversation(
    val id: String,
    val name: String,
    val secondParticipantId: String = ""
)
