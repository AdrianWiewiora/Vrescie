package com.example.vresciecompose.screens

import LocalContext
import ProvideContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.view_models.LocationViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


private const val TAG = "AnonymousChatConfig"

@Composable
fun AnonymousChatConfigurationScreen(
    viewModel: LocationViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    onClick: (String) -> Unit
) {
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current
    val fusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(key1 = Unit) {
        viewModel.getLocation(
            fusedLocationProviderClient = fusedLocationProviderClient,
            context = context,
            requestPermissionLauncher = requestPermissionLauncher,
            onSuccess = { location ->
                latitude = location.latitude
                longitude = location.longitude
            },
            onFailure = {
                // Obsługa niepowodzenia
                Log.e("Location", "Failed to get location")
            }
        )
    }


    var selectedGenders by remember { mutableStateOf("FM") }

    var ageRange by remember { mutableStateOf(18f..100f) }
    val minAge by remember { mutableStateOf(18f) }
    val maxAge by remember { mutableStateOf(100f) }

    var isProfileVerified by remember { mutableStateOf(false) }

    var relationshipPreference by remember { mutableStateOf(true) }
    var maxDistance by remember { mutableStateOf(10f) }


    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
            ) {
            GenderSelectionRow(modifier = Modifier)

            Text(
                text = "Płeć:",
                fontSize = 16.sp,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedGenders.contains("F"),
                    onCheckedChange = {
                        selectedGenders = if (it) {
                            if (selectedGenders.contains("M")) "FM" else "F"
                        } else {
                            selectedGenders.replace("F", "")
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Kobieta"
                )
                Checkbox(
                    checked = selectedGenders.contains("M"),
                    onCheckedChange = {
                        selectedGenders = if (it) {
                            if (selectedGenders.contains("F")) "FM" else "M"
                        } else {
                            selectedGenders.replace("M", "")
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = "Mężczyzna",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Przedział wiekowy: ${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()} lat",
                fontSize = 16.sp,
            )
            RangeSlider(
                value = ageRange,
                onValueChange = {
                    when {
                        it.start == it.endInclusive -> {
                            if (it.start == minAge) {
                                ageRange =
                                    (it.start..(it.endInclusive + 1).coerceAtMost(maxAge))
                            } else if (it.endInclusive == maxAge) {
                                ageRange =
                                    (((it.start - 1).coerceAtLeast(minAge))..it.endInclusive)
                            }
                        }

                        it.start > it.endInclusive -> {
                            ageRange = (it.endInclusive..it.start)
                        }

                        else -> {
                            ageRange = it
                        }
                    }
                },
                valueRange = minAge..maxAge,
                steps = 80,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Profil:",
                fontSize = 16.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isProfileVerified,
                    onClick = { isProfileVerified = true },
                    modifier = Modifier.padding(end = 0.dp)
                )
                Text(
                    text = "Zweryfikowany",
                    fontSize = 14.sp,
                )
                RadioButton(
                    selected = !isProfileVerified,
                    onClick = { isProfileVerified = false },
                    modifier = Modifier.padding(start = 0.dp)
                )
                Text(
                    text = "Nie zweryfikowany",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Relacja:",
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = relationshipPreference,
                    onClick = { relationshipPreference = true },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Stała"
                )
                RadioButton(
                    selected = !relationshipPreference,
                    onClick = { relationshipPreference = false },
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = "Krótka"
                )
            }


            Text(
                text = "Maksymalna odległość: ${maxDistance.roundToInt()} km",
                fontSize = 16.sp
            )
            Slider(
                value = maxDistance,
                onValueChange = { maxDistance = it },
                valueRange = 5f..150f,
                steps = 28,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

        }


        Button(
            onClick = {
                if (latitude == null || longitude == null) {
                    viewModel.getLocation(
                        fusedLocationProviderClient = fusedLocationProviderClient,
                        context = context,
                        requestPermissionLauncher = requestPermissionLauncher,
                        onSuccess = { location ->
                            latitude = location.latitude
                            longitude = location.longitude
                            saveUserDataToDatabase(
                                selectedGenders,
                                ageRange,
                                isProfileVerified,
                                relationshipPreference,
                                maxDistance,
                                latitude,
                                longitude
                            )
                            onClick(Navigation.Destinations.LOADING_SCREEN_TO_V_CHAT)
                        },
                        onFailure = {
                            // Obsługa niepowodzenia
                            Log.e(TAG, "Failed to get location")
                            Toast.makeText(
                                context,
                                "Problem z pobraniem lokalizacji",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else {
                    saveUserDataToDatabase(
                        selectedGenders,
                        ageRange,
                        isProfileVerified,
                        relationshipPreference,
                        maxDistance,
                        latitude,
                        longitude
                    )
                    onClick(Navigation.Destinations.LOADING_SCREEN_TO_V_CHAT)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            )
        ) {
            Text(
                text = "LOSUJ",
                style = MaterialTheme.typography.titleMedium
            )
        }

    }
}


@Composable
fun GenderSelectionRow(
    modifier: Modifier,

){

}

// Funkcja do zapisywania danych użytkownika i preferencji do bazy danych
private fun saveUserDataToDatabase(
    selectedGenders: String,
    ageRange: ClosedFloatingPointRange<Float>,
    isProfileVerified: Boolean,
    relationshipPreference: Boolean,
    maxDistance: Float,
    latitude: Double?,
    longitude: Double?
) {
    // Pobierz zalogowanego użytkownika z Firebase Auth
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Sprawdź, czy użytkownik jest zalogowany
    currentUser?.let { user ->
        val userId = user.uid

        // Pobierz bieżącą datę jako lastSeen
        val lastSeen = Calendar.getInstance().timeInMillis

        // Pobierz referencję do bazy danych
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("user/$userId")

        // Referencja do gałęzi vChatUsers/<userId>/info
        val userInfoRef = database.getReference("vChatUsers/$userId/info")

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val age = dataSnapshot.child("age").getValue(String::class.java)
            val gender = dataSnapshot.child("gender").getValue(String::class.java)

            // Dodaj warunek sprawdzający, czy wiek i płeć zostały pobrane poprawnie
            if (age != null && gender != null) {
                // Zapisz dane użytkownika
                userInfoRef.setValue(
                    mapOf(
                        "age" to age,
                        "email" to user.email,
                        "gender" to gender,
                        "lastSeen" to lastSeen,
                        "latitude" to latitude,
                        "longitude" to longitude
                    )
                ).addOnCompleteListener { userInfoTask ->
                    if (userInfoTask.isSuccessful) {
                        // Referencja do gałęzi vChatUsers/<userId>/pref
                        val userPrefRef = database.getReference("vChatUsers/$userId/pref")

                        // Zapisz preferencje użytkownika
                        userPrefRef.setValue(
                            mapOf(
                                "age_min_pref" to ageRange.start.toInt(),
                                "age_max_pref" to ageRange.endInclusive.toInt(),
                                "gender_pref" to selectedGenders,
                                "location_max_pref" to maxDistance.toInt(),
                                "verification_pref" to isProfileVerified,
                                "relation_pref" to relationshipPreference
                            )
                        ).addOnCompleteListener { userPrefTask ->
                            if (userPrefTask.isSuccessful) {
                                // Jeżeli zapis danych użytkownika i preferencji zakończył się sukcesem
                                // możesz wykonać dodatkowe czynności tutaj
                                // np. przejście do kolejnego ekranu, wyświetlenie powiadomienia itp.
                            } else {
                                // Jeżeli wystąpił błąd podczas zapisu preferencji
                                // możesz obsłużyć ten błąd tutaj
                            }
                        }
                    } else {
                        // Jeżeli wystąpił błąd podczas zapisu danych użytkownika
                        // możesz obsłużyć ten błąd tutaj
                    }
                }
            }
        }.addOnFailureListener { exception ->

        }
    }
}