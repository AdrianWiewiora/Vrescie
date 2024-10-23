package com.example.vresciecompose.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query


@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val localConversationId: Long = 0, // Lokalny ID konwersacji w Room
    val firebaseConversationId: String, // ID konwersacji z Firebase
    val memberId: String // ID członka konwersacji (drugiego użytkownika)
)


@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE memberId = :memberId")
    suspend fun getConversationsByMemberId(memberId: String): List<ConversationEntity>

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}
