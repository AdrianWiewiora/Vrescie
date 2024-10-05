package com.example.vresciecompose.authentication



import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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

                    // Loguj użytkownika natychmiast po rejestracji
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { loginTask ->
                            if (loginTask.isSuccessful) {
                                val userRef = database.getReference("user").child(userId)
                                userRef.child("email").setValue(userEmail)
                                    .addOnSuccessListener {
                                        Log.d("EMailAuthentication", "User added to database")

                                        // Dodaj flagę isFirstLogged
                                        userRef.child("profileConfigured").setValue(false)
                                            .addOnSuccessListener {
                                                Log.d("EMailAuthentication", "isFirstLogged flag added to user")

                                                // Wyślij e-mail weryfikacyjny
                                                user?.sendEmailVerification()
                                                    ?.addOnCompleteListener { emailTask ->
                                                        if (emailTask.isSuccessful) {
                                                            Log.d("EMailAuthentication", "Verification email sent")

                                                            // Wyloguj użytkownika zaraz po wysłaniu weryfikacji
                                                            auth.signOut()

                                                            onSuccess() // Wywołaj onSuccess, jeśli weryfikacja e-maila została wysłana
                                                        } else {
                                                            Log.e("EMailAuthentication", "Failed to send verification email: ${emailTask.exception?.message}")
                                                            onFailure("Failed to send verification email")
                                                        }
                                                    }
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("EMailAuthentication", "Failed to add isFirstLogged flag: ${exception.message}")
                                                onFailure(exception.message ?: "Unknown error")
                                            }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("EMailAuthentication", "Failed to add user to database: ${exception.message}")
                                        onFailure(exception.message ?: "Unknown error")
                                    }
                            } else {
                                onFailure("Failed to login user after registration")
                            }
                        }
                } else {
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email address."
                        is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                        is FirebaseNetworkException -> "Network error. Please try again."
                        else -> "Registration failed. ${exception?.message}"
                    }
                    onFailure(errorMessage)
                }
            }
    }

    fun isEmailVerified(): Boolean {
        val user = auth.currentUser
        return user?.isEmailVerified == true
    }
}


