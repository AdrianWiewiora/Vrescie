package com.example.vresciecompose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vresciecompose.authentication.UserProfile
import com.example.vresciecompose.view_models.ProfileViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ProfileScreen() {
    val profileViewModel: ProfileViewModel = viewModel()
    profileViewModel.loadUserProfile()
    val userProfile = profileViewModel.userProfile

    var dataLoaded by remember { mutableStateOf(false) }

    // Launch a coroutine that refreshes data every 0.15 seconds until data is loaded
    LaunchedEffect(dataLoaded) {
        while (!dataLoaded) {
            delay(150)
            if (userProfile.value == null) {
                profileViewModel.loadUserProfile()
            } else {
                dataLoaded = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top
    ) {
        Crossfade(targetState = userProfile.value != null, label = "") { isLoaded ->
            if (isLoaded) {
                ProfileContent(userProfile.value!!)
            } else {
                // Użycie AnimatedVisibility
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Ładowanie danych",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(userProfile: UserProfile) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        item {
            UserInfoRow("Imię", userProfile.name)
            UserInfoRow("Wiek", userProfile.age.toString())
            UserInfoRow("Email", userProfile.email)
            UserInfoRow("Płeć", userProfile.gender)
            UserInfoRow("Data dołączenia", formatDate(userProfile.joinDate))
        }
    }
}


@Composable
fun formatDate(joinDate: String): String {
    val timestamp = joinDate.toLongOrNull() ?: return ""
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

@Composable
fun UserInfoRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    val sampleUserProfile = UserProfile(
        name = "Jan Kowalski",
        age = 30,
        email = "jan.kowalski@example.com",
        gender = "Mężczyzna",
        joinDate = System.currentTimeMillis().toString()
    )

    val userProfileState = remember { mutableStateOf(sampleUserProfile) }

    ProfileContent(userProfile = userProfileState.value)
}
