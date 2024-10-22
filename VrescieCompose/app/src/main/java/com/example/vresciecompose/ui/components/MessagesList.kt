package com.example.vresciecompose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.view_models.SettingsViewModel

data class MessageType(
    val type: Type,
    val isSeen: Boolean = false
) {
    enum class Type {
        Received, Sent, System
    }
}

@Composable
fun MessageList(
    messages: List<Pair<String, MessageType>>,
    modifier: Modifier = Modifier,
    messageFontSize: TextUnit = 14.sp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(messages) { (message, type) ->
                when (type.type) {
                    MessageType.Type.Received -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            ReceivedMessage(message, messageFontSize)
                        }
                    }
                    MessageType.Type.Sent -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            SentMessage(message, messageFontSize)
                        }
                    }
                    MessageType.Type.System -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            SystemMessage(message, messageFontSize)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageListPreview() {
    val messages = listOf(
        "Hello, how are you?" to MessageType(MessageType.Type.Received, isSeen = false),
        "I'm doing great, thanks!" to MessageType(MessageType.Type.Sent),
        "System message: User joined the chat" to MessageType(MessageType.Type.System),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true), // Przykład wiadomości widzianej
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent)
    )

    MessageList(
        messages = messages,
        modifier = Modifier.padding(16.dp),
        messageFontSize = 14.sp
    )
}
