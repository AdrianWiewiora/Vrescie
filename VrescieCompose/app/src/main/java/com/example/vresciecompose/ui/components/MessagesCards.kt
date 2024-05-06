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
            containerColor = Color(0xFF7FC1FD),
            contentColor = Color.Black
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(8.dp),
            color = Color.Black
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
            containerColor = Color(0xFFCCCCCC),
            contentColor = Color.Black
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(8.dp),
            color = Color.Black,
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
            containerColor = Color.Red,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(8.dp),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}
