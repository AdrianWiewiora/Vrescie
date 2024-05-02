package com.example.vresciecompose.screens

import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.MessageList
import com.example.vresciecompose.view_models.ConversationViewModel


@Composable
fun AnonymousConversationScreen(
    conversationID: String,
    onClick: (String) -> Unit,
    viewModel: ConversationViewModel)
{
    // Pole tekstowe do wprowadzania wiadomości
    var messageText by remember { mutableStateOf("") }

    val showDialog = remember { mutableStateOf(false) }
    val onBackPressedDispatcher = LocalBackPressedDispatcher.current
    DisposableEffect(key1 = onBackPressedDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialog.value = true
            }
        }
        onBackPressedDispatcher.addCallback(callback)

        onDispose {
            // Resetuj stan wiadomości w ViewModelu po utracie dostępu do komponentu
            viewModel.resetMessages()
            callback.remove()
        }
    }

    if (showDialog.value) {
        BackToMenuConfirmationDialog(
            onConfirm = {
                showDialog.value = false
                onClick(Navigation.Destinations.MAIN_MENU)
            },
            onDismiss = {
                showDialog.value = false
            }
        )
    }

    viewModel.setConversationId(conversationID)
    val messages by viewModel.messages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logotype_vreescie_svg),
                contentDescription = "logotyp",
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(width = 198.dp, height = 47.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 1.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_reaction_24),
                    contentDescription = "Add Like",
                    modifier = Modifier.size(48.dp)
                )

                Icon(
                    painter = painterResource(id = R.drawable.baseline_menu_24),
                    contentDescription = "Menu",
                    modifier = Modifier
                        .size(52.dp)
                        .padding(end = 4.dp)
                )
            }
        }

        MessageList(
            messages = messages,
            modifier = Modifier.weight(1f)
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 0.dp)
                .padding(bottom = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_highlight_off_24),
                contentDescription = "Cancel",
                modifier = Modifier.size(55.dp)
            )

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 5.dp),
                textStyle = TextStyle(color = Color.Black),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedLabelColor = Color.Black,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Black
                ),
                placeholder = { Text(text = "Wpisz wiadomość", color = Color.Black) }
            )

            Icon(
                painter = painterResource(id = R.drawable.baseline_send_24),
                contentDescription = "Send",
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        // Wywołanie funkcji sendMessage w ViewModelu
                        viewModel.sendMessage(messageText)
                        // Wyczyszczenie pola tekstowego po wysłaniu wiadomości
                        messageText = ""
                    }
            )
        }
    }
}

@Composable
fun BackToMenuConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Potwierdź wyjście")
        },
        text = {
            Text(text = "Czy na pewno chcesz wyjść z konwersacji?")
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
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

//@Preview
//@Composable
//fun PreviewAnonymousConversationScreen() {
//    val onClick: (String) -> Unit = {}
//    val viewModel = ConversationViewModel()
//
//    AnonymousConversationScreen(onClick, viewModel)
//}
