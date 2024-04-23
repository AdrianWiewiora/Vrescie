package com.example.vresciecompose.view_models

import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData

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
        requestPermissionLauncher: ActivityResultLauncher<String>,
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
            Log.i("Location", "No location permissions granted")
            // Jeśli brak uprawnień, prosimy o nie
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        val location = fusedLocationProviderClient.lastLocation
        location.addOnSuccessListener {
            if (it != null) {
                onSuccess(it)
                setLocation(it.latitude, it.longitude)
            } else {
                Log.i("Location", "Failed to get location")
                onFailure()
            }
        }.addOnFailureListener {
            Log.i("Location", "Failed to get location")
            onFailure()
        }
    }
}
