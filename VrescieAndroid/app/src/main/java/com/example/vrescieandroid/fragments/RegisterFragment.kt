package com.example.vrescieandroid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val buttonRegister = view.findViewById<Button>(R.id.nextBtn)

        buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_anonymousChatFragment)
        }

        return view
    }

}