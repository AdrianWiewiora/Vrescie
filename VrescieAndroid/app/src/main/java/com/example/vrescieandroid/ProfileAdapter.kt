package com.example.vrescieandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.data.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileAdapter(private var profiles: List<UserProfile>) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {


    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val ageTextView: TextView = itemView.findViewById(R.id.ageTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val genderTextView: TextView = itemView.findViewById(R.id.genderTextView)
        val joinTimeTextView: TextView = itemView.findViewById(R.id.joinTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_item, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]

        holder.nameTextView.text = profile.name
        holder.ageTextView.text = profile.age
        holder.emailTextView.text = profile.e_mail
        if (profile.gender == "M") {
            holder.genderTextView.text = "Mężczyzna"
        } else {
            holder.genderTextView.text = "Kobieta"
        }

        val joinTimeFormatted = profile.join_time?.let { formatDate(it) }
        holder.joinTimeTextView.text = joinTimeFormatted

    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    fun updateData(newProfiles: List<UserProfile>) {
        profiles = newProfiles
        notifyDataSetChanged()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }
}
