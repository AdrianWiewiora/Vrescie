package com.example.vresciecompose.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vresciecompose.data.UserChatPrefs
import com.example.vresciecompose.data.UserChatPrefsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserChatPrefsViewModel( private val userChatPrefsDao: UserChatPrefsDao) : ViewModel() {
    private val viewModelScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _allChatPrefs = MutableLiveData<List<UserChatPrefs>>()
    val allChatPrefs: LiveData<List<UserChatPrefs>> = _allChatPrefs

    init {
        getAllUserChatPrefsFromDatabase()
    }

    private fun getAllUserChatPrefsFromDatabase() {
        viewModelScope.launch {
            val chatPrefs = withContext(Dispatchers.IO) {
                userChatPrefsDao.getAllUserChatPrefs()
            }
            withContext(Dispatchers.Main) {
                _allChatPrefs.value = chatPrefs
            }
        }
    }

    fun fetchChatPrefs() {
        getAllUserChatPrefsFromDatabase()
    }

    fun insertUserChatPrefs(userChatPrefs: UserChatPrefs) {
        viewModelScope.launch {
            userChatPrefsDao.insert(userChatPrefs)
            getAllUserChatPrefsFromDatabase()
        }
    }

    fun getUserChatPrefsById(id: Long): LiveData<UserChatPrefs?> {
        val userChatPrefsLiveData = MutableLiveData<UserChatPrefs?>()
        viewModelScope.launch {
            val userChatPrefs = withContext(Dispatchers.IO) {
                userChatPrefsDao.getUserChatPrefsById(id)
            }
            withContext(Dispatchers.Main) {
                userChatPrefsLiveData.value = userChatPrefs
            }
        }
        return userChatPrefsLiveData
    }

    // Zmieniona funkcja, aby wstawiać lub aktualizować
    fun savePreferences(selectedGenders: String, ageRange: ClosedRange<Float>, isProfileVerified: Boolean, relationshipPreference: Boolean, maxDistance: Float) {
        viewModelScope.launch {
            val existingPrefs = withContext(Dispatchers.IO) {
                userChatPrefsDao.getUserChatPrefsById(1) // Zmień na odpowiedni ID
            }

            val userChatPrefs = UserChatPrefs(
                id = 1, // Zmień na odpowiedni ID, który chcesz aktualizować
                selectedGenders = selectedGenders,
                ageStart = ageRange.start,
                ageEnd = ageRange.endInclusive,
                isProfileVerified = isProfileVerified,
                relationshipPreference = relationshipPreference,
                maxDistance = maxDistance
            )

            if (existingPrefs != null) {
                // Zaktualizuj istniejący rekord
                userChatPrefsDao.update(userChatPrefs) // Zdefiniuj tę metodę w DAO
            } else {
                // Wstaw nowy rekord
                userChatPrefsDao.insert(userChatPrefs)
            }
        }
    }

}