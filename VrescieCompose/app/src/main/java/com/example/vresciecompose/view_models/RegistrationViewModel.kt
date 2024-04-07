package com.example.vresciecompose.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vresciecompose.authentication.EMailAuthentication
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegistrationViewModel : ViewModel() {

    private val emailAuthentication = EMailAuthentication()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun registerWithEmail(email: String, password: String, repeatPassword: String) {
        if (password != repeatPassword) {
            _errorMessage.value = "Passwords do not match"
            return
        }
        emailAuthentication.registerWithEmail(email, password,
            onSuccess = {
                _registrationSuccess.value = true
            },
            onFailure = { errorMessage ->
                _errorMessage.value = errorMessage
            }
        )
    }

}
