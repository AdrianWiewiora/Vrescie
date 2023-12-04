package com.example.vrescieandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class FirstLaunchActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private var currentStep = 0

    private val textArray = arrayOf("Nowi znajomi", "Nowi przyjaciele", "Nowa miłość", "Szczęśliwi")
    private val timerDuration = 1000L
    private val totalDuration = timerDuration * textArray.size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_launch)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        // Rozpocznij animację
        startAnimation()
    }

    private fun startAnimation() {
        object : CountDownTimer(totalDuration, timerDuration) {
            override fun onTick(millisUntilFinished: Long) {
                // Zmiana tekstu co sekundę
                textView.text = textArray[currentStep]

                // Przesunięcie do następnego kroku
                currentStep++
            }

            override fun onFinish() {
                // Po zakończeniu animacji przejdź do activity_main.xml
                val intent = Intent(this@FirstLaunchActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }
}