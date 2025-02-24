package com.example.vresciecompose.data

data class SignInState(
    val isSignedSuccessful: Boolean = false,
    val signInError: String? = null,
    val isNewAccount: Boolean = false
)
