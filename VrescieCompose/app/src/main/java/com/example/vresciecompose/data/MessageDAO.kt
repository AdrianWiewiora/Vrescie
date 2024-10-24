package com.example.vresciecompose.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["localConversationId"],
            childColumns = ["localConversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // ID wiadomości w Room, auto-generated
    val messageId: String, // ID wiadomości z Firebase
    val senderId: String, // ID nadawcy
    val text: String, // Treść wiadomości
    val timestamp: Long, // Znacznik czasu
    val messageSeen: Boolean, // Status przeczytania
    val localConversationId: String // Klucz obcy do konwersacji
) {
    // Domyślny konstruktor bezargumentowy wymagany przez Firebase
    constructor() : this(0, "", "", "", 0L, false, "")
}


@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE localConversationId = :conversationId ORDER BY timestamp DESC")
    suspend fun getMessagesByConversationId(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT * FROM messages WHERE localConversationId = :localConversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForConversation(localConversationId: String): MessageEntity?

    @Update
    suspend fun updateMessage(message: MessageEntity)
}
