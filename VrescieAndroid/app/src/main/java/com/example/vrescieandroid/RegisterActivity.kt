package com.example.vrescieandroid


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEt)
        val passwordEditText = findViewById<EditText>(R.id.passEt)
        val retypePasswordEditText = findViewById<EditText>(R.id.rePassEt)

        val btnRegister = findViewById<Button>(R.id.nextBtn)
        btnRegister.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val retypePassword = retypePasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
                Toast.makeText(this@RegisterActivity, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            } else if (password != retypePassword) {
                Toast.makeText(this@RegisterActivity, "Hasła nie pasują do siebie", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("Dupa", "Register start 1")
                registerWithEmailAndPassword(email, password)
            }
        }
    }

    private fun registerWithEmailAndPassword(email: String, password: String) {
        Log.d("Dupa", "Register start 2")
        val progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        progressBar.visibility = ProgressBar.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Dupa", "Register ok")

                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.d("Dupa", "Register not ok")
                    // Rejestracja nieudana
                    Toast.makeText(this@RegisterActivity, "Błąd rejestracji: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }

                progressBar.visibility = ProgressBar.INVISIBLE
            }

    }
}
