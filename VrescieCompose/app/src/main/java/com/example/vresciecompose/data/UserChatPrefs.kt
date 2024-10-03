package com.example.vresciecompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "UserChatPrefs")
data class UserChatPrefs(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val selectedGenders: String,
    val ageStart: Float,
    val ageEnd: Float,
    val isProfileVerified: Boolean,
    val relationshipPreference: Boolean,
    val maxDistance: Float
)

@Dao
interface UserChatPrefsDao {
    @Insert
    suspend fun insert(userChatPrefs: UserChatPrefs)

    @Query("SELECT * FROM UserChatPrefs LIMIT 1")
    suspend fun getUserChatPrefs(): UserChatPrefs?

    @Query("SELECT * FROM UserChatPrefs ORDER BY id DESC")
    suspend fun getAllUserChatPrefs(): List<UserChatPrefs>

    @Query("SELECT * FROM UserChatPrefs WHERE id = :id")
    suspend fun getUserChatPrefsById(id: Long): UserChatPrefs?

    @Update
    suspend fun update(userChatPrefs: UserChatPrefs)
}