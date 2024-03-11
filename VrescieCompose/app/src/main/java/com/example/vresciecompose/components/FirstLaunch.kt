package com.example.vresciecompose.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun FirstLaunch(navController: NavHostController) {

    val textArray =
        remember { arrayOf("Nowi znajomi", "Nowi przyjaciele", "Nowa miłość", "Szczęśliwi") }
    val timerDuration = 3000L
    val totalDuration = timerDuration * textArray.size

    var currentStep by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        repeat(textArray.size) { index ->
            currentStep = index
            delay(timerDuration)
        }
        delay(1000)
        navController.navigate("start")
    }

    val alphaText = remember { Animatable(0f) } // Animacja dla tekstu
    val alphaImage = remember { Animatable(0f) } // Animacja dla obrazka

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animacja pojawiania się obrazka
        LaunchedEffect(Unit) {
            alphaImage.animateTo(1f, animationSpec = tween(durationMillis = 1000))
            delay(totalDuration - 1000)
            alphaImage.animateTo(0f, animationSpec = tween(durationMillis = 1000))
        }

        // Obrazek
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .padding(horizontal = 20.dp)
                .alpha(alphaImage.value)
        )

        // Tekst
        Crossfade(
            targetState = textArray[currentStep],
            modifier = Modifier.alpha(alphaText.value),
            label = ""
        ) { currentText ->
            LaunchedEffect(currentText) {
                alphaText.animateTo(
                    1f,
                    animationSpec = tween(durationMillis = 1500)
                ) // Animacja z 0 do 1 (pojawienie się nowego tekstu)
                alphaText.animateTo(
                    0f,
                    animationSpec = tween(durationMillis = 1500)
                ) // Animacja z obecnej alfy do 0 (wygaszanie)
            }

            Text(
                textAlign = TextAlign.Center,
                text = currentText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
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