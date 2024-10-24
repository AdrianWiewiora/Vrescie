package com.example.vresciecompose.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

    BackHandler {
        onClick("${Navigation.Destinations.MAIN_MENU}/${2}")
    }

    DisposableEffect(Unit) {
        viewModel.listenForMessages(conversationID)

        onDispose {
            viewModel.resetMessages()
            viewModel.removeMessageListener()
            viewModel.removeExplicitListener()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setConversationIdExplicit(conversationID)
        viewModel.updateMessagesAsSeen(conversationID)
    }

    val messages by viewModel.messages.collectAsState()

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
        messageFontSize = messageFontSize
    )

}


@Composable
fun ExplicitConversationColumn(
    modifier: Modifier = Modifier,
    messages: List<Pair<String, MessageType>> = emptyList(),
    messageText: String = "",
    setMessageText: (String) -> Unit = {},
    sendMessage: (String) -> Unit = {},
    messageFontSize: TextUnit = 14.sp
){

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