package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R
import com.example.vrescieandroid.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener


class AnonymousChatLoadingFragment : Fragment() {

    private lateinit var usersList: MutableList<User>
    private var isUserActive = false

    private lateinit var usersRef: DatabaseReference
    private lateinit var conversationsRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    private lateinit var userRef: DatabaseReference
    private var userAge: String? = null
    private var userGender: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anonymous_chat_loading, container, false)

        usersList = mutableListOf()

        // Obserwuj zmiany w węźle konwersacji
        val conversationsRef = FirebaseDatabase.getInstance().reference.child("conversations")
        conversationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Nowy rekord został dodany, sprawdzamy konwersacje

                // Wydrukuj informacje o dodanym elemencie
                val addedItemId = snapshot.key
                val addedItemValue = snapshot.value

                Log.d("AnonymousChatFragment", "Nowy rekord dodany. ID: $addedItemId, Wartość: $addedItemValue")
                val currentUser = auth.currentUser

                // Sprawdź, czy currentUser.uid jest jednym z uczestników nowej konwersacji
                val ids = addedItemId?.split("_")
                if (currentUser != null) {
                    if (ids != null && ids.size == 2 && (ids[0] == currentUser.uid || ids[1] == currentUser.uid)) {
                        Log.d("AnonymousChatFragment", "Jesteś jednym z uczestników nowej konwersacji. ID: $addedItemId")

                        // Przypisz drugie ID (otherId) do zmiennej
                        val otherId = if (ids[0] == currentUser.uid) ids[1] else ids[0]

                        // Sprawdź, czy konwersacja ma parametr canConnected ustawiony na true
                        val canConnected = snapshot.child("canConnected").getValue(Boolean::class.java)

                        if (canConnected == true) {
                            Log.d("AnonymousChatFragment", "Jesteś jednym z uczestników nowej konwersacji. ID: $addedItemId")

                            updateHandler.removeCallbacks(updateRunnable)
                            startConversation(otherId)
                        } else {
                            Log.d("AnonymousChatFragment", "Konwersacja istnieje, ale nie można dołączyć (canConnected != true).")
                        }

                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Zmieniono rekord, jeśli to konieczne, obsłuż zmianę
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Usunięto rekord, jeśli to konieczne, obsłuż usunięcie
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Przeniesiono rekord, jeśli to konieczne, obsłuż przeniesienie
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu
                Log.e("AnonymousChatFragment", "Wystąpił błąd w czasie pobierania danych: ${error.message}")
            }
        })

        usersRef = FirebaseDatabase.getInstance().reference.child("users")


        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        usersList.add(it)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("AnonymousChatFragment", "Wystąpił błąd w czasie pobierania danych: ${error.message}")
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = view?.findNavController()
            navController?.let {
                val args = Bundle()
                args.putString("chooseFragment", "1")
                findNavController().navigate(R.id.action_anonymousChatLoadingFragment_to_mainMenu, args)
            }
        }

        connectUserToDatabase()

        return view
    }

    private val updateHandler = Handler(Looper.myLooper()!!)
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isUserActive) {
                updateLastSeenTime()
            }
            updateHandler.postDelayed(this, 5000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateHandler.removeCallbacks(updateRunnable)
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
        val navController = view?.findNavController()

        // Czyść back stack do określonego fragmentu (jeśli istnieje)
        navController?.popBackStack(R.id.anonymousChatLoadingFragment, false)

        // Nawiguj do nowego fragmentu
        val args = Bundle()
        args.putString("userId1", auth.currentUser?.uid)
        args.putString("userId2", otherUserId)

        Log.d("AnonymousChatFragment", "startConversation 1: $otherUserId")

        navController?.navigate(R.id.action_anonymousChatLoadingFragment_to_conversationFragment, args)
    }

    private fun updateLastSeenTime() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersRef.child(currentUser.uid).child("lastSeen").setValue(ServerValue.TIMESTAMP)
        }
    }

    private fun connectUserToDatabase() {

        userRef = FirebaseDatabase.getInstance().reference.child("user")
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userRef.child(currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            userAge = user.age
                            userGender = user.gender

                            val user2 = User(
                                currentUser.uid,
                                currentUser.email.toString(),
                                userAge ?: "",
                                userGender ?: "",
                                ServerValue.TIMESTAMP
                            )
                            usersRef.child(currentUser.uid).setValue(user2)
                                .addOnSuccessListener {
                                    Log.d("AnonymousChatFragment", "Dodano użytkownika do bazy danych")
                                    isUserActive = true
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "AnonymousChatFragment",
                                        "Nie udało się dodać użytkownika do bazy danych: ${it.message}"
                                    )
                                }

                            updateHandler.postDelayed(updateRunnable, 5000)

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TwójFragment", "Błąd podczas pobierania danych użytkownika: ${error.message}")
                }
            })
        }
    }


}
