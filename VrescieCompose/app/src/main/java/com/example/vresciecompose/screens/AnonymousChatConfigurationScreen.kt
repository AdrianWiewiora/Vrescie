package com.example.vresciecompose.screens

import LocalContext
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.view_models.LocationViewModel
import com.example.vresciecompose.view_models.UserChatPrefsViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


private const val TAG = "AnonymousChatConfig"

@Composable
fun AnonymousChatConfigurationScreen(
    viewModel: LocationViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    onClick: (String) -> Unit,
    userChatPrefsViewModel: UserChatPrefsViewModel,
    isConnected: Boolean
) {
    val allChatPrefs by userChatPrefsViewModel.allChatPrefs.observeAsState(emptyList())


    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
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
                Log.e("Location", "Failed to get location")
            }
        )
    }

    val (selectedGenders, setSelectedGenders) = remember { mutableStateOf(allChatPrefs.firstOrNull()?.selectedGenders ?: "FM") }
    val (ageRange, setAgeRange) = remember {mutableStateOf(allChatPrefs.firstOrNull()?.ageStart?.rangeTo(allChatPrefs.firstOrNull()?.ageEnd ?: 50f) ?: 18f..50f)}
    val minAge by remember { mutableStateOf(18f) }
    val maxAge by remember { mutableStateOf(50f) }
    val (isProfileVerified, setProfileVerified) = remember { mutableStateOf(allChatPrefs.firstOrNull()?.isProfileVerified ?: false) }
    val (relationshipPreference, setRelationshipPreference) = remember { mutableStateOf(allChatPrefs.firstOrNull()?.relationshipPreference ?: false) }
    val (maxDistance, setMaxDistance) = remember { mutableStateOf(allChatPrefs.firstOrNull()?.maxDistance ?: 10f) }


    // Odczyt danych z bazy
    LaunchedEffect(Unit) {
        userChatPrefsViewModel.fetchChatPrefs()
    }
    // Jeżeli dane są dostępne, ustaw wartości
    LaunchedEffect(allChatPrefs) {
        allChatPrefs.firstOrNull()?.let {
            setSelectedGenders(it.selectedGenders)
            setAgeRange(it.ageStart..it.ageEnd)
            setProfileVerified(it.isProfileVerified)
            setRelationshipPreference(it.relationshipPreference)
            setMaxDistance(it.maxDistance)
        }
    }


    val updatePreferences: (String, ClosedRange<Float>, Boolean, Boolean, Float) -> Unit = { genders, range, isVerified, relationship, distance ->
        userChatPrefsViewModel.savePreferences(genders, range, isVerified, relationship, distance)
    }

    fun updateGenderPreferences(newSelectedGenders: String) {
        updatePreferences(newSelectedGenders, ageRange, isProfileVerified, relationshipPreference, maxDistance)
    }

    fun updateAgeRangePreferences(newAgeRange: ClosedRange<Float>) {
        updatePreferences(selectedGenders, newAgeRange, isProfileVerified, relationshipPreference, maxDistance)
    }

    fun updateProfileVerifiedPreferences(newIsProfileVerified: Boolean) {
        updatePreferences(selectedGenders, ageRange, newIsProfileVerified, relationshipPreference, maxDistance)
    }

    fun updateRelationshipPreferences(newRelationshipPreference: Boolean) {
        updatePreferences(selectedGenders, ageRange, isProfileVerified, newRelationshipPreference, maxDistance)
    }

    fun updateMaxDistancePreferences(newMaxDistance: Float) {
        updatePreferences(selectedGenders, ageRange, isProfileVerified, relationshipPreference, newMaxDistance)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
            ) {

            GenderSelectionRow(
                modifier = Modifier,
                selectedGenders = selectedGenders,
                setSelectedGenders = setSelectedGenders,
                updateGenderPreferences = ::updateGenderPreferences
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

            AgeSelectionRow(
                ageRange = ageRange,
                setAgeRange = setAgeRange,
                minAge = minAge,
                maxAge = maxAge,
                updateAgeRangePreferences = ::updateAgeRangePreferences,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

//            ProfilePrefSelectionRow(
//                modifier = Modifier,
//                isProfileVerified = isProfileVerified,
//                setProfileVerified = setProfileVerified,
//                updateProfileVerifiedPreferences = ::updateProfileVerifiedPreferences
//            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

            RelationPrefSelectionRow(
                modifier = Modifier,
                relationshipPreference = relationshipPreference,
                setRelationshipPreference = setRelationshipPreference,
                updateRelationshipPreferences = ::updateRelationshipPreferences
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

            LocationPrefSelectionRow(
                modifier = Modifier,
                maxDistance = maxDistance,
                setMaxDistance = setMaxDistance,
                updateMaxDistancePreferences = ::updateMaxDistancePreferences
            )

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
                            Log.e(TAG, "Failed to get location")
                            Toast.makeText(
                                context,
                                context.getString(R.string.problem_with_determining_the_location),
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
            ),
            enabled = isConnected
        ) {
            Text(
                text = if (isConnected) stringResource(R.string.draw) else stringResource(R.string.no_internet_connection),
                style = MaterialTheme.typography.titleMedium
            )
        }

    }
}


@Composable
fun GenderSelectionRow(
    modifier: Modifier,
    selectedGenders: String,
    setSelectedGenders: (String) -> Unit,
    updateGenderPreferences: (String) -> Unit,
){
    Column(
        modifier = modifier,
    )
    {
        Text(
            text = stringResource(R.string.gender) +":",
            style = MaterialTheme.typography.titleMedium,
        )

        Row(horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = selectedGenders.contains("F"),
                onCheckedChange = {
                    val newGenders = if (it) {
                        if (selectedGenders.contains("M")) "FM" else "F"
                    } else {
                        selectedGenders.replace("F", "")
                    }
                    setSelectedGenders(newGenders)
                    updateGenderPreferences(newGenders)
                },
                modifier = Modifier
            )
            Text(
                text = stringResource(R.string.woman),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
            )
            Icon(
                imageVector = Icons.Filled.Female,
                contentDescription = "none",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.icon_small_size))
            )

            Checkbox(
                checked = selectedGenders.contains("M"),
                onCheckedChange = {
                    val newGenders = if (it) {
                        if (selectedGenders.contains("F")) "FM" else "M"
                    } else {
                        selectedGenders.replace("M", "")
                    }
                    setSelectedGenders(newGenders)
                    updateGenderPreferences(newGenders)
                },
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = stringResource(R.string.man),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(
                imageVector = Icons.Filled.Male,
                contentDescription = "none",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.icon_small_size))
            )
        }
    }

}

@Preview()
@Composable
fun GenderSelectionRowPreview() {
    val (selectedGenders, setSelectedGenders) = remember { mutableStateOf("FM") }
    GenderSelectionRow(
        modifier = Modifier.fillMaxWidth(),
        selectedGenders = selectedGenders,
        setSelectedGenders = setSelectedGenders,
        updateGenderPreferences = {  }
    )
}

@Composable
fun AgeSelectionRow(
    ageRange: ClosedFloatingPointRange<Float> = 18f..50f,
    setAgeRange: (ClosedFloatingPointRange<Float>) -> Unit = {},
    minAge: Float = 18f,
    maxAge: Float = 50f,
    updateAgeRangePreferences: (ClosedRange<Float>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(
                R.string.age_range_years,
                ageRange.start.toInt(),
                ageRange.endInclusive.toInt(),
                if (ageRange.endInclusive.toInt() == 50) "+" else ""
            ),
            style = MaterialTheme.typography.titleMedium
        )
        RangeSlider(
            value = ageRange,
            onValueChange = { newRange ->
                setAgeRange(newRange)
                updateAgeRangePreferences(newRange)
            },
            valueRange = minAge..maxAge,
            steps = 31,
            colors = SliderDefaults.colors(
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
    // To jest wersja onValueChange żeby nie miał min i max tej samej wartośći
//    if (newRange.start < newRange.endInclusive) {
//        val start = newRange.start.coerceAtLeast(minAge).coerceAtMost(maxAge)
//        val end = newRange.endInclusive.coerceAtLeast(minAge).coerceAtMost(maxAge)
//        setAgeRange(start..end)
//    }

}

@Preview()
@Composable
fun AgeSelectionRowPreview() {
    val (ageRange, setAgeRange) = remember { mutableStateOf(18f..50f) }
    val minAge by remember { mutableStateOf(26f) }
    val maxAge by remember { mutableStateOf(39f) }

    AgeSelectionRow(
        ageRange,
        setAgeRange,
        minAge,
        maxAge,
        updateAgeRangePreferences = { },
    )

}

@Composable
fun ProfilePrefSelectionRow(
    modifier: Modifier,
    isProfileVerified: Boolean,
    setProfileVerified: (Boolean) -> Unit,
    updateProfileVerifiedPreferences: (Boolean) -> Unit,
){
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.profile_ac),
            style = MaterialTheme.typography.titleMedium,
        )
        Column(modifier = Modifier.width(250.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = isProfileVerified,
                    onClick = { setProfileVerified(true); updateProfileVerifiedPreferences(true) },
                    modifier = Modifier.padding(end = 0.dp)
                )
                Text(
                    text = stringResource(R.string.verified),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Icon(
                    imageVector = Icons.Filled.GppGood,
                    contentDescription = "none",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.icon_small_size))
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = !isProfileVerified,
                    onClick = { setProfileVerified(false); updateProfileVerifiedPreferences(false)  },
                    modifier = Modifier.padding(start = 0.dp)
                )
                Text(
                    text = stringResource(R.string.unverified),
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Filled.GppBad,
                    contentDescription = "none",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.icon_small_size))
                )
            }
        }
    }
}

@Preview
@Composable
fun ProfilePrefSelectionRowPreview() {
    val (isProfileVerified, setProfileVerified) = remember { mutableStateOf(false) }
    ProfilePrefSelectionRow(
        modifier = Modifier.fillMaxWidth(),
        isProfileVerified = isProfileVerified,
        setProfileVerified = setProfileVerified,
        updateProfileVerifiedPreferences = {  }
    )
}

@Composable
fun RelationPrefSelectionRow(
    modifier: Modifier,
    relationshipPreference: Boolean,
    setRelationshipPreference: (Boolean) -> Unit,
    updateRelationshipPreferences: (Boolean) -> Unit,
){
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.relation),
            style = MaterialTheme.typography.titleMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = relationshipPreference,
                onClick = { setRelationshipPreference(true); updateRelationshipPreferences(true) },
                modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_small))
            )
            Text(
                text = stringResource(R.string.long_term),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "none",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
            )
            RadioButton(
                selected = !relationshipPreference,
                onClick = { setRelationshipPreference(false); updateRelationshipPreferences(false)  },
                modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_medium))
            )
            Text(
                text = stringResource(R.string.short_rel),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = "none",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
            )
        }
    }
}

@Preview()
@Composable
fun RelationPrefSelectionRowPreview() {
    val (relationshipPreference, setRelationshipPreference) = remember { mutableStateOf(false) }

    RelationPrefSelectionRow(
        modifier = Modifier.fillMaxWidth(),
        relationshipPreference = relationshipPreference,
        setRelationshipPreference = setRelationshipPreference,
        updateRelationshipPreferences = {  }
    )
}

@Composable
fun LocationPrefSelectionRow(
    modifier: Modifier,
    maxDistance: Float,
    setMaxDistance: (Float) -> Unit,
    updateMaxDistancePreferences: (Float) -> Unit,
){
    Column(modifier = modifier) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .width(300.dp)
                .padding(bottom = dimensionResource(R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(R.string.maximum_distance_km, maxDistance.roundToInt()),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
            )
            Icon(
                imageVector = Icons.Filled.SocialDistance,
                contentDescription = "none",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.icon_small_size))
                    .padding(start = dimensionResource(R.dimen.padding_small))
            )
        }

        Slider(
            value = maxDistance,
            onValueChange = { setMaxDistance(it); updateMaxDistancePreferences(it) },
            valueRange = 5f..150f,
            steps = 28,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview()
@Composable
fun LocationPrefSelectionRowPreview() {
    val (maxDistance, setMaxDistance) = remember { mutableStateOf(10f) }
    LocationPrefSelectionRow(
        modifier = Modifier.fillMaxWidth(),
        maxDistance = maxDistance,
        setMaxDistance = setMaxDistance,
        updateMaxDistancePreferences = {  }
    )
}


// Funkcja do zapisywania danych użytkownika i preferencji do bazy danych
private fun saveUserDataToDatabase(
    selectedGenders: String,
    ageRange: ClosedFloatingPointRange<Float>,
    isProfileVerified: Boolean = false,
    relationshipPreference: Boolean,
    maxDistance: Float,
    latitude: Double?,
    longitude: Double?
) {
    // Pobiera zalogowanego użytkownika z Firebase Auth
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