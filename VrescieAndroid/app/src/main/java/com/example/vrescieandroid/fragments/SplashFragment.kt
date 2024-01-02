package com.example.vrescieandroid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R
import com.google.firebase.auth.FirebaseAuth


class SplashFragment : Fragment() {

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        val isFirstRun = requireActivity().getSharedPreferences("PREFS_NAME", 0)
            .getBoolean("isFirstRun", true)                         //Checking first run
        val currentUser = FirebaseAuth.getInstance().currentUser    //Get current user

        progressBar = view.findViewById(R.id.progressBar3)
        progressBar.visibility = View.VISIBLE

        view?.postDelayed({
            progressBar.visibility = View.GONE

            if (currentUser != null) {
                findNavController().navigate(R.id.action_splashFragment_to_anonymousChatFragment)
            } else {
                if (isFirstRun){
                    requireActivity().getSharedPreferences("PREFS_NAME", 0)
                        .edit()
                        .putBoolean("isFirstRun", false)
                        .apply()
                    findNavController().navigate(R.id.action_splashFragment_to_firstLaunchSplashFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_startFragment)
                }
            }
        }, 1000)

        return view
    }

}