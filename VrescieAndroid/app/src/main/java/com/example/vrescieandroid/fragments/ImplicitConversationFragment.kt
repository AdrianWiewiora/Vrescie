package com.example.vrescieandroid.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R


class ImplicitConversationFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_implicit_conversation, container, false)

        val conversationId = arguments?.getString("conversationId")

        // Pobierz referencję do TextView1
        val textView1: TextView = view.findViewById(R.id.textView1)

        // Wyświetl conversationId w TextView1
        textView1.setText("Conversation ID: $conversationId")

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = view?.findNavController()
            navController?.let {
                val args = Bundle()
                args.putString("chooseFragment", "2")
                findNavController().navigate(R.id.action_implicitConversationFragment_to_mainMenu, args)
            }
        }

        return view
    }


}