package com.example.vresciecompose.view_models

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.*

class ConfigurationProfileViewModel : ViewModel() {
    private val database = Firebase.database
    private val auth = Firebase.auth

    fun saveUserData(name: String, age: String, gender: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("user").child(userId)

        userRef.child("name").setValue(name)
        userRef.child("age").setValue(age)
        userRef.child("gender").setValue(gender)
        userRef.child("join_time").setValue(ServerValue.TIMESTAMP) // Firebase timestamp for join time
    }
}

