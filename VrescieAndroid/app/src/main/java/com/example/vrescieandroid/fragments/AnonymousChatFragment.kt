package com.example.vrescieandroid.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.RangeSlider


class AnonymousChatFragment : Fragment() {

    private lateinit var buttonRandom: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anonymous_chat, container, false)

        buttonRandom = view.findViewById(R.id.random)

        // Inicjalizacja SharedPreferences
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        // Pobranie wartości z SharedPreferences i ustawienie na widokach
        val checkBoxFemale = view.findViewById<CheckBox>(R.id.checkBoxFemale)
        val checkBoxMale = view.findViewById<CheckBox>(R.id.checkBoxMale)
        val rangeSliderMaxAge = view.findViewById<RangeSlider>(R.id.rangeSliderMaxAge)
        val radioButtonRequireVerification = view.findViewById<RadioButton>(R.id.radioButtonRequireVerification)
        val radioButtonNoVerification = view.findViewById<RadioButton>(R.id.radioButtonNoVerification)
        val radioButtonStableRelationship = view.findViewById<RadioButton>(R.id.radioButtonStableRelationship)
        val radioButtonNoStableRelationship = view.findViewById<RadioButton>(R.id.radioButtonNoStableRelationship)

        checkBoxFemale.isChecked = sharedPreferences.getBoolean("femaleChecked", false)
        checkBoxMale.isChecked = sharedPreferences.getBoolean("maleChecked", false)
        rangeSliderMaxAge.values = mutableListOf(
            sharedPreferences.getFloat("minAge", 18f),
            sharedPreferences.getFloat("maxAge", 70f)
        )
        radioButtonRequireVerification.isChecked = sharedPreferences.getBoolean("requireVerificationChecked", false)
        radioButtonNoVerification.isChecked = sharedPreferences.getBoolean("noVerificationChecked", false)
        radioButtonStableRelationship.isChecked = sharedPreferences.getBoolean("stableRelationshipChecked", false)
        radioButtonNoStableRelationship.isChecked = sharedPreferences.getBoolean("noStableRelationshipChecked", false)

        buttonRandom.setOnClickListener {
            savePreferences()
            findNavController().navigate(R.id.action_mainMenu_to_anonymousChatLoadingFragment)
        }

        return view
    }

    fun savePreferences(){
        // Pobranie wartości z SharedPreferences i ustawienie na widokach
        val checkBoxFemale = view?.findViewById<CheckBox>(R.id.checkBoxFemale)
        val checkBoxMale = view?.findViewById<CheckBox>(R.id.checkBoxMale)
        val rangeSliderMaxAge = view?.findViewById<RangeSlider>(R.id.rangeSliderMaxAge)
        val radioButtonRequireVerification = view?.findViewById<RadioButton>(R.id.radioButtonRequireVerification)
        val radioButtonNoVerification = view?.findViewById<RadioButton>(R.id.radioButtonNoVerification)
        val radioButtonStableRelationship = view?.findViewById<RadioButton>(R.id.radioButtonStableRelationship)
        val radioButtonNoStableRelationship = view?.findViewById<RadioButton>(R.id.radioButtonNoStableRelationship)

        // Zapisanie wartości do SharedPreferences przed opuszczeniem fragmentu
        with(sharedPreferences.edit()) {
            if (checkBoxFemale != null) {
                putBoolean("femaleChecked", checkBoxFemale.isChecked)
            }
            if (checkBoxMale != null) {
                putBoolean("maleChecked", checkBoxMale.isChecked)
            }
            if (rangeSliderMaxAge != null) {
                putFloat("minAge", rangeSliderMaxAge.values[0])
            }
            if (rangeSliderMaxAge != null) {
                putFloat("maxAge", rangeSliderMaxAge.values[1])
            }
            if (radioButtonRequireVerification != null) {
                putBoolean("requireVerificationChecked", radioButtonRequireVerification.isChecked)
            }
            if (radioButtonNoVerification != null) {
                putBoolean("noVerificationChecked", radioButtonNoVerification.isChecked)
            }
            if (radioButtonStableRelationship != null) {
                putBoolean("stableRelationshipChecked", radioButtonStableRelationship.isChecked)
            }
            if (radioButtonNoStableRelationship != null) {
                putBoolean("noStableRelationshipChecked", radioButtonNoStableRelationship.isChecked)
            }
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        savePreferences()
    }

}