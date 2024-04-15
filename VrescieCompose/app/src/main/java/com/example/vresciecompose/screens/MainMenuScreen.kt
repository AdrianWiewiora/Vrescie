package com.example.vresciecompose.screens

import android.app.Activity
import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.R
import androidx.compose.animation.Crossfade

@Composable
fun MainMenuScreen(onClick:(String) -> Unit) {
    val (currentFragment, setCurrentFragment) = remember { mutableIntStateOf(1) }
    val showDialog = remember { mutableStateOf(false) }
    val onBackPressedDispatcher = LocalBackPressedDispatcher.current


    DisposableEffect(key1 = onBackPressedDispatcher) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialog.value = true
            }
        }
        onBackPressedDispatcher.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    if (showDialog.value) {
        ExitConfirmationDialog(
            onConfirm = {
                // Handle exit confirmation
                showDialog.value = false
            },
            onDismiss = {
                // Dismiss dialog
                showDialog.value = false
            }
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentFragment,
            label = "",
            modifier = Modifier.fillMaxSize().align(Alignment.TopStart)
                .padding(bottom = 78.dp)
        ) { target ->
            when (target) {
                1 -> AnonymousChatConfigurationScreen()
                2 -> ImplicitChatsScreen()
                3 -> ProfileScreen()
            }
        }




        // Row always at the bottom with fixed height
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .align(Alignment.BottomStart)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                MenuItem(
                    text = stringResource(id = R.string.v_czat_pl),
                    imageResource = R.drawable.chat_bubble_question,
                    isSelected = currentFragment == 1,
                    onClick = { setCurrentFragment(1) },
                    modifier = Modifier.weight(1f)
                )
                MenuItem(
                    text = stringResource(id = R.string.chats_pl),
                    imageResource = R.drawable.chat_bubble_check,
                    isSelected = currentFragment == 2,
                    onClick = { setCurrentFragment(2) },
                    modifier = Modifier.weight(1f)
                )
                MenuItem(
                    text = stringResource(id = R.string.profile_pl),
                    imageResource = R.drawable.user,
                    isSelected = currentFragment == 3,
                    onClick = { setCurrentFragment(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    imageResource: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxHeight()
            .wrapContentHeight(),
    ) {
        val iconResource = if (isSelected) {
            when (imageResource) {
                R.drawable.chat_bubble_question -> R.drawable.chat_bubble_question_purple
                R.drawable.chat_bubble_check -> R.drawable.chat_bubble_check_purple
                R.drawable.user -> R.drawable.user_purple
                else -> imageResource
            }
        } else {
            imageResource
        }

        Image(
            painter = painterResource(id = iconResource),
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