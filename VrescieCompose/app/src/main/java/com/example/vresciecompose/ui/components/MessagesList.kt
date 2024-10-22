package com.example.vresciecompose.ui.components

import android.util.Log
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
    val listState = rememberLazyListState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
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
                            SentMessage(message, type.isSeen, messageFontSize)
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

        // Automatyczne przewijanie tylko dla wiadomości typu Sent i System
        LaunchedEffect(messages) {
            if (messages.isNotEmpty()) {
                val lastMessageType = messages.last().second.type
                Log.d("MessageList", "Last message type: $lastMessageType")
                Log.d("MessageList", "First visible item index: ${listState.firstVisibleItemIndex}")

                // Przewijamy, jeśli ostatnia wiadomość jest typu Sent lub System
                if (lastMessageType == MessageType.Type.Sent || lastMessageType == MessageType.Type.System) {
                    Log.d("MessageList", "Scrolling to the last item")
                    listState.animateScrollToItem(messages.size - 1) // Przewijamy do ostatniego elementu
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
        "I'm doing great, thanks!" to MessageType(MessageType.Type.Sent, isSeen = false),
        "System message: User joined the chat" to MessageType(MessageType.Type.System),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true), // Przykład wiadomości widzianej
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true)
    )

    MessageList(
        messages = messages,
        modifier = Modifier.padding(16.dp),
        messageFontSize = 14.sp
    )
}
