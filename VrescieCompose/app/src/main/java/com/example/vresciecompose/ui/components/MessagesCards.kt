package com.example.vresciecompose.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
) {
    constructor() : this("", "", 0)
}


@Composable
fun ReceivedMessage(message: String) {
    Card(
        modifier = Modifier.padding(8.dp)
            .padding(end = 25.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
fun SentMessage(message: String) {
    Card(
        modifier = Modifier.padding(8.dp)
            .padding(start = 25.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun SystemMessage(message: String) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}
