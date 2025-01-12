package com.example.vresciecompose.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ThemePreferenceKeys {
    val THEME_KEY = intPreferencesKey("theme") // 0 = System, 1 = Jasny, 2 = Ciemny, 3 = Ciemny Wysoki kontrast, 4 - Jasny Wysoki kontrast
}

object SizeMessagesPreferenceKeys {
    val MESSAGE_SIZE_KEY = intPreferencesKey("mess_size") // 0 = Mały, 1 = Normalny 2 = Duży 3 = Bardzo duży
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val themeFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[ThemePreferenceKeys.THEME_KEY] ?: 0
        }

    val messageSizeFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[SizeMessagesPreferenceKeys.MESSAGE_SIZE_KEY] ?: 1
        }

    suspend fun saveTheme(theme: Int) {
        dataStore.edit { preferences ->
            preferences[ThemePreferenceKeys.THEME_KEY] = theme
        }
    }

    suspend fun saveMessageSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[SizeMessagesPreferenceKeys.MESSAGE_SIZE_KEY] = size
        }
    }
}
