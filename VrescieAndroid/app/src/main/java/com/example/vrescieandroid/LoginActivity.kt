package com.example.vrescieandroid


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var passEt: EditText
    private lateinit var nextBtn: ImageView
    private lateinit var authTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var button: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEt = findViewById(R.id.emailEt)
        passEt = findViewById(R.id.passEt)
        nextBtn = findViewById(R.id.nextBtn)
        authTextView = findViewById(R.id.authTextView)
        progressBar = findViewById(R.id.progressBar)
        button = findViewById(R.id.button)

        firebaseAuth = FirebaseAuth.getInstance()

        nextBtn.setOnClickListener {
            loginUser()
        }

        authTextView.setOnClickListener {
            // Przejście do ekranu rejestracji
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        configureGoogleSignIn()

        button.setOnClickListener {
            // Logowanie za pomocą Google
            signInWithGoogle()
        }
    }

    private fun loginUser() {
        val email = emailEt.text.toString().trim()
        val password = passEt.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Wprowadź poprawne dane logowania.")
            return
        }

        progressBar.visibility = View.VISIBLE

        // Logowanie przy użyciu Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Logowanie udane
                    showToast("Logowanie udane.")
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Logowanie nieudane
                    showToast("Logowanie nieudane. Sprawdź poprawność danych.")
                }
            }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this) { connectionResult ->
                Log.d("GoogleSignIn", "Connection failed: $connectionResult")
                showToast("Google Sign-In failed. Try again later.")
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }
            if (result != null) {
                if (result.isSuccess) {
                    val account = result.signInAccount
                    firebaseAuthWithGoogle(account!!)
                } else {
                    showToast("Google Sign-In failed. Try again later.")
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Logowanie za pomocą Google udane
                    showToast("Logowanie za pomocą Google udane.")
                    // Tutaj możesz przekierować użytkownika do głównej aktywności lub innego ekranu
                } else {
                    // Logowanie za pomocą Google nieudane
                    showToast("Logowanie za pomocą Google nieudane.")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
