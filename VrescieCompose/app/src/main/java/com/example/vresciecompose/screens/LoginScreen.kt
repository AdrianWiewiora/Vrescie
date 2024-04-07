package com.example.vresciecompose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.vresciecompose.R
import com.example.vresciecompose.authentication.SignInViewModel
import com.example.vresciecompose.ui.components.BlackButton
import com.example.vresciecompose.ui.components.WhiteOutlinedButton
import com.example.vresciecompose.view_models.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onClick:(String) -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    val email by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 40.dp)
                .padding(horizontal = 20.dp)
        )

        // Email field
        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                cursorColor = Color.Black
            ),
            value = email,
            onValueChange = { },
            label = { Text(text = "E-mail") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        // Password field
        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
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

        // Login button
        BlackButton(
            onClick = { },
            text = "Zaloguj się"
        )

        // Forgot password button
        WhiteOutlinedButton(
            onClick = { /*TODO*/ },
            text = "Nie pamiętam hasła"
        )
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

