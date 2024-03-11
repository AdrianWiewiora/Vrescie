package com.example.vresciecompose.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController

@Composable
fun Start(navController: NavHostController) {
    // Komponent do wyświetlania ekranu startowego
    Text(
        text = "Hello start!"
    )
}