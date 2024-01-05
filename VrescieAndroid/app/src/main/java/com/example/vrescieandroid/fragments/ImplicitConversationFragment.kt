package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
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


class ImplicitConversationFragment : Fragment() {

    private lateinit var conversationId: String

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSendMessage: ImageView
    private lateinit var buttonAddPhoto: ImageView
    private lateinit var buttonVideoCall: ImageView
    private lateinit var buttonProfile: ImageView

    private val messagesList = mutableListOf<Message>()
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var messagesRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_implicit_conversation, container, false)

        conversationId = arguments?.getString("conversationId").toString()

        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSendMessage = view.findViewById(R.id.buttonSendMessage)
        buttonAddPhoto = view.findViewById(R.id.buttonPhoto)
        buttonVideoCall = view.findViewById(R.id.buttonCall)
        buttonProfile = view.findViewById(R.id.profilePerson)

        messagesAdapter = MessagesAdapter(messagesList)
        recyclerView.adapter = messagesAdapter
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager


        messagesRef = FirebaseDatabase.getInstance().reference.child("explicit_conversations")
            .child(conversationId)
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToMainMenuFragment()
        }


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

    private fun navigateToMainMenuFragment() {
        val navController = view?.findNavController()
        navController?.let {
            val args = Bundle()
            args.putString("chooseFragment", "2")
            findNavController().navigate(R.id.action_implicitConversationFragment_to_mainMenu, args)
        }
    }


}