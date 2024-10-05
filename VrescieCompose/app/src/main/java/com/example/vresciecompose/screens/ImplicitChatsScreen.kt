package com.example.vresciecompose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.data.Conversation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ImplicitChatsScreen(onClick: (String) -> Unit) {
    val conversationList = remember { mutableStateListOf<Conversation>() }
    val lastMessageMap = remember { mutableMapOf<String, String>() }

    // Pobranie konwersacji z bazy danych Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance()
    val conversationRef = database.getReference("/explicit_conversations")

    // Listener dla zmian w bazie danych
    val conversationListener = remember {
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversationList.clear()
                lastMessageMap.clear()
                for (conversationSnapshot in snapshot.children) {
                    // Sprawdź, czy bieżący użytkownik jest uczestnikiem konwersacji
                    if (conversationSnapshot.key?.contains(currentUser?.uid.toString()) == true) {
                        // Pobierz imię drugiego uczestnika konwersacji
                        val participants = conversationSnapshot.child("members")
                        val secondParticipant = participants.children.find { it.key != currentUser?.uid }
                        val secondParticipantName = secondParticipant?.value as? String ?: ""

                        // Pobierz ostatnią wiadomość
                        val lastMessage = conversationSnapshot.child("messages")
                            .children.sortedByDescending { it.child("timestamp").value as? Long }
                            .firstOrNull()?.child("text")?.value?.toString() ?: ""

                        // Stwórz obiekt Conversation i dodaj go do listy
                        val conversation = Conversation(id = conversationSnapshot.key ?: "", name = secondParticipantName)
                        conversationList.add(conversation)
                        lastMessageMap[conversationSnapshot.key ?: ""] = lastMessage
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

    // Obsługa kliknięcia w element LazyColumn
    val onItemClick: (Conversation) -> Unit = { conversation ->
        onClick("${Navigation.Destinations.EXPLICIT_CONVERSATION}/${conversation.id}")
    }


    if (conversationList.isNotEmpty()) {
        ImplicitChats(conversationList, onItemClick, lastMessageMap)
    } else {
        Text(
            text = "Jeszcze nic tu nie ma :(\nPostaraj się o polubienia\ninnych użytkowników\nw anonimowym czacie aby\n pojawiły się tu konwersacje",
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 20.dp).fillMaxSize()
        )
    }
}

@Composable
fun ImplicitChats(
    conversationList: List<Conversation>,
    onItemClick: (Conversation) -> Unit,
    lastMessageMap: Map<String, String>
){
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(conversationList) { conversation ->
            val lastMessage = lastMessageMap[conversation.id] ?: ""
            ConversationItem(
                conversation = conversation,
                lastMessage = lastMessage,
                onItemClick = onItemClick
            )
        }
    }
}


@Composable
fun ConversationItem(conversation: Conversation, lastMessage: String, onItemClick: (Conversation) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onItemClick(conversation) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = conversation.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewImplicitChatsScreen() {
    // Przykładowe dane konwersacji do podglądu
    val sampleConversations = listOf(
        Conversation(id = "1", name = "Jan Kowalski"),
        Conversation(id = "2", name = "Anna Nowak"),
        Conversation(id = "3", name = "Piotr Wiśniewski")
    )

    // Przykładowe ostatnie wiadomości
    val sampleLastMessages = mapOf(
        "1" to "Cześć! Jak się masz?",
        "2" to "Dziękuję, do zobaczenia!",
        "3" to "Co słychać?"
    )

    // Mock funkcji onClick
    val mockOnClick: (String) -> Unit = { conversationId ->
        println("Clicked on conversation with id: $conversationId")
    }

    // Wywołanie ImplicitChats z przykładowymi danymi
    ImplicitChats(
        conversationList = sampleConversations,
        onItemClick = { conversation -> mockOnClick(conversation.id) },
        lastMessageMap = sampleLastMessages
    )
}
