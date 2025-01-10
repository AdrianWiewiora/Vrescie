package com.example.vresciecompose.view_models

import androidx.lifecycle.ViewModel
import com.example.vresciecompose.data.SignInResult
import com.example.vresciecompose.data.SignInState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel: ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun oneSignInResult(result: SignInResult) {
        _state.update { it.copy(
            isSignedSuccessful = result.data != null,
            signInError = result.errorMessage,
            isNewAccount = result.isNewAccount
        ) }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}