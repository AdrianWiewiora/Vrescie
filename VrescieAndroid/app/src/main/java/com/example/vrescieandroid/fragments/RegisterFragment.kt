package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var retypePasswordEditText: TextInputEditText
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = FirebaseAuth.getInstance()
        emailEditText = view.findViewById(R.id.emailEtRegister)
        passwordEditText = view.findViewById(R.id.passEtRegister)
        retypePasswordEditText = view.findViewById(R.id.rePassEtRegister)

        val buttonRegister = view.findViewById<Button>(R.id.nextBtn)
        buttonRegister.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val retypePassword = retypePasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
                Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            } else if (password != retypePassword) {
                Toast.makeText(requireContext(), "Hasła nie pasują do siebie", Toast.LENGTH_SHORT).show()
            } else {
                registerWithEmailAndPassword(email, password)
            }

        }

        return view
    }

    private fun registerWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Rejestracja udana
                    Log.d("Register", "Rejestracja udana")

                    // Logowanie po udanej rejestracji
                    loginWithEmailAndPassword(email, password)
                } else {
                    // Rejestracja nieudana, pobierz komunikat o błędzie i wyświetl go
                    val exception = task.exception
                    Log.e("Register", "Błąd rejestracji", exception)
                    Toast.makeText(requireContext(), "Błąd rejestracji: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("Login", "Logowanie udane")

                    // Pobierz zalogowanego użytkownika
                    val user: FirebaseUser? = auth.currentUser

                    // Dodaj rekord do bazy danych
                    user?.let { addUserToDatabase(it) }

                    findNavController().navigate(R.id.action_registerFragment_to_addNameFragment)
                } else {
                    val exception = task.exception
                    Log.e("Login", "Błąd logowania", exception)
                    Toast.makeText(requireContext(), "Błąd logowania: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDatabase(user: FirebaseUser) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.reference.child("user")

        // Tworzenie obiektu userMap z danymi do dodania
        val userMap = HashMap<String, Any>()
        userMap["id"] = user.uid
        userMap["e_mail"] = user.email.orEmpty()

        // Dodanie rekordu do bazy danych
        reference.child(user.uid).setValue(userMap)
    }


}