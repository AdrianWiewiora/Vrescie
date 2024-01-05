package com.example.vrescieandroid.data

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val age: String = "",
    val e_mail: String = "",
    val gender: String = "",
    val join_time: Long? = 0
) {
    constructor() : this("", "", "", "", "", 0)
}

