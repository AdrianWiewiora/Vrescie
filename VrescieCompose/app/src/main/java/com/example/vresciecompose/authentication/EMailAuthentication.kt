package com.example.vresciecompose.authentication

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class EMailAuthentication {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun registerWithEmail(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EMailAuthentication", "Registration successful")
                    onSuccess()
                } else {
                    Log.e("EMailAuthentication", "Registration failed: ${task.exception?.message}")
                    onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }
}
