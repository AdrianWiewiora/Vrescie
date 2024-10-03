package com.example.vresciecompose.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ErrorAlertDialog(
    onDismiss: () -> Unit,
    text1: String = "Nieprawidłowe dane",
    text2: String = "Wprowadzono niepoprawny e-mail lub hasło"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).padding(end = 8.dp)
                )
                Text(
                    text = text1,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Text(
                text = text2,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Ok") // Przycisk "OK"
            }
        },
        containerColor = MaterialTheme.colorScheme.errorContainer
    )
}