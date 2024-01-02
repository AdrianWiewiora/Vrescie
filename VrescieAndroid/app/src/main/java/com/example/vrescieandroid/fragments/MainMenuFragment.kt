package com.example.vrescieandroid.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R


class MainMenuFragment : Fragment() {

    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var imageViewAnonymousChat: ImageView
    private lateinit var imageViewImplicitChat: ImageView
    private lateinit var imageViewUser: ImageView
    private lateinit var imageViewAnonymousChatText: TextView
    private lateinit var imageViewImplicitChatText: TextView
    private lateinit var imageViewUserText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)

        imageViewAnonymousChat = view.findViewById(R.id.imageViewAnonymousChat)
        imageViewImplicitChat = view.findViewById(R.id.imageViewImplicitChat)
        imageViewUser = view.findViewById(R.id.imageViewUser)

        imageViewAnonymousChatText = view.findViewById(R.id.textViewAnonymousChat)
        imageViewImplicitChatText = view.findViewById(R.id.textViewImplicitChat)
        imageViewUserText = view.findViewById(R.id.textViewUser)

        imageViewAnonymousChat.setOnClickListener { onMenuItemClick(it) }
        imageViewImplicitChat.setOnClickListener { onMenuItemClick(it) }
        imageViewUser.setOnClickListener { onMenuItemClick(it) }

        onMenuItemClick(imageViewAnonymousChat)
        val color = ContextCompat.getColor(requireContext(), R.color.button_clicked_menu)
        imageViewAnonymousChatText.setTextColor(color)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPressed()
        }

        return view
    }

    private fun onMenuItemClick(clickedView: View) {
        clearHighlights()

        val defaultColor = ContextCompat.getColor(requireContext(), R.color.black)
        imageViewAnonymousChatText.setTextColor(defaultColor)
        imageViewImplicitChatText.setTextColor(defaultColor)
        imageViewUserText.setTextColor(defaultColor)

        val clickedColor = ContextCompat.getColor(requireContext(), R.color.button_clicked_menu)
        when (clickedView.id) {
            R.id.imageViewAnonymousChat -> {
                imageViewAnonymousChat.setImageResource(R.drawable.chat_bubble_question_purple)
                imageViewAnonymousChatText.setTextColor(clickedColor)
                navigateToFragment(AnonymousChatFragment())
            }
            R.id.imageViewImplicitChat -> {
                imageViewImplicitChat.setImageResource(R.drawable.chat_bubble_check_purple)
                imageViewImplicitChatText.setTextColor(clickedColor)
                navigateToFragment(ImplicitChatsFragment())
            }
            R.id.imageViewUser -> {
                imageViewUser.setImageResource(R.drawable.user_purple)
                imageViewUserText.setTextColor(clickedColor)
                navigateToFragment(ProfileFragment())
            }
        }

    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }

    private fun clearHighlights() {
        imageViewAnonymousChat.setImageResource(R.drawable.chat_bubble_question)
        imageViewImplicitChat.setImageResource(R.drawable.chat_bubble_check)
        imageViewUser.setImageResource(R.drawable.user)
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

