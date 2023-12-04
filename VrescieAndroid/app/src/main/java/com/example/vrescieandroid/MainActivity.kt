package com.example.vrescieandroid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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
            setContentView(R.layout.activity_main)
        }
    }
}