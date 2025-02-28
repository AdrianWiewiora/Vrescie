package com.example.vresciecompose.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.data.UserProfile
import com.example.vresciecompose.view_models.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    isConnected: Boolean,
    profileViewModel: ProfileViewModel,
    navigateTo: (String) -> Unit
) {
    val localUserProfile = profileViewModel.getStoredProfile()
    val userProfile = profileViewModel.userProfile.observeAsState()
    var dataLoaded by remember { mutableStateOf(false) }

    // Brak połączenia
    if (!isConnected) {
        if (localUserProfile != null) {
            profileViewModel.setProfileConfigured(true)
            profileViewModel._userProfile.value = localUserProfile
            dataLoaded = true
        } else {
            dataLoaded = false
        }
    } else {
        profileViewModel.loadUserProfile()
    }

    LaunchedEffect(userProfile.value) {
        if (userProfile.value != null) {
            dataLoaded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
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
                    val localImagePath = profileViewModel.getLocalImagePath()
                    ProfileContent(profile, localImagePath = localImagePath)
                } else {
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
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))

        Button(
            onClick = {
                navigateTo(Navigation.Destinations.FIRST_CONFIGURATION + "/1")
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
                text = if (isConnected) stringResource(R.string.change_photo) else stringResource(R.string.no_internet_connection),
                style = MaterialTheme.typography.titleMedium
            )
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
        profileImageUrl = null.toString()
    )

    val userProfileState = remember { mutableStateOf(sampleUserProfile) }

    ProfileContent(userProfile = userProfileState.value)
}
