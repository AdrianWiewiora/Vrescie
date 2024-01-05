package com.example.vrescieandroid.data

import com.google.firebase.database.ServerValue

data class User(
    val id: String = "",
    val email: String = "",
    val age: String = "",
    val gender: String = "",
    val lastSeen: Any = ServerValue.TIMESTAMP
)
