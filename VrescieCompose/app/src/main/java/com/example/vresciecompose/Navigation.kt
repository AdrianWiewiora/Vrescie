package com.example.vresciecompose

import android.app.Activity.RESULT_OK
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.vresciecompose.authentication.GoogleAuthentication
import com.example.vresciecompose.view_models.SignInViewModel
import com.example.vresciecompose.screens.AnonymousConversationScreen
import com.example.vresciecompose.screens.ExplicitConversationScreen
import com.example.vresciecompose.screens.FirstConfigurationProfileScreen
import com.example.vresciecompose.screens.FirstLaunchScreen
import com.example.vresciecompose.screens.LoadingToAnonymousChatScreen
import com.example.vresciecompose.screens.LoginScreen
import com.example.vresciecompose.screens.MainMenuScreen
import com.example.vresciecompose.screens.RegistrationScreen
import com.example.vresciecompose.screens.SettingsScreen
import com.example.vresciecompose.screens.StartScreen
import com.example.vresciecompose.view_models.ConfigurationProfileViewModel
import com.example.vresciecompose.view_models.ConversationViewModel
import com.example.vresciecompose.view_models.LoadingToAnonymousChatViewModel
import com.example.vresciecompose.view_models.LocationViewModel
import com.example.vresciecompose.view_models.LoginViewModel
import com.example.vresciecompose.view_models.ProfileViewModel
import com.example.vresciecompose.view_models.RegistrationViewModel
import com.example.vresciecompose.view_models.SettingsViewModel
import com.example.vresciecompose.view_models.UserChatPrefsViewModel
import kotlinx.coroutines.launch


object Navigation {

    object Destinations {
        const val MAIN_MENU = "main_menu"
        const val FIRST_LAUNCH = "first_launch"
        const val START = "start"
        const val LOGIN = "login"
        const val REGISTRATION = "registration"
        const val FIRST_CONFIGURATION = "first_configuration"
        const val LOADING_SCREEN_TO_V_CHAT = "loading_screen_to_v_chat"
        const val ANONYMOUS_CONVERSATION = "anonymous_conversation"
        const val EXPLICIT_CONVERSATION = "explicit_conversation"
        const val SETTINGS = "settings"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    googleAuthClient: GoogleAuthentication,
    lifecycleScope: LifecycleCoroutineScope,
    applicationContext: Context,
    registrationViewModel: RegistrationViewModel,
    loginViewModel: LoginViewModel,
    configurationProfileViewModel: ConfigurationProfileViewModel,
    profileViewModel: ProfileViewModel,
    locationViewModel: LocationViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    conversationViewModel: ConversationViewModel,
    userChatPrefsViewModel: UserChatPrefsViewModel,
    settingsViewModel: SettingsViewModel,
    loadingToAnonymousChatViewModel: LoadingToAnonymousChatViewModel
    ) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Navigation.Destinations.SETTINGS) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigate = { route -> navController.navigate(route) },
            )
        }
        composable(Navigation.Destinations.MAIN_MENU + "/{defaultFragment}",
            arguments = listOf(navArgument("defaultFragment") { type = NavType.StringType })
        ) { backStackEntry ->
            val defaultFragment = backStackEntry.arguments?.getString("defaultFragment") ?: "1"
            MainMenuScreen(
                onClick = { route -> navController.navigate(route) },
                profileViewModel = profileViewModel,
                locationViewModel = locationViewModel,
                requestPermissionLauncher = requestPermissionLauncher,
                defaultFragment = defaultFragment,
                userChatPrefsViewModel = userChatPrefsViewModel,
                conversationViewModel = conversationViewModel
            )
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
                        navController.navigate(Navigation.Destinations.FIRST_CONFIGURATION+ "/0")
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Log in successful",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigate(Navigation.Destinations.MAIN_MENU + "/1")
                    }
                }
            }

            StartScreen(
                onClick = {
                    navController.navigate(route = it)
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
        composable(Navigation.Destinations.FIRST_CONFIGURATION + "/{isChangePhoto}",
            arguments = listOf(navArgument("isChangePhoto") { type = NavType.StringType })
        ) { backStackEntry ->
            val isChangePhoto = backStackEntry.arguments?.getString("isChangePhoto") ?: "0"
            FirstConfigurationProfileScreen(
                onClick = { navController.navigate(route = it) },
                configurationProfileViewModel = configurationProfileViewModel,
                profileViewModel = profileViewModel,
                isChangePhoto = isChangePhoto.toInt(),
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
        composable(Navigation.Destinations.LOADING_SCREEN_TO_V_CHAT) {
            LoadingToAnonymousChatScreen(
                onClick = { route -> navController.navigate(route) },
                loadingToAnonymousChatViewModel
            )
        }

        composable(Navigation.Destinations.ANONYMOUS_CONVERSATION + "/{conversationID}",
            arguments = listOf(navArgument("conversationID") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationID = backStackEntry.arguments?.getString("conversationID") ?: ""
            AnonymousConversationScreen(
                conversationID = conversationID,
                onNavigate = { route -> navController.navigate(route);},
                conversationViewModel,
                settingsViewModel = settingsViewModel
            )
        }
        composable(Navigation.Destinations.EXPLICIT_CONVERSATION + "/{conversationID}",
            arguments = listOf(navArgument("conversationID") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationID = backStackEntry.arguments?.getString("conversationID") ?: ""
            ExplicitConversationScreen(
                conversationID = conversationID,
                onClick = { route -> navController.navigate(route) },
                conversationViewModel,
                settingsViewModel = settingsViewModel
            )
        }

    }
}

