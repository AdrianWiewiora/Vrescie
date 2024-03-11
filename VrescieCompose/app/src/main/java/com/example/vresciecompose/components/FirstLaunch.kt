package com.example.vresciecompose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.vresciecompose.R
import kotlinx.coroutines.delay

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun FirstLaunch(navController: NavHostController) {
    val textArray = remember { arrayOf("Nowi znajomi", "Nowi przyjaciele", "Nowa miłość", "Szczęśliwi") }
    val timerDuration = 4000L

    var currentStep by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        repeat(textArray.size) { index ->
            currentStep = index
            delay(timerDuration)
        }
        delay(1000)
        navController.navigate("start")
    }

    val alpha = remember { Animatable(0f) } // Zaczynamy od wartości 0, aby tekst był niewidoczny na początku

    Column(
        modifier = Modifier.fillMaxSize().background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )

        Crossfade(
            targetState = textArray[currentStep],
            modifier = Modifier.alpha(alpha.value),
            label = "",
        ) { currentText ->
            LaunchedEffect(currentText) {
                alpha.animateTo(1f, animationSpec = tween(durationMillis = 2000)) // Animacja z 0 do 1 (pojawienie się nowego tekstu)
                alpha.animateTo(0f, animationSpec = tween(durationMillis = 2000)) // Animacja z obecnej alfy do 0 (wygaszanie)
            }

            Text(
                textAlign = TextAlign.Center,
                text = currentText,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun FirstLaunchPreview() {
    val navController = rememberNavController()
    FirstLaunch(navController = navController)
}