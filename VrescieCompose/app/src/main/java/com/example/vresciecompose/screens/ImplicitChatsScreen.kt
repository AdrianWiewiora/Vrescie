package com.example.vresciecompose.screens

import LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.vresciecompose.view_models.ConversationViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ImplicitChatsScreen(
    navigateTo: (String) -> Unit,
    conversationViewModel: ConversationViewModel,
    isConnected: Boolean
) {
    val conversationList by conversationViewModel.conversationList.collectAsState()
    val lastMessageMap by conversationViewModel.lastMessageMap.collectAsState()
    val imagePaths by conversationViewModel.imagePaths.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    val localContext = LocalContext.current

    var localImagePathMap by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    LaunchedEffect(conversationList) {
        conversationList.forEach { conversation ->
            val secondParticipantId = conversation.secondParticipantId
            val localImagePath = conversationViewModel.getLocalImagePath(secondParticipantId, localContext)
            localImagePathMap = localImagePathMap + (secondParticipantId to localImagePath)
            conversationViewModel.fetchUserProfile(conversation, localContext)
        }
    }

    LaunchedEffect(key1 = userId) {
        conversationViewModel.fetchConversationsAndListen(userId)
    }

    DisposableEffect(Unit) {
        onDispose {
            conversationViewModel.stopListeningForConversations()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isConnected) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_internet_connection),
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }

        }

        if (conversationList.isNotEmpty()) {
            ImplicitChats(
                conversationList = conversationList,
                onItemClick = { conversation ->
                    navigateTo("${Navigation.Destinations.EXPLICIT_CONVERSATION}/${conversation.id}")
                },
                lastMessageMap = lastMessageMap,
                imagePaths = imagePaths,
                userId = userId,
                localImagePathMap = localImagePathMap
            )
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
}



@Composable
fun ImplicitChats(
    conversationList: List<Conversation>,
    onItemClick: (Conversation) -> Unit,
    lastMessageMap: Map<String, Triple<String, Boolean, String>>,
    imagePaths: Map<String, String?>,
    userId: String,
    localImagePathMap: Map<String, String?>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(conversationList) { conversation ->
            val (lastMessage, isSeen, senderId) = lastMessageMap[conversation.id] ?: Triple("", true, "")
            val imagePath = imagePaths[conversation.secondParticipantId]

            // Pobieramy lokalną ścieżkę obrazu dla secondParticipantId
            val localImagePath = localImagePathMap[conversation.secondParticipantId]

            ConversationItem(
                conversation = conversation,
                lastMessage = lastMessage,
                isSeen = isSeen,
                senderId = senderId,
                onItemClick = onItemClick,
                userId = userId,
                imagePath = imagePath,
                localImagePath = localImagePath
            )
        }
    }
}


@Composable
fun ConversationItem(
    conversation: Conversation,
    lastMessage: String,
    isSeen: Boolean,
    senderId: String,
    onItemClick: (Conversation) -> Unit,
    userId: String,
    imagePath: String?,
    localImagePath: String?
) {
    val shouldBoldMessage = !isSeen && senderId != userId
    val imageUrl = localImagePath ?: imagePath

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onItemClick(conversation) }
            .border(1.dp, if (shouldBoldMessage) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
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
        imagePaths = emptyMap(),
        userId = "1",
        localImagePathMap =emptyMap()
    )
}
