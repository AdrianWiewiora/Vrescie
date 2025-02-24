package com.example.vresciecompose.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.vresciecompose.R

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.confirm_closing))
        },
        text = {
            Text(text = stringResource(R.string.do_you_really_want_to_apply_the_application))
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    exitApplication(context)
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

private fun exitApplication(context: Context) {
    val activity = context as? Activity
    activity?.finish()
}



