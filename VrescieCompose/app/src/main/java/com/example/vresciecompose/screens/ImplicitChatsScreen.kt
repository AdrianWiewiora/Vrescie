package com.example.vresciecompose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ImplicitChatsScreen() {
    val conversationList = remember { mutableStateListOf<Conversation>() }

    // Pobranie konwersacji z bazy danych Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance()
    val conversationRef = database.getReference("/explicit_conversations")

    // Listener dla zmian w bazie danych
    val conversationListener = remember {
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversationList.clear()
                for (conversationSnapshot in snapshot.children) {
                    // Sprawdź, czy bieżący użytkownik jest uczestnikiem konwersacji
                    if (conversationSnapshot.key?.contains(currentUser?.uid.toString()) == true) {
                        // Pobierz imię drugiego uczestnika konwersacji
                        val participants = conversationSnapshot.child("members")
                        val secondParticipant = participants.children.find { it.key != currentUser?.uid }
                        val secondParticipantName = secondParticipant?.value as? String ?: ""

                        // Stwórz obiekt Conversation i dodaj go do listy
                        val conversation = Conversation(id = conversationSnapshot.key ?: "", name = secondParticipantName)
                        conversationList.add(conversation)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłuż błąd
            }
        }
    }

    // Dodaj listener do bazy danych
    val listenerRef = remember { mutableStateOf(conversationListener) }
    conversationRef.addValueEventListener(listenerRef.value)


    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = com.example.vresciecompose.R.drawable.logotype_vreescie_svg),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 198.dp, height = 47.dp)
                    .padding(2.dp)
            )

            Image(
                painter = painterResource(id = com.example.vresciecompose.R.drawable.baseline_settings_24),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .padding(vertical = 0.dp),

            ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 5.dp, bottom = 8.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color.Black),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )

            ) {
                if (conversationList.isNotEmpty()) {
                    // Jeśli są konwersacje, wyświetl listę
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(conversationList) { conversation ->
                            ConversationItem(conversation = conversation)
                        }
                    }
                } else {
                    // Jeśli brak konwersacji, wyświetl komunikat
                    Text(
                        text = "Jeszcze nic tu nie ma :(\nPostaraj się o polubienia\ninnych użytkowników\nw anonimowym czacie aby\n pojawiły się tu konwersacje",
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 40.dp, horizontal = 20.dp)
                    )
                }
            }

        }
    }
}

@Composable
fun ConversationItem(conversation: Conversation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        border = BorderStroke(2.dp, Color.Black),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = conversation.name,
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp
        )
    }
}

data class Conversation(
    val id: String,
    val name: String,
)
