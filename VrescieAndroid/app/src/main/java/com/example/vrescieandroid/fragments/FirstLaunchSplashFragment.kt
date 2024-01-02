package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R


class FirstLaunchSplashFragment : Fragment() {

    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    private var currentStep = 0

    private val textArray = arrayOf("Nowi znajomi", "Nowi przyjaciele", "Nowa miłość", "Szczęśliwi")
    private val timerDuration = 2000L
    private val totalDuration = timerDuration * textArray.size

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_launch_splash, container, false)
        textView = view.findViewById(R.id.textViewSplashFragment)
        imageView = view.findViewById(R.id.imageViewSplashFragment)

        Handler(Looper.getMainLooper()).postDelayed({
            startAnimation()
        }, 500)

        return view
    }

    private fun startAnimation() {
        object : CountDownTimer(totalDuration, timerDuration) {
            override fun onTick(millisUntilFinished: Long) {
                textView.alpha = 0f
                textView.text = textArray[currentStep++]


                val isLastStep = currentStep == textArray.size

                textView.animate()
                    .alpha(1f)
                    .setDuration(timerDuration / 2)
                    .withEndAction {
                        textView.animate()
                            .alpha(0f)
                            .setDuration(timerDuration / 2)
                            .start()
                    }
                    .start()

                if (isLastStep) {
                    imageView.postDelayed({
                        imageView.animate()
                            .alpha(0f)
                            .setDuration(timerDuration / 2)
                            .start()
                    }, timerDuration / 2)
                }
            }

            override fun onFinish() {
                val action =
                    FirstLaunchSplashFragmentDirections.actionFirstLaunchSplashFragmentToStartFragment()
                findNavController().navigate(action)
            }
        }.start()
    }



}