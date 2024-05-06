package com.example.vresciecompose.screens

import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.MessageList
import com.example.vresciecompose.view_models.ConversationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@Composable
fun AnonymousConversationScreen(
    conversationID: String,
    onClick: (String) -> Unit,
    viewModel: ConversationViewModel
) {
    // Pole tekstowe do wprowadzania wiadomości
    var messageText by remember { mutableStateOf("") }
    val showDialogLike = remember { mutableStateOf(false) }
    val showDialogLikeNotification = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val onBackPressedDispatcher = LocalBackPressedDispatcher.current
    var conversationRef by remember { mutableStateOf<DatabaseReference?>(null) }

    // Zdefiniuj likeEventListener
    val likeEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            checkIfBothLiked()
        }

        private fun checkIfBothLiked() {
            conversationRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likesCount = snapshot.childrenCount.toInt()

                    // Sprawdź, czy są dwa lajki
                    if (likesCount >= 2) {
                        showDialogLikeNotification.value = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LikeEventListener", "Database error: ${error.message}")
                }
            })
        }


        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {
            Log.e("LikeEventListener", "Database error: ${error.message}")
        }
    }

    conversationRef = FirebaseDatabase.getInstance().reference
        .child("conversations")
        .child(conversationID)
        .child("likes")
    conversationRef!!.addChildEventListener(likeEventListener)

    DisposableEffect(key1 = onBackPressedDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialog.value = true
            }
        }
        onBackPressedDispatcher.addCallback(callback)

        onDispose {
            conversationRef?.removeEventListener(likeEventListener)
            viewModel.resetMessages()
            callback.remove()
        }
    }
    if (showDialog.value) {
        BackToMenuConfirmationDialog(
            onConfirm = {
                // Pobranie aktualnie zalogowanego użytkownika
                val currentUser = FirebaseAuth.getInstance().currentUser
                val currentUserID = currentUser?.uid

                showDialog.value = false
                // Aktualizacja wartości w Firebase Realtime Database
                val database = FirebaseDatabase.getInstance()
                val conversationRef2 = database.reference
                    .child("conversations")
                    .child(conversationID)
                conversationRef2.child("canConnected").setValue(false)
                if (currentUserID != null) {
                    conversationRef2.child("members").child(currentUserID).setValue(false)
                }
                viewModel.sendMessage("Użytkownik się rozłączył", senderId = "system")
                // Przejście do głównego menu
                onClick(Navigation.Destinations.MAIN_MENU)
            },
            onDismiss = {
                showDialog.value = false
            }
        )
    }

    if (showDialogLike.value) {
        ShowAddLikeConfirmationDialog(
            onConfirm = {
                showDialogLike.value = false
                addLike(conversationID)
            },
            onDismiss = {
                showDialogLike.value = false
            }
        )
    }

    if (showDialogLikeNotification.value) {
        ShowLikeNotificationDialog(
            onConfirm = {
                showDialogLikeNotification.value = false
                onClick(Navigation.Destinations.MAIN_MENU + "?defaultFragment=2")
            },
            onDismiss = {
                showDialogLikeNotification.value = false
            }
        )
    }

    viewModel.setConversationId(conversationID)
    val messages by viewModel.messages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
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
                        .clickable {
                            showDialogLike.value = true
                        }
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
                    .clickable {
                        showDialog.value = true
                    }
            )

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 5.dp),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedLabelColor = Color.Black,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Black
                ),
                placeholder = { Text(text = "Wpisz wiadomość", color = Color.Black) },
                maxLines = 5,
            )

            Icon(
                painter = painterResource(id = R.drawable.baseline_send_24),
                contentDescription = "Send",
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        messageText = messageText.trimEnd(' ', '\n')
                        viewModel.sendMessage(messageText)
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


@Composable
fun ShowAddLikeConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = "Potwierdź polubienie")
        },
        text = {
            Text(text = "Czy na pewno chcesz polubić osobę z którą rozmawiasz?")
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

fun addLike(conversationID : String) {
    val conversationRef = FirebaseDatabase.getInstance().reference
        .child("conversations")
        .child(conversationID)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserID = currentUser?.uid
    currentUserID?.let { userId ->
        conversationRef.child("likes").child(userId).setValue(true)
    }
}

@Composable
fun ShowLikeNotificationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = "Brawo!!! Użytkownik cię polubił.")
        },
        text = {
            Text(text = "Czy chcesz opuścić konwersację by przejść do jawnej konwersacji?")
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
