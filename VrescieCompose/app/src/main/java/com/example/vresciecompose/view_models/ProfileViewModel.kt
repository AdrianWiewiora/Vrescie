package com.example.vresciecompose.view_models

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vresciecompose.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ProfileViewModel(private val appContext: Context) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)

    val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

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
                        val age = snapshot.child("age").getValue(String::class.java)?.toIntOrNull() ?: 0
                        val email = snapshot.child("email").getValue(String::class.java) ?: ""
                        val gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                        val joinTime = snapshot.child("join_time").getValue(Long::class.java) ?: 0
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val profileConfigured = snapshot.child("profileConfigured").getValue(Boolean::class.java) ?: false
                        val profileImageUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""

                        // Zapisz zdjęcie lokalnie
                        saveImageLocally(profileImageUrl, userId)

                        // Zapisz dane do SharedPreferences
                        saveProfileToPreferences(name, age, email, gender, joinTime, profileConfigured)

                        val userProfile = UserProfile(name, age, email, gender, joinTime.toString(), profileImageUrl)
                        _userProfile.postValue(userProfile)
                        _isProfileConfigured.postValue(profileConfigured)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Obsługa błędu pobierania danych
                }
            })
        }
    }

    private fun saveProfileToPreferences(name: String, age: Int, email: String, gender: String, joinTime: Long, profileConfigured: Boolean) {
        with(sharedPreferences.edit()) {
            putString("name", name)
            putInt("age", age)
            putString("email", email)
            putString("gender", gender)
            putLong("joinTime", joinTime)
            putBoolean("profileConfigured", profileConfigured)
            apply()
        }
    }

    private fun saveImageLocally(imageUrl: String, userId: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(imageUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Obsłuż błąd
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val inputStream = responseBody.byteStream()
                    val localFile = File(appContext.filesDir, "$userId.jpg") // Możesz dostosować nazwę pliku
                    val outputStream = FileOutputStream(localFile)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Zapisz ścieżkę do lokalnego pliku w SharedPreferences
                    saveLocalImagePath(localFile.absolutePath)
                }
            }
        })
    }

    private fun saveLocalImagePath(imagePath: String) {
        with(sharedPreferences.edit()) {
            putString("profileImagePath", imagePath)
            apply()
        }
    }

    fun getLocalImagePath(): String? {
        return sharedPreferences.getString("profileImagePath", null)
    }

    fun getStoredProfile(): UserProfile? {
        val name = sharedPreferences.getString("name", "") ?: return null
        val age = sharedPreferences.getInt("age", 0)
        val email = sharedPreferences.getString("email", "") ?: ""
        val gender = sharedPreferences.getString("gender", "") ?: ""
        val joinTime = sharedPreferences.getLong("joinTime", 0L).toString()
        val profileConfigured = sharedPreferences.getBoolean("profileConfigured", false)

        return UserProfile(name, age, email, gender, joinTime, "")
    }

    fun isProfileConfigured(): Boolean {
        return _isProfileConfigured.value ?: false
    }

    fun setProfileConfigured(isConfigured: Boolean) {
        _isProfileConfigured.value = isConfigured
    }
}

