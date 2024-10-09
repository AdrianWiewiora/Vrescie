package com.example.vresciecompose.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.vresciecompose.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val themeFlow = repository.themeFlow.asLiveData()

    fun saveTheme(theme: Int) {
        viewModelScope.launch {
            repository.saveTheme(theme)
        }
    }

    fun getCurrentTheme(): LiveData<Int> {
        return themeFlow
    }
}
