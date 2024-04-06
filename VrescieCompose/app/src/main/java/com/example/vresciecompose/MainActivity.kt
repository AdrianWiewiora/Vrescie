package com.example.vresciecompose

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
import androidx.navigation.compose.rememberNavController
import com.example.vresciecompose.screens.LocalBackPressedDispatcher
import com.example.vresciecompose.ui.theme.VrescieComposeTheme
import com.example.vresciecompose.view_models.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var backDispatcher: OnBackPressedDispatcher

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(getSharedPreferences("MyPrefs", Context.MODE_PRIVATE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        backDispatcher = onBackPressedDispatcher

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }

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
                        AppNavigation(navController, startDestination)
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

class MainViewModelFactory(private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
