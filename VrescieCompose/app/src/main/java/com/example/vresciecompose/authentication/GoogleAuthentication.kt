package com.example.vresciecompose.authentication

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.vresciecompose.R
import com.example.vresciecompose.data.SignInResult
import com.example.vresciecompose.data.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthentication(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth
    private val database = FirebaseDatabase.getInstance()

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        }
        catch(e: Exception){
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val authResult = auth.signInWithCredential(googleCredentials).await()
            val isNewAccount = authResult.additionalUserInfo?.isNewUser ?: false
            val user = authResult.user

            // Wyodrębnianie tylko imienia użytkownika z pola displayName
            val nameParts = user?.displayName?.split(" ")
            val firstName = nameParts?.firstOrNull() ?: ""

            // Dodawanie danych użytkownika do bazy danych Firebase
            val userId = user?.uid ?: ""
            val userEmail = user?.email ?: ""
            val userRef = database.getReference("user").child(userId)
            userRef.child("email").setValue(userEmail)
            userRef.child("name").setValue(firstName)

            if (isNewAccount) {
                userRef.child("profileConfigured").setValue(false) // Ustawiamy na false
            }

            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = firstName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null,
                isNewAccount = isNewAccount
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message,
                isNewAccount = false
            )
        }
    }



    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? {
        val user = auth.currentUser
        val username = user?.displayName?.split(" ")?.firstOrNull() ?: ""
        return user?.run {
            UserData(
                userId = uid,
                username = username,
                profilePictureUrl = photoUrl?.toString()
            )
        }
    }


    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

}