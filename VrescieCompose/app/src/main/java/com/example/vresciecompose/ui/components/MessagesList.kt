package com.example.vresciecompose.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
            state = listState,
            reverseLayout = true,
            verticalArrangement = Arrangement.Top
        ) {
            itemsIndexed(messages.asReversed()) { index, (message, type) ->
                val paddingValues = getMessagePadding(index, messages)

                when (type.type) {
                    MessageType.Type.Received -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            ReceivedMessage(
                                modifier = Modifier.padding(paddingValues).padding(horizontal = 8.dp),
                                message,
                                messageFontSize
                            )
                        }
                    }
                    MessageType.Type.Sent -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            val isSeen = type.isSeen
                            val showIcon = isLastInGroup(index, messages)
                            SentMessage(
                                modifier = Modifier.padding(paddingValues).padding(horizontal = 8.dp),
                                message,
                                isSeen,
                                showIcon,
                                messageFontSize
                            )
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

        LaunchedEffect(messages) {
            if (messages.isNotEmpty()) {
                val lastMessageType = messages.last().second.type
                if (lastMessageType == MessageType.Type.Sent || lastMessageType == MessageType.Type.System) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }
}


fun isLastInGroup(index: Int, messages: List<Pair<String, MessageType>>): Boolean {
    val reversedIndex = messages.size - 1 - index
    // Sprawdza tylko wiadomości typu Sent
    if (reversedIndex < 0 || reversedIndex >= messages.size) return false
    val currentMessageType = messages[reversedIndex].second.type
    // Sprawdza, czy następna wiadomość (w oryginalnej kolejności) jest innego typu lub jest ostatnia
    return reversedIndex == messages.size - 1 || messages[reversedIndex + 1].second.type != currentMessageType
}


fun getMessagePadding(index: Int, messages: List<Pair<String, MessageType>>): PaddingValues {
    // Indeks wiadomości jest liczony od końca w przypadku odwróconej listy
    val reversedIndex = messages.size - 1 - index

    val currentType = messages[reversedIndex].second.type
    val isFirstMessage = reversedIndex == messages.size - 1
    val isLastMessage = reversedIndex == 0

    // Logika dla paddingu uwzględnia odwróconą kolejność
    val topPadding = if (!isFirstMessage && messages[reversedIndex + 1].second.type == currentType) 1.dp else 8.dp
    val bottomPadding = if (!isLastMessage && messages[reversedIndex - 1].second.type == currentType) 1.dp else 8.dp

    return PaddingValues(top = bottomPadding, bottom = topPadding)
}




@Preview(showBackground = true)
@Composable
fun MessageListPreview() {
    val messages = listOf(
        "Hello, how are you?" to MessageType(MessageType.Type.Received, isSeen = true),
        "Hello, how are you?" to MessageType(MessageType.Type.Received, isSeen = true),
        "I'm doing great, thanks!" to MessageType(MessageType.Type.Sent, isSeen = true),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true), // Przykład wiadomości widzianej
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = false),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = false),
        "System message: User exit the chat" to MessageType(MessageType.Type.System),
    )

    MessageList(
        messages = messages,
        modifier = Modifier.padding(16.dp),
        messageFontSize = 14.sp
    )
}
