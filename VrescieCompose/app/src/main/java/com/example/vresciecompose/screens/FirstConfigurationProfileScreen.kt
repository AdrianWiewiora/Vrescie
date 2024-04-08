package com.example.vresciecompose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.BlackButton
import com.example.vresciecompose.view_models.ConfigurationProfileViewModel
import com.example.vresciecompose.Navigation

@Composable
fun FirstConfigurationProfileScreen(
    onClick: (String) -> Unit,
    configurationProfileViewModel: ConfigurationProfileViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val nameState = remember { mutableStateOf("") }
    val ageState = remember { mutableStateOf("") }
    val genderState = remember { mutableStateOf("") }



    Image(
        painter = painterResource(id = R.drawable.logotype_vreescie_svg),
        contentDescription = null,
        modifier = Modifier
            .padding(top = 140.dp)
            .padding(horizontal = 20.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Konfiguracja twojego profilu",
            modifier = Modifier.padding(bottom = 20.dp, top = 90.dp),
            color = Color.Black,
            fontSize = 20.sp,
        )

        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedTrailingIconColor = Color.Black
            ),
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text(text = "Podaj swoje imię") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        )

        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedTrailingIconColor = Color.Black
            ),
            value = ageState.value,
            onValueChange = { ageState.value = it },
            label = { Text(text = "Podaj swoj wiek") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        )

        Text(
            text = "Wybierz swoją płeć",
            modifier = Modifier
                .padding(bottom = 10.dp)
                .padding(top = 30.dp),
            color = Color.Black,
            fontSize = 18.sp,
        )

        // Gender Radio Buttons
        Column(
            modifier = Modifier.padding(0.dp)
        ) {
            Row(modifier = Modifier.padding(0.dp)) {
                RadioButton(
                    selected = genderState.value == "male",
                    onClick = { genderState.value = "male" },
                    colors = RadioButtonColors(
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black,
                        selectedColor = Color.Black,
                        unselectedColor = Color.Gray,
                    )
                )
                Text(
                    text = "Mężczyzna",
                    color = Color.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Row() {
                RadioButton(
                    selected = genderState.value == "female",
                    onClick = { genderState.value = "female" },
                    colors = RadioButtonColors(
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black,
                        selectedColor = Color.Black,
                        unselectedColor = Color.Gray,
                    )
                )
                Text(
                    text = "Kobieta",
                    color = Color.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Row() {
                RadioButton(
                    selected = genderState.value == "other",
                    onClick = { genderState.value = "other" },
                    colors = RadioButtonColors(
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black,
                        selectedColor = Color.Black,
                        unselectedColor = Color.Gray,
                    )
                )
                Text(
                    text = "Inna",
                    color = Color.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }


        }
        BlackButton(
            onClick = {
                val name = nameState.value
                val age = ageState.value
                val gender = genderState.value

                configurationProfileViewModel.saveUserData(name, age, gender)
                onClick(Navigation.Destinations.MAIN_MENU)
            },
            text = "Kontynuuj",
        )
    }
}

@Preview
@Composable
fun FirstConfigurationProfileScreenPreview() {
    FirstConfigurationProfileScreen(onClick = {}, configurationProfileViewModel = ConfigurationProfileViewModel())
}