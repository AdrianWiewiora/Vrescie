package com.example.vresciecompose.view_models

import androidx.lifecycle.ViewModel
import com.example.vresciecompose.ui.components.Message
import com.example.vresciecompose.ui.components.MessageType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.database.*

class ConversationViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Pair<String, MessageType>>>(emptyList())
    val messages: StateFlow<List<Pair<String, MessageType>>> = _messages

    private var conversationId: String = ""
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

    fun sendMessage(message: String, senderId: String = "") {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val currentTime = System.currentTimeMillis()
            val senderId2 = senderId.ifEmpty { user.uid }
            val messageData = Message(senderId2, message, currentTime)

            val conversationMessagesRef = FirebaseDatabase.getInstance().reference
                .child("conversations")
                .child(conversationId)
                .child("messages")

            conversationMessagesRef.push().setValue(messageData)
        }
    }

    // Funkcja do ustawiania conversationId
    fun setConversationId(id: String) {
        conversationId = id
        initializeDatabaseRef()
    }

    fun resetMessages() {
        _messages.value = emptyList()
    }

    private fun initializeDatabaseRef() {
        // Utwórz odwołanie do węzła konwersacji
        val conversationRef = FirebaseDatabase.getInstance().reference
            .child("conversations")
            .child(conversationId)
            .child("messages")

        // Nasłuchuj zmian w bazie danych Firebase
        conversationRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    val messageType = when {
                        it.senderId == currentUserID -> MessageType.Sent
                        it.senderId == "system" -> MessageType.System
                        else -> MessageType.Received
                    }

                    // Sprawdzamy, czy wiadomość nie istnieje już na liście
                    val messageExists = _messages.value.any { pair ->
                        pair.first == it.text && pair.second == messageType
                    }

                    // Jeśli wiadomość nie istnieje, dodaj ją do listy
                    if (!messageExists) {
                        _messages.value += it.text to messageType
                    }
                }
            }


            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługa zmian w wiadomościach (opcjonalne)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Obsługa usunięcia wiadomości (opcjonalne)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługa przemieszczenia wiadomości (opcjonalne)
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów
            }
        })
    }
}