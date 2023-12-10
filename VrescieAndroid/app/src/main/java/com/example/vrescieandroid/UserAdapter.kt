package com.example.vrescieandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.data.User

class UsersAdapter(private val usersList: List<User>) :
    RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = usersList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userIdTextView: TextView = itemView.findViewById(R.id.userIdTextView)

        fun bind(user: User) {
            userIdTextView.text = "UserID: ${user.userId}"
        }
    }
}
