package com.example.vresciecompose.view_models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.example.vresciecompose.data.MessageEntity
import com.example.vresciecompose.data.MoveData
import com.example.vresciecompose.data.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.compose.runtime.State

class ConversationViewModel(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : ViewModel() {
    // Podstawowe zmienne id konwersacji i użytkownika
    private var conversationId: String = ""
    val currentConversationId: String
        get() = conversationId
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

    // Zmienne dotyczące połączenia z internetem
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // Zmienne TicTacToe
    var board: MutableState<Array<Array<String>>> = mutableStateOf(Array(10) { Array(10) { "" } })
    private var lastMoveByPlayer: Boolean = false
    val _gameWinner = MutableStateFlow<String?>(null)
    private var gameWinListener: ValueEventListener? = null
    private val _currentPlayerMessage = MutableStateFlow("Twój ruch")
    val currentPlayerMessage: StateFlow<String> = _currentPlayerMessage.asStateFlow()
    // Zmienna przechowująca liczbę wygranych drugiego użytkownika
    private val _gameWins = MutableStateFlow(0L)
    val gameWins: StateFlow<Long> = _gameWins
    private var _gameWinListener: ValueEventListener? = null
    // Pole wantNewGame przechowuje liczbę osób, które chcą rozpocząć nową grę
    private val _wantNewGameCount = MutableStateFlow(0)
    val wantNewGameCount: StateFlow<Int> = _wantNewGameCount

    // Lista wiadomości
    private val _messages = MutableStateFlow<List<Pair<String, MessageType>>>(emptyList())
    val messages: StateFlow<List<Pair<String, MessageType>>> = _messages

    // Lista konwersacji
    private val _conversationList = MutableStateFlow<List<Conversation>>(emptyList())
    val conversationList: StateFlow<List<Conversation>> = _conversationList

    // Mapa z ostatnimi wiadomościami (tekst, czy wiadomość została przeczytana, ID nadawcy)
    private val _lastMessageMap = MutableStateFlow<Map<String, Triple<String, Boolean, String>>>(emptyMap())
    val lastMessageMap: StateFlow<Map<String, Triple<String, Boolean, String>>> = _lastMessageMap

    // Nasłuchiwacze
    private var explicitMessageListener: ChildEventListener? = null // Wiadomości
    private var conversationListener: ValueEventListener? = null // Konwersacji
    private var wantNewGameListener: ChildEventListener? = null // chęci nowej gry TicTactoe

    // Zmienne dotyczące polubień
    private val _likesNotification = MutableLiveData<Boolean>()
    val likesNotification: LiveData<Boolean> get() = _likesNotification
    private var conversationLikesRef: DatabaseReference? = null
    private var likeEventListener: ChildEventListener? = null


    init {
        fetchAndStoreConversations()
    }


    fun fetchGameStats(currentUserId: String, onStatsFetched: (String, Int, Int) -> Unit) {
        val statsRef = Firebase.database.reference
            .child("explicit_conversations/$conversationId/games/tic-tac-toe/statistic")

        statsRef.get().addOnSuccessListener { snapshot ->
            val totalGames = snapshot.child("games").getValue(Int::class.java) ?: 0
            val userWins = snapshot.child(currentUserId).getValue(Int::class.java) ?: 0

            onStatsFetched("Kółko-krzyżyk", totalGames, userWins)
        }.addOnFailureListener { error ->
            Log.e("GameStats", "Failed to fetch stats: ${error.message}")
        }
    }

    // Funkcja nasłuchująca zmiany w kolekcji wantNewGame w Firebase
    fun listenForNewGameRequests(conversationId: String, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"

        // Usuwamy poprzedni nasłuchiwacz (jeśli istnieje)
        wantNewGameListener?.let {
            Firebase.database.reference
                .child(basePath)
                .child(conversationId)
                .child("games")
                .child("tic-tac-toe")
                .child("wantNewGame")
                .removeEventListener(it)
        }

        // Nasłuchujemy zmian w kolekcji 'wantNewGame' dla danej rozmowy
        wantNewGameListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Zliczamy nowe rekordy
                _wantNewGameCount.value = (_wantNewGameCount.value ?: 0) + 1
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // W przypadku zmiany rekordu możemy to pominąć lub obsłużyć, jeśli jest potrzebne
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Zmniejszamy licznik, gdy rekord zostaje usunięty
                setNewGame()
                _wantNewGameCount.value = (_wantNewGameCount.value ?: 0) - 1
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Możemy zignorować ten przypadek
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("TicTacToeGame", "Błąd podczas nasłuchiwania: ${error.message}")
            }
        }

        // Dodaj nasłuchiwacz
        Firebase.database.reference
            .child(basePath)
            .child(conversationId)
            .child("games")
            .child("tic-tac-toe")
            .child("wantNewGame")
            .addChildEventListener(wantNewGameListener!!)
    }

    // Funkcja usuwająca nasłuchiwacz
    fun removeNewGameListener(conversationId: String, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"

        wantNewGameListener?.let {
            Firebase.database.reference
                .child(basePath)
                .child(conversationId)
                .child("games")
                .child("tic-tac-toe")
                .child("wantNewGame")
                .removeEventListener(it)
        }
    }

    fun updateCurrentPlayerMessage(isPlayerTurn: Boolean) {
        _currentPlayerMessage.value = if (isPlayerTurn) "Twój ruch" else "Ruch przeciwnika"
    }

    fun setWantNewGame(userId: String, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"

        val database = Firebase.database.reference
        val wantNewGameRef = database
            .child(basePath)
            .child(conversationId)
            .child("games")
            .child("tic-tac-toe")
            .child("wantNewGame")

        // Sprawdź aktualny stan "wantNewGame" w Firebase
        wantNewGameRef.get().addOnSuccessListener { snapshot ->
            val currentWantNewGame = snapshot.children.mapNotNull { it.key }

            if (currentWantNewGame.contains(userId)) {
                Log.d("TicTacToe", "User already marked as wanting a new game.")
            } else if (currentWantNewGame.size == 1 && currentWantNewGame.firstOrNull() != userId) {
                // Jeśli jest dokładnie jeden gracz i nie jest to bieżący użytkownik
                setNewGame(isAnonymous) // Rozpocznij nową grę
            } else {
                // Dodaj bieżącego użytkownika do listy
                wantNewGameRef.child(userId).setValue(true)
            }
        }.addOnFailureListener { error ->
            Log.e("TicTacToe", "Failed to fetch wantNewGame: ${error.message}")
        }
    }


    private fun setNewGame(isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"

        val databaseRef = Firebase.database.reference
            .child(basePath)
            .child(conversationId)
            .child("games")
            .child("tic-tac-toe")

        // Usuwanie wszystkich ruchów z Firebase
        databaseRef.child("moves").removeValue()

        // Usuwanie rekordu o wygranej
        databaseRef.child("current-game-win").removeValue()

        // Usuwanie rekordów w wantNewGame
        databaseRef.child("wantNewGame").removeValue()

        // Resetowanie statusu gry na 'false'
        databaseRef.child("isActualGameDone").setValue(false)

        // Resetowanie planszy (tutaj zakładamy, że board to stan MutableState)
        board.value = Array(10) { Array(10) { "" } }  // Ustawiamy początkowy stan planszy (10x10, puste pola)
    }


    fun listenForGameWin(conversationId: String, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"

        val databaseRef = Firebase.database.reference
            .child(basePath)
            .child(conversationId)
            .child("games")
            .child("tic-tac-toe")
            .child("current-game-win")

        gameWinListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Jeśli wynik gry jest dostępny (nie null i nie pusty), ustawiamy go
                val winnerId = snapshot.getValue(String::class.java)
                if (!winnerId.isNullOrEmpty()) {
                    _gameWinner.value = winnerId
                } else {
                    // Jeśli nie ma wyniku (np. rekord został usunięty), ustawiamy null
                    _gameWinner.value = null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TicTacToeGame", "Błąd podczas nasłuchiwania: ${error.message}")
            }
        }
        databaseRef.addValueEventListener(gameWinListener!!)
    }

    fun removeGameWinListener(conversationId: String, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"

        gameWinListener?.let {
            Firebase.database.reference
                .child(basePath)
                .child(conversationId)
                .child("games")
                .child("tic-tac-toe")
                .child("current-game-win")
                .removeEventListener(it)
        }
    }

    fun makeMove(conversationId: String, playerId: String, positionX: Int, positionY: Int, isAnonymous: Boolean = false): Boolean {
        // Sprawdzamy, czy gra już się zakończyła
        if (_gameWinner.value != null) {
            Log.d("TicTacToeGame", "Gra zakończona, nie możesz wykonać ruchu.")
            return false
        }
        if (lastMoveByPlayer) {
            Log.d("TicTacToeGame", "Nie możesz teraz wykonać ruchu.")
            return false
        }

        // Sprawdź, czy miejsce na planszy jest puste
        if (board.value[positionX][positionY].isEmpty()) {
            // Użyj mutableStateOf do aktualizacji planszy
            val updatedBoard = board.value.map { it.clone() }.toTypedArray() // Skopiuj tablicę
            updatedBoard[positionX][positionY] = "X"
            board.value = updatedBoard // Ustaw zaktualizowaną planszę

            lastMoveByPlayer = true
            saveMoveToFirebase(conversationId, playerId, positionX, positionY, isAnonymous)
            updateCurrentPlayerMessage(!lastMoveByPlayer)
            // Sprawdź, czy gracz wygrał
            if (checkForWin(positionX, positionY)) {
                if (!isAnonymous) { updateGameStatistics(conversationId, playerId) } else {
                    updateUnlockedStages(conversationId, playerId)
                }
                saveGameWinToFirebase(conversationId, playerId, isAnonymous)
                return true
            }
        }
        return false
    }

    private fun saveGameWinToFirebase(conversationId: String, winnerId: String, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child(basePath)
            .child(conversationId)
            .child("games")
            .child("tic-tac-toe")

        val winData = mapOf("current-game-win" to winnerId)

        databaseReference.updateChildren(winData)
            .addOnSuccessListener {
                Log.d("TicTacToeGame", "Zwycięstwo zostało zapisane w Firebase.")
            }
            .addOnFailureListener { exception ->
                Log.e("TicTacToeGame", "Błąd podczas zapisywania zwycięstwa: ${exception.message}")
            }
    }


    private fun saveMoveToFirebase(conversationId: String, playerId: String, positionX: Int, positionY: Int, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"
        val moveData = mapOf(
            "playerId" to playerId,
            "positionX" to positionX,
            "positionY" to positionY
        )
        FirebaseDatabase.getInstance().reference
            .child("$basePath/$conversationId/games/tic-tac-toe/moves")
            .push()
            .setValue(moveData)
    }

    private fun checkForWin(x: Int, y: Int): Boolean {
        val target = if (lastMoveByPlayer) "X" else "O" // Sprawdź, jaki symbol gracz używa
        val boardState = board.value

        // Funkcja pomocnicza do liczenia elementów w linii
        fun countInDirection(deltaX: Int, deltaY: Int): Int {
            var count = 0
            var currX = x + deltaX
            var currY = y + deltaY

            while (currX in 0 until 10 && currY in 0 until 10 && boardState[currX][currY] == target) {
                count++
                currX += deltaX
                currY += deltaY
            }
            return count
        }

        // Sprawdź w pionie
        val verticalCount = 1 + countInDirection(-1, 0) + countInDirection(1, 0)
        if (verticalCount >= 4) return true

        // Sprawdź w poziomie
        val horizontalCount = 1 + countInDirection(0, -1) + countInDirection(0, 1)
        if (horizontalCount >= 4) return true

        // Sprawdź główny ukos
        val mainDiagonalCount = 1 + countInDirection(-1, -1) + countInDirection(1, 1)
        if (mainDiagonalCount >= 4) return true

        // Sprawdź przeciwny ukos
        val antiDiagonalCount = 1 + countInDirection(-1, 1) + countInDirection(1, -1)
        if (antiDiagonalCount >= 4) return true

        // Jeżeli żaden warunek nie został spełniony
        return false
    }
    private fun updateUnlockedStages(conversationId: String, playerId: String) {
        val unlockedStagesRef = FirebaseDatabase.getInstance().reference
            .child("conversations/$conversationId/unlockedStagesOfPhoto")

        unlockedStagesRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val dataSnapshot = currentData.value as? Map<*, *>

                // Pobieramy liczbę wygranych tego gracza (jeśli istnieje)
                val currentWins = (dataSnapshot?.get(playerId) as? Long) ?: 0L

                // Jeśli liczba wygranych przekroczy 3, nie zmieniamy wartości
                val newWins = if (currentWins < 3) currentWins + 1 else currentWins

                // Zapisujemy nową liczbę wygranych w Firebase
                currentData.value = dataSnapshot?.toMutableMap()?.apply {
                    this[playerId] = newWins
                } ?: mapOf(playerId to newWins)

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("UpdateUnlockedStages", "Transaction failed: ${error.message}")
                } else if (committed) {
                    Log.d("UpdateUnlockedStages", "Transaction successful!")
                }
            }
        })
    }


    private fun updateGameStatistics(conversationId: String, winningPlayerId: String) {
        val statsRef = FirebaseDatabase.getInstance().reference
            .child("explicit_conversations/$conversationId/games/tic-tac-toe/statistic")

        statsRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val dataSnapshot = currentData.value

                // Inicjalizacja zmiennych do aktualizacji
                var gamesCount = 0L
                var playerWins = 0L

                // Sprawdzamy, czy dane istnieją i są odpowiedniej struktury
                if (dataSnapshot is Map<*, *>) {
                    gamesCount = (dataSnapshot["games"] as? Long) ?: 0L
                    playerWins = (dataSnapshot[winningPlayerId] as? Long) ?: 0L
                }

                // Aktualizacja wartości
                gamesCount += 1
                playerWins += 1

                // Zapis nowych danych w Firebase
                currentData.value = mapOf(
                    "games" to gamesCount,
                    winningPlayerId to playerWins
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("UpdateStats", "Transaction failed: ${error.message}")
                } else if (committed) {
                    Log.d("UpdateStats", "Transaction successful!")
                }
            }
        })
    }

    fun listenForMoves(conversationId: String, playerId: String, isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"
        val movesRef = FirebaseDatabase.getInstance().reference
            .child("$basePath/$conversationId/games/tic-tac-toe/moves")

        // Najpierw załaduj istniejące ruchy
        movesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Utwórz nową planszę na podstawie obecnej
                val currentBoard = Array(10) { Array(10) { "" } }
                var lastPlayerId: String? = null

                for (childSnapshot in snapshot.children) {
                    val moveData = childSnapshot.getValue(MoveData::class.java)
                    moveData?.let {
                        // Dodaj istniejące ruchy do planszy
                        currentBoard[it.positionX][it.positionY] = if (it.playerId == playerId) "X" else "O"
                        lastPlayerId = it.playerId
                    }
                }

                // Zaktualizuj planszę w MutableState
                board.value = currentBoard
                // Ustal, czy to obecny gracz wykonał ostatni ruch
                lastMoveByPlayer = lastPlayerId == playerId
                updateCurrentPlayerMessage(!lastMoveByPlayer) // Aktualizuj komunikat na podstawie ruchu
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TicTacToeGame", "Load failed: ${error.message}")
            }
        })


        movesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val moveData = snapshot.getValue(MoveData::class.java)
                moveData?.let {
                    if (it.playerId != playerId) {
                        // Użyj mutableStateOf do aktualizacji planszy
                        val updatedBoard = board.value.map { it.clone() }.toTypedArray()
                        updatedBoard[it.positionX][it.positionY] = "O"
                        board.value = updatedBoard // Ustaw zaktualizowaną planszę
                    }
                    // Aktualizuj stan ostatniego gracza, który wykonał ruch
                    lastMoveByPlayer = it.playerId == playerId
                    updateCurrentPlayerMessage(!lastMoveByPlayer)
                }
            }
            // Inne funkcje ChildEventListener pozostają puste
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("TicTacToeGame", "Listen failed: ${error.message}")
            }
        })
    }


    // Funkcje monitorujące połączenie z internetem
    fun monitorNetworkConnection(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val isConnectedInitially = network != null && connectivityManager.getNetworkCapabilities(network)?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        // Ustawia początkowy stan połączenia
        _isConnected.value = isConnectedInitially

        // Nasłuchiwanie, aby monitorować zmiany w stanie połączenia
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


    private fun startListeningForConversations(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val conversationRef = database.getReference("/explicit_conversations")

        conversationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    // Aktualizacja mapy z ostatnimi wiadomościami
                    val lastMessageMap = mutableMapOf<String, Triple<String, Boolean, String>>()

                    // Iteracja przez konwersacje z Firebase
                    for (conversationSnapshot in snapshot.children) {
                        if (conversationSnapshot.key?.contains(userId) == true) {
                            val participants = conversationSnapshot.child("members")
                            val secondParticipant = participants.children.find { it.key != userId }
                            val secondParticipantName = secondParticipant?.value as? String ?: ""
                            val secondParticipantId = secondParticipant?.key ?: ""

                            val conversationId = conversationSnapshot.key ?: continue

                            // Pobranie lokalnego ID konwersacji z Room na podstawie firebaseConversationId
                            val localConversationEntity = conversationDao.getConversationByFirebaseId(conversationId)

                            // Jeśli konwersacja nie istnieje, dodaj ją
                            if (localConversationEntity == null) {
                                // Dodanie nowej konwersacji do listy
                                val conversation = Conversation(
                                    id = conversationId,
                                    name = secondParticipantName,
                                    secondParticipantId = secondParticipantId
                                )
                                _conversationList.value += conversation
                            }

                            // Pobierz ostatnią wiadomość z Firebase (jeśli istnieje)
                            val lastMessageSnapshot = conversationSnapshot.child("messages")
                                .children.sortedByDescending { it.child("timestamp").value as? Long }
                                .firstOrNull()

                            var lastMessageDisplay = "Brak wiadomości"
                            var isSeen = true
                            var senderId = ""

                            if (lastMessageSnapshot != null) {
                                val firebaseLastMessageText = lastMessageSnapshot.child("text").value?.toString() ?: ""
                                senderId = lastMessageSnapshot.child("senderId").value?.toString() ?: ""
                                isSeen = lastMessageSnapshot.child("messageSeen").value as? Boolean ?: true

                                lastMessageDisplay = if (senderId == userId) {
                                    "Ty: $firebaseLastMessageText"
                                } else {
                                    firebaseLastMessageText
                                }
                            } else {
                                // Pobierz ostatnią wiadomość z Room
                                val localLastMessage = localConversationEntity?.localConversationId?.let { localId ->
                                    messageDao.getLastMessageForConversation(localId.toString())
                                }
                                lastMessageDisplay = localLastMessage?.text ?: "Brak wiadomości"
                                isSeen = localLastMessage?.messageSeen ?: true
                                senderId = localLastMessage?.senderId ?: ""
                            }


                            // Aktualizacja mapy z ostatnimi wiadomościami
                            lastMessageMap[conversationId] = Triple(lastMessageDisplay, isSeen, senderId)
                        }
                    }

                    // Uaktualnij tylko mapę z ostatnimi wiadomościami
                    _lastMessageMap.value = lastMessageMap
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching conversations: ", error.toException())
            }
        }

        conversationRef.addValueEventListener(conversationListener!!)
    }



    fun fetchConversationsAndListen(userId: String) {
        viewModelScope.launch {
            // Pobierz konwersacje z Room
            val existingConversations = conversationDao.getAllConversations()

            // Jeśli nie ma konwersacji, uruchom nasłuch z Firebase
            if (existingConversations.isEmpty()) {
                startListeningForConversations(userId)
            } else {
                // Lista do przechowywania konwersacji z ostatnimi wiadomościami
                val conversationList = mutableListOf<Conversation>()
                val lastMessageMap = mutableMapOf<String, Triple<String, Boolean, String>>() // Do przechowywania ostatnich wiadomości

                // Iteracja przez istniejące konwersacje
                for (conversationEntity in existingConversations) {
                    // Pobierz lastConversationId i wyświetl w logach
                    val localConversationId = conversationEntity.localConversationId.toString()
                    Log.d("FetchConversations2", "Fetching last message for conversationId: $localConversationId")

                    // Pobranie ostatniej wiadomości i logowanie wyniku
                    val localLastMessage = messageDao.getLastMessageForConversation(localConversationId)
                    Log.d("FetchConversations2", "Last message for conversationId $localConversationId: $localLastMessage")

                    val lastMessageDisplay = localLastMessage?.text ?: "Brak wiadomości"
                    Log.d("FetchConversations2", "Last message display for conversationId $localConversationId: $lastMessageDisplay")
                    val isSeen = localLastMessage?.messageSeen ?: true
                    val senderId = localLastMessage?.senderId ?: ""

                    // Utworzenie obiektu Conversation
                    val conversation = Conversation(
                        id = conversationEntity.firebaseConversationId,
                        name = conversationEntity.participantName,
                        secondParticipantId = conversationEntity.memberId
                    )

                    conversationList.add(conversation)
                    lastMessageMap[conversationEntity.firebaseConversationId] = Triple(lastMessageDisplay, isSeen, senderId)
                }

                // Logowanie mapy z ostatnimi wiadomościami
                Log.d(" ", "Last message map: $lastMessageMap")

                _conversationList.value = conversationList
                _lastMessageMap.value = lastMessageMap

                startListeningForConversations(userId)
            }
        }
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
                val conversationsToInsert = mutableListOf<ConversationEntity>()

                viewModelScope.launch {
                    val existingConversations = conversationDao.getAllConversations()
                    val existingConversationIds = existingConversations.map { it.firebaseConversationId }.toSet()

                    for (conversationSnapshot in snapshot.children) {
                        val firebaseConversationId = conversationSnapshot.key ?: continue

                        // Sprawdza, czy konwersacja już istnieje
                        if (!existingConversationIds.contains(firebaseConversationId)) {
                            val members = conversationSnapshot.child("members")
                            // Sprawdza, czy bieżący użytkownik jest uczestnikiem konwersacji
                            if (members.hasChild(currentUserId)) {
                                val secondParticipantId = members.children.find { it.key != currentUserId }?.key ?: continue
                                val participantName = members.child(secondParticipantId).value as? String ?: ""
                                val conversationEntity = ConversationEntity(
                                    firebaseConversationId = firebaseConversationId,
                                    memberId = secondParticipantId,
                                    participantName = participantName
                                )

                                conversationsToInsert.add(conversationEntity)
                            } else {
                                Log.d("FetchConversations", "Użytkownik $currentUserId nie jest uczestnikiem konwersacji: $firebaseConversationId")
                            }
                        } else {
                            Log.d("FetchConversations", "Konwersacja już istnieje: $firebaseConversationId")
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
                Log.e("FirebaseError", "Błąd podczas pobierania konwersacji: ", error.toException())
            }
        })
    }

    private fun fetchMessagesForConversations() {
        viewModelScope.launch {
            // Pobierz wszystkie konwersacje z Room
            val conversations = conversationDao.getAllConversations()

            for (conversation in conversations) {
                val firebaseConversationId = conversation.firebaseConversationId
                val localConversationId = conversation.localConversationId.toString()

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
                                    } else if (messageData.messageSeen && !existingMessage.messageSeen) {
                                        val updatedMessage = existingMessage.copy(messageSeen = true)
                                        messageDao.updateMessage(updatedMessage)
                                    }
                                }

                                // Zapisz nowo pobrane wiadomości do Room
                                if (messagesToInsert.isNotEmpty()) {
                                    messagesToInsert.forEach { messageDao.insertMessage(it) }
                                }

                                // Aktualizuj messageSeen = true dla wiadomości, które nie istnieją już w Firebase
                                val localMessages = messageDao.getMessagesByConversationId(localConversationId)
                                for (localMessage in localMessages) {
                                    if (localMessage.messageId !in firebaseMessageIds && !localMessage.messageSeen) {
                                        val updatedMessage = localMessage.copy(messageSeen = true)
                                        messageDao.updateMessage(updatedMessage)
                                    }
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

    fun sendMessage(message: String, senderId: String = "", isAnonymous: Boolean = false) {
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

            // Determine the database path based on `isAnonymous`
            val basePath = if (isAnonymous) "conversations" else "explicit_conversations"

            // Define the reference path for messages
            val conversationMessagesRef = FirebaseDatabase.getInstance().reference
                .child(basePath)
                .child(conversationId) // Ensure you have a `conversationId` value
                .child("messages")

            // Push the message to the selected conversation node
            if (senderId2 != "system") {
                conversationMessagesRef.push().setValue(messageData)
            }
        }
    }

    fun resetMessages() {
        _messages.value = emptyList()
    }

    // Funkcja do ustawiania conversationId i inicjalizacji konwersacji wraz z nasłuchiwaczami
    fun setConversationId(id: String, isAnonymous: Boolean = false) {
        conversationId = id
        initializeDatabaseRef(isAnonymous)
    }

    private fun initializeDatabaseRef(isAnonymous: Boolean = false) {
        val basePath = if (isAnonymous) "conversations" else "explicit_conversations"
        // Odwołanie do węzła konwersacji
        val conversationRef = FirebaseDatabase.getInstance().reference
            .child(basePath)
            .child(conversationId)
            .child("messages")

        if(isAnonymous) {
            addAnonymousMessageListener(conversationRef)
        } else {

            viewModelScope.launch {

                if (explicitMessageListener != null) {
                    removeExplicitListener() // Funkcja, która usuwa listenera z Firebase gdyby taki istniał
                }

                // Pobiera lokalne konwersacje na podstawie firebaseConversationId
                val conversation = conversationDao.getAllConversations()

                val localConversationId = conversation.firstOrNull { it.firebaseConversationId == conversationId }?.localConversationId
                    ?: run {
                        return@launch
                    }

                // Ładuje wiadomości z Room Database na podstawie localConversationId
                val localMessages = messageDao.getMessagesByConversationId(localConversationId.toString())

                // Generowanie listy wiadomości do wyświetlenia, korzystając z istniejącej wartości messageSeen
                val roomMessages = localMessages.map { messageEntity ->
                    val messageType = when {
                        messageEntity.senderId == currentUserID -> MessageType(MessageType.Type.Sent, messageEntity.messageSeen, messageEntity.timestamp)
                        messageEntity.senderId == "system" -> MessageType(MessageType.Type.System, messageEntity.messageSeen, messageEntity.timestamp)
                        else -> MessageType(MessageType.Type.Received, messageEntity.messageSeen, messageEntity.timestamp)
                    }
                    messageEntity.text to messageType
                }.reversed()

                // Zaktualizuj _messages z wiadomościami z Room
                _messages.value = roomMessages

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
                                    return@launch // Nie dodawaj wiadomości, jeśli już istnieje
                                }

                                // Ustal, czy wiadomość została odczytana
                                val isSeen = it.messageSeen
                                val timestamp = it.timestamp

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

                            }
                        }
                        message?.let {
                            // Tylko dla wiadomości od innych użytkowników
                            if (!it.messageSeen && it.senderId != currentUserID) {
                                updateMessageSeenStatus(snapshot.key)
                            }
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
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
                        val messageId = snapshot.key ?: return  // Użyj klucza snapshotu jako ID wiadomości

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
                                messageEntity.text to messageType
                            }.reversed()

                            // Zaktualizuj _messages z wiadomościami z Room
                            _messages.value = roomMessages3
                        }

                        val message = snapshot.getValue(Message::class.java)
                        message?.let {
                            _messages.value = _messages.value.filterNot { pair -> pair.first == it.text }
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
    }

    // Funkcja do obsługi wiadomości anonimowych
    private fun addAnonymousMessageListener(conversationRef: DatabaseReference) {
        conversationRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let { addMessageToList(it) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }
            override fun onChildRemoved(snapshot: DataSnapshot) { }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    // Funkcja do dodawania wiadomości do listy
    private fun addMessageToList(message: Message) {
        val messageType = getMessageType(message)
        if (!_messages.value.any { it.first == message.text && it.second.type == messageType.type }) {
            _messages.value += message.text to messageType
        }
    }

    // Funkcja do uzyskiwania typu wiadomości
    private fun getMessageType(message: Message): MessageType {
        val isSeen = message.messageSeen
        val timestamp = message.timestamp
        return when {
            message.senderId == currentUserID -> MessageType(MessageType.Type.Sent, isSeen, timestamp)
            message.senderId == "system" -> MessageType(MessageType.Type.System, isSeen, timestamp)
            else -> MessageType(MessageType.Type.Received, isSeen, timestamp)
        }
    }

    fun removeExplicitListener() {
        explicitMessageListener?.let {
            val messagesRef = FirebaseDatabase.getInstance().getReference("explicit_conversations/$conversationId/messages")
            messagesRef.removeEventListener(it)
            explicitMessageListener = null  // Ustaw na null po usunięciu
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

    // Pobieranie zdjęcia drugiego użytkownika z storage
    fun getUserImageFromStorage(imageIdentifier: String, onResult: (Bitmap?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/$imageIdentifier")

        Log.d("UserImage", "Fetching image with identifier: $imageIdentifier")

        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            Log.d("UserImage", "Image successfully fetched for identifier: $imageIdentifier")
            onResult(bitmap)
        }.addOnFailureListener { exception ->
            Log.e("UserImage", "Failed to fetch image for identifier: $imageIdentifier", exception)
            onResult(null)
        }
    }

    fun getOtherUserProfileData(conversationId: String, onResult: (String?) -> Unit) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserID == null) {
            Log.e("UserProfileData", "Failed to get current user ID")
            onResult(null)
            return
        }

        val database = Firebase.database.reference
        val conversationRef = database.child("conversations").child(conversationId).child("members")

        Log.d("UserProfileData", "Fetching other user ID for conversation: $conversationId")

        conversationRef.get().addOnSuccessListener { dataSnapshot ->
            val members = dataSnapshot.children.mapNotNull { it.key }
            val otherUserID = members.firstOrNull { it != currentUserID }

            if (otherUserID != null) {
                Log.d("UserProfileData", "Other user ID found: $otherUserID")
                // Fetch user profile data, such as photoUrl
                fetchUserProfileData(otherUserID) { photoUrl ->
                    onResult(photoUrl)
                }
            } else {
                Log.d("UserProfileData", "No other user ID found for conversation: $conversationId")
                onResult(null)
            }
        }.addOnFailureListener { exception ->
            Log.e("UserProfileData", "Failed to fetch other user ID for conversation: $conversationId", exception)
            onResult(null)
        }
    }

    fun fetchUserProfileData(userId: String, onResult: (String?) -> Unit) {
        val database = Firebase.database.reference
        val userRef = database.child("user").child(userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val photoUrl = dataSnapshot.child("photoUrl").getValue(String::class.java)
            if (photoUrl != null) {
                Log.d("UserProfileData", "Fetched photoUrl: $photoUrl")
                onResult(photoUrl)
            } else {
                Log.e("UserProfileData", "No photoUrl found for user: $userId")
                onResult(null)
            }
        }.addOnFailureListener { exception ->
            Log.e("UserProfileData", "Failed to fetch user profile data for user: $userId", exception)
            onResult(null)
        }
    }

    fun fetchOtherUserImage(conversationId: String, onResult: (Bitmap?) -> Unit) {
        Log.d("FetchUserImage", "Starting fetch for conversation: $conversationId")

        getOtherUserProfileData(conversationId) { photoUrl ->
            if (photoUrl != null) {
                Log.d("FetchUserImage", "Other user photo URL retrieved: $photoUrl")
                // Extract image identifier from the URL, e.g., extract `1729537873669.jpg`
                val imageIdentifier = photoUrl.split("images%2F")[1].split("?")[0]
                getUserImageFromStorage(imageIdentifier, onResult)
            } else {
                Log.e("FetchUserImage", "Failed to retrieve photo URL for conversation: $conversationId")
                onResult(null)
            }
        }
    }


    fun listenForGameWins(conversationId: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserID == null) {
            Log.e("GameWinsListener", "Failed to get current user ID")
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val conversationRef = database.child("conversations").child(conversationId).child("members")

        Log.d("GameWinsListener", "Starting to listen for game wins in conversation: $conversationId")

        // Pobieramy danych o członkach konwersacji
        conversationRef.get().addOnSuccessListener { dataSnapshot ->
            val members = dataSnapshot.children.mapNotNull { it.key }
            val otherUserID = members.firstOrNull { it != currentUserID }

            if (otherUserID != null) {
                Log.d("GameWinsListener", "Found other user ID: $otherUserID")

                val unlockedStagesRef = FirebaseDatabase.getInstance().reference
                    .child("conversations/$conversationId/unlockedStagesOfPhoto")

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as? Map<*, *>
                        val gameWins = (data?.get(otherUserID) as? Long) ?: 0L
                        _gameWins.value = gameWins // Aktualizacja liczby wygranych
                        Log.d("GameWinsListener", "Updated game wins for $otherUserID: $gameWins")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("GameWinsListener", "Error: ${error.message}")
                    }
                }

                unlockedStagesRef.addValueEventListener(listener)

                // Zachowujemy listener, żeby później móc go usunąć
                _gameWinListener = listener

                Log.d("GameWinsListener", "Listener added to unlockedStagesRef for conversation: $conversationId")
            } else {
                Log.e("GameWinsListener", "No other user ID found in conversation: $conversationId")
            }
        }.addOnFailureListener { exception ->
            Log.e("GameWinsListener", "Failed to fetch conversation members for $conversationId", exception)
        }
    }



    fun removeGameWinListener() {
        _gameWinListener?.let {
            FirebaseDatabase.getInstance().reference
                .child("conversations/$conversationId/unlockedStagesOfPhoto")
                .removeEventListener(it)
        }
        _gameWinListener = null
    }

    // Funkcja do obliczenia stanu wyświetlania zdjęcia
    fun getImageDisplayState(): String {
        return when (_gameWins.value) {
            0L -> "Wygraj 3 gry aby wyświetlić całe zdjęcie. Wygraj jedną grę aby odsłonić część zdjęcia."
            1L -> "Odsłonięta 1/3 zdjęcia. Wygraj jedną grę aby odsłonić następną część zdjęcia."
            2L -> "Odsłonięta 2/3 zdjęcia. Wygraj jedną grę aby odsłonić ostatnią część."
            3L -> "Zdjęcie odsłonięte w całości."
            else -> "Zdjęcie odsłonięte w całości."
        }
    }

    fun getCroppedImage(bitmap: Bitmap?, gameWins: Long): Bitmap? {
        if (bitmap == null) return null

        val height = bitmap.height
        val width = bitmap.width

        return when (gameWins) {
            0L -> null // Nie wyświetlamy obrazu
            1L -> Bitmap.createBitmap(bitmap, 0, 0, width, height / 3) // Pierwsza część
            2L -> Bitmap.createBitmap(bitmap, 0, 0, width, (height * 2) / 3) // Druga część
            3L -> bitmap // Cały obraz
            else -> bitmap
        }
    }


    // Function to listen for likes in a conversation
    fun startListeningForLikes(conversationID: String) {
        conversationLikesRef = FirebaseDatabase.getInstance().reference
            .child("conversations")
            .child(conversationID)
            .child("likes")

        // Create the listener
        likeEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                checkIfBothLiked() // Check if both users liked when a like is added
            }

            // Function to check if both users have liked the conversation
            private fun checkIfBothLiked() {
                conversationLikesRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val likesCount = snapshot.childrenCount.toInt()
                        // Check if there are two likes
                        if (likesCount >= 2) {
                            // Update LiveData to show notification
                            _likesNotification.postValue(true)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("LikeEventListener", "Database error: ${error.message}")
                    }
                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("LikeEventListener", "Database error: ${error.message}")
            }
        }

        // Add the listener to the database reference
        conversationLikesRef!!.addChildEventListener(likeEventListener!!)
    }

    // Function to stop listening for likes
    fun stopListeningForLikes() {
        likeEventListener?.let { listener ->
            conversationLikesRef?.removeEventListener(listener)
            likeEventListener = null
        }
    }

    // Reset likes notification
    fun resetLikesNotification() {
        _likesNotification.value = false
    }

    fun addLike(conversationID : String) {
        val conversationRef = FirebaseDatabase.getInstance().reference
            .child("conversations")
            .child(conversationID)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserID = currentUser?.uid
        currentUserID?.let { userId ->
            conversationRef.child("likes").child(userId).setValue(true)
        }
    }


    // Implicit ChatsScreen
    private val _userProfiles = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val userProfiles: StateFlow<Map<String, UserProfile>> = _userProfiles

    private val _imagePaths = MutableStateFlow<Map<String, String?>>(emptyMap())
    val imagePaths: StateFlow<Map<String, String?>> = _imagePaths

    fun fetchUserProfile(conversation: Conversation, context: Context) {
        val userId = conversation.secondParticipantId
        if (_userProfiles.value.containsKey(userId)) return // Jeśli dane są już załadowane

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("user/$userId")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val profileImageUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""
                    _userProfiles.value += (userId to UserProfile(
                        name,
                        profileImageUrl = profileImageUrl
                    ))

                    // Sprawdzenie i zapisanie zdjęcia lokalnie
                    if (!isImageLocallySaved(userId, context)) {
                        saveImageLocally(profileImageUrl, userId, context)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłuż błąd
            }
        })
    }

    // Funkcja do sprawdzania, czy zdjęcie jest zapisane lokalnie
    private fun isImageLocallySaved(userId: String, context: Context): Boolean {
        // Sprawdź, czy lokalny plik zdjęcia istnieje
        return getLocalImagePath(userId, context) != null
    }

    // Funkcja do zapisywania zdjęcia w pamięci wewnętrznej
    fun saveImageLocally(imageUrl: String, userId: String, context: Context) {
        val client = OkHttpClient()
        val request = Request.Builder().url(imageUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Obsłuż błąd
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val inputStream = responseBody.byteStream()
                    // Ścieżka do pliku w pamięci wewnętrznej aplikacji
                    val localFile = File(context.filesDir, "$userId.jpg") // Możesz dostosować nazwę pliku
                    val outputStream = FileOutputStream(localFile)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        })
    }

    // Funkcja do odczytywania lokalnej ścieżki do zdjęcia
    fun getLocalImagePath(userId: String, context: Context): String? {
        val localFile = File(context.filesDir, "$userId.jpg")
        return if (localFile.exists()) {
            localFile.absolutePath // Zwróć ścieżkę do lokalnego pliku
        } else {
            null // Zwróć null, jeśli plik nie istnieje
        }
    }
}