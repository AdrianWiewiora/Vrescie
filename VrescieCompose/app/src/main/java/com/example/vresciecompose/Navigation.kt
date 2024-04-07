package com.example.vresciecompose

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vresciecompose.authentication.GoogleAuthentication
import com.example.vresciecompose.authentication.SignInViewModel
import com.example.vresciecompose.screenss.FirstLaunchScreen
import com.example.vresciecompose.screenss.MainMenuScreens
import com.example.vresciecompose.screenss.StartScreens
import com.example.vresciecompose.view_models.StartScreenViewModel
import kotlinx.coroutines.launch
import kotlin.contracts.contract


object Navigation {

    object Destinations {
        const val MAIN_MENU = "main_menu"
        const val FIRST_LAUNCH = "first_launch"
        const val START = "start"
        const val LOGIN = "login"
        const val REGISTRATION = "registration"
        const val REGISTRATION_GOOGLE = "registration_google"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    startScreenViewModel: StartScreenViewModel,
    googleAuthClient: GoogleAuthentication,
    lifecycleScope: LifecycleCoroutineScope,
    applicationContext: Context
) {

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Navigation.Destinations.MAIN_MENU) {
            MainMenuScreens(onClick = {
                navController.navigate(route = it)
            })
        }
        composable(Navigation.Destinations.FIRST_LAUNCH) {
            FirstLaunchScreen(onClose = {
                navController.navigate(
                    route = Navigation.Destinations.START)
            })
        }
        composable(Navigation.Destinations.START) {
            val viewModel = viewModel<SignInViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {result ->
                    if(result.resultCode == RESULT_OK) {
                        lifecycleScope.launch {
                            val signInResult = googleAuthClient.signInWithIntent(
                                intent = result.data ?: return@launch
                            )
                            viewModel.oneSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(key1 = state.isSignedSuccessful) {
                if(state.isSignedSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Sign in successful",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            StartScreens(
                viewModel = startScreenViewModel,
                onClick = {
                    navController.navigate(route = it)
                },
                onConfirmExit = {
                    navController.navigateUp()
                },
                state = state,
                onSignInClick = {
                    lifecycleScope.launch {
                        val signInIntentSender = googleAuthClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                signInIntentSender ?: return@launch
                            ).build()
                        )
                    }
                }
                )
        }
    }
}

