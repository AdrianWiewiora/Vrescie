package com.example.vresciecompose.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.vresciecompose.data.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val themeFlow = repository.themeFlow.asLiveData()
    val messageSizeFlow = repository.messageSizeFlow.asLiveData()

    fun saveTheme(theme: Int) {
        viewModelScope.launch {
            repository.saveTheme(theme)
        }
    }

    fun getCurrentTheme(): LiveData<Int> {
        return themeFlow
    }

    fun saveMessageSize(size: Int) {
        viewModelScope.launch {
            repository.saveMessageSize(size)
        }
    }

    fun getCurrentMessageSize(): LiveData<Int> {
        return messageSizeFlow
    }



    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}
