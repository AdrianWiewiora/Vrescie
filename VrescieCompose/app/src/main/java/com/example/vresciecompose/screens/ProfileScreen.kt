package com.example.vresciecompose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vresciecompose.authentication.UserProfile
import com.example.vresciecompose.view_models.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel? = viewModel()
) {
    val userProfile = profileViewModel?.userProfile

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
                    if (userProfile != null) {
                        if (userProfile.value != null) {
                            ProfileContent(userProfile.value!!)
                        } else {
                            Text(
                                text = "Ładowanie danych",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

        }
    }

}

@Composable
fun ProfileContent(userProfile: UserProfile) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        item {
            UserInfoRow("Imię", userProfile.name)
            UserInfoRow("Wiek", userProfile.age.toString())
            UserInfoRow("Email", userProfile.email)
            UserInfoRow("Płeć", userProfile.gender)
            UserInfoRow("Data dołączenia", formatDate(userProfile.joinDate))
        }
    }
}


@Composable
fun formatDate(joinDate: String): String {
    val timestamp = joinDate.toLongOrNull() ?: return ""
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

@Composable
fun UserInfoRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = key)
        Text(text = value)
    }
}

@Preview
@Composable
fun PreviewProfileScreen(){
    ProfileScreen()
}