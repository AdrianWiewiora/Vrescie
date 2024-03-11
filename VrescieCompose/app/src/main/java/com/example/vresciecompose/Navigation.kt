package com.example.vresciecompose

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vresciecompose.components.FirstLaunch
import com.example.vresciecompose.components.MainMenu
import com.example.vresciecompose.components.Start

object Navigation {

    object Destinations {
        const val MainMenu = "main_menu"
        const val FirstLaunch = "first_launch"
        const val Start = "start"
    }
}

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Navigation.Destinations.MainMenu) {
            MainMenu(navController)
        }
        composable(Navigation.Destinations.FirstLaunch) {
            FirstLaunch(navController)
        }
        composable(Navigation.Destinations.Start) {
            Start(navController)
        }
    }
}

