package com.example.vresciecompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vresciecompose.screenss.FirstLaunchScreen
import com.example.vresciecompose.screenss.MainMenuScreens
import com.example.vresciecompose.screenss.StartScreens
import com.example.vresciecompose.view_models.StartScreenViewModel


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
    startScreenViewModel: StartScreenViewModel
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
            StartScreens(
                viewModel = startScreenViewModel,
                onClick = {
                    navController.navigate(route = it)
                },
                onConfirmExit = {
                    navController.navigateUp()
                })
        }
    }
}

