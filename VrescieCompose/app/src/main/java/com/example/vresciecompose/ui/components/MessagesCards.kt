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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


@Composable
fun ReceivedMessage(message: String, messageFontSize: TextUnit) {
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
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(vertical = 5.dp),
            fontSize = messageFontSize
        )
    }
}

@Composable
fun SentMessage(message: String, messageFontSize: TextUnit) {
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
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(vertical = 5.dp),
            textAlign = TextAlign.End,
            fontSize = messageFontSize
        )
    }
}

@Composable
fun SystemMessage(message: String, messageFontSize: TextUnit) {
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
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(vertical = 5.dp),
            textAlign = TextAlign.Center,
            fontSize = messageFontSize
        )
    }
}
