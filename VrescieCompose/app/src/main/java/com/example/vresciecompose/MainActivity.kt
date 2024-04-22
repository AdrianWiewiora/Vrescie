package com.example.vresciecompose

import ProvideContext
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.vresciecompose.authentication.GoogleAuthentication
import com.example.vresciecompose.screens.LocalBackPressedDispatcher
import com.example.vresciecompose.ui.theme.VrescieComposeTheme
import com.example.vresciecompose.view_models.ConfigurationProfileViewModel
import com.example.vresciecompose.view_models.LocationViewModel
import com.example.vresciecompose.view_models.LoginViewModel
import com.example.vresciecompose.view_models.MainViewModel
import com.example.vresciecompose.view_models.ProfileViewModel
import com.example.vresciecompose.view_models.RegistrationViewModel
import com.example.vresciecompose.view_models.StartScreenViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var backDispatcher: OnBackPressedDispatcher

    private lateinit var startScreenViewModel: StartScreenViewModel
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var configurationProfileViewModel: ConfigurationProfileViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var locationViewModel: LocationViewModel


    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(getSharedPreferences("MyPrefs", Context.MODE_PRIVATE))
    }

    val googleAuthClient by lazy {
        GoogleAuthentication(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        backDispatcher = onBackPressedDispatcher

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }

        // Inicjalizacja ViewModel
        startScreenViewModel = ViewModelProvider(this).get(StartScreenViewModel::class.java)
        registrationViewModel = ViewModelProvider(this).get(RegistrationViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        configurationProfileViewModel =
            ViewModelProvider(this).get(ConfigurationProfileViewModel::class.java)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        super.onCreate(savedInstanceState)
        if (viewModel.isReady.value) {
            val startDestination = when {
                viewModel.isFirstRun() -> Navigation.Destinations.FIRST_LAUNCH
                viewModel.isLoggedIn() -> Navigation.Destinations.MAIN_MENU
                else -> Navigation.Destinations.START
            }
            setContent {
                CompositionLocalProvider(
                    LocalBackPressedDispatcher provides backDispatcher
                ) {
                    VrescieComposeTheme {
                        val navController = rememberNavController()
                        ProvideContext(context = applicationContext) {
                            AppNavigation(
                                navController,
                                startDestination,
                                startScreenViewModel,
                                googleAuthClient,
                                lifecycleScope,
                                applicationContext = applicationContext,
                                registrationViewModel,
                                loginViewModel,
                                configurationProfileViewModel,
                                profileViewModel,
                                locationViewModel
                            )
                        }
                    }
                }
            }
            // Ustawienie isFirstRun na false dopiero po ustawieniu setContent na FirstLaunch
            if (startDestination == Navigation.Destinations.FIRST_LAUNCH) {
                applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply()
            }
        }
    }
}

class MainViewModelFactory(private val sharedPreferences: SharedPreferences) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
