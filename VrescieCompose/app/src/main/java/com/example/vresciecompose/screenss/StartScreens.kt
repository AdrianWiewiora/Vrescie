package com.example.vresciecompose.screenss

import android.app.Activity
import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.R

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.ui.components.BlackButton
import com.example.vresciecompose.ui.components.WhiteOutlinedButton
import androidx.compose.ui.text.style.TextAlign
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.view_models.StartScreenViewModel

internal val LocalBackPressedDispatcher = staticCompositionLocalOf<OnBackPressedDispatcher> {
    error("No Back Dispatcher provided")
}



@Composable
fun StartScreens(
    viewModel: StartScreenViewModel,
    onClick:(String) -> Unit,
    onConfirmExit: () -> Unit
) {
    val onBackPressedDispatcher = LocalBackPressedDispatcher.current
    val showDialog = viewModel.showDialog.value


    DisposableEffect(key1 = onBackPressedDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.toggleDialogVisibility()
            }
        }
        onBackPressedDispatcher.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 40.dp)
                .padding(horizontal = 20.dp)
        )

        WhiteOutlinedButton(
            onClick = {
                onClick(Navigation.Destinations.LOGIN)
            },
            text = "Mam już konto"
        )

        Text(
            text = "lub",
            color = Color.Black,
            fontSize = 24.sp,
            modifier = Modifier
                .padding(vertical = 10.dp)
        )

        BlackButton(
            onClick = {
                onClick(Navigation.Destinations.REGISTRATION)
            },
            text = "Zarejestruj się"
        )

        BlackButton(
            onClick = {

            },
            text = "Zarejestruj za pomocą Google",
            icon = R.drawable.google_svgrepo_com,
            iconSize = 28,
            fontSize = 18
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Nigdy nie udostępniamy nic bez twojej zgody",
            color = Color.Black,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 10.dp)

        )

        Text(
            text = "Rejestrując się potwierdasz, że akceptujesz nasz Regulamin. Dowiedz się, jak przetwarzamy Twoje dane z Polityki prywatności",
            color = Color.Black,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 10.dp)

        )


        if (showDialog) {
            ExitConfirmationDialog(
                onConfirm = {
                    // Handle exit confirmation
                    viewModel.toggleDialogVisibility()
                    onConfirmExit()
                },
                onDismiss = {
                    // Dismiss dialog
                    viewModel.toggleDialogVisibility()
                }
            )
        }
    }
}

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
    activity?.finishAffinity()
}

@Preview
@Composable
fun PreviewStart() {
    val viewModel = remember { StartScreenViewModel() }

    CompositionLocalProvider(
        LocalBackPressedDispatcher provides OnBackPressedDispatcher {}
    ) {
        StartScreens(viewModel = viewModel, onClick = { }, onConfirmExit = { })
    }
}
