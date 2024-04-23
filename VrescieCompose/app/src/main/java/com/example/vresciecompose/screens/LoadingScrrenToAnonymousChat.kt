package com.example.vresciecompose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


@Composable
fun LoadingToAnonymousChatScreen(onClick: (String) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    DisposableEffect(true) {
        onDispose {
            currentUser?.uid?.let { userId ->
                removeUserFromFirebaseDatabase(userId)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .padding(vertical = 0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = com.example.vresciecompose.R.drawable.logotype_vreescie_svg),
            contentDescription = "logotype",
            modifier = Modifier.size(width = 198.dp, height = 47.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 5.dp, bottom = 8.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color.Black),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text ="Czekaj!!!\n\nTrwa szukanie osoby odpowiedniej dla twoich preferencji" ,
                    modifier = Modifier
                        .padding(horizontal = 30.dp),
                    color = Color.Black,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        lineHeight = 40.sp,
                    )
                )
            }
        }
    }
}

fun removeUserFromFirebaseDatabase(userId: String) {
    val database = Firebase.database
    val usersRef = database.getReference("vChatUsers")
    usersRef.child(userId).removeValue()
}

@Preview(showBackground = true)
@Composable
fun LoadingToAnonymousChatScreenPreview() {
    LoadingToAnonymousChatScreen {}
}