package com.example.vrescieandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vrescieandroid.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    // Dodaj zmienne flagowe do śledzenia stanu połączenia
    private var isConnected = false
    private val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        .child("users")
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Sprawdź, czy to pierwsze uruchomienie
        val isFirstRun = getSharedPreferences("PREFS_NAME", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        if (isFirstRun) {
            // Jeśli to pierwsze uruchomienie, przejdź do FirstLaunchActivity
            getSharedPreferences("PREFS_NAME", MODE_PRIVATE)
                .edit()
                .putBoolean("isFirstRun", false)
                .apply()

            val intent = Intent(this@MainActivity, FirstLaunchActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            // Sprawdzanie, czy użytkownik jest już zalogowany
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                Log.d("currentUser", "currentUser: $currentUser")
                setContentView(R.layout.activity_main)
            } else {
                val intentB = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intentB)
                finish()

            }


            // Dodaj obsługę przycisków
            val btnRandom = findViewById<Button>(R.id.btnRandom)
            val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
            val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
            val btnSendMessage = findViewById<Button>(R.id.btnSendMessage)

            // Inicjalizuj stan widoczności elementów interfejsu użytkownika
            updateUI()

            // Obsługa przycisku losowania
            btnRandom.setOnClickListener {
                val user = auth.currentUser
                val uid = user?.uid
                val email = user?.email
                Log.d("Firebase", "userId: $uid")
                Log.d("Firebase", "usersRef: $usersRef")

                // Tworzenie obiektu User
                val currentUser = uid?.let { it1 -> User(it1, email ?: "") }

                if (uid != null) {
                    val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
                    usersRef.child(uid).setValue(currentUser)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Pomyślnie dodano do bazy danych")
                            Toast.makeText(this@MainActivity, "Dodano do bazy danych", Toast.LENGTH_SHORT).show()


                            val intentC = Intent(this@MainActivity, Chat::class.java)
                            startActivity(intentC)
                            finish()

                        }
                        .addOnFailureListener {
                            Log.e("Firebase", "Błąd: ${it.message}")
                            Toast.makeText(this@MainActivity, "Błąd: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            // Obsługa przycisku rozłączania
            btnDisconnect.setOnClickListener {
                // Tutaj dodaj kod do obsługi rozłączania
                // Po rozłączeniu ustaw isConnected na false
                isConnected = false
                updateUI()
            }

            // Obsługa przycisku wysyłania wiadomości
            btnSendMessage.setOnClickListener {
                // Tutaj dodaj kod do wysyłania wiadomości
                // Jeśli isConnected jest true, to można wysłać wiadomość
                if (isConnected) {
                    val message = editTextMessage.text.toString()
                    // Tutaj dodaj kod do obsługi wysyłania wiadomości
                    Toast.makeText(this@MainActivity, "Wysłano wiadomość: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Metoda do aktualizacji widoczności elementów interfejsu użytkownika w zależności od stanu połączenia
    private fun updateUI() {
        val btnRandom = findViewById<Button>(R.id.btnRandom)
        val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        val btnSendMessage = findViewById<Button>(R.id.btnSendMessage)

        // Aktualizuj widoczność przycisków w zależności od isConnected
        if (isConnected) {
            editTextMessage.visibility = View.VISIBLE
            btnSendMessage.visibility = View.VISIBLE
            btnRandom.visibility = View.GONE
            btnDisconnect.visibility = View.VISIBLE
        } else {
            editTextMessage.visibility = View.GONE
            btnSendMessage.visibility = View.GONE
            btnRandom.visibility = View.VISIBLE
            btnDisconnect.visibility = View.GONE
        }
    }
}
