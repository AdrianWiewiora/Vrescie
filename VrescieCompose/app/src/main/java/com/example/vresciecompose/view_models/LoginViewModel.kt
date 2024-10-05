package com.example.vresciecompose.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess() // Po zalogowaniu wywołaj onSuccess
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

    fun checkFirstLogin(onSuccess: (Boolean) -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure("Użytkownik nie jest zalogowany.")
            return
        }

        val userRef = database.getReference("user").child(userId)
        userRef.child("profileConfigured").get()
            .addOnSuccessListener { dataSnapshot ->
                val profileConfigured = dataSnapshot.getValue(Boolean::class.java) ?: false
                onSuccess(profileConfigured)
            }
            .addOnFailureListener { exception ->
                onFailure("Nie udało się sprawdzić statusu logowania: ${exception.message}")
            }
    }


}




