package com.example.vrescieandroid

// MessagesAdapter.kt

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.data.Message
import com.google.firebase.auth.FirebaseAuth

class MessagesAdapter(private val messagesList: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val VIEW_TYPE_SYSTEM = 3

    private val auth = FirebaseAuth.getInstance()

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textViewSentMessage)
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textViewReceivedMessage)
    }

    inner class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textViewSystemMessage)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_sent_message, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_received_message, parent, false)
                ReceivedMessageViewHolder(view)
            }
            VIEW_TYPE_SYSTEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_system_message, parent, false)
                SystemMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messagesList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> {
                val sentHolder = holder as SentMessageViewHolder
                sentHolder.messageText.text = message.text
            }
            VIEW_TYPE_RECEIVED -> {
                val receivedHolder = holder as ReceivedMessageViewHolder
                receivedHolder.messageText.text = message.text
            }
            VIEW_TYPE_SYSTEM -> {
                val systemHolder = holder as SystemMessageViewHolder
                systemHolder.messageText.text = message.text
            }
        }
    }



    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messagesList[position]
        return when {
            message.senderId == auth.currentUser?.uid -> VIEW_TYPE_SENT
            message.senderId == "system" -> VIEW_TYPE_SYSTEM
            else -> VIEW_TYPE_RECEIVED
        }
    }

}


