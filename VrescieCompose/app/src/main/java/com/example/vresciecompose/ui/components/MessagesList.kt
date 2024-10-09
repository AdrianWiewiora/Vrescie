package com.example.vresciecompose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class MessageType {
    Received, Sent, System
}

@Composable
fun MessageList(
    messages: List<Pair<String, MessageType>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(messages) { (message, type) ->
                when (type) {
                    MessageType.Received -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            ReceivedMessage(message)
                        }
                    }
                    MessageType.Sent -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            SentMessage(message)
                        }
                    }
                    MessageType.System -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SystemMessage(message)
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
        "Hello, how are you?" to MessageType.Received,
        "I'm doing great, thanks!" to MessageType.Sent,
        "System message: User joined the chat" to MessageType.System,
        "What about you?" to MessageType.Received,
        "I'm fine too, thank you!" to MessageType.Sent
    )

    MessageList(
        messages = messages,
        modifier = Modifier
            .padding(16.dp)
    )
}
