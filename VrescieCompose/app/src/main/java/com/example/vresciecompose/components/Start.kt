package com.example.vresciecompose.components

import android.app.Activity
import android.content.Context
import android.view.Gravity
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.vresciecompose.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.material3.contentColorFor
import androidx.compose.ui.text.TextStyle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.ui.components.BlackButton
import com.example.vresciecompose.ui.components.WhiteOutlinedButton
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign

internal val LocalBackPressedDispatcher = staticCompositionLocalOf<OnBackPressedDispatcher> {
    error("No Back Dispatcher provided")
}

@Composable
fun Start(
    navController: NavHostController
) {
    val onBackPressedDispatcher = LocalBackPressedDispatcher.current
    var showDialog by remember { mutableStateOf(false) }

    DisposableEffect(key1 = onBackPressedDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialog = true
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
                navController.navigate("login")
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
                navController.navigate("register")
            },
            text = "Zarejestruj się"
        )

        BlackButton(
            onClick = {
                navController.navigate("register_google")
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
                    showDialog = false
                    navController.navigateUp()
                },
                onDismiss = {
                    // Dismiss dialog
                    showDialog = false
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
    // Mocking NavHostController for preview
    val navController = rememberNavController()
    CompositionLocalProvider(
        LocalBackPressedDispatcher provides OnBackPressedDispatcher {}
    ) {
        Start(navController = navController)
    }
}
