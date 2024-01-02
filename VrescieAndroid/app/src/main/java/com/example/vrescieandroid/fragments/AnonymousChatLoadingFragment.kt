package com.example.vrescieandroid.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.ConversationActivity
import com.example.vrescieandroid.R
import com.example.vrescieandroid.UsersAdapter
import com.example.vrescieandroid.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AnonymousChatLoadingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var usersList: MutableList<User>

    private lateinit var usersRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anonymous_chat_loading, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)

        if (recyclerView.adapter == null) {
            usersList = mutableListOf()
            usersAdapter = UsersAdapter(usersList)

            val layoutManager = LinearLayoutManager(requireContext())
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = usersAdapter
        }

        usersRef = FirebaseDatabase.getInstance().reference.child("users")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val user = User(currentUser.uid, currentUser.email.toString())
            usersRef.child(currentUser.uid).setValue(user)
        }

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        usersList.add(it)
                    }
                }
                usersAdapter.notifyDataSetChanged()

                // Dodaj logi, aby zobaczyć, czy lista użytkowników jest aktualizowana
                Log.d("AnonymousChatFragment", "Liczba użytkowników: ${usersList.size}")

                for (user in usersList) {
                    Log.d("AnonymousChatFragment", "UserID: ${user.userId}, Username: ${user.email}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("AnonymousChatFragment", "Wystąpił błąd w czasie pobierania danych: ${error.message}")
            }
        })

        usersAdapter.setOnItemClickListener(object : UsersAdapter.OnItemClickListener {
            override fun onItemClick(user: User) {
                startConversation(user.userId)
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersRef.child(currentUser.uid).removeValue()
                .addOnSuccessListener {
                    Log.d("AnonymousChatFragment", "Usunięto użytkownika z bazy danych")
                }
                .addOnFailureListener {
                    Log.e("AnonymousChatFragment", "Nie udało się usunąć użytkownika z bazy danych: ${it.message}")
                }
        }
    }

    private fun startConversation(otherUserId: String) {
        val navController = findNavController()

        val args = Bundle()
        args.putString("userId1", auth.currentUser?.uid)
        args.putString("userId2", otherUserId)

        navController.navigate(R.id.action_anonymousChatLoadingFragment_to_conversationFragment, args)
    }

}