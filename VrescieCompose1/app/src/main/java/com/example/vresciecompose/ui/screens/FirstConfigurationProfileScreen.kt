package com.example.vresciecompose.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.insertImage
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.ExitConfirmationDialog
import com.example.vresciecompose.ui.components.FilledButton
import com.example.vresciecompose.view_models.ProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun FirstConfigurationProfileScreen(
    navigateTo: (String) -> Unit,
    profileViewModel: ProfileViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    isChangePhoto: Int = 0,
) {
    val numberOfConfigurationStage = remember { mutableStateOf(if (isChangePhoto == 1) 2 else 1) }
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
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

    fun sendData(context: Context, selectedImageUri: Uri) {
        val name = nameState.value
        val age = ageState.value
        val gender = genderState.value

        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        val fileName = "images/$currentUserID.jpg"
        val imageRef = storageRef.child(fileName)

        // Wczytuje obraz jako bajty
        val inputStream = context.contentResolver.openInputStream(selectedImageUri)
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
                // Zapisuje dane po czym nawiguje
                profileViewModel.saveUserData(name, age, gender, downloadUri.toString())
                navigateTo("${Navigation.Destinations.MAIN_MENU}/${1}")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Upload failed", exception)
        }
    }

    fun sendNewPhoto(context: Context, selectedImageUri: Uri?) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val database = Firebase.database
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        // Odczytje URL starego zdjęcia z Firebase
        val userRef = database.getReference("user").child(userId)
        userRef.child("photoUrl").get().addOnSuccessListener { snapshot ->
            val oldPhotoUrl = snapshot.value as? String

            // Usuwa stare zdjęcie w Firebase
            oldPhotoUrl?.let {
                val oldPhotoRef = storage.getReferenceFromUrl(it)
                oldPhotoRef.delete()
                    .addOnSuccessListener { Log.d("Firebase", "Old photo deleted successfully") }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Failed to delete old photo: ${exception.message}")
                    }
            }

            // Usuwa stare zdjęcie lokalnie
            val localFile = File(context.filesDir, "$userId.jpg")
            if (localFile.exists()) {
                if (localFile.delete()) {
                    Log.d("LocalFile", "Old local photo deleted successfully")
                } else {
                    Log.e("LocalFile", "Failed to delete old local photo")
                }
            }

            // Jeśli użytkownik wybrał nowe zdjęcie, prześlij je
            selectedImageUri?.let { uri ->
                val fileName = "images/$userId.jpg"
                val imageRef = storageRef.child(fileName)

                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        userRef.child("photoUrl").setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                Log.d("Firebase", "New photo URL saved successfully")

                                // Zapisz zdjęcie lokalnie
                                profileViewModel.saveImageLocally(downloadUri.toString(), userId)

                                navigateTo("${Navigation.Destinations.MAIN_MENU}/${1}")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firebase", "Failed to save new photo URL: ${exception.message}")
                            }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to upload new photo: ${exception.message}")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Failed to get old photo URL: ${exception.message}")
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
            sendData = if (isChangePhoto == 1) ::sendNewPhoto else ::sendData,
            requestPermissionLauncher = requestPermissionLauncher
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
        }
        FilledButton(
            onClick = {
                numberOfConfigurationStage.value = 2
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
    sendData: (Context, Uri) -> Unit,
    currentUserID: String = FirebaseAuth.getInstance().currentUser?.uid ?: "",
    requestPermissionLauncher: ActivityResultLauncher<String>
) {
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraPermissionGranted = remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Przygotowanie zmiennej do URI
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    // Stany do skali i przesunięcia
    val scale = remember { mutableStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }

    // Launcher do wyboru obrazu z galerii
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri.value = uri }

    // Launcher do robienia zdjęcia
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { selectedImageUri.value = photoUri.value }

    // Uruchomienie kamery po przyznaniu uprawnień
    LaunchedEffect(cameraPermissionGranted.value) {
        if (cameraPermissionGranted.value && photoUri.value != null) {
            cameraLauncher.launch(photoUri.value!!)
        }
    }

    fun savePhotoToGallery(): Uri? {
        return selectedImageUri.value?.let { uri ->
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            if (originalBitmap != null) {
                val orientation = getOrientation(context, uri)
                val bitmap = rotateBitmap(originalBitmap, orientation)

                val maxDimension = maxOf(bitmap.width, bitmap.height)
                val squareBitmap = Bitmap.createBitmap(maxDimension, maxDimension, Bitmap.Config.ARGB_8888)

                val canvas = Canvas(squareBitmap)
                canvas.drawColor(Color.White.toArgb())

                val left = (maxDimension - bitmap.width) / 2
                val top = (maxDimension - bitmap.height) / 2

                canvas.drawBitmap(bitmap, left.toFloat(), top.toFloat(), null)

                val targetSize = (maxDimension / scale.value).toInt()

                val centerX = (squareBitmap.width / 2)
                val centerY = (squareBitmap.height / 2)

                val x = (centerX - (targetSize / 2) - (offset.value.x * scale.value)).toInt()
                val y = (centerY - (targetSize / 2) - (offset.value.y * scale.value)).toInt()

                val validX = x.coerceIn(0, squareBitmap.width - targetSize)
                val validY = y.coerceIn(0, squareBitmap.height - targetSize)

                val croppedBitmap = Bitmap.createBitmap(squareBitmap, validX, validY, targetSize, targetSize)

                val savedUri = insertImage(
                    context.contentResolver,
                    croppedBitmap,
                    "Cropped Image $currentUserID",
                    null
                )

                if (savedUri != null) {
                    Log.d("SavePhoto", "Image successfully saved to gallery: $savedUri")
                    return Uri.parse(savedUri)
                } else {
                    Log.e("SavePhoto", "Failed to save image")
                }
            } else {
                Log.e("SavePhoto", "Failed to decode image from URI")
            }
            null
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
            text = stringResource(R.string.choose_from_gallery),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

        // Przycisk do robienia zdjęcia
        FilledButton(
            onClick = {
                photoUri.value = createImageUri(context,currentUserID )
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                        cameraLauncher.launch(photoUri.value!!)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            text = stringResource(R.string.take_a_photo),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedImageUri.value != null) {
            ZoomableImage(
                imageUri = selectedImageUri.value!!,
                modifier = Modifier.size(200.dp),
                currentScale = scale,
                currentOffset = offset,
                context = context
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isLoading = remember { mutableStateOf(false) }

        FilledButton(
            onClick = {
                isLoading.value = true

                // Wywołanie funkcji zapisywania i obsługa jej wyniku
                val savedUri = savePhotoToGallery()
                if (savedUri != null) {
                    sendData(context, savedUri) // Przekazanie URI zapisanego zdjęcia
                } else {
                    isLoading.value = false // Zakończenie operacji
                }
            },
            text = if (isLoading.value) stringResource(R.string.loading) else stringResource(R.string.continue_string),
            enabled = !isLoading.value,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

    }
}


fun getOrientation(context: Context, uri: Uri): Int {
    val exif = ExifInterface(context.contentResolver.openInputStream(uri)!!)
    return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
}
fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply {
                postRotate(90f)
            }, true)
        }
        ExifInterface.ORIENTATION_ROTATE_180 -> {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply {
                postRotate(180f)
            }, true)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply {
                postRotate(270f)
            }, true)
        }
        else -> bitmap
    }
}

// Funkcja do tworzenia URI do zapisu zdjęcia
private fun createImageUri(context: Context, currentUserID: String): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "Photo_$currentUserID.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
}

@Composable
fun ZoomableImage(
    imageUri: Uri,                          modifier: Modifier = Modifier,          maxZoom: Float = 3f,
    currentScale: MutableState<Float>,      currentOffset: MutableState<Offset>,    context: Context
) {
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val squareBitmap = remember(imageUri) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val orientation = getOrientation(context, imageUri)
        val bitmap = rotateBitmap(originalBitmap, orientation)
        run {
            val maxDimension = maxOf(bitmap.width, bitmap.height)
            val squareBitmap = Bitmap.createBitmap(maxDimension, maxDimension, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(squareBitmap)
            canvas.drawColor(Color.White.toArgb())
            val left = (maxDimension - bitmap.width) / 2
            val top = (maxDimension - bitmap.height) / 2
            canvas.drawBitmap(bitmap, left.toFloat(), top.toFloat(), null)
            squareBitmap
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(2.dp, Color.Transparent, CircleShape)
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    currentScale.value = (currentScale.value * zoom).coerceIn(1f, maxZoom)
                    val scaledWidth = imageSize.width * currentScale.value
                    val scaledHeight = imageSize.height * currentScale.value
                    val maxX = (scaledWidth - size.width).coerceAtLeast(0f) / 2
                    val maxY = (scaledHeight - size.height).coerceAtLeast(0f) / 2
                    currentOffset.value = Offset(
                        (currentOffset.value.x + pan.x * currentScale.value).coerceIn(-maxX, maxX),
                        (currentOffset.value.y + pan.y * currentScale.value).coerceIn(-maxY, maxY)
                    )
                }
            }
            .onGloballyPositioned { layoutCoordinates ->
                imageSize = layoutCoordinates.size
            }
    ) {
        Image(
            bitmap = squareBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = currentScale.value,
                    scaleY = currentScale.value,
                    translationX = currentOffset.value.x,
                    translationY = currentOffset.value.y
                ),
            contentScale = ContentScale.Fit
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
        genderState = genderState
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSecondConfigurationStage() {
    // Dummy context for preview
    val context = LocalContext.current
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    // Dummy sendData function
    fun dummySendData(context: Context, uri: Uri?) {
    }

//    SecondConfigurationStage(
//        modifier = Modifier.fillMaxSize(),
//        sendData = ::dummySendData,
//        requestPermissionLauncher = ActivityResultLauncher.
//    )
}
