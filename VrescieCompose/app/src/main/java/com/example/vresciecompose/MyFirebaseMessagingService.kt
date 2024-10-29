package com.example.vresciecompose

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Wywołane za każdym razem, gdy przychodzi nowe powiadomienie
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Wyciągnij dane powiadomienia
        remoteMessage.notification?.let {
            val title = it.title ?: "New Message"
            val body = it.body ?: ""

            // Sprawdź, czy aplikacja jest w foreground
            if (!isAppInForeground()) {
                showNotification(title, body)
            } else {
                Log.d("FCM", "App is in foreground, notification not shown")
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val CHANNEL_ID = "new_message_channel"

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.vrescie_logo_foreground_noti_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName

        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && processInfo.processName == packageName) {
                Log.d("FCM", "App is in foreground")
                return true
            }
        }

        Log.d("FCM", "App is in background")
        return false
    }


    override fun onNewToken(token: String) {
        Log.d("FCM", "New token generated: $token")
        // Tutaj możesz zaktualizować token na serwerze, jeśli tego potrzebujesz
        saveTokenToFirebase(token)
    }

    fun saveTokenToFirebase(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val databaseRef = FirebaseDatabase.getInstance().getReference("user/$userId")

            // Zapisz token jako child o nazwie 'fcmToken' w bazie
            databaseRef.child("fcmToken").setValue(token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token saved to Firebase")
                }
                .addOnFailureListener { e ->
                    Log.w("FCM", "Failed to save token", e)
                }
        } else {
            Log.w("FCM", "User not logged in, token not saved")
        }
    }

}
