package com.example.vresciecompose.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ReceivedMessage(
    modifier: Modifier,
    message: String,
    messageFontSize: TextUnit,
    showTime: Boolean,
    timestamp: Long,
    cornerShape: RoundedCornerShape
) {
    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    Card(
        modifier = modifier.padding(end = 25.dp),
        shape = cornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.End // ustawia czas na prawą stronę
        ) {
            Text(text = message, fontSize = messageFontSize)
            if (showTime) {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceDim,
                )
            }
        }
    }
}

@Composable
fun SentMessage(
    modifier: Modifier,
    message: String,
    isSeen: Boolean,
    showIcon: Boolean,
    messageFontSize: TextUnit,
    showTime: Boolean,
    timestamp: Long,
    cornerShape: RoundedCornerShape
) {
    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    Card(
        modifier = modifier.padding(start = 25.dp),
        shape = cornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showIcon) {
                    Icon(
                        imageVector = if (isSeen) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (isSeen) "Message seen" else "Message not seen",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(25.dp).padding(start = 8.dp, end = 3.dp)
                    )
                }
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = messageFontSize
                )
            }
            if (showTime) {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 10.dp),
                    color = MaterialTheme.colorScheme.surfaceDim,
                )
            }
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
