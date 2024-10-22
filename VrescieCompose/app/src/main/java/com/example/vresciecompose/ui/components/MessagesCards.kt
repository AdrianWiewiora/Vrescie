package com.example.vresciecompose.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


@Composable
fun ReceivedMessage(modifier: Modifier, message: String, messageFontSize: TextUnit) {
    Card(
        modifier = modifier
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
fun SentMessage(modifier: Modifier, message: String, isSeen: Boolean, showIcon: Boolean, messageFontSize: TextUnit) {
    Card(
        modifier = modifier
            .padding(start = 25.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically // Wyrównanie ikony i tekstu w pionie
        ) {
            if (showIcon) {
                Icon(
                    imageVector = if (isSeen) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = if (isSeen) "Message seen" else "Message not seen",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(25.dp)
                        .padding(start = 8.dp, end = 3.dp)
                )
            }

            // Tekst wiadomości
            Text(
                text = message,
                modifier = Modifier
                    .padding(end = 8.dp, start = if (showIcon) 0.dp else 8.dp)
                    .padding(vertical = 5.dp),
                textAlign = TextAlign.End,
                fontSize = messageFontSize
            )
        }
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
