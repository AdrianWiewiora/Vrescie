package com.example.vresciecompose.view_models

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vresciecompose.Navigation
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

class MainViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            determineStartDestination()
        }
    }

    private fun determineStartDestination() {
        val destination = when {
            isFirstRun() -> Navigation.Destinations.FIRST_LAUNCH
            isLoggedIn() -> Navigation.Destinations.MAIN_MENU + "/1"
            else -> Navigation.Destinations.START
        }
        _startDestination.value = destination
        _isReady.value = true
    }

    private fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("isFirstRun", true)
    }

    fun markFirstRunCompleted() {
        sharedPreferences.edit()
            .putBoolean("isFirstRun", false)
            .apply()
    }
}
