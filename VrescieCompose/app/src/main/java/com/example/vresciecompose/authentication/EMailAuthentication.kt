package com.example.vresciecompose.authentication



import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EMailAuthentication {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    fun registerWithEmail(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""
                    val userEmail = user?.email ?: ""

                    // Dodanie danych użytkownika do bazy danych
                    val userRef = database.getReference("user").child(userId)
                    userRef.child("email").setValue(userEmail)
                        .addOnSuccessListener {
                            Log.d("EMailAuthentication", "User added to database")
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("EMailAuthentication", "Failed to add user to database: ${exception.message}")
                            onFailure(exception.message ?: "Unknown error")
                        }
                } else {
                    Log.e("EMailAuthentication", "Registration failed: ${task.exception?.message}")
                    onFailure(task.exception?.message ?: "Unknown error")
                }
            }
    }
}

