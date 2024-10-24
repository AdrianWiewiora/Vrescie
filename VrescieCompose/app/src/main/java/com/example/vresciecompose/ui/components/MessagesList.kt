package com.example.vresciecompose.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.view_models.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MessageType(
    val type: Type,
    val isSeen: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Type {
        Received, Sent, System
    }
}

@Composable
fun MessageList(
    messages: List<Pair<String, MessageType>>,
    modifier: Modifier = Modifier,
    messageFontSize: TextUnit = 14.sp
) {
    val listState = rememberLazyListState()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            reverseLayout = true,
            verticalArrangement = Arrangement.Top
        ) {
            itemsIndexed(messages.asReversed()) { index, (message, type) ->

                val timestamp = type.timestamp
                val date = Date(timestamp)
                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
                val currentCalendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = currentCalendar.get(Calendar.MINUTE)

                if (index > 0) { // Sprawdzamy,
                    val previousTimestamp = messages[messages.size - 1 - index + 1].second.timestamp
                    val previousDate = Date(previousTimestamp)

                    val calendar1 = Calendar.getInstance().apply { time = date }
                    val calendar2 = Calendar.getInstance().apply { time = previousDate }
                    val formattedPreviousDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(previousDate)

                    if (calendar1.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR) ||
                        calendar1.get(Calendar.DAY_OF_YEAR) != calendar2.get(Calendar.DAY_OF_YEAR)) {
                        DateHeader(formattedPreviousDate)
                    }
                }

                val paddingValues = getMessagePadding(index, messages)

                var showTime = when{
                    index == 0 -> true
                    type.type != messages[messages.size - 1 - index + 1].second.type -> true
                    else -> false
                }

                if(index > 0) {
                    val previousTimestamp = messages[messages.size - 1 - index + 1].second.timestamp
                    val previousCalendar = Calendar.getInstance().apply { timeInMillis = previousTimestamp }
                    val previousHour = previousCalendar.get(Calendar.HOUR_OF_DAY)
                    val previousMinute = previousCalendar.get(Calendar.MINUTE)
                    if(type.type == messages[messages.size - 1 - index + 1].second.type &&
                        (previousHour != currentHour && previousMinute != currentMinute ||
                                previousHour == currentHour && previousMinute != currentMinute ||
                                previousHour != currentHour))
                        showTime = true
                }


                when (type.type) {
                    MessageType.Type.Received -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            ReceivedMessage(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .padding(horizontal = 8.dp),
                                message,
                                messageFontSize,
                                showTime,
                                timestamp
                            )
                        }
                    }
                    MessageType.Type.Sent -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            val isSeen = type.isSeen
                            val showIcon = isLastInGroup(index, messages)
                            SentMessage(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .padding(horizontal = 8.dp),
                                message,
                                isSeen,
                                showIcon,
                                messageFontSize,
                                showTime,
                                timestamp
                            )
                        }
                    }
                    MessageType.Type.System -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            SystemMessage(message, messageFontSize)
                        }
                    }
                }
                if (index == messages.size - 1) {
                    DateHeader(formattedDate)
                }
            }
        }

        LaunchedEffect(messages) {
            if (messages.isNotEmpty()) {
                val lastMessageType = messages.last().second.type
                if (lastMessageType == MessageType.Type.Sent || lastMessageType == MessageType.Type.System) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }
}


@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}


fun isLastInGroup(index: Int, messages: List<Pair<String, MessageType>>): Boolean {
    val reversedIndex = messages.size - 1 - index
    // Sprawdza tylko wiadomości typu Sent
    if (reversedIndex < 0 || reversedIndex >= messages.size) return false

    // Odczytaj obecny typ wiadomości i stan 'isSeen'
    val currentMessageType = messages[reversedIndex].second
    if (currentMessageType.type != MessageType.Type.Sent) return false // tylko dla wiadomości wysłanych

    // Sprawdź następne wiadomości
    val nextMessageType = messages.getOrNull(reversedIndex + 1)?.second
    return if (nextMessageType != null) {
        nextMessageType.type == MessageType.Type.Sent && !nextMessageType.isSeen && currentMessageType.isSeen || (nextMessageType.type != MessageType.Type.Sent)
    } else true
}


fun getMessagePadding(index: Int, messages: List<Pair<String, MessageType>>): PaddingValues {
    // Indeks wiadomości jest liczony od końca w przypadku odwróconej listy
    val reversedIndex = messages.size - 1 - index

    val currentType = messages[reversedIndex].second.type
    val isFirstMessage = reversedIndex == messages.size - 1
    val isLastMessage = reversedIndex == 0

    // Logika dla paddingu uwzględnia odwróconą kolejność
    var topPadding = if (!isFirstMessage && messages[reversedIndex + 1].second.type == currentType) 1.dp else 8.dp
    val bottomPadding = if (!isLastMessage && messages[reversedIndex - 1].second.type == currentType) 1.dp else 8.dp

    // Sprawdzamy różnicę czasu
    if (reversedIndex < messages.size - 1) {
        val currentTimestamp = messages[reversedIndex].second.timestamp
        val previousTimestamp = messages[reversedIndex + 1].second.timestamp

        // Obliczamy różnicę czasu w minutach
        val diffInMinutes = (previousTimestamp - currentTimestamp) / (1000 * 60)

        // Logujemy szczegóły
        Log.d("MessageList", "Current timestamp: $currentTimestamp, Previous timestamp: $previousTimestamp")
        Log.d("MessageList", "Difference in minutes: $diffInMinutes")

        if (diffInMinutes >= 2 && messages[reversedIndex - 1].second.type == currentType) {
            topPadding = 6.dp
            Log.d("MessageList", "Adding 5.dp padding due to time difference.")
        } else {
            Log.d("MessageList", "No additional padding required.")
        }
    }

    return PaddingValues(top = bottomPadding, bottom = topPadding)
}




@Preview(showBackground = true)
@Composable
fun MessageListPreview() {
    val messages = listOf(
        "Hello, how are you?" to MessageType(MessageType.Type.Received, isSeen = true, timestamp = 1698451200000L),
        "Hello, how are you?" to MessageType(MessageType.Type.Received, isSeen = true, timestamp = 1698451200000L),
        "I'm doing great, thanks!" to MessageType(MessageType.Type.Sent, isSeen = true, timestamp = 1698451200000L),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true, timestamp = 1698451200000L),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true, timestamp = 1698364800000L),
        "What about you?" to MessageType(MessageType.Type.Received, isSeen = true, timestamp = 1698364800000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true, timestamp = 1698364800000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true, timestamp = 1698278500000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true, timestamp = 1698278500000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = false, timestamp = 1698278500000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = false, timestamp = 1698278500000L),
        "System message: User exit the chat" to MessageType(MessageType.Type.System, timestamp = 1698278500000L),
    )

    MessageList(
        messages = messages,
        modifier = Modifier.padding(16.dp),
        messageFontSize = 14.sp
    )
}
