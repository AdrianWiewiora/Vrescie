package com.example.vresciecompose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.R

@Composable
fun MainMenuScreen(onClick:(String) -> Unit) {
    val (currentFragment, setCurrentFragment) = remember { mutableStateOf(1) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.TopStart)
        ) {
            when (currentFragment) {
                1 -> AnonymousChatConfigurationScreen()
                2 -> ImplicitChatsScreen()
                3 -> ProfileScreen()
            }
        }

        // Row always at the bottom with fixed height
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .align(Alignment.BottomStart)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                MenuItem(
                    text = stringResource(id = R.string.v_czat_pl),
                    imageResource = R.drawable.chat_bubble_question,
                    onClick = { setCurrentFragment(1) },
                    modifier = Modifier.weight(1f)
                )
                MenuItem(
                    text = stringResource(id = R.string.chats_pl),
                    imageResource = R.drawable.chat_bubble_check,
                    onClick = { setCurrentFragment(2) },
                    modifier = Modifier.weight(1f)
                )
                MenuItem(
                    text = stringResource(id = R.string.profile_pl),
                    imageResource = R.drawable.user,
                    onClick = { setCurrentFragment(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MenuItem(text: String, imageResource: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxHeight()
            .wrapContentHeight(),
    ) {
        Image(
            painter = painterResource(id = imageResource),
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text)
    }
}

@Preview
@Composable
fun MainMenuScreenPreview() {
    MainMenuScreen(onClick = {})
}