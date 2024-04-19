package com.example.vresciecompose

import ProvideContext
import android.app.Activity.RESULT_OK
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vresciecompose.authentication.GoogleAuthentication
import com.example.vresciecompose.authentication.SignInViewModel
import com.example.vresciecompose.screens.FirstConfigurationProfileScreen
import com.example.vresciecompose.screens.FirstLaunchScreen
import com.example.vresciecompose.screens.LoginScreen
import com.example.vresciecompose.screens.MainMenuScreen
import com.example.vresciecompose.screens.RegistrationScreen
import com.example.vresciecompose.screens.StartScreens
import com.example.vresciecompose.view_models.ConfigurationProfileViewModel
import com.example.vresciecompose.view_models.LocationViewModel
import com.example.vresciecompose.view_models.LoginViewModel
import com.example.vresciecompose.view_models.ProfileViewModel
import com.example.vresciecompose.view_models.RegistrationViewModel
import com.example.vresciecompose.view_models.StartScreenViewModel
import kotlinx.coroutines.launch


object Navigation {

    object Destinations {
        const val MAIN_MENU = "main_menu"
        const val FIRST_LAUNCH = "first_launch"
        const val START = "start"
        const val LOGIN = "login"
        const val REGISTRATION = "registration"
        const val FIRST_CONFIGURATION = "first_configuration"

    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    startScreenViewModel: StartScreenViewModel,
    googleAuthClient: GoogleAuthentication,
    lifecycleScope: LifecycleCoroutineScope,
    applicationContext: Context,
    registrationViewModel: RegistrationViewModel,
    loginViewModel: LoginViewModel,
    configurationProfileViewModel: ConfigurationProfileViewModel,
    profileViewModel: ProfileViewModel,
    locationViewModel: LocationViewModel
) {
    ProvideContext {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Navigation.Destinations.MAIN_MENU) {
                MainMenuScreen(onClick = {
                    navController.navigate(route = it)
                }, profileViewModel = profileViewModel,
                    locationViewModel = locationViewModel)
            }
            composable(Navigation.Destinations.FIRST_LAUNCH) {
                FirstLaunchScreen(onClose = {
                    navController.navigate(
                        route = Navigation.Destinations.START
                    )
                })
            }
            composable(Navigation.Destinations.START) {
                val viewModel = viewModel<SignInViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if (result.resultCode == RESULT_OK) {
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
                    if (state.isSignedSuccessful) {
                        if (state.isNewAccount) {
                            Toast.makeText(
                                applicationContext,
                                "Sign in successful",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate(Navigation.Destinations.FIRST_CONFIGURATION)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Log in successful",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate(Navigation.Destinations.MAIN_MENU)
                        }
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
            composable(Navigation.Destinations.FIRST_CONFIGURATION) {
                FirstConfigurationProfileScreen(
                    onClick = { navController.navigate(route = it) },
                    configurationProfileViewModel = configurationProfileViewModel
                )
            }
            composable(Navigation.Destinations.REGISTRATION) {
                RegistrationScreen(
                    onClick = { route -> navController.navigate(route) },
                    registrationViewModel = registrationViewModel
                )
            }
            composable(Navigation.Destinations.LOGIN) {
                LoginScreen(
                    loginViewModel = loginViewModel,
                    onClick = { navController.navigate(route = it) }
                )
            }
        }
    }
}

