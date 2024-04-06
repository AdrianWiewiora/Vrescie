package com.example.vresciecompose.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


class StartScreenViewModel : ViewModel() {
    val showDialog = mutableStateOf(false)

    fun toggleDialogVisibility() {
        showDialog.value = !showDialog.value
    }
}

