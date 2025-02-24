package com.example.vresciecompose.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

                if (index > 0) {
                    val previousTimestamp = messages[messages.size - 1 - index + 1].second.timestamp
                    val previousDate = Date(previousTimestamp)

                    val calendar1 = Calendar.getInstance().apply { time = date }
                    val calendar2 = Calendar.getInstance().apply { time = previousDate }
                    val formattedPreviousDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(previousDate)

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
                        val cornerShape = getMessageCornerRadius(index, messages)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            ReceivedMessage(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .padding(horizontal = 8.dp),
                                message = message,
                                messageFontSize = messageFontSize,
                                showTime = showTime,
                                timestamp = timestamp,
                                cornerShape = cornerShape
                            )
                        }
                    }
                    MessageType.Type.Sent -> {
                        val cornerShape = getMessageCornerRadius(index, messages)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            SentMessage(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .padding(horizontal = 8.dp),
                                message = message,
                                isSeen = type.isSeen,
                                showIcon = isLastInGroup(index, messages),
                                messageFontSize = messageFontSize,
                                showTime = showTime,
                                timestamp = timestamp,
                                cornerShape = cornerShape
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

fun getMessageCornerRadius(
    index: Int,
    messages: List<Pair<String, MessageType>>
): RoundedCornerShape {
    val reversedIndex = messages.size - 1 - index
    val currentType = messages[reversedIndex].second.type

    var topLeftCorner = 20.dp
    var bottomLeftCorner = 20.dp
    var topRightCorner = 20.dp
    var bottomRightCorner = 20.dp
    val currentTimestamp = messages[reversedIndex].second.timestamp

    if (reversedIndex < messages.size - 1 && reversedIndex > 0) {
        val previousTimestamp = messages[reversedIndex + 1].second.timestamp
        val nextTimestamp = messages[reversedIndex - 1].second.timestamp

        // Obliczamy różnicę czasu w minutach
        val diffInMinutesPrev = (previousTimestamp - currentTimestamp) / (1000 * 60)
        val diffInMinutesNext = (currentTimestamp - nextTimestamp) / (1000 * 60)

        if (diffInMinutesPrev <= 2 && messages[reversedIndex + 1].second.type == currentType) {
            bottomLeftCorner = 10.dp
            bottomRightCorner = 10.dp
        }
        if (diffInMinutesNext <= 2 && messages[reversedIndex - 1].second.type == currentType) {
            topLeftCorner = 10.dp
            topRightCorner = 10.dp
        }
    }
    // Sprawdzamy, czy to jedyny element w liście
    if (messages.size == 1) {
        // Jeśli jest tylko jeden element, możemy dodać obie logiki
        val onlyMessageTimestamp = messages[0].second.timestamp
        val diffInMinutes = (currentTimestamp - onlyMessageTimestamp) / (1000 * 60)

        if (diffInMinutes <= 2) {
            topLeftCorner = 20.dp
            topRightCorner = 20.dp
            bottomLeftCorner = 20.dp
            bottomRightCorner = 20.dp
        }
    }

    // Sprawdzamy, czy to pierwszy element
    else if (reversedIndex == messages.size - 1) {
        val nextTimestamp = messages[reversedIndex - 1].second.timestamp
        val diffInMinutesNext = (currentTimestamp - nextTimestamp) / (1000 * 60)
        if (diffInMinutesNext <= 2 && messages[reversedIndex - 1].second.type == currentType) {
            topLeftCorner = 10.dp
            topRightCorner = 10.dp
        }
    }

    // Sprawdzamy, czy to ostatni element
    else if (reversedIndex == 0) {
        val previousTimestamp = messages[reversedIndex + 1].second.timestamp
        val diffInMinutesPrev = (previousTimestamp - currentTimestamp) / (1000 * 60)
        if (diffInMinutesPrev <= 2 && messages[reversedIndex + 1].second.type == currentType) {
            bottomLeftCorner = 10.dp
            bottomRightCorner = 10.dp
        }
    }

    return when (currentType) {
        MessageType.Type.Received -> RoundedCornerShape(
            topStart = topLeftCorner, bottomStart = bottomLeftCorner,
            topEnd = 20.dp, bottomEnd = 20.dp
        )
        MessageType.Type.Sent -> RoundedCornerShape(
            topStart = 20.dp, bottomStart = 20.dp,
            topEnd = topRightCorner, bottomEnd = bottomRightCorner
        )
        else -> RoundedCornerShape(20.dp) // Domyślne dla innych typów wiadomości
    }
}


fun isLastInGroup(index: Int, messages: List<Pair<String, MessageType>>): Boolean {
    val reversedIndex = messages.size - 1 - index
    if (reversedIndex < 0 || reversedIndex >= messages.size) return false
    val currentMessageType = messages[reversedIndex].second
    if (currentMessageType.type != MessageType.Type.Sent) return false
    val nextMessageType = messages.getOrNull(reversedIndex + 1)?.second
    return if (nextMessageType != null) {
        nextMessageType.type == MessageType.Type.Sent && !nextMessageType.isSeen
                && currentMessageType.isSeen || (nextMessageType.type != MessageType.Type.Sent)
    } else true
}


fun getMessagePadding(index: Int, messages: List<Pair<String, MessageType>>): PaddingValues {
    val reversedIndex = messages.size - 1 - index
    val currentType = messages[reversedIndex].second.type
    val isFirstMessage = reversedIndex == messages.size - 1
    val isLastMessage = reversedIndex == 0
    var bottomPadding = if (!isFirstMessage && messages[reversedIndex + 1].second.type == currentType) 1.dp else 8.dp
    val topPadding = if (!isLastMessage && messages[reversedIndex - 1].second.type == currentType) 1.dp else 8.dp
    if (reversedIndex < messages.size - 1) {
        val currentTimestamp = messages[reversedIndex].second.timestamp
        val previousTimestamp = messages[reversedIndex + 1].second.timestamp
        val diffInMinutes = (previousTimestamp - currentTimestamp) / (1000 * 60)
        if (diffInMinutes >= 2 && reversedIndex > 0 && messages[reversedIndex - 1].second.type == currentType) {
            bottomPadding = 6.dp
        }
    }
    return PaddingValues(top = topPadding, bottom = bottomPadding)
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
        "What about you  sdf s sd sdf sd ?" to MessageType(MessageType.Type.Received, isSeen = true, timestamp = 1698364800000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true, timestamp = 1698364800000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true, timestamp = 1698278500000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = true, timestamp = 1698278500000L),
        "I'm fine too, thank you!" to MessageType(MessageType.Type.Sent, isSeen = false, timestamp = 1698278500000L),
        "I'm fine too, thank you sdf sd sdfsdf sd fsd fsd fs!" to MessageType(MessageType.Type.Sent, isSeen = false, timestamp = 1698278500000L),
        "System message: User exit the chat" to MessageType(MessageType.Type.System, timestamp = 1698278500000L),
    )

    MessageList(
        messages = messages,
        modifier = Modifier.padding(16.dp),
        messageFontSize = 14.sp
    )
}
