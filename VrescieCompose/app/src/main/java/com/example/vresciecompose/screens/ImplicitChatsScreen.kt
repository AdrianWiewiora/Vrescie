package com.example.vresciecompose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImplicitChatsScreen() {



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
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Jeszcze nic tu nie ma :(\nPostaraj się o polubienia\ninnych użytkowników\nw anonimowym czacie aby\n pojawiły się tu konwersacje",
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 40.dp, horizontal = 20.dp)

                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun ImplicitChatsScreenPreview() {
    ImplicitChatsScreen()
}
