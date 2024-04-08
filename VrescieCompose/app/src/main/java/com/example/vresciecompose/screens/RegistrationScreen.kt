package com.example.vresciecompose.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onFocusedBoundsChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.BlackButton
import com.example.vresciecompose.view_models.RegistrationViewModel


@Composable
fun RegistrationScreen(
    onClick:(String) -> Unit,
    registrationViewModel: RegistrationViewModel
) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var repeatPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val registrationSuccess by registrationViewModel.registrationSuccess.collectAsState()
    val errorMessage by registrationViewModel.errorMessage.collectAsState()

    var isErrorShown by remember { mutableStateOf(false) }

    if (registrationSuccess) {
        Toast.makeText(
            LocalContext.current,
            "Sign in successful",
            Toast.LENGTH_LONG
        ).show()
        onClick(Navigation.Destinations.FIRST_CONFIGURATION)
    } else if (!errorMessage.isNullOrEmpty() && !isErrorShown) {
        isErrorShown = true
        Toast.makeText(
            LocalContext.current,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = "Vrescie Logo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp,90.dp,10.dp, 130.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )

        Text(
            text = "Podaj swój e-mail i hasło",
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black
            ),
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "E-mail") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedTrailingIconColor = Color.Black
            ),
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Hasło") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, description)
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedTrailingIconColor = Color.Black
            ),
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = { Text(text = "Powtórz hasło") },
            singleLine = true,
            visualTransformation = if (repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (repeatPasswordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (repeatPasswordVisible) "Hide password" else "Show password"

                IconButton(onClick = {repeatPasswordVisible = !repeatPasswordVisible}){
                    Icon(imageVector  = image, description)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        BlackButton(
            onClick = {
                registrationViewModel.registerWithEmail(email, password, repeatPassword)
            },
            text = "Zarejestruj się",
        )
    }
}

@Preview
@Composable
fun RegistrationScreenPreview() {
    val viewModel = RegistrationViewModel() // Tworzenie instancji view model
    RegistrationScreen(onClick = {}, registrationViewModel = viewModel)
}