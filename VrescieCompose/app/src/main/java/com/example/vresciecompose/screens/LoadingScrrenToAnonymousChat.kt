package com.example.vresciecompose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.view_models.LoadingToAnonymousChatViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun LoadingToAnonymousChatScreen(
    onClick: (String) -> Unit,
    loadingToAnonymousChatViewModel: LoadingToAnonymousChatViewModel
) {
    val hasNavigated = remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            loadingToAnonymousChatViewModel.listenForNewConversations(userId)
            loadingToAnonymousChatViewModel.startTimer(userId)
        }
    }

    DisposableEffect(true) {
        onDispose {
            currentUser?.uid?.let { userId ->
                loadingToAnonymousChatViewModel.stopTimer()  // Zatrzymujemy timer
                loadingToAnonymousChatViewModel.removeUserFromFirebaseDatabase(userId)
            }
        }
    }

    val navigateToConversation by loadingToAnonymousChatViewModel.navigateToConversation.observeAsState()
    navigateToConversation?.let { conversationId ->
        if (!hasNavigated.value) {
            onClick("${Navigation.Destinations.ANONYMOUS_CONVERSATION}/$conversationId")
            hasNavigated.value = true
            loadingToAnonymousChatViewModel.resetNavigation()
        }
    }

    LoadingScreenImageAndCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .padding(vertical = 0.dp),
    )
}

@Composable
fun LoadingScreenImageAndCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = "logotype",
            modifier = Modifier.size(width = 198.dp, height = 47.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 5.dp, bottom = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.wait_we_are_looking) ,
                    modifier = Modifier
                        .padding(horizontal = 30.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    LoadingScreenImageAndCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .padding(vertical = 0.dp),
    )
}