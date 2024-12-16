package com.example.vresciecompose.screens

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.MessageList
import com.example.vresciecompose.ui.components.MessageType
import com.example.vresciecompose.ui.components.SimpleAlertDialog
import com.example.vresciecompose.view_models.ConversationViewModel
import com.example.vresciecompose.view_models.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnonymousConversationScreen(
    conversationID: String,
    onNavigate: (String) -> Unit,
    viewModel: ConversationViewModel,
    settingsViewModel: SettingsViewModel,
) {
    Log.d("AnonymousConversationScreen", "AnonymousConversationScreen Composed")
    // Pobieranie zdjęcia
    var imageState by remember { mutableStateOf<Bitmap?>(null) }
    val showImageDialog = remember { mutableStateOf(false) }


    val currentMessageSize by settingsViewModel.messageSizeFlow.observeAsState(1)
    // Mapowanie rozmiarów czcionek na TextUnit
    val messageFontSize = when (currentMessageSize) {
        0 -> dimensionResource(id = R.dimen.message_small_size) // 10sp
        1 -> dimensionResource(id = R.dimen.message_normal_size) // 14sp
        2 -> dimensionResource(id = R.dimen.message_big_size) // 18sp
        3 -> dimensionResource(id = R.dimen.message_huge_size) // 22sp
        else -> dimensionResource(id = R.dimen.message_normal_size) // Domyślny rozmiar
    }.value.sp // Konwersja na TextUnit

    // Pole tekstowe do wprowadzania wiadomości
    val (messageText, setMessageText) = remember { mutableStateOf("") }
    val showDialogLike = remember { mutableStateOf(false) }
    val showDialogLikeNotification = remember { mutableStateOf(false) }
    val showExitDialog = remember { mutableStateOf(false) }
    var conversationRef by remember { mutableStateOf<DatabaseReference?>(null) }

    // Games - tic tac toe
    val currentPlayerMessage by viewModel.currentPlayerMessage.collectAsState()
    val board = viewModel.board
    val conversationId = viewModel.currentConversationId
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    val gameWinner by viewModel._gameWinner.collectAsState()
    val gameStatusMessage = when {
        gameWinner.isNullOrEmpty() -> null
        gameWinner == currentUserID -> "Wygrałeś !!!"
        else -> "Przegrałeś! :("
    }
    val wantNewGameCount by viewModel.wantNewGameCount.collectAsState()

    fun makeMove(positionX: Int, positionY: Int): Boolean {
        return viewModel.makeMove(conversationId, currentUserID.toString(), positionX, positionY, isAnonymous = true)
    }

    fun listenForMoves() {
        viewModel.listenForMoves(conversationId, currentUserID.toString(), isAnonymous = true)
    }

    fun setWantNewGame(){
        viewModel.setWantNewGame(currentUserID.toString(), isAnonymous = true)
    }

    // Zdefiniuj likeEventListener
    val likeEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            checkIfBothLiked()
        }

        private fun checkIfBothLiked() {
            conversationRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likesCount = snapshot.childrenCount.toInt()
                    // Sprawdź, czy są dwa lajki
                    if (likesCount >= 2) {
                        showDialogLikeNotification.value = true
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

    conversationRef = FirebaseDatabase.getInstance().reference
        .child("conversations")
        .child(conversationID)
        .child("likes")
    conversationRef!!.addChildEventListener(likeEventListener)

    BackHandler {
        showExitDialog.value = true
    }

    LaunchedEffect(conversationId) {
        viewModel.fetchOtherUserImage(conversationId) { bitmap ->
            imageState = bitmap
        }
    }

    DisposableEffect(Unit) {
        Log.d("DisposableEffect", "Effect started2")  // Log przy inicjalizacji
        viewModel.listenForGameWin(conversationID, isAnonymous = true)
        viewModel.listenForNewGameRequests(conversationID, isAnonymous = true)

        onDispose {
            Log.d("DisposableEffect", "Effect disposed2")  // Log przy wywołaniu onDispose

            conversationRef?.removeEventListener(likeEventListener)
            viewModel.removeGameWinListener(conversationID, isAnonymous = true)
            viewModel.removeNewGameListener(conversationID, isAnonymous = true)
            viewModel.resetMessages()
        }
    }

    val userDisconnectedMessage = stringResource(R.string.user_disconnected)
    if (showExitDialog.value) {
        SimpleAlertDialog(
            onConfirm = {
                // Pobranie aktualnie zalogowanego użytkownika
                val currentUser = FirebaseAuth.getInstance().currentUser
                val currentUserID = currentUser?.uid

                showExitDialog.value = false
                // Aktualizacja wartości w Firebase Realtime Database
                val database = FirebaseDatabase.getInstance()
                val conversationRef2 = database.reference
                    .child("conversations")
                    .child(conversationID)
                conversationRef2.child("canConnected").setValue(false)
                if (currentUserID != null) {
                    conversationRef2.child("members").child(currentUserID).setValue(false)
                }

                viewModel.sendMessage(userDisconnectedMessage, senderId = "system")
                // Przejście do głównego menu
                onNavigate("${Navigation.Destinations.MAIN_MENU}/${1}")
            },
            onDismiss = {
                showExitDialog.value = false
            },
            text1 = stringResource(R.string.confirm_exit),
            text2 = stringResource(R.string.are_you_sure_you_want_to_leave_the_conversation)
        )
    }

    if (showDialogLike.value) {
        SimpleAlertDialog(
            onConfirm = {
                showDialogLike.value = false
                addLike(conversationID)
            },
            onDismiss = {
                showDialogLike.value = false
            },
            text1 = stringResource(R.string.confirm_like),
            text2 = stringResource(R.string.are_you_sure_want_to_like_the_person)
        )
    }

    if (showDialogLikeNotification.value) {
        SimpleAlertDialog(
            onConfirm = {
                showDialogLikeNotification.value = false

                // Pobranie aktualnie zalogowanego użytkownika
                val currentUser = FirebaseAuth.getInstance().currentUser
                val currentUserID = currentUser?.uid

                val database = FirebaseDatabase.getInstance()
                val conversationRef3 = database.reference
                    .child("conversations")
                    .child(conversationID)
                conversationRef3.child("canConnected").setValue(false)
                if (currentUserID != null) {
                    conversationRef3.child("members").child(currentUserID).setValue(false)
                }

                onNavigate("${Navigation.Destinations.MAIN_MENU}/${2}")
            },
            onDismiss = {
                showDialogLikeNotification.value = false
            },
            text1 = stringResource(R.string.bravo_user_liked_you),
            text2 = stringResource(R.string.would_you_like_to_go_public_conversation)
        )
    }

    viewModel.setConversationId(conversationID)
    val messages by viewModel.messages.collectAsState()

    fun sendMessageToDb(message: String) {
        viewModel.sendMessage(message)
    }

    if (showImageDialog.value) {
        BasicAlertDialog(
            onDismissRequest = { showImageDialog.value = false },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                ) {
                    imageState?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Zdjęcie rozmówcy",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    } ?: Text(
                        text = "Nie udało się załadować zdjęcia.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        )
    }


    AnonymousConversationColumn(
        modifier = Modifier
            .fillMaxSize(),
        showExitDialog = showExitDialog,
        showDialogLike = showDialogLike,
        messages = messages,
        messageText = messageText,
        setMessageText = setMessageText,
        sendMessageToDb = ::sendMessageToDb,
        messageFontSize = messageFontSize,
        board = board,
        makeMove = ::makeMove,
        listenForMoves = ::listenForMoves,
        gameStatusMessage,
        currentPlayerMessage,
        setWantNewGame = ::setWantNewGame,
        wantNewGameCount = wantNewGameCount,
        showImageDialog = showImageDialog
    )
}

@Composable
fun AnonymousConversationColumn(
    modifier: Modifier = Modifier,
    showExitDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    showDialogLike: MutableState<Boolean> = remember { mutableStateOf(false) },
    messages: List<Pair<String, MessageType>> = emptyList(),
    messageText: String = "",
    setMessageText: (String) -> Unit = {},
    sendMessageToDb: (String) -> Unit = {},
    messageFontSize: TextUnit = 14.sp,
    board: MutableState<Array<Array<String>>> = mutableStateOf(Array(10) { Array(10) { "" } }),
    makeMove: (Int, Int) -> Boolean = { _, _ -> false },
    listenForMoves: () -> Unit = {},
    gameStatusMessage: String? = null,
    currentPlayerMessage: String = "",
    setWantNewGame: () -> Unit = {},
    wantNewGameCount: Int = 0,
    showImageDialog: MutableState<Boolean> = remember { mutableStateOf(false) }
){
    val isShowGamesMenu = remember { mutableStateOf(false) }
    val isShowTicTacToe = remember { mutableStateOf(false) }

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
                        if (isShowTicTacToe.value) {
                            isShowTicTacToe.value = false
                            isShowGamesMenu.value = false
                        }
                        else {
                            isShowGamesMenu.value = !isShowGamesMenu.value
                        }
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
                        showDialogLike.value = true
                    },
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .size(dimensionResource(R.dimen.image_medium_size))
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddReaction,
                        contentDescription = "Add Like",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.image_medium_size))
                            .padding(vertical = 4.dp)

                    )
                }

                IconButton(
                    onClick = { showImageDialog.value = true },
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.image_medium_size))
                        .padding(end = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Photo,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.image_medium_size))

                    )
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
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("Kółko-krzyżyk")
                    }
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
                            makeMove(row, col)
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
                .height(60.dp)
                .padding(horizontal = 5.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showExitDialog.value = true
                },
                modifier = Modifier
                    .size(55.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.HighlightOff,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(55.dp)
                )
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = { setMessageText(it) },
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .weight(1f),
                shape = RoundedCornerShape(25.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = {
                    Text(text = stringResource(R.string.enter_message), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 0.dp))
                },
                singleLine = false,
                maxLines = 5,
            )

            IconButton(
                onClick = {
                    val trimmedMessage = messageText.trim()
                    setMessageText(messageText.trimEnd(' ', '\n'))
                    if (trimmedMessage.isNotEmpty()) {
                        sendMessageToDb(trimmedMessage)
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

@Preview
@Composable
fun AnonymousConversationColumnPreview() {
    AnonymousConversationColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),

    )
}
