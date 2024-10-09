package com.example.vresciecompose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vresciecompose.R
import com.example.vresciecompose.view_models.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit
){

    // Odczyt aktualnego motywu
    val currentTheme by settingsViewModel.getCurrentTheme().observeAsState(0) // 0 = domyślny
    fun saveThemeVM(theme: Int){
        settingsViewModel.saveTheme(theme)
    }

    Settings(
        modifier = Modifier
            .fillMaxSize(),
        onNavigate = onNavigate,
        currentTheme = currentTheme,
        saveThemeVM = ::saveThemeVM
    )
}


@Composable
fun Settings(
    modifier: Modifier,
    onNavigate: (String) -> Unit,
    currentTheme: Int,
    saveThemeVM: (Int) -> Unit
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
            currentTheme
        )
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier,
    saveThemeVM: (Int) -> Unit,
    currentTheme: Int
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
                                .clickable { setCurrentContent("appearanceSettings")}
                        )
                    }
                }

                "appearanceSettings" -> AppearanceSettings(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    setCurrentContent,
                    saveThemeVM,
                    currentTheme
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
            text = "Wygląd",
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
    currentTheme: Int
){
    // Zmienna sterująca widocznością AlertDialog
    var showDialog by remember { mutableStateOf(false) }
    BackHandler {
        setCurrentContent("settingsList")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ){
        Text(
            text = "Motyw",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clickable { showDialog = true }
                .padding(bottom = dimensionResource(R.dimen.padding_medium))
        )
        Text(
            text = "Rozmiar czcionki dla wiadomości",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(bottom = dimensionResource(R.dimen.padding_medium))
        )
        Text(
            text = "Język",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
        )

        // Wyświetlenie AlertDialog jeśli showDialog jest true
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Motyw") },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected =  currentTheme == 0,
                                onClick = {
                                    saveThemeVM(0)
                                    showDialog = false
                                }
                            )
                            Text(text = "Domyślny systemowy")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected =  currentTheme == 1,
                                onClick = {
                                    saveThemeVM(1)
                                    showDialog = false
                                }
                            )
                            Text(text = "Jasny")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentTheme == 2,
                                onClick = {
                                    saveThemeVM(2)
                                    showDialog = false
                                }
                            )
                            Text(text = "Ciemny")
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
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview(){
    Settings(
        modifier = Modifier,
        onNavigate = {},
        saveThemeVM = {},
        currentTheme = 0,
    )
}

