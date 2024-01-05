package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vrescieandroid.ProfileAdapter
import com.example.vrescieandroid.R
import com.example.vrescieandroid.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: ProfileAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        recyclerView = view.findViewById(R.id.profileRecyclerView)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adapter = ProfileAdapter(emptyList())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val userReference = database.getReference("/user/$userId")

            // Dodajmy log przed próbą pobrania danych
            Log.d("ProfileFragment", "Attempting to retrieve user data for user ID: $userId")

            userReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserProfile::class.java)
                        userProfile?.let {
                            val profiles = listOf(it)
                            adapter.updateData(profiles)
                            Log.d("ProfileFragment", "Data loaded successfully")
                        }
                    } else {
                        // Dodajmy więcej informacji do logów w przypadku braku danych
                        Log.d("ProfileFragment", "User data not found in the database for user ID: $userId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to get user data: ${error.message}")
                }
            })
        } else {
            Log.d("ProfileFragment", "User not logged in")
        }

        return view
    }
}
