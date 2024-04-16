package com.example.vresciecompose.authentication


data class SignInResult(
    val data: UserData?,
    val errorMessage: String?,
    val isNewAccount: Boolean = false
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val email: String = "",
    val gender: String = "",
    val joinDate: String = ""
)
