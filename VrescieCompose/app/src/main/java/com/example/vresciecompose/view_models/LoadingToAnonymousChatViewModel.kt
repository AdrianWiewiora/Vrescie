package com.example.vresciecompose.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vresciecompose.Navigation
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Timer
import java.util.TimerTask

class LoadingToAnonymousChatViewModel : ViewModel() {

    private val _navigateToConversation = MutableLiveData<String?>()
    val navigateToConversation: LiveData<String?> = _navigateToConversation

    fun removeUserFromFirebaseDatabase(userId: String) {
        val database = com.google.firebase.ktx.Firebase.database
        val usersRef = database.getReference("vChatUsers")
        usersRef.child(userId).removeValue()
    }

    fun updateUserLastSeen(userId: String) {
        val database = com.google.firebase.ktx.Firebase.database
        val usersRef = database.getReference("vChatUsers")
        val currentTime = System.currentTimeMillis()
        usersRef.child(userId).child("info").child("lastSeen").setValue(currentTime)
    }

    fun listenForNewConversations(userId: String) {
        val database = Firebase.database
        val conversationRef = database.reference.child("conversations")

        conversationRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val conversationData = snapshot.value as? Map<*, *>
                if (conversationData != null && userId in (conversationData["members"] as? Map<*, *> ?: emptyMap<String, Any>())) {
                    val canConnected = conversationData["canConnected"] as? Boolean
                    val conversationID = snapshot.key
                    if (canConnected == true) {
                        _navigateToConversation.postValue(conversationID)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun resetNavigation() {
        _navigateToConversation.value = null
    }
}
