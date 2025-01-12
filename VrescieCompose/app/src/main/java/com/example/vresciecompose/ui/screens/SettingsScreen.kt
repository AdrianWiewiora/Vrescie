package com.example.vresciecompose.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.Navigation
import com.example.vresciecompose.R
import com.example.vresciecompose.view_models.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    navigateTo: (String) -> Unit
){
    //Wygląd
    // Odczyt aktualnego motywu
    val currentTheme by settingsViewModel.getCurrentTheme().observeAsState(0) // 0 = domyślny
    fun saveThemeVM(theme: Int){
        settingsViewModel.saveTheme(theme)
    }
    //Odczyt aktualnej wielkości czcionki wiadomości
    val currentMessageSize by settingsViewModel.getCurrentMessageSize().observeAsState(1)
    fun saveMessageSizeVM(size: Int){
        settingsViewModel.saveMessageSize(size)
    }
    fun logoutVM(){
        settingsViewModel.logout { navigateTo(Navigation.Destinations.START)}
    }

    Settings(
        modifier = Modifier
            .fillMaxSize(),
        currentTheme = currentTheme,
        saveThemeVM = ::saveThemeVM,
        currentMessageSize = currentMessageSize,
        saveMessageSizeVM = ::saveMessageSizeVM,
        logoutVM = ::logoutVM
    )
}


@Composable
fun Settings(
    modifier: Modifier,
    currentTheme: Int,
    saveThemeVM: (Int) -> Unit,
    currentMessageSize: Int = 1,
    saveMessageSizeVM: (Int) -> Unit,
    logoutVM: () -> Unit
){
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        }

        SettingsContent(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(top = 5.dp, bottom = 8.dp)
                .fillMaxSize(),
            saveThemeVM,
            currentTheme,
            currentMessageSize,
            saveMessageSizeVM,
            logoutVM
        )
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier,
    saveThemeVM: (Int) -> Unit,
    currentTheme: Int,
    currentMessageSize: Int,
    saveMessageSizeVM: (Int) -> Unit,
    logoutVM: () -> Unit,
){
    val (currentContent, setCurrentContent) = remember { mutableStateOf("settingsList") }


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
        ) {
            when (currentContent){
                "settingsList"-> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ){
                    item {
                        AppearanceSettingsItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable { setCurrentContent("appearanceSettings") }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                logoutVM()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = stringResource(R.string.logout),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = stringResource(R.string.logout))
                        }
                    }
                }

                "appearanceSettings" -> AppearanceSettings(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    setCurrentContent,
                    saveThemeVM,
                    currentTheme,
                    currentMessageSize,
                    saveMessageSizeVM
                )
            }

        }
    }
}

@Composable
fun AppearanceSettingsItem(
    modifier: Modifier = Modifier,
){
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ){
        Icon(
            imageVector = Icons.Filled.BrightnessMedium,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(dimensionResource(R.dimen.icon_settings_size))
                .padding(end = dimensionResource(R.dimen.padding_small))
        )
        Text(
            text = stringResource(R.string.appearance),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(start = dimensionResource(R.dimen.padding_small))
        )
    }
}

@Composable
fun AppearanceSettings(
    modifier: Modifier = Modifier,
    setCurrentContent: (String) -> Unit,
    saveThemeVM: (Int) -> Unit,
    currentTheme: Int,
    currentMessageSize: Int = 1,
    saveMessageSizeVM: (Int) -> Unit
){
    var showDialogTheme by remember { mutableStateOf(false) }
    var showDialogMessageSize by remember { mutableStateOf(false) }
    BackHandler {
        setCurrentContent("settingsList")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ){
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clickable { showDialogTheme = true }
                .padding(bottom = dimensionResource(R.dimen.padding_medium))
        )
        Text(
            text = stringResource(R.string.font_size_for_messages),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clickable { showDialogMessageSize = true }
                .padding(bottom = dimensionResource(R.dimen.padding_medium))
        )

        // Theme Dialog
        if (showDialogTheme) {
            AlertDialog(
                onDismissRequest = { showDialogTheme = false },
                title = { Text(text = stringResource(R.string.theme)) },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected =  currentTheme == 0,
                                onClick = {
                                    saveThemeVM(0)
                                    showDialogTheme = false
                                }
                            )
                            Text(text = stringResource(R.string.system_default))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected =  currentTheme == 1,
                                onClick = {
                                    saveThemeVM(1)
                                    showDialogTheme = false
                                }
                            )
                            Text(text = stringResource(R.string.light))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentTheme == 2,
                                onClick = {
                                    saveThemeVM(2)
                                    showDialogTheme = false
                                }
                            )
                            Text(text = stringResource(R.string.dark))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentTheme == 3,
                                onClick = {
                                    saveThemeVM(3)
                                    showDialogTheme = false
                                }
                            )
                            Text(text = stringResource(R.string.high_contrast_dark))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentTheme == 4,
                                onClick = {
                                    saveThemeVM(4)
                                    showDialogTheme = false
                                }
                            )
                            Text(text = stringResource(R.string.high_contrast_light))
                        }
                    }
                },
                confirmButton = {},
            )
        }

        // MessageSize Dialog
        if (showDialogMessageSize) {
            AlertDialog(
                onDismissRequest = { showDialogMessageSize = false },
                title = { Text(text = stringResource(R.string.font_size)) },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected =  currentMessageSize == 0,
                                onClick = {
                                    saveMessageSizeVM(0)
                                    showDialogMessageSize = false
                                }
                            )
                            Text(text = stringResource(R.string.small))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected =  currentMessageSize == 1,
                                onClick = {
                                    saveMessageSizeVM(1)
                                    showDialogMessageSize = false
                                }
                            )
                            Text(text = stringResource(R.string.normal))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentMessageSize == 2,
                                onClick = {
                                    saveMessageSizeVM(2)
                                    showDialogMessageSize = false
                                }
                            )
                            Text(text = stringResource(R.string.large))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentMessageSize == 3,
                                onClick = {
                                    saveMessageSizeVM(3)
                                    showDialogMessageSize = false
                                }
                            )
                            Text(text = stringResource(R.string.very_large))
                        }
                    }
                },
                confirmButton = {},
            )
        }

    }
}


@Preview(showBackground = true)
@Composable
fun SettingsAppearancePreview(){
    AppearanceSettings(
        modifier = Modifier,
        setCurrentContent = {},
        saveThemeVM = {},
        currentTheme = 0,
        currentMessageSize = 1,
        saveMessageSizeVM = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview(){
    Settings(
        modifier = Modifier,
        saveThemeVM = {},
        currentTheme = 0,
        currentMessageSize = 1,
        saveMessageSizeVM = {},
        logoutVM = {}
    )
}

