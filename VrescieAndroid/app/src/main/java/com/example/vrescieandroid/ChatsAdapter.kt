package com.example.vrescieandroid

// ChatsAdapter.kt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.R
import com.example.vrescieandroid.data.Chat

class ChatsAdapter(
    private var chatList: List<Chat>,
    private val currentUserUid: String,
    private val navController: NavController
) :
    RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatNameTextView: TextView = itemView.findViewById(R.id.chatNameTextView)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
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

        holder.chatNameTextView.text = otherMemberName
        holder.lastMessageTextView.text = chat.lastMessage ?: "No messages"

        holder.itemView.setOnClickListener {
            val args = Bundle()
            args.putString("conversationId", chat.conversationId)

            navController.navigate(R.id.action_mainMenu_to_implicitConversationFragment, args)
        }

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

    fun updateData(newChatList: List<Chat>) {
        chatList = newChatList
        notifyDataSetChanged()
    }
}


