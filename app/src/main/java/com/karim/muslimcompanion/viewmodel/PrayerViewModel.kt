package com.karim.muslimcompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karim.muslimcompanion.alarm.AlarmScheduler
import com.karim.muslimcompanion.data.local.PreferencesManager
import com.karim.muslimcompanion.data.location.LocationHelper
import com.karim.muslimcompanion.data.location.LocationResult
import com.karim.muslimcompanion.data.repository.PrayerRepository
import com.karim.muslimcompanion.data.repository.PrayerResult
import com.karim.muslimcompanion.model.DailyPrayerSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

sealed class PrayerUiState {
    data object LocatingDevice : PrayerUiState()
    data object LoadingTimings : PrayerUiState()
    data class Loaded(val schedule: DailyPrayerSchedule) : PrayerUiState()
    data class LocationError(val message: String) : PrayerUiState()
    data class NetworkError(val message: String) : PrayerUiState()
}

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val locationHelper: LocationHelper,
    private val prayerRepository: PrayerRepository,
    private val preferencesManager: PreferencesManager,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.LocatingDevice)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val _now = MutableStateFlow(LocalDateTime.now())
    val now: StateFlow<LocalDateTime> = _now.asStateFlow()

    private val _lastCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    val lastCoordinates: StateFlow<Pair<Double, Double>?> = _lastCoordinates.asStateFlow()

    init {
        startClock()
        refresh()
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                _now.value = LocalDateTime.now()
                delay(1000L)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = PrayerUiState.LocatingDevice

            if (!locationHelper.hasLocationPermission()) {
                _uiState.value = PrayerUiState.LocationError("permission")
                return@launch
            }

            when (val locationResult = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val lat = locationResult.location.latitude
                    val lon = locationResult.location.longitude
                    _lastCoordinates.value = Pair(lat, lon)
                    preferencesManager.saveLastLocation(lat, lon)
                    fetchTimings(lat, lon)
                }
                is LocationResult.Error -> {
                    // Fall back to the last known coordinates if we have any,
                    // so the user still sees today's timings.
                    val cached = preferencesManager.lastKnownLocation.first()
                    if (cached != null) {
                        _lastCoordinates.value = cached
                        fetchTimings(cached.first, cached.second)
                    } else {
                        _uiState.value = PrayerUiState.LocationError(locationResult.message)
                    }
                }
            }
        }
    }

    private suspend fun fetchTimings(latitude: Double, longitude: Double) {
        _uiState.value = PrayerUiState.LoadingTimings
        val method = preferencesManager.calculationMethod.first()

        when (val result = prayerRepository.fetchTodayTimings(latitude, longitude, method)) {
            is PrayerResult.Success -> {
                _uiState.value = PrayerUiState.Loaded(result.schedule)
                alarmScheduler.scheduleAll(result.schedule)
            }
            is PrayerResult.Error -> {
                _uiState.value = PrayerUiState.NetworkError(result.message)
            }
        }
    }
}
