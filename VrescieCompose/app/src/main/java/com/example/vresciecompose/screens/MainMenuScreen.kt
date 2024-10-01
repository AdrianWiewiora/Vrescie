package com.example.vresciecompose.screens

import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.R
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.example.vresciecompose.view_models.LocationViewModel
import com.example.vresciecompose.view_models.ProfileViewModel
import androidx.activity.compose.BackHandler
import com.example.vresciecompose.ui.components.ExitConfirmationDialog

@Composable
fun MainMenuScreen(
    onClick: (String) -> Unit,
    profileViewModel: ProfileViewModel,
    locationViewModel: LocationViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    defaultFragment: String,
) {
    Log.d("MainMenuScreen", "Default Fragment received: $defaultFragment")

    val (currentFragment, setCurrentFragment) = remember { mutableIntStateOf(defaultFragment.toInt()) }
    val showDialog = remember { mutableStateOf(false) }
    if(currentFragment == 0) setCurrentFragment(1)

    if (showDialog.value) {
        ExitConfirmationDialog(
            onConfirm = {
                showDialog.value = false
            },
            onDismiss = {
                showDialog.value = false
            }
        )
    }

    BackHandler {
        showDialog.value = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentFragment,
            label = "",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart)
                .padding(bottom = 78.dp)
        ) { target ->
            when (target) {
                1 -> AnonymousChatConfigurationScreen(locationViewModel,requestPermissionLauncher, onClick)
                2 -> ImplicitChatsScreen(onClick)
                3 -> ProfileScreen(profileViewModel)
            }
        }
        BottomMenu(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .align(Alignment.BottomStart),
            currentFragment,
            setCurrentFragment
        )
    }
}

@Composable
fun BottomMenu(
    modifier: Modifier = Modifier,
    currentFragment: Int,
    setCurrentFragment: (Int) -> Unit = {}
){
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            MenuItem(
                text = stringResource(id = R.string.v_czat_pl),
                icon = Icons.Filled.AddComment,
                onClick = { setCurrentFragment(1) },
                modifier = Modifier.weight(1f),
                color = if(currentFragment == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
            MenuItem(
                text = stringResource(id = R.string.chats_pl),
                icon = Icons.Filled.MarkChatRead,
                onClick = { setCurrentFragment(2) },
                modifier = Modifier.weight(1f),
                color = if(currentFragment == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
            MenuItem(
                text = stringResource(id = R.string.profile_pl),
                icon = Icons.Filled.AccountCircle,
                onClick = { setCurrentFragment(3) },
                modifier = Modifier.weight(1f),
                color = if(currentFragment == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxHeight()
            .wrapContentHeight(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(dimensionResource(R.dimen.image_medium_size))
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text,style = MaterialTheme.typography.labelSmall,color = color)

    }
}

@Preview
@Composable
fun BottomMenuPreview() {
    BottomMenu(currentFragment = 1)
}