package com.example.vresciecompose.view_models

import LocalContext
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LocationViewModel() : ViewModel() {
    private var latitude: Double? = null
    private var longitude: Double? = null

    fun setLocation(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun getLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        context: Context,
        onSuccess: (Location) -> Unit,
        onFailure: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onFailure()
            return
        }

        val location = fusedLocationProviderClient.lastLocation
        location.addOnSuccessListener {
            if (it != null) {
                onSuccess(it)
                setLocation(it.latitude, it.longitude)
            } else {
                onFailure()
            }
        }.addOnFailureListener {
            onFailure()
        }
    }
}
