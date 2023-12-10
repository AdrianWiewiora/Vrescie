package com.example.vrescieandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Chat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var usersList: MutableList<User>

    private lateinit var usersRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerView)
        usersList = mutableListOf()
        usersAdapter = UsersAdapter(usersList)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = usersAdapter

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
                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Obsługa gestów cofania
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Obsługa cofania do MainActivity
                val intent = Intent(this@Chat, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onDestroy() {
        super.onDestroy()
        val user = auth.currentUser
        val uid = user?.uid

        if (uid != null) {
            usersRef.child(uid).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this@Chat, "Usunięto", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@Chat, "Nie usuinieto", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                Log.d("ChatActivity", "Cofanie do MainActivity")
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}




