package com.example.vresciecompose.view_models

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.flow.asStateFlow

class ConversationViewModel(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : ViewModel() {

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    companion object {
        private const val CHANNEL_ID = "new_message_channel" // Unikalny identyfikator kanału
    }

    private val _messages = MutableStateFlow<List<Pair<String, MessageType>>>(emptyList())
    val messages: StateFlow<List<Pair<String, MessageType>>> = _messages

    private var conversationId: String = ""
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    private var messageListener: ChildEventListener? = null  // Nowa zmienna
    private var explicitMessageListener: ChildEventListener? = null

    // Lista konwersacji
    private val _conversationList = MutableStateFlow<List<Conversation>>(emptyList())
    val conversationList: StateFlow<List<Conversation>> = _conversationList

    // Mapa z ostatnimi wiadomościami (tekst, czy wiadomość została przeczytana, ID nadawcy)
    private val _lastMessageMap = MutableStateFlow<Map<String, Triple<String, Boolean, String>>>(emptyMap())
    val lastMessageMap: StateFlow<Map<String, Triple<String, Boolean, String>>> = _lastMessageMap

    private var conversationListener: ValueEventListener? = null

    init {
        fetchAndStoreConversations()
    }

    fun monitorNetworkConnection(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val isConnectedInitially = network != null && connectivityManager.getNetworkCapabilities(network)?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        // Ustaw początkowy stan połączenia
        _isConnected.value = isConnectedInitially

        // Zarejestruj nasłuchiwanie, aby monitorować zmiany w stanie połączenia
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isConnected.value = true
            }

            override fun onLost(network: Network) {
                _isConnected.value = false
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }


    fun stopMonitoringNetworkConnection(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        networkCallback = null
    }

    fun createNotificationChannel(context: Context) {
        Log.d("Notification", "Creating notification channel")

        val name = "New Messages"
        val descriptionText = "Notifications for new messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d("Notification", "Notification channel created: $name")
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, senderName: String, messageText: String) {
        Log.d("Notification", "Attempting to show notification for message: $messageText from $senderName")

        // Sprawdzenie uprawnienia na Androidzie 13+ (API 33 i wyżej)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {

                // Brak uprawnień - obsługa sytuacji
                Log.w("Notification", "No permission to post notifications")
                return
            }
        }

        val truncatedMessage = if (messageText.length > 30) {
            messageText.take(30) + "..."
        } else {
            messageText
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Ikona powiadomienia
            .setContentTitle(senderName)
            .setContentText(truncatedMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        Log.d("Notification", "Showing notification with content: $truncatedMessage")
        notificationManager.notify(1, notification)
    }

    private fun isAppInForeground(context: Context): Boolean {
        Log.d("Notification", "Checking if app is in foreground")

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName

        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && processInfo.processName == packageName) {
                Log.d("Notification", "App is in foreground")
                return true
            }
        }

        Log.d("Notification", "App is in background")
        return false
    }


    fun startListeningForConversations(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val conversationRef = database.getReference("/explicit_conversations")

        conversationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val conversationList = mutableListOf<Conversation>()
                    val lastMessageMap = mutableMapOf<String, Triple<String, Boolean, String>>()

                    // Przeiterowanie przez konwersacje z Firebase
                    for (conversationSnapshot in snapshot.children) {
                        if (conversationSnapshot.key?.contains(userId) == true) {
                            val participants = conversationSnapshot.child("members")
                            val secondParticipant = participants.children.find { it.key != userId }
                            val secondParticipantName = secondParticipant?.value as? String ?: ""
                            val secondParticipantId = secondParticipant?.key ?: ""

                            val conversationId = conversationSnapshot.key ?: continue

                            // Pobranie lokalnego ID konwersacji z Room na podstawie firebaseConversationId
                            val localConversationEntity = conversationDao.getConversationByFirebaseId(conversationId)
                            val localConversationId = localConversationEntity?.localConversationId.toString()

                            // Pobranie ostatniej wiadomości z lokalnej bazy Room
                            val localLastMessage = messageDao.getLastMessageForConversation(localConversationId)
                            var lastMessageDisplay = localLastMessage?.text ?: "Brak wiadomości"
                            var isSeen = localLastMessage?.messageSeen ?: true
                            var senderId = localLastMessage?.senderId ?: ""

                            // Dodanie konwersacji do listy
                            val conversation = Conversation(
                                id = conversationId,
                                name = secondParticipantName,
                                secondParticipantId = secondParticipantId
                            )
                            conversationList.add(conversation)

                            // Pobierz ostatnią wiadomość z Firebase (jeśli istnieje)
                            val lastMessageSnapshot = conversationSnapshot.child("messages")
                                .children.sortedByDescending { it.child("timestamp").value as? Long }
                                .firstOrNull()

                            if (lastMessageSnapshot != null) {
                                // Zaktualizowanie wiadomości danymi z Firebase, jeśli jest dostępna
                                val firebaseLastMessageText = lastMessageSnapshot.child("text").value?.toString() ?: ""
                                senderId = lastMessageSnapshot.child("senderId").value?.toString() ?: ""
                                isSeen = lastMessageSnapshot.child("messageSeen").value as? Boolean ?: true

                                lastMessageDisplay = if (senderId == userId) {
                                    "Ty: $firebaseLastMessageText"
                                } else {
                                    firebaseLastMessageText
                                }
                            }

                            // Aktualizacja mapy z ostatnimi wiadomościami
                            lastMessageMap[conversationId] = Triple(lastMessageDisplay, isSeen, senderId)
                        }
                    }

                    // Aktualizacja stanu
                    _conversationList.value = conversationList
                    _lastMessageMap.value = lastMessageMap
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching conversations: ", error.toException())
            }
        }

        conversationRef.addValueEventListener(conversationListener!!)
    }

    // Zatrzymywanie słuchacza
    fun stopListeningForConversations() {
        conversationListener?.let {
            FirebaseDatabase.getInstance().getReference("/explicit_conversations")
                .removeEventListener(it)
        }
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
                            // Lista do przechowywania wiadomości do dodania i identyfikatorów z Firebase
                            val messagesToInsert = mutableListOf<MessageEntity>()
                            val firebaseMessageIds = mutableSetOf<String>()

                            viewModelScope.launch {
                                for (messageSnapshot in snapshot.children) {
                                    val messageId = messageSnapshot.key ?: continue
                                    firebaseMessageIds.add(messageId)

                                    val messageData = messageSnapshot.getValue(MessageEntity::class.java) ?: continue
                                    val existingMessage = messageDao.getMessageById(messageId)

                                    if (existingMessage == null) {
                                        val messageEntity = messageData.copy(messageId = messageId, localConversationId = localConversationId)
                                        messagesToInsert.add(messageEntity)
                                        Log.d("FetchMessages", "Message added to insert list: ${messageEntity.messageId}")
                                    } else if (messageData.messageSeen && !existingMessage.messageSeen) {
                                        val updatedMessage = existingMessage.copy(messageSeen = true)
                                        messageDao.updateMessage(updatedMessage)
                                        Log.d("FetchMessages", "Updated messageSeen for message: $messageId")
                                    }
                                }

                                // Zapisz nowo pobrane wiadomości do Room
                                if (messagesToInsert.isNotEmpty()) {
                                    messagesToInsert.forEach { messageDao.insertMessage(it) }
                                    Log.d("FetchMessages", "Inserted ${messagesToInsert.size} new messages to Room.")
                                }

                                // Aktualizuj messageSeen = true dla wiadomości, które nie istnieją już w Firebase
                                val localMessages = messageDao.getMessagesByConversationId(localConversationId)
                                for (localMessage in localMessages) {
                                    if (localMessage.messageId !in firebaseMessageIds && !localMessage.messageSeen) {
                                        val updatedMessage = localMessage.copy(messageSeen = true)
                                        messageDao.updateMessage(updatedMessage)
                                        Log.d("FetchMessages", "Set messageSeen=true for missing message in Firebase: ${localMessage.messageId}")
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
    fun setConversationIdExplicit(id: String, context: Context) {
        conversationId = id
        initializeDatabaseRefExplicit(context)
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

    private fun initializeDatabaseRefExplicit(context: Context) {
        viewModelScope.launch {

            if (explicitMessageListener != null) {
                Log.d("InitializeDatabase", "Removing existing message listener.")
                removeExplicitListener() // Funkcja, która usuwa listenera z Firebase
            }

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

            // Generowanie listy wiadomości do wyświetlenia, korzystając z istniejącej wartości messageSeen
            val roomMessages = localMessages.map { messageEntity ->
                val messageType = when {
                    messageEntity.senderId == currentUserID -> MessageType(MessageType.Type.Sent, messageEntity.messageSeen, messageEntity.timestamp)
                    messageEntity.senderId == "system" -> MessageType(MessageType.Type.System, messageEntity.messageSeen, messageEntity.timestamp)
                    else -> MessageType(MessageType.Type.Received, messageEntity.messageSeen, messageEntity.timestamp)
                }
                Log.d("InitializeDatabase", "Loaded message from Room: ${messageEntity.text}, type: ${messageType.type}")
                messageEntity.text to messageType
            }.reversed()

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
                        val messageId = snapshot.key ?: return  // Użyj klucza snapshotu jako ID wiadomości

                        // Sprawdzenie istnienia wiadomości w Room w korutynie
                        viewModelScope.launch {
                            val existingMessage = messageDao.getMessageById(messageId)
                            if (existingMessage != null) {
                                Log.d("MessageListener", "Message with ID $messageId already exists in Room, skipping addition.")
                                return@launch // Nie dodawaj wiadomości, jeśli już istnieje
                            }

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

                            // Po dodaniu wiadomości do listy, zapisujemy ją w Room
                            val localConversationEntity = conversationDao.getConversationByFirebaseId(conversationId)
                            val localConvId = localConversationEntity?.localConversationId ?: return@launch  // Zmieniona nazwa zmiennej

                            // Tworzymy encję wiadomości dla Room
                            val messageEntity = MessageEntity(
                                messageId = messageId, // Użyj klucza Firebase jako messageId
                                localConversationId = localConvId.toString(),  // Używamy nowej nazwy zmiennej
                                senderId = it.senderId,
                                text = it.text,
                                timestamp = it.timestamp,
                                messageSeen = it.messageSeen
                            )

                            // Zapisz wiadomość w Room
                            messageDao.insertMessage(messageEntity)
                            Log.d("MessageListener", "Message saved to Room: ${messageEntity.text}, from sender: ${messageEntity.senderId}")

                            // Sprawdź, czy aplikacja jest w tle
                            if (!isAppInForeground(context)) {
                                // Pokaż powiadomienie
                                getSenderNameAndShowNotification(context, it.senderId, it.text)
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Obsługa zmian w wiadomościach (opcjonalne)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val messageId = snapshot.key ?: return  // Użyj klucza snapshotu jako ID wiadomości
                    Log.d("MessageListener", "Message with ID $messageId has been removed from Firebase.")

                    // Usuwanie wiadomości z _messages
                    _messages.value = _messages.value.filter { (text, _) ->
                        text != messageId // Filtruj wiadomości, które nie mają usuniętego ID
                    }

                    // Zaktualizuj stan wiadomości w Room na messageSeen = true
                    viewModelScope.launch {
                        // Pobierz lokalną konwersację na podstawie conversationId
                        val localConversationId3 = conversationDao.getAllConversations()
                            .firstOrNull { it.firebaseConversationId == conversationId }
                            ?.localConversationId ?: return@launch

                        // Pobierz wiadomość z Room na podstawie messageId
                        val localMessage = messageDao.getMessageById(messageId)

                        localMessage?.let { messageEntity ->
                            // Zaktualizuj messageSeen na true, jeśli wiadomość istnieje
                            if (!messageEntity.messageSeen) {
                                val updatedMessage = messageEntity.copy(messageSeen = true)
                                messageDao.updateMessage(updatedMessage)
                                Log.d("MessageListener", "Updated messageSeen to true for message: $messageId")
                            }
                        }

                        // Pobierz zaktualizowane wiadomości z Room na podstawie conversationId
                        val localMessages3 = messageDao.getMessagesByConversationId(localConversationId3.toString())

                        // Generowanie listy wiadomości do wyświetlenia
                        val roomMessages3 = localMessages3.map { messageEntity ->
                            val messageType = when {
                                messageEntity.senderId == currentUserID -> MessageType(MessageType.Type.Sent, messageEntity.messageSeen, messageEntity.timestamp)
                                messageEntity.senderId == "system" -> MessageType(MessageType.Type.System, messageEntity.messageSeen, messageEntity.timestamp)
                                else -> MessageType(MessageType.Type.Received, messageEntity.messageSeen, messageEntity.timestamp)
                            }
                            Log.d("InitializeDatabase", "Loaded message from Room: ${messageEntity.text}, type: ${messageType.type}")
                            messageEntity.text to messageType
                        }.reversed()

                        // Zaktualizuj _messages z wiadomościami z Room
                        _messages.value = roomMessages3
                    }
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

    // Funkcja do pobierania imienia nadawcy
    private fun getSenderNameAndShowNotification(context: Context, senderId: String, messageText: String) {
        val membersRef = FirebaseDatabase.getInstance().getReference("/explicit_conversations/$conversationId/members")

        membersRef.child(senderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val senderName = snapshot.getValue(String::class.java) ?: "Unknown"
                Log.d("MessageListener", "Sender name: $senderName for senderId: $senderId")

                // Wyświetl powiadomienie z imieniem nadawcy
                showNotification(context, senderName, messageText)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching sender name: ", error.toException())
            }
        })
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
            viewModelScope.launch {
                for (childSnapshot in snapshot.children) {
                    val messageId = childSnapshot.key
                    val message = childSnapshot.getValue(Message::class.java)
                    message?.let {
                        // Sprawdzamy, czy wiadomość nie została już odczytana
                        // oraz czy została wysłana przez innego użytkownika
                        if (!it.messageSeen && it.senderId != currentUserID) {
                            // Aktualizujemy stan wiadomości w Firebase
                            messagesRef.child(messageId!!).child("messageSeen").setValue(true)

                            // Aktualizujemy stan wiadomości w Room
                            val localMessage = messageDao.getMessageById(messageId)
                            if (localMessage != null) {
                                // Zmiana wartości messageSeen na true
                                val updatedMessage = localMessage.copy(messageSeen = true)
                                messageDao.updateMessage(updatedMessage) // Używamy updateMessage zamiast insertMessage
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseUpdate", "Error updating messages seen status: ", exception)
        }
    }






}