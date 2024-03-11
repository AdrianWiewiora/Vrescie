package com.example.vresciecompose.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class MainViewModel(context: Context): ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

            if (!isFirstRun) {
                // Ustaw _isReady na true, jeśli użytkownik jest zalogowany i to nie jest pierwsze uruchomienie aplikacji
                _isReady.value = true
            } else {
                // Symulacja opóźnienia
                delay(2000L)
                _isReady.value = true
                // Ustaw flagę isFirstRun na false, aby oznaczyć, że aplikacja nie jest już pierwszym uruchomieniem
                sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("isFirstRun", true)
    }

}