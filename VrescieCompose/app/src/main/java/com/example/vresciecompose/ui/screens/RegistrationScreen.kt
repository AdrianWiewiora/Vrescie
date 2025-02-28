package com.example.vresciecompose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.ui.components.ErrorAlertDialog
import com.example.vresciecompose.ui.components.FilledButton
import com.example.vresciecompose.view_models.RegistrationViewModel


@Composable
fun RegistrationScreen(
    navigateTo:(String) -> Unit,
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

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrEmpty()) {
            isErrorShown = true
        }
    }

    if (registrationSuccess) {
        AlertDialog(
            onDismissRequest = { navigateTo(Navigation.Destinations.LOGIN)  },
            title = {
                Text(text = stringResource(R.string.you_have_registered))
            },
            text = {
                Text(text = stringResource(R.string.remember_to_confirm_your_account))
            },
            confirmButton = {
                Button(onClick = {navigateTo(Navigation.Destinations.LOGIN) }) {
                    Text("Ok")
                }
            }
        )
    }

    // Wyświetlanie ErrorAlertDialog, jeśli jest błąd
    if (isErrorShown) {
        ErrorAlertDialog(
            onDismiss = { isErrorShown = false }, // Zamykanie dialogu
            text1 = stringResource(R.string.registration_error),
            text2 = errorMessage ?: stringResource(R.string.an_unknown_error_occurred)
        )
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
                .padding(10.dp, 90.dp, 10.dp, 130.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )

        Text(
            text = stringResource(R.string.enter_your_email_and_password),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "E-mail") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(25.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, description)
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = { Text(text = stringResource(R.string.repeat_password)) },
            singleLine = true,
            visualTransformation = if (repeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            trailingIcon = {
                val image = if (repeatPasswordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (repeatPasswordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)

                IconButton(onClick = {repeatPasswordVisible = !repeatPasswordVisible}){
                    Icon(imageVector  = image, description)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        FilledButton(
            onClick = {
                registrationViewModel.registerWithEmail(email, password, repeatPassword)
            },
            text = stringResource(R.string.sign_up),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
fun RegistrationScreenPreview() {
    val viewModel = RegistrationViewModel() // Tworzenie instancji view model
    RegistrationScreen(navigateTo = {}, registrationViewModel = viewModel)
}