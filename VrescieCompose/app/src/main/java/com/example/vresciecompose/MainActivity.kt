package com.example.vresciecompose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.vresciecompose.components.MainViewModel
import com.example.vresciecompose.ui.theme.VrescieComposeTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }

        super.onCreate(savedInstanceState)
        if(viewModel.isReady.value){
            val startDestination = when {
                viewModel.isFirstRun() -> Navigation.Destinations.FirstLaunch
                viewModel.isLoggedIn() -> Navigation.Destinations.MainMenu
                else -> Navigation.Destinations.Start
            }
            setContent {
                VrescieComposeTheme {
                    val navController = rememberNavController()
                    AppNavigation(navController, startDestination)
                }
            }
            // Ustawienie isFirstRun na false dopiero po ustawieniu setContent na FirstLaunch
            if (startDestination == Navigation.Destinations.FirstLaunch) {
                applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply()
            }
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
