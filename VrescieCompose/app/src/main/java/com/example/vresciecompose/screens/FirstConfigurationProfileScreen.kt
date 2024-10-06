package com.example.vresciecompose.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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

@Composable
fun FirstConfigurationProfileScreen(
    onClick: (String) -> Unit,
    configurationProfileViewModel: ConfigurationProfileViewModel,
    profileViewModel: ProfileViewModel
) {

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


    FirstConfigurationStage(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        nameState,
        ageState,
        genderState,
        sendData = ::sendData,
    )
}

@Composable
fun FirstConfigurationStage(
    modifier: Modifier = Modifier,
    nameState: MutableState<String> = remember { mutableStateOf("") },
    ageState: MutableState<String> = remember { mutableStateOf("") },
    genderState: MutableState<String> = remember { mutableStateOf("") },
    sendData: () -> Unit,
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
            text = "Konfiguracja twojego profilu",
            modifier = Modifier.padding(bottom = 20.dp, top = 90.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text(text = "Podaj swoje imię") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = ageState.value,
            onValueChange = { ageState.value = it },
            label = { Text(text = "Podaj swoj wiek") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        Text(
            text = "Wybierz swoją płeć",
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
                    text = "Mężczyzna",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = genderState.value == "female",
                    onClick = { genderState.value = "female" }
                )
                Text(
                    text = "Kobieta",
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
                    text = "Inna",
                    style = MaterialTheme.typography.bodyLarge
                )
            }


        }
        FilledButton(
            onClick = {sendData() },
            text = "Kontynuuj",
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
