package com.example.vresciecompose.view_models

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.vresciecompose.Navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File

class ConfigurationProfileViewModel : ViewModel() {
    private val database = Firebase.database
    private val auth = Firebase.auth
    private val storage = FirebaseStorage.getInstance()

    fun saveUserData(name: String, age: String, gender: String, photoUrl: String?, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("user").child(userId)

        userRef.child("name").setValue(name)
        userRef.child("age").setValue(age)
        userRef.child("gender").setValue(gender)
        userRef.child("join_time").setValue(ServerValue.TIMESTAMP)

        // Zapisz URL zdjęcia, jeśli jest dostępny
        photoUrl?.let {
            userRef.child("photoUrl").setValue(it)
        }

        onComplete()
    }


    fun setProfileConfigured() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("user").child(userId)
        userRef.child("profileConfigured").setValue(true)
            .addOnSuccessListener {
                Log.d("ConfigurationProfileViewModel", "Profile configured successfully.")
            }
            .addOnFailureListener { exception ->
                Log.e("ConfigurationProfileViewModel", "Failed to set profileConfigured: ${exception.message}")
            }
    }
}


