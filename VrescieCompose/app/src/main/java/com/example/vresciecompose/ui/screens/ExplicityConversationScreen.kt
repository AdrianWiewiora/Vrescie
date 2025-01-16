package com.example.vresciecompose.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.MessageList
import com.example.vresciecompose.ui.components.MessageType
import com.example.vresciecompose.ui.components.TicTacToeGame
import com.example.vresciecompose.view_models.ConversationViewModel
import com.example.vresciecompose.view_models.SettingsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.vertexai.type.ResponseStoppedException
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun ExplicitConversationScreen(
    conversationID: String,
    navigateTo: (String) -> Unit,
    conversationViewModel: ConversationViewModel,
    settingsViewModel: SettingsViewModel,
) {
    // Rozmiar wiadomości w zależności od ustawień
    val currentMessageSize by settingsViewModel.messageSizeFlow.observeAsState(1)
    val messageFontSize = when (currentMessageSize) {
        0 -> dimensionResource(id = R.dimen.message_small_size) // 10sp
        1 -> dimensionResource(id = R.dimen.message_normal_size) // 14sp
        2 -> dimensionResource(id = R.dimen.message_big_size) // 18sp
        3 -> dimensionResource(id = R.dimen.message_huge_size) // 22sp
        else -> dimensionResource(id = R.dimen.message_normal_size) // Domyślny rozmiar
    }.value.sp // Konwersja na TextUnit
    // Pole tekstowe do wprowadzania wiadomości
    val (messageText, setMessageText) = remember { mutableStateOf("") }
    // Inicjalizacja konwersacji oraz zmienna zawierająca jej wiadomości
    val messages by conversationViewModel.messages.collectAsState()
    // Zmienna do przechowywania stanu wyjścia
    var isDisposed by remember { mutableStateOf(false) }

    // Zmienne dla VertexAI
    val config = generationConfig {
        maxOutputTokens = 300
        temperature = 0.2f
        topK = 20
        topP = 0.85f
    }
    val generativeModel = Firebase.vertexAI.generativeModel(modelName ="gemini-1.0-pro", generationConfig  = config)
    val (aiResponse, setAiResponse) = remember { mutableStateOf("") }

    // Zmienne dla Tic Tac Toe
    val currentPlayerMessage by conversationViewModel.currentPlayerMessage.collectAsState()
    val board = conversationViewModel.board
    val conversationId = conversationViewModel.currentConversationId
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    val gameWinner by conversationViewModel._gameWinner.collectAsState()
    val youWonString = stringResource(R.string.you_won)
    val youLostString = stringResource(R.string.you_lost)
    val gameStatusMessage = remember(gameWinner) {
        when {
            gameWinner.isNullOrEmpty() -> null
            gameWinner == currentUserID -> youWonString
            else -> youLostString
        }
    }

    val wantNewGameCount by conversationViewModel.wantNewGameCount.collectAsState()

    fun makeMove(positionX: Int, positionY: Int): Boolean {
        return conversationViewModel.makeMove(conversationId, currentUserID.toString(), positionX, positionY)
    }

    fun listenForMoves() {
        conversationViewModel.listenForMoves(conversationId, currentUserID.toString())
    }

    fun setWantNewGame(){
        conversationViewModel.setWantNewGame(currentUserID.toString())
    }

    fun sendMessage(message: String) {
        conversationViewModel.sendMessage(message)
    }

    BackHandler {
        if (!isDisposed) {
            isDisposed = true
            conversationViewModel.resetMessages()
            conversationViewModel.removeExplicitListener()
            conversationViewModel.removeGameWinListener(conversationID)
            conversationViewModel.removeNewGameListener(conversationID)
        }
        navigateTo("${Navigation.Destinations.MAIN_MENU}/${2}")
    }

    DisposableEffect(Unit) {
        conversationViewModel.removeExplicitListener()
        conversationViewModel.setConversationId(conversationID)
        conversationViewModel.updateMessagesAsSeen(conversationID)
        conversationViewModel.listenForGameWin(conversationID)
        conversationViewModel.listenForNewGameRequests(conversationID)

        onDispose {
            if (!isDisposed) {
                isDisposed = true
                conversationViewModel.resetMessages()
                conversationViewModel.removeExplicitListener()
                conversationViewModel.removeGameWinListener(conversationID)
                conversationViewModel.removeNewGameListener(conversationID)
            }
        }
    }

    val textFriend = stringResource(R.string.friend)
    val textMe = stringResource(R.string.me)
    val textSystem = stringResource(R.string.system)
    val textNoUser = stringResource(R.string.no_user)

    val promptReplyTemplate = stringResource(R.string.prompt1)
    val promptTopics = stringResource(R.string.prompt2)
    val promptGreetings = stringResource(R.string.prompt3)
    val promptFarewells = stringResource(R.string.prompt4)
    val promptQuestions = stringResource(R.string.prompt5)

    val errorResponseStopped = stringResource(R.string.problem_generating_the_response)
    val errorGeneral = stringResource(R.string.an_error_occurred_please_try_again)

    fun getPromptReply(formattedMessages: String): String { return String.format(promptReplyTemplate, formattedMessages) }
    fun getPromptGreetings(formattedMessages: String): String {  return String.format(promptGreetings, formattedMessages) }
    fun getPromptFarewells(formattedMessages: String): String { return String.format(promptFarewells, formattedMessages) }
    fun getPromptQuestions(formattedMessages: String): String { return String.format(promptQuestions, formattedMessages)  }

    suspend fun getStats(): Triple<String, Int, Int> {
        return currentUserID?.let { userId ->
            suspendCoroutine { continuation ->
                conversationViewModel.fetchGameStats(userId) { name, games, wins ->
                    continuation.resume(Triple(name, games, wins))
                }
            }
        } ?: Triple(textNoUser, 0, 0)
    }

    // Funkcja wysyłająca prompt do Vertex AI
    suspend fun fetchAIResponse(numberOfButton: Int) {
        val messagesList = messages.takeLast(
            when (numberOfButton) {
                1 -> 10
                3, 4, 5 -> 20
                else -> 0
            }
        )

        // Formatuje wiadomości
        val formattedMessages = messagesList.joinToString("\n") { (text, messageType) ->
            when (messageType.type) {
                MessageType.Type.Received -> "$textFriend: $text"
                MessageType.Type.Sent -> "$textMe: $text"
                MessageType.Type.System -> "$textSystem: $text"
            }
        }

        val prompt: String = when (numberOfButton) {
            1 -> getPromptReply(formattedMessages)
            2 -> promptTopics
            3 -> getPromptGreetings(formattedMessages)
            4 -> getPromptFarewells(formattedMessages)
            5 -> getPromptQuestions(formattedMessages)
            else -> ""
        }

        try {
            val response = generativeModel.generateContent(prompt)
            response.text?.let { setAiResponse(it) }
        } catch (e: ResponseStoppedException) {
            setAiResponse(errorResponseStopped)
        } catch (e: Exception) {
            setAiResponse(errorGeneral)
        }
    }

    ExplicitConversationColumn(
        modifier = Modifier
            .fillMaxSize(),
        messages = messages,
        messageText = messageText,
        setMessageText = setMessageText,
        sendMessage = ::sendMessage,
        messageFontSize = messageFontSize,
        aiResponse = aiResponse,
        setAiResponse = setAiResponse,
        onAiButtonClick = { numberOfButton ->
            CoroutineScope(Dispatchers.IO).launch {
                fetchAIResponse(numberOfButton )
            }
        },
        board = board,
        makeMove = ::makeMove,
        listenForMoves = ::listenForMoves,
        gameStatusMessage,
        currentPlayerMessage,
        setWantNewGame = ::setWantNewGame,
        wantNewGameCount = wantNewGameCount,
        getStats = ::getStats
    )

}


@Composable
fun ExplicitConversationColumn(
    modifier: Modifier = Modifier,
    messages: List<Pair<String, MessageType>> = emptyList(),
    messageText: String = "",
    setMessageText: (String) -> Unit = {},
    sendMessage: (String) -> Unit = {},
    messageFontSize: TextUnit = 14.sp,
    aiResponse: String = "",
    setAiResponse: (String) -> Unit = {},
    onAiButtonClick: (Int) -> Unit = {},
    board: MutableState<Array<Array<String>>> = mutableStateOf(Array(10) { Array(10) { "" } }),
    makeMove: (Int, Int) -> Boolean,
    listenForMoves: () -> Unit,
    gameStatusMessage: String? = null,
    currentPlayerMessage: String = "",
    setWantNewGame: () -> Unit = {},
    wantNewGameCount: Int = 0,
    getStats: suspend () -> Triple<String, Int, Int>

){
    val isShowAiMenu = remember { mutableStateOf(false) }
    val isShowPrompt = remember { mutableStateOf(false) }
    val isShowGamesMenu = remember { mutableStateOf(false) }
    val isShowTicTacToe = remember { mutableStateOf(false) }

    val isShowStats = remember { mutableStateOf(false) }
    val gameName = remember { mutableStateOf("") }
    val totalGames = remember { mutableStateOf(0) }
    val userWins = remember { mutableStateOf(0) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logotype_vreescie_svg),
                contentDescription = "logotyp",
                modifier = Modifier
                    .size(width = 198.dp, height = 47.dp)
                    .padding(2.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isShowPrompt.value = false
                        isShowAiMenu.value = false
                        if (isShowTicTacToe.value) {
                            isShowTicTacToe.value = false
                            isShowGamesMenu.value = false
                        }
                        else {
                            isShowGamesMenu.value = !isShowGamesMenu.value
                        }
                        isShowStats.value = false
                    },
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.image_medium_size))
                        .padding(end = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsEsports,
                        contentDescription = "Games",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.image_medium_size))

                    )
                }
                IconButton(
                    onClick = {
                        isShowAiMenu.value = !isShowAiMenu.value
                        isShowPrompt.value = false
                        isShowGamesMenu.value = false
                        setAiResponse("")
                    },
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.image_medium_size))
                        .padding(end = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Quiz,
                        contentDescription = "VrescieAI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.image_medium_size))

                    )
                }
            }
        }
        if (isShowAiMenu.value) {
            LazyRow(
                modifier = Modifier.height(50.dp)
            ) {
                item {
                    Button(
                        onClick = {
                            isShowAiMenu.value = !isShowAiMenu.value
                            onAiButtonClick(1)
                            isShowPrompt.value = !isShowPrompt.value
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Co mogę odpisać?")
                    }
                }
                item {
                    Button(
                        onClick = {
                            isShowAiMenu.value = !isShowAiMenu.value
                            onAiButtonClick(2)
                            isShowPrompt.value = !isShowPrompt.value
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Podaj ciekawe tematy do rozmowy")
                    }
                }
                item {
                    Button(
                        onClick = {
                            isShowAiMenu.value = !isShowAiMenu.value
                            onAiButtonClick(3)
                            isShowPrompt.value = !isShowPrompt.value
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Zaproponuj oryginalne powitanie")
                    }
                }
                item {
                    Button(
                        onClick = {
                            isShowAiMenu.value = !isShowAiMenu.value
                            onAiButtonClick(4)
                            isShowPrompt.value = !isShowPrompt.value
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Zaproponuj oryginalne pożegnanie")
                    }
                }
                item {
                    Button(
                        onClick = {
                            isShowAiMenu.value = !isShowAiMenu.value
                            onAiButtonClick(5)
                            isShowPrompt.value = !isShowPrompt.value
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Zaproponuj pytania jakie mogę zadać dotyczące rozmowy.")
                    }
                }
            }
        }

        // Wyświetlenie odpowiedzi modelu AI nad MessageList
        if (isShowPrompt.value) {
            Column{
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                ) {
                    if (aiResponse.isEmpty()) {
                        Text(
                            text = "Ładowanie",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .height(100.dp)
                        ) {
                            item {
                                Text(
                                    text = aiResponse,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        setMessageText(aiResponse)
                        isShowPrompt.value = !isShowPrompt.value
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    enabled = aiResponse.isNotEmpty()
                ) {
                    Text("Wklej odpowiedź")
                }
            }
        }

        // Wyświetlenie Menu gier
        if (isShowGamesMenu.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ){
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                        .height(100.dp)
                        .fillMaxWidth(),
                ) {
                    Button(
                        onClick = {
                            isShowTicTacToe.value = !isShowTicTacToe.value
                            isShowGamesMenu.value = false
                            isShowStats.value = false
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Kółko-krzyżyk")
                    }
                    Button(
                        onClick = {
                            isShowStats.value = !isShowStats.value
                            if (isShowStats.value) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val (name, games, wins) = getStats()
                                    gameName.value = name
                                    totalGames.value = games
                                    userWins.value = wins
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Statystyki")
                    }
                }

            }
            if (isShowStats.value) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Statystyki dla gry: ${gameName.value}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Rozegrane gry: ${totalGames.value}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Twoje wygrane: ${userWins.value}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Wyświetlenie gry Kółko-krzyżyk
        if (isShowTicTacToe.value) {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        // Sprawdź, czy gra jest zakończona
                        if (!gameStatusMessage.isNullOrEmpty()) {
                            // Wyświetl komunikat o zakończeniu gry
                            Text(
                                text = gameStatusMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (gameStatusMessage == "Wygrałeś!") Color.Green else Color.Red,
                            )
                        } else {
                            // Wyświetl informację o obecnym graczu, jeśli gra trwa
                            Text(
                                text = currentPlayerMessage,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                setWantNewGame()
                            },
                            modifier = Modifier.padding(horizontal = 4.dp),

                        ) {
                            val wantNewGameDisplay = "Nowa gra ${wantNewGameCount}/2"
                            Text(text = wantNewGameDisplay)
                        }
                    }
                    TicTacToeGame(
                        board = board,
                        onCellClick = { row, col ->
                            val isWinningMove = makeMove(row, col)
                        },
                        listenForMoves = {
                            listenForMoves()
                        }
                    )
                }
            }
        }


        MessageList(
            messages = messages,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 15.dp)
                .padding(vertical = 0.dp),
            messageFontSize = messageFontSize
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 0.dp)
                .padding(bottom = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = messageText,
                onValueChange = { setMessageText(it) },
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .padding(start = 10.dp)
                    .weight(1f),
                shape = RoundedCornerShape(25.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = { Text(text = stringResource(R.string.enter_message), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 0.dp)) },
                singleLine = false,
                maxLines = 5,
            )

            IconButton(
                onClick = {
                    val trimmedMessage = messageText.trim()
                    setMessageText(messageText.trimEnd(' ', '\n'))
                    if (trimmedMessage.isNotEmpty()) {
                        sendMessage(trimmedMessage)
                        setMessageText("")
                    }
                },
                modifier = Modifier
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(50.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun ExplicitConversationColumnPreview() {
    fun makeMove(positionX: Int, positionY: Int): Boolean {return true}
    fun listenForMoves() {}
    fun getStats(): Triple<String, Int, Int> {return Triple("Brak użytkownika", 0, 0)}
    ExplicitConversationColumn(makeMove = ::makeMove, listenForMoves = { listenForMoves() }, getStats = {getStats()})
}