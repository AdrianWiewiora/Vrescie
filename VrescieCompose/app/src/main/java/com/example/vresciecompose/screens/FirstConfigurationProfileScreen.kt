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
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

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

    fun sendData(context: Context, selectedImageUri: Uri?, selectedBitmap: Bitmap?) {
        val name = nameState.value
        val age = ageState.value
        val gender = genderState.value

        // Sprawdzenie, czy jest wybrane zdjęcie z URI
        selectedImageUri?.let { uri ->
            Log.d("ImageUri", "Selected image URI: $uri")
            val storage = FirebaseStorage.getInstance()
            val storageRef: StorageReference = storage.reference

            // Unikalna nazwa pliku
            val fileName = "images/${System.currentTimeMillis()}.jpg"
            val imageRef = storageRef.child(fileName)

            // Wczytaj obraz jako bajty
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // Przesyłanie do Firebase
            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                // Po pomyślnym przesłaniu uzyskaj URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("Firebase", "Uploaded Image URL: $downloadUri")

                    // Zapisz dane użytkownika, w tym URL zdjęcia
                    configurationProfileViewModel.saveUserData(name, age, gender, downloadUri.toString()) {
                        configurationProfileViewModel.setProfileConfigured()
                    }
                    profileViewModel.setProfileConfigured(true)
                    onClick("${Navigation.Destinations.MAIN_MENU}/${1}")
                }
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Upload failed", exception)
                // Obsługa błędu przesyłania
            }
        } ?: selectedBitmap?.let { bitmap ->
            // Jeśli jest wybrane zdjęcie z Bitmapy
            Log.d("Bitmap", "Selected Bitmap")
            val storage = FirebaseStorage.getInstance()
            val storageRef: StorageReference = storage.reference

            // Unikalna nazwa pliku
            val fileName = "images/${System.currentTimeMillis()}.jpg"
            val imageRef = storageRef.child(fileName)

            // Wczytaj bitmapę jako bajty
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // Przesyłanie do Firebase
            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                // Po pomyślnym przesłaniu uzyskaj URL
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("Firebase", "Uploaded Bitmap URL: $downloadUri")

                    // Zapisz dane użytkownika, w tym URL zdjęcia
                    configurationProfileViewModel.saveUserData(name, age, gender, downloadUri.toString()) {
                        configurationProfileViewModel.setProfileConfigured()
                    }
                    profileViewModel.setProfileConfigured(true)
                    onClick("${Navigation.Destinations.MAIN_MENU}/${1}")
                }
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Upload failed", exception)
                // Obsługa błędu przesyłania
            }
        } ?: run {
            // Jeśli nie ma zdjęcia, przejdź dalej bez uploadu
            configurationProfileViewModel.saveUserData(name, age, gender, null) {
                configurationProfileViewModel.setProfileConfigured()
            }
            profileViewModel.setProfileConfigured(true)
            onClick("${Navigation.Destinations.MAIN_MENU}/${1}")
        }
    }




    when (numberOfConfigurationStage.value) {
        1 -> FirstConfigurationStage(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            nameState,
            ageState,
            genderState,
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
    sendData: (Context, Uri?, Bitmap?) -> Unit,
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
                ZoomableImage(
                    imageUri = selectedImageUri.value!!,
                    modifier = Modifier.size(200.dp)
                )
            }
            selectedBitmap.value != null -> {
                ZoomableImage(
                    bitmap = selectedBitmap.value!!,
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isLoading = remember { mutableStateOf(false) }

        FilledButton(
            onClick = {
                isLoading.value = true // Ustawienie stanu na "loading"
                sendData(context, selectedImageUri.value, selectedBitmap.value)
            },
            text = if (isLoading.value) "Loading..." else stringResource(R.string.continue_string),
            enabled = !isLoading.value, // Wyłączenie przycisku, gdy stan "loading"
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

    }
}

@Composable
fun ZoomableImage(
    imageUri: Uri? = null,
    bitmap: Bitmap? = null,
    modifier: Modifier = Modifier,
    maxZoom: Float = 3f
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .clip(CircleShape) // Przycięcie do kształtu koła
            .border(2.dp, Color.Gray, CircleShape) // Dodanie ramki
            .background(Color.LightGray) // Kolor tła (gdyby coś nie wypełniło)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, maxZoom)

                    // Obliczanie rozmiarów po skalowaniu
                    val scaledWidth = imageSize.width * scale
                    val scaledHeight = imageSize.height * scale

                    // Ograniczenia przesunięć: w zależności od rozmiarów obrazu i kontenera
                    val maxX = ((scaledWidth - containerSize.width) / 2).coerceAtLeast(0f)
                    val maxY = ((scaledHeight - containerSize.height) / 2).coerceAtLeast(0f)

                    // Nowe przesunięcie z ograniczeniami
                    val newOffsetX = (offset.x + pan.x * scale).coerceIn(-maxX, maxX)
                    val newOffsetY = (offset.y + pan.y * scale).coerceIn(-maxY, maxY)

                    offset = Offset(newOffsetX, newOffsetY)
                }
            }
            .onGloballyPositioned { layoutCoordinates ->
                containerSize = layoutCoordinates.size // Pobieramy rozmiar kontenera
            }
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize() // Obraz wypełnia cały kontener
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .onGloballyPositioned { coordinates ->
                        imageSize = coordinates.size // Pobieramy rozmiar obrazu
                    },
                contentScale = ContentScale.Crop // Przycięcie obrazu do koła, bez pustych przestrzeni
            )
        } else if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize() // Obraz wypełnia cały kontener
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .onGloballyPositioned { coordinates ->
                        imageSize = coordinates.size // Pobieramy rozmiar obrazu
                    },
                contentScale = ContentScale.Crop // Przycięcie obrazu do koła, bez pustych przestrzeni
            )
        }
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
        genderState = genderState
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSecondConfigurationStage() {
    // Dummy context for preview
    val context = LocalContext.current
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val selectedBitmap = remember { mutableStateOf<Bitmap?>(null) }

    // Dummy sendData function
    fun dummySendData(context: Context, uri: Uri?, bitmap: Bitmap?) {
    }

    SecondConfigurationStage(
        modifier = Modifier.fillMaxSize(),
        sendData = ::dummySendData
    )
}
