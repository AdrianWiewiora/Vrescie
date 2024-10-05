package com.example.vresciecompose.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        onSuccess()
                    } else {
                        onFailure("E-mail nie został zweryfikowany. Sprawdź swoją skrzynkę pocztową.")
                    }
                } else {
                    val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                    val errorMessage = when (errorCode) {
                        "ERROR_INVALID_CREDENTIAL" -> "Niepoprawne dane"
                        "ERROR_INVALID_EMAIL" -> "Nieprawidłowy format adresu email"
                        else -> "Wystąpił nieznany błąd"
                    }
                    onFailure(errorMessage)
                }
            }
    }
}




