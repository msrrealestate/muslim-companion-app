package com.karim.muslimcompanion.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data class Error(val message: String) : LocationResult()
}

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun isLocationServiceEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.Error("Location permission not granted")
        }
        if (!isLocationServiceEnabled()) {
            return LocationResult.Error("Location service disabled")
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(60_000L)
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.getCurrentLocation(request, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(LocationResult.Success(location))
                    } else {
                        // Fall back to the last known location if a fresh fix could not be obtained.
                        client.lastLocation
                            .addOnSuccessListener { last ->
                                if (last != null) {
                                    continuation.resume(LocationResult.Success(last))
                                } else {
                                    continuation.resume(LocationResult.Error("Unable to determine location"))
                                }
                            }
                            .addOnFailureListener { e ->
                                continuation.resume(LocationResult.Error(e.message ?: "Location lookup failed"))
                            }
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
        }
    }
}
