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
import com.example.vresciecompose.data.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
                // Lista do przechowywania konwersacji, które muszą być dodane
                val conversationsToInsert = mutableListOf<ConversationEntity>()

                // Pobiera wszystkie istniejące konwersacje z lokalnej bazy danych (Room)
                viewModelScope.launch {
                    val existingConversations = conversationDao.getAllConversations() // Pobiera wszystkie konwersacje z Room
                    val existingConversationIds = existingConversations.map { it.firebaseConversationId }.toSet()

                    for (conversationSnapshot in snapshot.children) {
                        val firebaseConversationId = conversationSnapshot.key ?: continue

                        // Sprawdza, czy konwersacja już istnieje w lokalnej bazie danych
                        if (!existingConversationIds.contains(firebaseConversationId)) {
                            // Jeśli nie istnieje, dodaje do listy do wstawienia

                            val members = conversationSnapshot.child("members")
                            val secondParticipantId = members.children.find { it.key != currentUserId }?.key ?: continue

                            // Tworzy obiekt ConversationEntity
                            val conversationEntity = ConversationEntity(
                                firebaseConversationId = firebaseConversationId,
                                memberId = secondParticipantId
                            )

                            conversationsToInsert.add(conversationEntity)
                            Log.d("FetchConversations", "New conversation added to insert list: $firebaseConversationId")
                        } else {
                            Log.d("FetchConversations", "Conversation already exists: $firebaseConversationId")
                        }
                    }

                    // Teraz zapisz tylko te konwersacje, które są nowe
                    if (conversationsToInsert.isNotEmpty()) {
                        conversationDao.insertConversations(conversationsToInsert)
                    }

                    fetchMessagesForConversations()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching conversations: ", error.toException())
            }
        })
    }

    private fun fetchMessagesForConversations() {
        viewModelScope.launch {
            // Pobierz wszystkie konwersacje z Room
            val conversations = conversationDao.getAllConversations()
            Log.d("FetchMessages", "Fetched ${conversations.size} conversations from Room.")

            for (conversation in conversations) {
                val firebaseConversationId = conversation.firebaseConversationId
                val localConversationId = conversation.localConversationId.toString()
                Log.d("FetchMessages", "Fetching messages for conversation: $firebaseConversationId")

                val messagesRef = FirebaseDatabase.getInstance().getReference("/explicit_conversations/$firebaseConversationId/messages")

                messagesRef.orderByChild("timestamp").limitToLast(100) // Ogranicz do 100 wiadomości
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Lista do przechowywania wiadomości do dodania
                            val messagesToInsert = mutableListOf<MessageEntity>()

                            viewModelScope.launch {
                                for (messageSnapshot in snapshot.children) {
                                    val messageId = messageSnapshot.key ?: continue
                                    val messageData = messageSnapshot.getValue(MessageEntity::class.java) ?: continue

                                    // Sprawdź, czy wiadomość już istnieje w Room
                                    if (messageDao.getMessageById(messageId) == null) {
                                        // Dodaj lokalne ID konwersacji do wiadomości
                                        val messageEntity = messageData.copy(messageId = messageId, localConversationId = localConversationId)
                                        messagesToInsert.add(messageEntity)
                                        Log.d("FetchMessages", "Message added to insert list: ${messageEntity.messageId}")
                                    } else {
                                        Log.d("FetchMessages", "Message already exists: $messageId")
                                    }
                                }

                                // Zapisz nowo pobrane wiadomości do Room
                                if (messagesToInsert.isNotEmpty()) {
                                    messagesToInsert.forEach { messageDao.insertMessage(it) }
                                    Log.d("FetchMessages", "Inserted ${messagesToInsert.size} new messages to Room.")
                                } else {
                                    Log.d("FetchMessages", "No new messages to insert for conversation: $firebaseConversationId")
                                }

                                // Usuń wiadomości z Firebase, jeśli messageSeen jest true
                                for (messageSnapshot in snapshot.children) {
                                    val messageData = messageSnapshot.getValue(MessageEntity::class.java) ?: continue
                                    if (messageData.messageSeen) { // Sprawdzamy, czy messageSeen jest true
                                        val messageId = messageSnapshot.key ?: continue
                                        messagesRef.child(messageId).removeValue()
                                        Log.d("FetchMessages", "Deleted message from Firebase: $messageId")
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FirebaseError", "Error fetching messages: ", error.toException())
                        }
                    })
            }
        }
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
                    val timestamp = it.timestamp

                    // Określenie typu wiadomości
                    val messageType = when {
                        it.senderId == currentUserID -> MessageType(MessageType.Type.Sent, isSeen, timestamp)
                        it.senderId == "system" -> MessageType(MessageType.Type.System, isSeen, timestamp)
                        else -> MessageType(MessageType.Type.Received, isSeen, timestamp)
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
        viewModelScope.launch {
            // Pobierz lokalne konwersacje na podstawie firebaseConversationId
            Log.d("InitializeDatabase", "Fetching localConversationId for firebaseConversationId: $conversationId")
            val conversation = conversationDao.getAllConversations() // Pobierz wszystkie konwersacje

            val localConversationId = conversation.firstOrNull { it.firebaseConversationId == conversationId }?.localConversationId
                ?: run {
                    Log.d("InitializeDatabase", "No localConversationId found for firebaseConversationId: $conversationId")
                    return@launch
                }

            Log.d("InitializeDatabase", "Using localConversationId: $localConversationId for firebaseConversationId: $conversationId")

            // Ładujemy wiadomości z Room Database na podstawie localConversationId
            val localMessages = messageDao.getMessagesByConversationId(localConversationId.toString())
            val roomMessages = localMessages
                .filter { it.messageSeen } // Filtrujemy wiadomości, aby uwzględnić tylko te z messageSeen = true
                .map { messageEntity ->
                    val messageType = when {
                        messageEntity.senderId == currentUserID -> MessageType(MessageType.Type.Sent, messageEntity.messageSeen, messageEntity.timestamp)
                        messageEntity.senderId == "system" -> MessageType(MessageType.Type.System, messageEntity.messageSeen, messageEntity.timestamp)
                        else -> MessageType(MessageType.Type.Received, messageEntity.messageSeen, messageEntity.timestamp)
                    }
                    Log.d("InitializeDatabase", "Loaded message from Room: ${messageEntity.text}, type: ${messageType.type}")
                    messageEntity.text to messageType
                }
                .reversed() // Odwrócenie listy wiadomości

            // Zaktualizuj _messages z wiadomościami z Room
            _messages.value = roomMessages

            // Utwórz odwołanie do węzła konwersacji w Firebase
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
                        val timestamp = it.timestamp
                        Log.d("MessageListener", "Received message from Firebase: $it, isSeen: $isSeen")

                        // Określenie typu wiadomości
                        val messageType = when {
                            it.senderId == currentUserID -> MessageType(MessageType.Type.Sent, isSeen, timestamp)
                            it.senderId == "system" -> MessageType(MessageType.Type.System, isSeen, timestamp)
                            else -> MessageType(MessageType.Type.Received, isSeen, timestamp)
                        }

                        // Dodaj wiadomość do _messages
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
                    Log.e("FirebaseError", "Error fetching messages: ", error.toException())
                }
            }

            // Dodaj nasłuchiwacz do Firebase
            conversationRef.addChildEventListener(explicitMessageListener!!)
        }
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