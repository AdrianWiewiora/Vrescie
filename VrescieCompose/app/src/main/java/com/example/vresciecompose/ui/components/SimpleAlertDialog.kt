package com.example.vresciecompose.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.vresciecompose.R

@Composable
fun SimpleAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    text1: String = "Błąd",
    text2: String = "Coś poszło nie tak"
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = text1)
        },
        text = {
            Text(text = text2)
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.no))
            }
        }
    )
}