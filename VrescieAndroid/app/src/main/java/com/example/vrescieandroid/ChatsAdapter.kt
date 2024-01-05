package com.example.vrescieandroid

// ChatsAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.R
import com.example.vrescieandroid.data.Chat

class ChatsAdapter(private val chatList: List<Chat>, private val currentUserUid: String) :
    RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatNameTextView: TextView = itemView.findViewById(R.id.chatNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val otherMemberName = getOtherMemberName(chat)

        holder.chatNameTextView.text = otherMemberName

        // Dodaj dowolne inne operacje, takie jak obsługa kliknięcia itp.
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    private fun getOtherMemberName(chat: Chat): String {
        return if (chat.memberIds.size == 2) {
            val otherMemberId =
                if (chat.memberIds[0] == currentUserUid) chat.memberIds[1] else chat.memberIds[0]

            val otherMemberName =
                chat.memberNames.getOrNull(chat.memberIds.indexOf(otherMemberId)) ?: "Unknown"
            otherMemberName
        } else {
            "Group Chat"  // Możesz dostosować to w zależności od liczby członków konwersacji
        }
    }
}


