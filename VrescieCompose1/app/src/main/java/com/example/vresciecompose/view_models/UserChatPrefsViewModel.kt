package com.example.vresciecompose.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vresciecompose.data.UserChatPrefs
import com.example.vresciecompose.data.UserChatPrefsDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class UserChatPrefsViewModel( private val userChatPrefsDao: UserChatPrefsDao) : ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _userChatPrefs = MutableLiveData<UserChatPrefs?>()
    val userChatPrefs: LiveData<UserChatPrefs?> = _userChatPrefs

    init {
        fetchUserChatPrefs()
    }

    fun fetchUserChatPrefs() {
        viewModelScope.launch {
            val chatPrefs = withContext(Dispatchers.IO) {
                userChatPrefsDao.getUserChatPrefs()
            }
            withContext(Dispatchers.Main) {
                _userChatPrefs.value = chatPrefs
            }
        }
    }

    fun savePreferences(
        selectedGenders: String,
        ageRange: ClosedRange<Float>,
        isProfileVerified: Boolean,
        relationshipPreference: Boolean,
        maxDistance: Float
    ) {
        viewModelScope.launch {
            val existingPrefs = withContext(Dispatchers.IO) {
                userChatPrefsDao.getUserChatPrefs()
            }

            val userChatPrefs = UserChatPrefs(
                id = 1,
                selectedGenders = selectedGenders,
                ageStart = ageRange.start,
                ageEnd = ageRange.endInclusive,
                isProfileVerified = isProfileVerified,
                relationshipPreference = relationshipPreference,
                maxDistance = maxDistance
            )

            if (existingPrefs != null) {
                userChatPrefsDao.update(userChatPrefs)
            } else {
                userChatPrefsDao.insert(userChatPrefs)
            }
        }
    }

    // Funkcja do zapisywania danych użytkownika i preferencji do firebase
    fun saveUserDataToDatabase(
        selectedGenders: String,
        ageRange: ClosedFloatingPointRange<Float>,
        isProfileVerified: Boolean = false,
        relationshipPreference: Boolean,
        maxDistance: Float,
        latitude: Double?,
        longitude: Double?
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val lastSeen = Calendar.getInstance().timeInMillis
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("user/$userId")
            val userInfoRef = database.getReference("vChatUsers/$userId/info")

            userRef.get().addOnSuccessListener { dataSnapshot ->
                val age = dataSnapshot.child("age").getValue(String::class.java)
                val gender = dataSnapshot.child("gender").getValue(String::class.java)

                if (age != null && gender != null) {
                    userInfoRef.setValue(
                        mapOf(
                            "age" to age,
                            "email" to user.email,
                            "gender" to gender,
                            "lastSeen" to lastSeen,
                            "latitude" to latitude,
                            "longitude" to longitude
                        )
                    ).addOnCompleteListener { userInfoTask ->
                        if (userInfoTask.isSuccessful) {
                            val userPrefRef = database.getReference("vChatUsers/$userId/pref")
                            userPrefRef.setValue(
                                mapOf(
                                    "age_min_pref" to ageRange.start.toInt(),
                                    "age_max_pref" to ageRange.endInclusive.toInt(),
                                    "gender_pref" to selectedGenders,
                                    "location_max_pref" to maxDistance.toInt(),
                                    "verification_pref" to isProfileVerified,
                                    "relation_pref" to relationshipPreference
                                )
                            )
                        } else {
                            throw RuntimeException("Failed to save user info: ${userInfoTask.exception}")
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                throw RuntimeException("Failed to fetch user data to database: $exception")
            }
        }
    }
}