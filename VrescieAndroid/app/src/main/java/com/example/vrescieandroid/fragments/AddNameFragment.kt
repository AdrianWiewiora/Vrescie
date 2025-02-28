package com.example.vrescieandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.vrescieandroid.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddNameFragment : Fragment() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_name, container, false)

        auth = FirebaseAuth.getInstance()
        nameEditText = view.findViewById(R.id.name)
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup)

        // W onCreateView() lub gdziekolwiek chcesz to zainicjować
        val ageSpinner: Spinner = view.findViewById(R.id.ageSpinner)

        // Przygotuj listę wieku od 16 do 120
        val ages = (16..120).map { it.toString() }

        // Utwórz adapter dla Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Przypisz adapter do Spinner
        ageSpinner.adapter = adapter

        val buttonNext = view.findViewById<Button>(R.id.nextBtn)
        buttonNext.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val selectedGenderId = genderRadioGroup.checkedRadioButtonId

            if (name.isEmpty() || selectedGenderId == -1) {
                Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            } else {
                val selectedGender = if (selectedGenderId == R.id.radioButtonFemale) "F" else "M"
                val selectedAge = ageSpinner.selectedItem.toString()
                saveUserDataToDatabase(name, selectedGender, selectedAge)
            }
        }

        return view
    }

    private fun saveUserDataToDatabase(name: String, gender: String, age: String) {
        val user: FirebaseUser? = auth.currentUser

        // Sprawdź, czy użytkownik jest zalogowany
        if (user != null) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val reference: DatabaseReference = database.reference.child("user").child(user.uid)

            // Aktualizuj rekord w bazie danych o nowe pola
            reference.updateChildren(
                mapOf(
                    "name" to name,
                    "gender" to gender,
                    "age" to age
                )
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val args = Bundle()
                    args.putString("chooseFragment", "1")
                    navController = findNavController()
                    navController.navigate(R.id.action_addNameFragment_to_mainMenu, args)
                } else {
                    Toast.makeText(requireContext(), "Błąd zapisu do bazy danych", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Użytkownik niezalogowany
            Toast.makeText(requireContext(), "Użytkownik niezalogowany", Toast.LENGTH_SHORT).show()
        }
    }
}
