package com.example.vresciecompose.screens

import LocalContext
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.vresciecompose.R
import com.example.vresciecompose.data.UserProfile
import com.example.vresciecompose.view_models.ProfileViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(isConnected: Boolean, profileViewModel: ProfileViewModel) {
    val localUserProfile = profileViewModel.getStoredProfile()
    val userProfile = profileViewModel.userProfile.observeAsState()
    var dataLoaded by remember { mutableStateOf(false) }

    // Jeśli nie ma połączenia, ustaw dane lokalne
    if (!isConnected) {
        if (localUserProfile != null) {
            profileViewModel.setProfileConfigured(true)
            profileViewModel._userProfile.value = localUserProfile
            dataLoaded = true
        } else {
            // Możesz wyświetlić komunikat o braku danych lokalnych
            dataLoaded = false
        }
    } else {
        // Jeśli połączenie jest dostępne, załaduj dane z Firebase
        profileViewModel.loadUserProfile()
    }

    // Obserwuj zmiany w userProfile i aktualizuj dataLoaded
    LaunchedEffect(userProfile.value) {
        if (userProfile.value != null) {
            dataLoaded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top
    ) {
        if (!isConnected) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_internet_connection),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Crossfade(targetState = dataLoaded, label = "") { isLoaded ->
            if (isLoaded) {
                val profile = userProfile.value
                if (profile != null) {
                    // Dodanie logu dla ścieżki lokalnego obrazu
                    val localImagePath = profileViewModel.getLocalImagePath()
                    Log.d("ProfileScreen", "Local image path: $localImagePath")

                    ProfileContent(profile, localImagePath = localImagePath)
                } else {
                    // Możesz wyświetlić komunikat o błędzie lub inny widok
                    Text(
                        text = stringResource(R.string.loading_data),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = stringResource(R.string.loading_data),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(userProfile: UserProfile, localImagePath: String? = null) {

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Jeśli lokalna ścieżka nie jest pusta, wyświetl lokalny obrazek
            if (!localImagePath.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(localImagePath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else if (userProfile.profileImageUrl.isNotEmpty()) {
                // Alternatywnie, wyświetl obrazek z URL, jeśli nie ma lokalnego
                Image(
                    painter = rememberAsyncImagePainter(userProfile.profileImageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            UserInfoRow(stringResource(R.string.name), userProfile.name)
            UserInfoRow(stringResource(R.string.age), userProfile.age.toString())
            UserInfoRow("Email", userProfile.email)
            UserInfoRow(stringResource(R.string.gender), userProfile.gender)
            UserInfoRow(stringResource(R.string.join_date), formatDate(userProfile.joinDate))
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
        joinDate = System.currentTimeMillis().toString(),
        profileImageUrl = "https://example.com/sample-profile-image.jpg" // Przykładowy URL
    )

    val userProfileState = remember { mutableStateOf(sampleUserProfile) }

    ProfileContent(userProfile = userProfileState.value)
}
