package com.example.vrescieandroid

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.vrescieandroid.databinding.ActivityConversationBinding
import com.example.vrescieandroid.fragments.ConversationFragment

class ConversationActivity : AppCompatActivity() {

    private lateinit var userId1: String
    private lateinit var userId2: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_conversation)

        val container = findViewById<FrameLayout>(R.id.content)
        Log.d("FragmentContainer", "Container ID: $container")

        userId1 = intent.getStringExtra("userId1") ?: ""
        userId2 = intent.getStringExtra("userId2") ?: ""

        val conversationFragment = ConversationFragment.newInstance(userId1, userId2)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, conversationFragment) // Zmiana identyfikatora na R.id.content
            .commit()
    }
}