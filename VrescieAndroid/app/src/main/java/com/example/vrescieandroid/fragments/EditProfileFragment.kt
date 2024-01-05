package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("/user/${auth.currentUser?.uid}")

        val confirmButton = view.findViewById<Button>(R.id.confirm)
        val editTextName = view.findViewById<TextInputEditText>(R.id.editTextName)

        // Obsługa argumentu
        val userName = arguments?.getString("userName")
        editTextName.setText(userName)

        confirmButton.setOnClickListener {
            // Przyjmowanie nowego imienia z pola edycji
            val newUserName = editTextName.text.toString()

            // Aktualizacja imienia w bazie danych
            updateUserInDatabase(newUserName)

            val args = Bundle()
            args.putString("chooseFragment", "3")
            navController = findNavController()
            navController.navigate(R.id.action_editProfileFragment_to_mainMenu, args)
        }

        return view
    }

    private fun updateUserInDatabase(newUserName: String) {
        val currentUser = auth.currentUser
        currentUser?.let {

            // Tworzymy obiekt do aktualizacji danych
            val updatedUserData = mapOf(
                "name" to newUserName
                // Tutaj możesz dodać inne pola do aktualizacji
            )

            // Aktualizujemy dane w bazie danych
            userRef.updateChildren(updatedUserData)
                .addOnSuccessListener {
                    // Pomyślnie zaktualizowano dane
                    // Dodaj odpowiednie logi lub działania w przypadku sukcesu
                }
                .addOnFailureListener {
                    // Wystąpił błąd podczas aktualizacji danych
                    // Dodaj odpowiednie logi lub działania w przypadku niepowodzenia
                }
        }
    }
}