package com.example.vresciecompose.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vresciecompose.authentication.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser: FirebaseUser? = auth.currentUser

        currentUser?.let { user ->
            val userId = user.uid
            val userReference: DatabaseReference = database.getReference("/user/$userId")

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val age = snapshot.child("age").getValue(String::class.java)?.toIntOrNull() ?: 0
                        val email = snapshot.child("email").getValue(String::class.java) ?: ""
                        val gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                        val joinTime = snapshot.child("join_time").getValue(Long::class.java) ?: 0
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""

                        val userProfile = UserProfile(name, age, email, gender, joinTime.toString())
                        _userProfile.postValue(userProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Obsługa błędu pobierania danych
                }
            })
        }
    }
}

