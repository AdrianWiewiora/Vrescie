package com.example.vresciecompose.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun MainMenu(navController: NavHostController) {
    // Komponent głównego menu
    Text(
        text = "Hello main menu!"
    )
}