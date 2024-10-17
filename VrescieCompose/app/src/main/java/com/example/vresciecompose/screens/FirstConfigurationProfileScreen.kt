package com.example.vresciecompose.screens

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.FilledButton
import com.example.vresciecompose.view_models.ConfigurationProfileViewModel
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.ui.components.ExitConfirmationDialog
import com.example.vresciecompose.view_models.ProfileViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import android.Manifest

@Composable
fun FirstConfigurationProfileScreen(
    onClick: (String) -> Unit,
    configurationProfileViewModel: ConfigurationProfileViewModel,
    profileViewModel: ProfileViewModel
) {
    val numberOfConfigurationStage = remember { mutableStateOf(1) }

    val nameState = remember { mutableStateOf("") }
    val ageState = remember { mutableStateOf("") }
    val genderState = remember { mutableStateOf("") }

    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value) {
        ExitConfirmationDialog(
            onConfirm = {
                showDialog.value = false
            },
            onDismiss = {
                showDialog.value = false
            }
        )
    }
    BackHandler {
        showDialog.value = true
    }

    fun sendData() {
        val name = nameState.value
        val age = ageState.value
        val gender = genderState.value

        configurationProfileViewModel.saveUserData(name, age, gender) {
            configurationProfileViewModel.setProfileConfigured()
        }
        profileViewModel.setProfileConfigured(true)
        onClick("${Navigation.Destinations.MAIN_MENU}/${1}")
    }

    when (numberOfConfigurationStage.value) {
        1 -> FirstConfigurationStage(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            nameState,
            ageState,
            genderState,
            sendData = ::sendData,
            numberOfConfigurationStage
        )
        2 -> SecondConfigurationStage(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            sendData = ::sendData,
        )
    }

}

@Composable
fun FirstConfigurationStage(
    modifier: Modifier = Modifier,
    nameState: MutableState<String> = remember { mutableStateOf("") },
    ageState: MutableState<String> = remember { mutableStateOf("") },
    genderState: MutableState<String> = remember { mutableStateOf("") },
    sendData: () -> Unit,
    numberOfConfigurationStage: MutableState<Int> = remember { mutableStateOf(1) }
){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 100.dp)
                .padding(horizontal = 20.dp)
        )

        Text(
            text = stringResource(R.string.configuring_your_profile),
            modifier = Modifier.padding(bottom = 20.dp, top = 90.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text(text = stringResource(R.string.enter_your_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = ageState.value,
            onValueChange = { ageState.value = it },
            label = { Text(text = stringResource(R.string.enter_your_age)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        Text(
            text = stringResource(R.string.select_your_gender),
            modifier = Modifier
                .padding(bottom = 10.dp)
                .padding(top = 30.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        // Gender Radio Buttons
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = genderState.value == "male",
                    onClick = { genderState.value = "male" }
                )
                Text(
                    text = stringResource(R.string.man),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = genderState.value == "female",
                    onClick = { genderState.value = "female" }
                )
                Text(
                    text = stringResource(R.string.woman),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = genderState.value == "other",
                    onClick = { genderState.value = "other" },
                    enabled = false
                )
                Text(
                    text = stringResource(R.string.other_gender),
                    style = MaterialTheme.typography.bodyLarge
                )
            }


        }
        FilledButton(
            onClick = {
                numberOfConfigurationStage.value = 2
                //sendData()
            },
            text = stringResource(R.string.continue_string),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
fun SecondConfigurationStage(
    modifier: Modifier = Modifier,
    sendData: () -> Unit,
){
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val selectedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val cameraPermissionGranted = remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Launcher do wyboru obrazu z galerii
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri.value = uri
    }

    // Launcher do robienia zdjęcia
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        selectedBitmap.value = bitmap
    }

    // Launcher do żądania uprawnień do aparatu
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        cameraPermissionGranted.value = isGranted
        if (isGranted) {
            cameraLauncher.launch(null) // Uruchomienie aparatu po przyznaniu uprawnienia
        } else {
            // Obsługa przypadku, gdy użytkownik odrzucił uprawnienie
            // Możesz wyświetlić odpowiedni komunikat lub zachować domyślne działanie
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 100.dp)
                .padding(horizontal = 20.dp)
        )

        Text(
            text = stringResource(R.string.configuring_your_profile),
            modifier = Modifier.padding(bottom = 20.dp, top = 10.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = stringResource(R.string.add_your_photo),
            modifier = Modifier.padding(bottom = 20.dp, top = 50.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        // Przycisk do wyboru zdjęcia z galerii
        FilledButton(
            onClick = {
                galleryLauncher.launch("image/*")
            },
            text = "Choose from Gallery",
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

        // Przycisk do robienia zdjęcia
        FilledButton(
            onClick = {
                // Sprawdzenie, czy uprawnienie do aparatu jest już przyznane
                when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        cameraLauncher.launch(null) // Uprawnienie już jest przyznane
                    }
                    else -> {
                        // Poproś o uprawnienie do aparatu, jeśli nie jest przyznane
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            text = "Take a Photo",
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Wyświetlanie obrazu: albo `Uri` z galerii, albo `Bitmap` z aparatu
        when {
            selectedImageUri.value != null -> {
                // Wyświetl obraz z Uri
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(selectedImageUri.value)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(10.dp),
                    contentScale = ContentScale.Crop
                )
            }
            selectedBitmap.value != null -> {
                // Wyświetl obraz z Bitmap (z aparatu)
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(selectedBitmap.value)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(10.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilledButton(
            onClick = {
                sendData()
            },
            text = stringResource(R.string.continue_string),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewFirstConfigurationStage() {
    val nameState = remember { mutableStateOf("Jan") }
    val ageState = remember { mutableStateOf("25") }
    val genderState = remember { mutableStateOf("male") }


    FirstConfigurationStage(
        nameState = nameState,
        ageState = ageState,
        genderState = genderState,
        sendData = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSecondConfigurationStage() {



    SecondConfigurationStage(
        sendData = {}
    )
}
