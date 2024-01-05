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
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatNameTextView: TextView = itemView.findViewById(R.id.chatNameTextView)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyTextView: TextView = itemView.findViewById(R.id.emptyTextView)
    }

    private val VIEW_TYPE_CHAT = 1
    private val VIEW_TYPE_EMPTY = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CHAT -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat, parent, false)
                ChatViewHolder(itemView)
            }
            VIEW_TYPE_EMPTY -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_empty_view, parent, false)
                EmptyViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ChatViewHolder -> {
                val chat = chatList[position]
                val otherMemberName = getOtherMemberName(chat)

                holder.chatNameTextView.text = otherMemberName
                holder.lastMessageTextView.text = chat.lastMessage ?: "No messages"

                holder.itemView.setOnClickListener {
                    val args = Bundle()
                    args.putString("conversationId", chat.conversationId)

                    navController.navigate(R.id.action_mainMenu_to_implicitConversationFragment, args)
                }
            }
            is EmptyViewHolder -> {
                //holder.emptyTextView.text = "Jeszcze nic tu nie ma :( ... Postaraj się o polubienia innych użytkowników w anonimowym czacie by pojawiły się tu konwersacje"
            }
        }

    }

    override fun getItemCount(): Int {
        return if (chatList.isEmpty()) {
            1
        } else {
            chatList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_CHAT
        }
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


