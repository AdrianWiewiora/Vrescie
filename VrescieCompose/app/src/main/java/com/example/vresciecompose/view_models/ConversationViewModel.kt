package com.example.vresciecompose.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vresciecompose.data.Conversation
import com.example.vresciecompose.data.ConversationDao
import com.example.vresciecompose.data.ConversationEntity
import com.example.vresciecompose.data.Message
import com.example.vresciecompose.data.MessageDao
import com.example.vresciecompose.ui.components.MessageType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData
import java.util.UUID

class ConversationViewModel(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Pair<String, MessageType>>>(emptyList())
    val messages: StateFlow<List<Pair<String, MessageType>>> = _messages

    private var conversationId: String = ""
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    private var messageListener: ChildEventListener? = null  // Nowa zmienna
    private var explicitMessageListener: ChildEventListener? = null

    init {
        fetchAndStoreConversations()
    }

    private fun fetchAndStoreConversations() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val conversationRef = FirebaseDatabase.getInstance().getReference("/explicit_conversations")

        conversationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Iteruj przez wszystkie konwersacje w Firebase
                for (conversationSnapshot in snapshot.children) {
                    // Sprawdź, czy bieżący użytkownik jest uczestnikiem konwersacji
                    if (conversationSnapshot.key?.contains(currentUserId) == true) {
                        // Pobierz ID konwersacji z Firebase
                        val firebaseConversationId = conversationSnapshot.key ?: return

                        // Pobierz ID drugiego uczestnika konwersacji
                        val members = conversationSnapshot.child("members")
                        val secondParticipantId = members.children.find { it.key != currentUserId }?.key ?: continue

                        // Stwórz obiekt ConversationEntity
                        val conversationEntity = ConversationEntity(
                            firebaseConversationId = firebaseConversationId,
                            memberId = secondParticipantId
                        )

                        // Sprawdź, czy konwersacja już istnieje w Room
                        viewModelScope.launch {
                            val existingConversations = conversationDao.getConversationsByMemberId(secondParticipantId)
                            if (existingConversations.isEmpty()) {
                                // Jeśli nie istnieje, dodaj do bazy
                                conversationDao.insertConversation(conversationEntity)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching conversations: ", error.toException())
            }
        })
    }


    fun sendMessage(message: String, senderId: String = "") {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val currentTime = System.currentTimeMillis()
            val senderId2 = senderId.ifEmpty { user.uid }

            val messageData = Message(
                senderId = senderId2,
                text = message,
                timestamp = currentTime,
                messageSeen = false
            )

            val conversationMessagesRef = FirebaseDatabase.getInstance().reference
                .child("conversations")
                .child(conversationId)
                .child("messages")

            conversationMessagesRef.push().setValue(messageData)
        }
    }

    fun sendMessageExp(message: String, senderId: String = "") {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val currentTime = System.currentTimeMillis()
            val senderId2 = senderId.ifEmpty { user.uid }
            val messageData = Message(senderId2, message, currentTime, messageSeen = false)

            val conversationMessagesRef = FirebaseDatabase.getInstance().reference
                .child("explicit_conversations")
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

    // Funkcja do ustawiania conversationIdJawnych
    fun setConversationIdExplicit(id: String) {
        conversationId = id
        initializeDatabaseRefExplicit()
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
                    // Ustal, czy wiadomość została odczytana
                    val isSeen = it.messageSeen // Użyj pola isMessageSeen z modelu Message

                    // Określenie typu wiadomości
                    val messageType = when {
                        it.senderId == currentUserID -> MessageType(MessageType.Type.Sent, isSeen)
                        it.senderId == "system" -> MessageType(MessageType.Type.System, isSeen)
                        else -> MessageType(MessageType.Type.Received, isSeen)
                    }

                    // Sprawdzamy, czy wiadomość nie istnieje już na liście
                    val messageExists = _messages.value.any { pair ->
                        pair.first == it.text && pair.second.type == messageType.type
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

    private fun initializeDatabaseRefExplicit() {
        // Utwórz odwołanie do węzła konwersacji
        val conversationRef = FirebaseDatabase.getInstance().reference
            .child("explicit_conversations")
            .child(conversationId)
            .child("messages")

        // Nasłuchuj zmian w bazie danych Firebase
        explicitMessageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    // Ustal, czy wiadomość została odczytana
                    val isSeen = it.messageSeen
                    Log.d("MessageListener", "Received message: $it, isSeen: $isSeen")

                    // Określenie typu wiadomości
                    val messageType = when {
                        it.senderId == currentUserID -> MessageType(MessageType.Type.Sent, isSeen)
                        it.senderId == "system" -> MessageType(MessageType.Type.System, isSeen)
                        else -> MessageType(MessageType.Type.Received, isSeen)
                    }

                    _messages.value += it.text to messageType
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
        }

        conversationRef.addChildEventListener(explicitMessageListener!!)
    }

    fun removeExplicitListener() {
        explicitMessageListener?.let {
            val messagesRef = FirebaseDatabase.getInstance().getReference("explicit_conversations/$conversationId/messages")
            messagesRef.removeEventListener(it)
            explicitMessageListener = null  // Ustaw na null po usunięciu
        }
    }

    fun listenForMessages(conversationId: String) {
        val messagesRef = FirebaseDatabase.getInstance().getReference("explicit_conversations/$conversationId/messages")

        // Usuwamy poprzedni nasłuchiwacz, jeśli istnieje
        messageListener?.let { messagesRef.removeEventListener(it) }

        messageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Pobieramy wiadomość z bazy
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    // Tylko dla wiadomości od innych użytkowników
                    if (!it.messageSeen && it.senderId != currentUserID) {
                        updateMessageSeenStatus(snapshot.key)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługuje zmiany wiadomości, np. aktualizacje messageSeen
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    // Uaktualniamy stan wiadomości, np. zmiana messageSeen
                    _messages.value = _messages.value.map { pair ->
                        if (pair.first == it.text) {
                            Pair(it.text, pair.second.copy(isSeen = it.messageSeen))
                        } else {
                            pair
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Obsługuje usunięcie wiadomości
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    _messages.value = _messages.value.filterNot { pair -> pair.first == it.text }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługuje przeniesienie wiadomości
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługuje błąd
                Log.e("FirebaseListener", "Error while listening for messages: ", error.toException())
            }
        }


        messagesRef.addChildEventListener(messageListener!!)
    }

    // Funkcja do usuwania nasłuchiwacza
    fun removeMessageListener() {
        messageListener?.let {
            val messagesRef = FirebaseDatabase.getInstance().getReference("explicit_conversations/$conversationId/messages")
            messagesRef.removeEventListener(it)
            messageListener = null
        }
    }

    // Funkcja do aktualizacji statusu messageSeen
    private fun updateMessageSeenStatus(messageId: String?) {
        val messageRef = FirebaseDatabase.getInstance().getReference("explicit_conversations/$conversationId/messages").child(messageId!!)

        messageRef.child("messageSeen").setValue(true)
            .addOnSuccessListener {
                Log.d("FirebaseUpdate", "Message seen status updated successfully for message ID: $messageId")
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUpdate", "Error updating message seen status: ", exception)
            }
    }


    fun updateMessagesAsSeen(conversationId: String) {
        val messagesRef = FirebaseDatabase.getInstance().getReference("explicit_conversations/$conversationId/messages")

        messagesRef.get().addOnSuccessListener { snapshot ->
            for (childSnapshot in snapshot.children) {
                val messageId = childSnapshot.key
                val message = childSnapshot.getValue(Message::class.java)
                message?.let {
                    // Sprawdzamy, czy wiadomość nie została już odczytana
                    // oraz czy została wysłana przez innego użytkownika
                    if (!it.messageSeen && it.senderId != currentUserID) {
                        messagesRef.child(messageId!!).child("messageSeen").setValue(true)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseUpdate", "Error updating messages seen status: ", exception)
        }
    }




}