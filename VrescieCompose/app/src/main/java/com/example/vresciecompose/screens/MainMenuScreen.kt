package com.example.vresciecompose.screens

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.ui.components.ExitConfirmationDialog
import com.example.vresciecompose.view_models.ConversationViewModel
import com.example.vresciecompose.view_models.UserChatPrefsViewModel

@Composable
fun MainMenuScreen(
    onClick: (String) -> Unit,
    profileViewModel: ProfileViewModel,
    locationViewModel: LocationViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    defaultFragment: String,
    userChatPrefsViewModel: UserChatPrefsViewModel,
    conversationViewModel: ConversationViewModel
) {
    val isConnected by conversationViewModel.isConnected.collectAsState()
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

    val isProfileConfigured by profileViewModel.isProfileConfigured.observeAsState(initial = true)
    LaunchedEffect(isProfileConfigured) {
        if (!isProfileConfigured) {
            onClick(Navigation.Destinations.FIRST_CONFIGURATION+ "/0")
        }
    }

    WholeMenu(
        modifier = Modifier.fillMaxSize(),
        currentFragment,
        onClick,
        locationViewModel,
        requestPermissionLauncher,
        setCurrentFragment,
        userChatPrefsViewModel,
        conversationViewModel,
        isConnected,
        profileViewModel
    )

}

@Composable
fun WholeMenu(
    modifier: Modifier,
    currentFragment: Int,
    onClick: (String) -> Unit,
    locationViewModel: LocationViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    setCurrentFragment: (Int) -> Unit,
    userChatPrefsViewModel: UserChatPrefsViewModel,
    conversationViewModel: ConversationViewModel,
    isConnected: Boolean = true,
    profileViewModel: ProfileViewModel
){
    Column(modifier = modifier) {
        TopRowMenu(modifier = Modifier.fillMaxWidth(), onClick)

        MiddleCard(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(vertical = 0.dp)
                .weight(1f),
            currentFragment,
            onClick,
            locationViewModel,
            requestPermissionLauncher,
            userChatPrefsViewModel,
            conversationViewModel,
            isConnected,
            profileViewModel
        )

        BottomMenu(
            modifier = Modifier,
            currentFragment,
            setCurrentFragment
        )
    }
}

@Composable
fun TopRowMenu(modifier: Modifier, onClick: (String) -> Unit){
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logotype_vreescie_svg),
            contentDescription = null,
            modifier = Modifier
                .size(width = 198.dp, height = 47.dp)
                .padding(2.dp)
        )
        IconButton(
            onClick = {onClick(Navigation.Destinations.SETTINGS)},
            modifier = Modifier
                .size(dimensionResource(R.dimen.image_medium_size))
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.image_medium_size))
            )
        }
    }
}

@Composable
fun MiddleCard(
    modifier: Modifier,
    currentFragment: Int,
    onClick: (String) -> Unit,
    locationViewModel: LocationViewModel,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    userChatPrefsViewModel: UserChatPrefsViewModel,
    conversationViewModel: ConversationViewModel,
    isConnected: Boolean,
    profileViewModel: ProfileViewModel
){
    Column(
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 8.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            Crossfade(targetState = currentFragment,
                label = "",
                modifier = Modifier
                    .fillMaxWidth()
            ) { target ->
                when (target) {
                    1 -> AnonymousChatConfigurationScreen(locationViewModel,requestPermissionLauncher, onClick, userChatPrefsViewModel, isConnected)
                    2 -> ImplicitChatsScreen(onClick, conversationViewModel, isConnected)
                    3 -> ProfileScreen(isConnected, profileViewModel, onClick)
                    else ->  Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text("Preview Screen")
                    }
                }
            }
        }
    }
}


@Composable
fun BottomMenu(
    modifier: Modifier = Modifier,
    currentFragment: Int,
    setCurrentFragment: (Int) -> Unit = {}
){
    Row(
        modifier = modifier
    ) {
        MenuItem(
            text = stringResource(id = R.string.v_czat),
            icon = Icons.Filled.AddComment,
            modifier = Modifier.weight(1f)
                .clickable(onClick = { setCurrentFragment(1) }),
            color = if(currentFragment == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
        MenuItem(
            text = stringResource(id = R.string.chats),
            icon = Icons.Filled.MarkChatRead,
            modifier = Modifier.weight(1f)
                .clickable(onClick = { setCurrentFragment(2) }),
            color = if(currentFragment == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
        MenuItem(
            text = stringResource(id = R.string.profile),
            icon = Icons.Filled.AccountCircle,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = { setCurrentFragment(3) }),
            color = if(currentFragment == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun MenuItem(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.wrapContentHeight(),
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

@Preview
@Composable
fun TopMenuPreview() {
    TopRowMenu(modifier = Modifier.fillMaxWidth(), onClick = {})
}

