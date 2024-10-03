package com.example.vresciecompose.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Potwierdź zamknięcie")
        },
        text = {
            Text(text = "Czy na pewno chcesz zamknąć aplikację?")
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    exitApplication(context)
                }
            ) {
                Text(text = "Tak")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = "Nie")
            }
        }
    )
}

private fun exitApplication(context: Context) {
    val activity = context as? Activity
    activity?.finish()
}