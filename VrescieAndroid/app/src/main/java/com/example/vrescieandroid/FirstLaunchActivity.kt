package com.example.vrescieandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class FirstLaunchActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private var currentStep = 0

    private val textArray = arrayOf("Nowi znajomi", "Nowi przyjaciele", "Nowa miłość", "Szczęśliwi")
    private val timerDuration = 2000L
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
                // Efekt zanikania tekstu
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = timerDuration / 2
                fadeOut.fillAfter = true

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        // Zmiana tekstu po zakończeniu animacji
                        textView.text = textArray[currentStep]
                        currentStep++

                        // Efekt pojawiania się tekstu
                        val fadeIn = AlphaAnimation(0f, 1f)
                        fadeIn.duration = timerDuration / 2
                        fadeIn.fillAfter = true

                        fadeIn.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}

                            override fun onAnimationEnd(animation: Animation?) {
                                // Animacja zakończona
                            }

                            override fun onAnimationRepeat(animation: Animation?) {}
                        })

                        // Rozpoczęcie animacji pojawiania się tekstu
                        textView.startAnimation(fadeIn)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })

                // Rozpoczęcie animacji zanikania tekstu
                textView.startAnimation(fadeOut)
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