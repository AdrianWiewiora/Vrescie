package com.example.vresciecompose.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



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
                .padding(bottom = 30.dp)
                .padding(horizontal = 20.dp)
        )

        OutlinedButton(
            onClick = {
                navController.navigate("login")
            },
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .fillMaxWidth()
                .background(color = Color.White),
            border = BorderStroke(2.dp, Color.Black),
            content = {
                Text(
                    text = "Mam już konto",
                    color = Color.Black,
                    fontSize = 24.sp
                )
            }
        )



        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            navController.navigate("register")
        }) {
            Text(text = "Register")
        }

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
