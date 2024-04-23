package com.example.vresciecompose.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnonymousConversationScreen(onClick: (String) -> Unit) {
    Text(
        text ="Witaj w konwersacji" ,
        modifier = Modifier
            .padding(horizontal = 30.dp),
        color = Color.Black,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        style = TextStyle(
            lineHeight = 40.sp,
        )
    )
}
