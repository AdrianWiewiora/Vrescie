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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.view_models.LocationViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


private const val TAG = "AnonymousChatConfig"

@Composable
fun AnonymousChatConfigurationScreen(viewModel: LocationViewModel, onClick: (String) -> Unit) {
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(key1 = Unit) {
        viewModel.getLocation(
            fusedLocationProviderClient = fusedLocationProviderClient,
            context = context,
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

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = com.example.vresciecompose.R.drawable.logotype_vreescie_svg),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 198.dp, height = 47.dp)
                    .padding(2.dp)
            )

            Image(
                painter = painterResource(id = com.example.vresciecompose.R.drawable.baseline_settings_24),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .padding(vertical = 0.dp),
            ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 5.dp, bottom = 8.dp, start = 8.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color.Black),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White, //Card background color
                    contentColor = Color.Black  //Card content color,e.g.text
                )

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),

                        ) {
                        Text(
                            text = "Płeć",
                            fontSize = 16.sp,
                            color = Color.Black
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
                                text = "Kobieta",
                                color = Color.Black
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
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Przedział wiekowy: ${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()} lat",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        RangeSlider(
                            value = ageRange,
                            onValueChange = {
                                when {
                                    it.start == it.endInclusive -> {
                                        if (it.start == minAge) {
                                            ageRange = (it.start..(it.endInclusive + 1).coerceAtMost(maxAge))
                                        } else if (it.endInclusive == maxAge) {
                                            ageRange = (((it.start - 1).coerceAtLeast(minAge))..it.endInclusive)
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
                            color = Color.Black
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
                                color = Color.Black
                            )
                            RadioButton(
                                selected = !isProfileVerified,
                                onClick = { isProfileVerified = false },
                                modifier = Modifier.padding(start = 0.dp)
                            )
                            Text(
                                text = "Nie zweryfikowany",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Relacja:",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = relationshipPreference,
                                onClick = { relationshipPreference = true },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Stała",
                                color = Color.Black
                            )
                            RadioButton(
                                selected = !relationshipPreference,
                                onClick = { relationshipPreference = false },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = "Krótka",
                                color = Color.Black
                            )
                        }


                        Text(
                            text = "Maksymalna odległość: ${maxDistance.roundToInt()} km",
                            fontSize = 16.sp,
                            color = Color.Black
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
                            saveUserDataToDatabase(selectedGenders, ageRange, isProfileVerified, relationshipPreference, maxDistance, latitude, longitude)
                            onClick(Navigation.Destinations.LOADING_SCREEN_TO_V_CHAT)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                    ) {
                        Text(
                            text = "Losuj",
                            color = Color.White
                        )
                    }

                }

            }

        }
    }
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

        // Referencja do gałęzi vChatUsers/<userId>/info
        val userInfoRef = database.getReference("vChatUsers/$userId/info")

        // Zapisz dane użytkownika
        userInfoRef.setValue(
            mapOf(
                "age" to ageRange.endInclusive.toInt(),
                "email" to user.email,
                "gender" to selectedGenders,
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
}

@Preview
@Composable
fun AnonymousChatConfigurationScreenPreview() {
    val locationViewModel = LocationViewModel()
    val context = LocalContext.current
    ProvideContext(context) {
        AnonymousChatConfigurationScreen(locationViewModel, onClick = {})
    }
}