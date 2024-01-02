package com.example.vrescieandroid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.MessagesAdapter
import com.example.vrescieandroid.R
import com.example.vrescieandroid.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// ConversationFragment.kt

class ConversationFragment : Fragment() {

    private lateinit var userId1: String
    private lateinit var userId2: String

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSendMessage: Button

    private val messagesList = mutableListOf<Message>()
    private lateinit var messagesAdapter: MessagesAdapter

    private lateinit var messagesRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    companion object {
        fun newInstance(userId1: String, userId2: String): ConversationFragment {
            val fragment = ConversationFragment()
            val args = Bundle()
            args.putString("userId1", userId1)
            args.putString("userId2", userId2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conversation, container, false)

        userId1 = arguments?.getString("userId1") ?: ""
        userId2 = arguments?.getString("userId2") ?: ""

        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSendMessage = view.findViewById(R.id.buttonSendMessage)

        messagesAdapter = MessagesAdapter(messagesList)
        recyclerView.adapter = messagesAdapter
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        messagesRef = FirebaseDatabase.getInstance().reference.child("conversations")
            .child(getConversationId(userId1, userId2))
            .child("messages")

        buttonSendMessage.setOnClickListener {
            sendMessage()
        }

        // Inicjalizuj nasłuchiwanie na nowe wiadomości
        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    messagesList.add(it)
                    messagesAdapter.notifyDataSetChanged()
                    scrollToBottom()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługa zmian w wiadomościach (opcjonalne)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Obsługa usunięcia wiadomości (opcjonalne)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługa przemieszczenia wiadomości (opcjonalne)
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów
            }
        })

        val conversationId = getConversationId(userId1, userId2)
        val conversationsRef = FirebaseDatabase.getInstance().reference.child("conversations")
        val conversationRef = conversationsRef.child(conversationId)
        val membersRef = conversationRef.child("members")

        // Dodaj nasłuchiwanie zmian w members
        membersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługa dodania nowego elementu (opcjonalne)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key
                val isConnected = snapshot.getValue(Boolean::class.java)

                if (userId != null && !isConnected!!) {
                    // Użytkownik się rozłączył, dodaj wiadomość o rozłączeniu
                    val disconnectedMessage = Message(
                        "system",
                        "Użytkownik się rozłączył",
                        System.currentTimeMillis()
                    )
                    messagesRef.push().setValue(disconnectedMessage)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Obsługa usunięcia elementu (opcjonalne)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Obsługa przemieszczenia elementu (opcjonalne)
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów
            }
        })

        return view
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val message = Message(auth.currentUser?.uid ?: "", messageText, currentTime)

            messagesRef.push().setValue(message)
                .addOnSuccessListener {
                    editTextMessage.text.clear()
                }
                .addOnFailureListener {
                    // Obsługa błędów
                }
        }
    }

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(messagesAdapter.itemCount - 1)
    }

    private fun getConversationId(userId1: String, userId2: String): String {
        // Wygeneruj unikalny identyfikator konwersacji na podstawie id użytkowników
        val users = listOf(userId1, userId2).sorted()
        return "${users[0]}_${users[1]}"
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Usuń bieżącego użytkownika z members
        val conversationRef = FirebaseDatabase.getInstance().reference.child("conversations")
            .child(getConversationId(userId1, userId2))

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val membersRef = conversationRef.child("members")
            membersRef.child(currentUser.uid).setValue(false)
        }
    }
}

