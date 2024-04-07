package com.example.vresciecompose.authentication

data class SignInState(
    val isSignedSuccessful: Boolean = false,
    val signInError: String? = null
)
