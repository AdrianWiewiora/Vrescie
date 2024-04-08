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
