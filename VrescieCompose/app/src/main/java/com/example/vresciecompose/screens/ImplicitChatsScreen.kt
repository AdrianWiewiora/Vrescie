package com.example.vresciecompose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.data.Conversation
import com.example.vresciecompose.data.UserProfile
import com.example.vresciecompose.view_models.ConversationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ImplicitChatsScreen(onClick: (String) -> Unit, conversationViewModel: ConversationViewModel) {
    val conversationList by conversationViewModel.conversationList.collectAsState()
    val lastMessageMap by conversationViewModel.lastMessageMap.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    // Dodaj słuchacza Firebase
    LaunchedEffect(key1 = userId) {
        conversationViewModel.startListeningForConversations(userId)
    }

    // Usuń słuchacza Firebase, gdy composable zostanie zniszczony
    DisposableEffect(Unit) {
        onDispose {
            conversationViewModel.stopListeningForConversations()
        }
    }

    // Obsługa kliknięcia w element LazyColumn
    val onItemClick: (Conversation) -> Unit = { conversation ->
        onClick("${Navigation.Destinations.EXPLICIT_CONVERSATION}/${conversation.id}")
    }

    if (conversationList.isNotEmpty()) {
        ImplicitChats(conversationList, onItemClick, lastMessageMap, userId)
    } else {
        Text(
            text = stringResource(R.string.nothing_here_yet),
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 40.dp, horizontal = 20.dp)
                .fillMaxSize()
        )
    }
}


@Composable
fun ImplicitChats(
    conversationList: List<Conversation>,
    onItemClick: (Conversation) -> Unit,
    lastMessageMap: Map<String, Triple<String, Boolean, String>>,
    userId: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(conversationList) { conversation ->
            val (lastMessage, isSeen, senderId) = lastMessageMap[conversation.id] ?: Triple("", true, "")
            ConversationItem(
                conversation = conversation,
                lastMessage = lastMessage,
                isSeen = isSeen,
                senderId = senderId,
                onItemClick = onItemClick,
                userId = userId
            )
        }
    }
}


@Composable
fun ConversationItem(
    conversation: Conversation,
    lastMessage: String,
    isSeen: Boolean, // Dodajemy isSeen jako parametr
    senderId: String, // Dodajemy senderId jako parametr
    onItemClick: (Conversation) -> Unit,
    userId: String
) {
    val secondUserProfile = remember { mutableStateOf<UserProfile?>(null) }


    // Pobierz dane użytkownika po ID
    LaunchedEffect(conversation.secondParticipantId) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("user/${conversation.secondParticipantId}")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val profileImageUrl = snapshot.child("photoUrl").getValue(String::class.java) ?: ""
                    secondUserProfile.value = UserProfile(name = name, profileImageUrl = profileImageUrl)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłuż błąd
            }
        })
    }

    // Sprawdzenie, czy wiadomość nie jest od bieżącego użytkownika i nie została wyświetlona
    val shouldBoldMessage = !isSeen && senderId != userId

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onItemClick(conversation) }
            .border(1.dp, if (shouldBoldMessage) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp)), // Ramka dla nieodczytanych wiadomości
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wyświetl zdjęcie jeśli jest dostępne
            if (secondUserProfile.value?.profileImageUrl?.isNotEmpty() == true) {
                Image(
                    painter = rememberAsyncImagePainter(secondUserProfile.value!!.profileImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder na wypadek braku zdjęcia
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = conversation.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lastMessage,
                    style = if (shouldBoldMessage) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    else MaterialTheme.typography.bodyMedium
                )
            }
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

    val sampleLastMessages = mapOf(
        "1" to Triple("Cześć! Jak się masz?", false, "uid123"), // wiadomość niewidoczna, nadawca inny niż user
        "2" to Triple("Dziękuję, do zobaczenia!", true, "uid456"), // wiadomość widoczna
        "3" to Triple("Co słychać?", false, "currentUserId") // wiadomość niewidoczna, nadawca to bieżący użytkownik
    )

    // Mock funkcji onClick
    val mockOnClick: (String) -> Unit = { conversationId ->
        println("Clicked on conversation with id: $conversationId")
    }

    // Wywołanie ImplicitChats z przykładowymi danymi
    ImplicitChats(
        conversationList = sampleConversations,
        onItemClick = { conversation -> mockOnClick(conversation.id) },
        lastMessageMap = sampleLastMessages,
        userId = "1"
    )
}
