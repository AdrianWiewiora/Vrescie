package com.example.vrescieandroid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R
import com.google.android.material.button.MaterialButton


class AnonymousChatFragment : Fragment() {

    private lateinit var buttonRandom: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anonymous_chat, container, false)

        buttonRandom = view.findViewById(R.id.random)

        buttonRandom.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_anonymousChatLoadingFragment)
        }

        return view
    }

}