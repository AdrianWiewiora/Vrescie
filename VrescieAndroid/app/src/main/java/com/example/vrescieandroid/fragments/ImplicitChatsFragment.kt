package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.ChatsAdapter
import com.example.vrescieandroid.R
import com.example.vrescieandroid.data.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class ImplicitChatsFragment : Fragment() {

    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var currentUserUid: String
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_implicit_chats, container, false)

        // Pobierz UID bieżącego użytkownika (przykład użycia Firebase Authentication)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        currentUserUid = firebaseUser?.uid.orEmpty()

        navController = findNavController()
        // Ustaw adapter z pustą listą chatów na początku
        chatsAdapter = ChatsAdapter(emptyList(), currentUserUid, navController)
        val recyclerView: RecyclerView = view.findViewById(R.id.chatsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatsAdapter

        // Pobierz listę chatów z Firebase Realtime Database
        val databaseReference = FirebaseDatabase.getInstance().getReference("/explicit_conversations")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<Chat>()

                for (chatSnapshot in snapshot.children) {
                    val conversationId = chatSnapshot.key.orEmpty()

                    val members = chatSnapshot.child("members").children.map { memberSnapshot ->
                        memberSnapshot.key.orEmpty() to memberSnapshot.value.toString()
                    }

                    val memberNames = members.map { it.second }

                    val lastMessage = chatSnapshot.child("messages").children.lastOrNull()?.child("text")?.value?.toString()

                    val chat = Chat(conversationId, members.map { it.first }, memberNames, lastMessage)
                    chatList.add(chat)
                }

                // Uaktualnij dane w adapterze
                chatsAdapter.updateData(chatList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłuż błąd odczytu z bazy danych
            }
        })

        return view
    }

    companion object {
        private const val TAG = "ImplicitChatsFragment"
    }
}