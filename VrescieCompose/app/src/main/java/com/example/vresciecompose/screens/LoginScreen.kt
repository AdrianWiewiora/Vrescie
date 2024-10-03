package com.example.vresciecompose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.ErrorAlertDialog
import com.example.vresciecompose.ui.components.FilledButton
import com.example.vresciecompose.ui.components.OutlinedButton
import com.example.vresciecompose.view_models.LoginViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onClick:(String) -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var showErrorDialog by rememberSaveable { mutableStateOf(false) } // Flaga dialogu

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(20.dp)
        )

        Column(
            verticalArrangement = Arrangement.Center,
        ){
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "E-mail") },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 0.dp)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(25.dp)
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Hasło") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
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

            // Login button
            FilledButton(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        showErrorDialog = true
                    } else {
                        loginViewModel.signInWithEmail(email, password,
                            onSuccess = {
                                onClick(Navigation.Destinations.MAIN_MENU+"/1")
                            },
                            onFailure = {
                                showErrorDialog = true
                            }
                        )
                    }
                },
                text = "Zaloguj się",
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .fillMaxWidth(),
            )

            // Forgot password button
            OutlinedButton(
                onClick = { /*TODO*/ },
                text = "Nie pamiętam hasła",
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .fillMaxWidth(),
            )

        }

        if (showErrorDialog) {
            ErrorAlertDialog(onDismiss = { showErrorDialog = false })
        }
    }
}


@Preview
@Composable
fun LoginScreenPreview() {
    val loginViewModel = LoginViewModel()

    LoginScreen(
        loginViewModel = loginViewModel,
        onClick = {}
    )
}

