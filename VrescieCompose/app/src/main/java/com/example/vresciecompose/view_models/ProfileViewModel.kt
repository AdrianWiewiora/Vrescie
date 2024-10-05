package com.example.vresciecompose.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vresciecompose.authentication.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

open class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    // Nowy MutableLiveData dla profileConfigured
    private val _isProfileConfigured = MutableLiveData<Boolean>()
    val isProfileConfigured: LiveData<Boolean> = _isProfileConfigured

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
                        // Pobieramy profil użytkownika
                        val age = snapshot.child("age").getValue(String::class.java)?.toIntOrNull() ?: 0
                        val email = snapshot.child("email").getValue(String::class.java) ?: ""
                        val gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                        val joinTime = snapshot.child("join_time").getValue(Long::class.java) ?: 0
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val profileConfigured = snapshot.child("profileConfigured").getValue(Boolean::class.java) ?: false

                        // Ustawiamy wartości
                        val userProfile = UserProfile(name, age, email, gender, joinTime.toString())
                        _userProfile.postValue(userProfile)
                        _isProfileConfigured.postValue(profileConfigured) // Zaktualizuj _isProfileConfigured
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Obsługa błędu pobierania danych
                }
            })
        }
    }

    // Metoda do pobierania wartości profileConfigured
    fun isProfileConfigured(): Boolean {
        return _isProfileConfigured.value ?: false
    }

}

