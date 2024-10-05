package com.example.vresciecompose.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vresciecompose.R

@Composable
fun FilledButton(
    onClick: () -> Unit,
    text: String,
    icon: Int? = null,
    iconSize: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {


    Button(
        onClick = onClick,
        modifier = modifier,
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    val painter = painterResource(icon)
                    val contentDescription = null
                    Image(
                        painter = painter,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(iconSize?.dp ?: 24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        enabled = enabled
    )
}

@Preview(showBackground = true)
@Composable
fun FilledButtonPreview(){
    FilledButton(
        onClick = {},
        text = "Zaloguj się",
        icon = R.drawable.google_svgrepo_com,
        iconSize = 28
    )
}