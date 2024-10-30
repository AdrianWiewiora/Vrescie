package com.example.vresciecompose.screens

import LocalContext
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.MessageList
import com.example.vresciecompose.ui.components.MessageType
import com.example.vresciecompose.view_models.ConversationViewModel
import com.example.vresciecompose.view_models.SettingsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.vertexai.type.ResponseStoppedException
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ExplicitConversationScreen(
    conversationID: String,
    onClick: (String) -> Unit,
    viewModel: ConversationViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val currentMessageSize by settingsViewModel.messageSizeFlow.observeAsState(1)
    val messageFontSize = when (currentMessageSize) {
        0 -> dimensionResource(id = R.dimen.message_small_size) // 10sp
        1 -> dimensionResource(id = R.dimen.message_normal_size) // 14sp
        2 -> dimensionResource(id = R.dimen.message_big_size) // 18sp
        3 -> dimensionResource(id = R.dimen.message_huge_size) // 22sp
        else -> dimensionResource(id = R.dimen.message_normal_size) // Domyślny rozmiar
    }.value.sp // Konwersja na TextUnit

    val (messageText, setMessageText) = remember { mutableStateOf("") }
    val context = LocalContext.current
    val config = generationConfig {
        maxOutputTokens = 300
        temperature = 0.2f
        topK = 20
        topP = 0.85f
    }
    val generativeModel = Firebase.vertexAI.generativeModel(modelName ="gemini-1.0-pro", generationConfig  = config)
    val (aiResponse, setAiResponse) = remember { mutableStateOf("") }

    BackHandler {
        onClick("${Navigation.Destinations.MAIN_MENU}/${2}")
    }

    DisposableEffect(Unit) {
        viewModel.listenForMessages(conversationID)

        onDispose {
            viewModel.resetMessages()
            viewModel.removeMessageListener()
        }
    }
    val messages by viewModel.messages.collectAsState()

    // Funkcja wysyłająca prompt do Vertex AI
    suspend fun fetchAIResponse(numberOfButton: Int) {
        val messagesList = messages.takeLast(
            when (numberOfButton) {
                1 -> 10
                3, 4, 5 -> 20
                else -> 0
            }
        )

        // Formatuj wiadomości
        val formattedMessages = messagesList.joinToString("\n") { (text, messageType) ->
            when (messageType.type) {
                MessageType.Type.Received -> "Obcy: $text"
                MessageType.Type.Sent -> "Ja: $text"
                MessageType.Type.System -> "System: $text"
            }
        }

        val prompt: String = when (numberOfButton) {
            1 -> "Odpowiedz maksymalnie jednym zdaniem. Po prostu napisz odpowiedź jaką proponujesz Jak mogę odpisać w tej rozmowie? Ostatnie 10 wiadomości tej konwersdacji prezentują się następująco:\n$formattedMessages. Moje wiadomości podpisałem jako Ja a mojego rozmówcy jako Obcy. Podpowiedz jak mam odpisać na wiadomości obcego a nie na moje własne"
            2 -> "Podaj tylko dwie propozycje ciekawych tematów do rozmowy i nic więcej. Odpowiedź ma być krótka, nie rozwijaj jej"
            3 -> "Zaproponuj tylko dwa oryginalne powitania i nic więcej, jedno dowolne a drugie na podstawie tych ostatnich 20 wiadomości w mojej rozmowie:\n$formattedMessages. Moje wiadomości podpisałem jako ja a mojego rozmówcy jako obcy."
            4 -> "Zaproponuj tylko dwa oryginalne pożegnania i nic więcej, jedno dowolne a drugie na podstawie tych ostatnich 20 wiadomości w mojej rozmowie:\n$formattedMessages. Moje wiadomości podpisałem jako ja a mojego rozmówcy jako obcy."
            5 -> "Zaproponuj tylko dwa pytania jakie ja mógłbym zadać obcemu dotyczące ostatnich wiadomości w mojej rozmowie:\n$formattedMessages. Moje wiadomości podpisałem jako ja a mojego rozmówcy jako obcy. Odpowiedź ma być krótka, maksymalnie dwa zdania"
            else -> ""
        }

        try {
            val response = generativeModel.generateContent(prompt)
            response.text?.let { setAiResponse(it) }
        } catch (e: ResponseStoppedException) {
            // Obsługa błędu, gdy generacja treści została przerwana
            Log.e("AIResponseError", "Generowanie treści zatrzymane: ${e.message}")
            setAiResponse("Wystąpił problem z generowaniem odpowiedzi. Spróbuj ponownie.")
        } catch (e: Exception) {
            // Ogólna obsługa błędów
            Log.e("AIResponseError", "Błąd podczas pobierania odpowiedzi AI: ${e.message}")
            setAiResponse("Wystąpił błąd. Spróbuj ponownie.")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setConversationIdExplicit(conversationID, context)
        viewModel.updateMessagesAsSeen(conversationID)
    }

    fun sendMessage(message: String) {
        viewModel.sendMessageExp(message)
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
        }
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
    onAiButtonClick: (Int) -> Unit = {}
){
    val isShowAiMenu = remember { mutableStateOf(false) }
    val isShowPrompt = remember { mutableStateOf(false) }
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
                        isShowAiMenu.value = !isShowAiMenu.value
                        isShowPrompt.value = false
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
                IconButton(
                    onClick = {

                    },
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.image_medium_size))
                        .padding(end = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
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
            ElevatedCard(
                modifier = Modifier
                    .padding(16.dp)
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
    ExplicitConversationColumn()
}