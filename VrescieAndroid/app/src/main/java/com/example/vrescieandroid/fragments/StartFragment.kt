package com.example.vrescieandroid.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R


class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        val buttonLogin = view.findViewById<Button>(R.id.account_login)
        val buttonRegister = view.findViewById<Button>(R.id.register)


        buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_loginFragment)
        }

        buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_registerFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPressed()
        }

        return view
    }

    private fun handleBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Potwierdź zamknięcie")
            .setMessage("Czy na pewno chcesz zamknąć aplikację?")
            .setPositiveButton("Tak") { _, _ ->
                requireActivity().finish()
            }
            .setNegativeButton("Nie", null)
            .show()
    }

}