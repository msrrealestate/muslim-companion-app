package com.karim.muslimcompanion.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karim.muslimcompanion.data.local.PreferencesManager
import com.karim.muslimcompanion.data.location.LocationHelper
import com.karim.muslimcompanion.data.location.LocationResult
import com.karim.muslimcompanion.model.QiblaState
import com.karim.muslimcompanion.util.QiblaCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QiblaUiState {
    data object Loading : QiblaUiState()
    data class Ready(val state: QiblaState) : QiblaUiState()
    data class Error(val message: String) : QiblaUiState()
}

@HiltViewModel
class QiblaViewModel @Inject constructor(
    application: Application,
    private val locationHelper: LocationHelper,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow<QiblaUiState>(QiblaUiState.Loading)
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private val sensorManager =
        application.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var bearingToMecca: Float = 0f
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    init {
        loadBearing()
        if (rotationSensor == null) {
            _uiState.value = QiblaUiState.Error("no_sensor")
        }
    }

    private fun loadBearing() {
        viewModelScope.launch {
            val coordinates = when (val result = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> Pair(result.location.latitude, result.location.longitude)
                is LocationResult.Error -> preferencesManager.lastKnownLocation.first()
            }

            if (coordinates == null) {
                _uiState.value = QiblaUiState.Error("no_location")
                return@launch
            }

            bearingToMecca = QiblaCalculator.calculateBearing(coordinates.first, coordinates.second)
            if (rotationSensor != null) {
                _uiState.value = QiblaUiState.Ready(QiblaState(bearingToMecca = bearingToMecca, deviceHeading = 0f))
            }
        }
    }

    fun startListening() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuthRadians = orientationAngles[0]
        val azimuthDegrees = (Math.toDegrees(azimuthRadians.toDouble()).toFloat() + 360f) % 360f

        _uiState.value = QiblaUiState.Ready(
            QiblaState(bearingToMecca = bearingToMecca, deviceHeading = azimuthDegrees)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op: UI shows a calibration hint at all times when accuracy is low.
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
