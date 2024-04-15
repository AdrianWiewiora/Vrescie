package com.example.vresciecompose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AnonymousChatConfigurationScreen() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = com.example.vresciecompose.R.drawable.logotype_vreescie_svg),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 198.dp, height = 47.dp)
                    .padding(2.dp)
            )

            Image(
                painter = painterResource(id = com.example.vresciecompose.R.drawable.baseline_settings_24),
                contentDescription = null,
                modifier = Modifier
                    .size(52.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .padding(vertical = 0.dp),

            ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 5.dp, bottom = 8.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color.Black),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White, //Card background color
                    contentColor = Color.Black  //Card content color,e.g.text
                )

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),

                        ) {
                        Text(
                            text = "Płeć",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = false,
                                onCheckedChange = { /* Ignored for now */ },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Kobieta",
                                color = Color.Black
                            )
                            Checkbox(
                                checked = false,
                                onCheckedChange = { /* Ignored for now */ },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = "Mężczyzna",
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Wiek:",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        /*                RangeSlider(
                                            value = remember { listOf(18f, 70f) },
                                            onValueChange = { *//* Ignored for now *//* },
                    valueRange = 18f..70f,
                    steps = 1,
                    modifier = Modifier.fillMaxWidth()
                    )*/

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Zweryfikowany:",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = false,
                                onClick = { /* Ignored for now */ },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Zweryfikowany",
                                color = Color.Black
                            )
                            RadioButton(
                                selected = false,
                                onClick = { /* Ignored for now */ },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = "Nie zweryfikowany",
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Preferencje:",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = false,
                                onClick = { /* Ignored for now */ },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Stała relacja",
                                color = Color.Black
                            )
                            RadioButton(
                                selected = false,
                                onClick = { /* Ignored for now */ },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = "Krótka relacja",
                                color = Color.Black
                            )
                        }
                    }
                    Button(
                        onClick = { /* Ignored for now */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                    ) {
                        Text(
                            text = "Losuj",
                            color = Color.White
                        )
                    }

                }

            }

        }
    }
}

@Preview
@Composable
fun AnonymousChatConfigurationScreenPreview() {
    AnonymousChatConfigurationScreen()
}
