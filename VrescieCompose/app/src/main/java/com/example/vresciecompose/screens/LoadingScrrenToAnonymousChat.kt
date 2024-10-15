package com.example.vresciecompose.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.view_models.UserChatPrefsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Timer
import java.util.TimerTask


@Composable
fun LoadingToAnonymousChatScreen(
    onClick: (String) -> Unit,
) {
    Log.d("LoadingToAnonymousChatScreen", "LoadingToAnonymousChatScreen Composed")
    val hasNavigated = remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser

    // Dodajemy efekt do zapisu lastSeen co 10 sekund
    val timer = remember { Timer() }

    DisposableEffect(true) {
        Log.d("DisposableEffect", "Effect started - Timer scheduled")

        val task = object : TimerTask() {
            override fun run() {
                currentUser?.uid?.let { userId ->
                    Log.d("DisposableEffect", "Updating lastSeen for userId: $userId")
                    updateUserLastSeen(userId)
                }
            }
        }

        timer.schedule(task, 0, 10000)

        onDispose {
            Log.d("DisposableEffect", "Effect disposed - Timer cancelled and user removed")

            timer.cancel()
            currentUser?.uid?.let { userId ->
                Log.d("DisposableEffect", "Removing user from Firebase for userId: $userId")
                removeUserFromFirebaseDatabase(userId)
            }
        }
    }


    // nasłuchiwanie nowych konwersacji dla zalogowanego użytkownika
    val conversationRef = Firebase.database.reference.child("conversations")
    conversationRef.addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val conversationData = snapshot.value as? Map<*, *>
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && conversationData != null && userId in (conversationData["members"] as? Map<*, *> ?: emptyMap<String, Any>())) {
                val canConnected = conversationData["canConnected"] as? Boolean
                val conversationID = snapshot.key
                if (canConnected == true && !hasNavigated.value) {
                    Log.d("Navigation", "Navigate to new conversation")
                    onClick("${Navigation.Destinations.ANONYMOUS_CONVERSATION}/$conversationID")
                    hasNavigated.value = true // Ustaw flagę, że nawigacja miała miejsce
                }
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    })

    LoadingScreenImageAndCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .padding(vertical = 0.dp),
    )
}

fun removeUserFromFirebaseDatabase(userId: String) {
    val database = Firebase.database
    val usersRef = database.getReference("vChatUsers")
    usersRef.child(userId).removeValue()
}

fun updateUserLastSeen(userId: String) {
    val database = Firebase.database
    val usersRef = database.getReference("vChatUsers")
    val currentTime = System.currentTimeMillis()
    usersRef.child(userId).child("info").child("lastSeen").setValue(currentTime)
}


@Composable
fun LoadingScreenImageAndCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = com.example.vresciecompose.R.drawable.logotype_vreescie_svg),
            contentDescription = "logotype",
            modifier = Modifier.size(width = 198.dp, height = 47.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 5.dp, bottom = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.wait_we_are_looking) ,
                    modifier = Modifier
                        .padding(horizontal = 30.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    LoadingScreenImageAndCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .padding(vertical = 0.dp),
    )
}