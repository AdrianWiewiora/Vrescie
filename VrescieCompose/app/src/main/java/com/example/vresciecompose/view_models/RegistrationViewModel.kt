package com.example.vresciecompose.view_models
import androidx.lifecycle.ViewModel
import com.example.vresciecompose.authentication.EMailAuthentication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegistrationViewModel : ViewModel() {
    private val emailAuthentication = EMailAuthentication()
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess
    private val _errorMessage = MutableStateFlow(false)
    val errorMessage: StateFlow<Boolean> = _errorMessage

    fun registerWithEmail(email: String, password: String, repeatPassword: String) {
        if (password != repeatPassword) {
            _errorMessage.value = true
            return
        }
        emailAuthentication.registerWithEmail(email, password,
            onSuccess = {
                _registrationSuccess.value = true
            },
            onFailure = {
                _errorMessage.value = true
            }
        )
    }
}

