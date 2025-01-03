package com.example.vresciecompose.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.R
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.example.vresciecompose.ui.components.FilledButton
import com.example.vresciecompose.ui.components.OutlinedButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.authentication.SignInState
import com.example.vresciecompose.ui.components.ExitConfirmationDialog


@Composable
fun StartScreen(
    onClick:(String) -> Unit,
    state: SignInState,
    onSignInClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    BackHandler {
        showDialog = true
    }

    if (showDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

    StartScreenColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        onClick = onClick,
        onSignInClick = onSignInClick
    )

}

@Composable
fun StartScreenColumn(
    modifier: Modifier,
    onClick:(String) -> Unit = {},
    onSignInClick: () -> Unit = {}
){

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 40.dp)
                .padding(horizontal = 20.dp)
        )

        OutlinedButton(
            onClick = {
                onClick(Navigation.Destinations.LOGIN)
            },
            text = stringResource(R.string.log_in),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

        FilledButton(
            onClick = {
                onClick(Navigation.Destinations.REGISTRATION)
            },
            text = stringResource(R.string.register),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.or),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(vertical = 10.dp)
        )

        FilledButton(
            onClick = onSignInClick,
            text = stringResource(R.string.continue_with_google),
            icon = R.drawable.google_svgrepo_com,
            iconSize = 28,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.we_never_share_wyc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 10.dp)

        )

        Text(
            text = stringResource(R.string.info_privacy_policy),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 10.dp)

        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreenColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        onClick = {},
        onSignInClick = {}
    )
}



