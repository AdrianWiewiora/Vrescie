package com.example.vrescieandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.data.Message

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContent: TextView = itemView.findViewById(R.id.messageContent)
        val messageSender: TextView = itemView.findViewById(R.id.messageSender)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.messageContent.text = message.content
        holder.messageSender.text = message.senderId
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
