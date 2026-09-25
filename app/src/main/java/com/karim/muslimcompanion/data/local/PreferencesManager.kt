package com.karim.muslimcompanion.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "muslim_companion_settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val AZAN_SOUND_ENABLED = booleanPreferencesKey("azan_sound_enabled")
        val CALCULATION_METHOD = intPreferencesKey("calculation_method")
        val LAST_LATITUDE = doublePreferencesKey("last_latitude")
        val LAST_LONGITUDE = doublePreferencesKey("last_longitude")
    }

    val isAzanSoundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AZAN_SOUND_ENABLED] ?: true
    }

    val calculationMethod: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.CALCULATION_METHOD] ?: 5 // 5 = Egyptian General Authority of Survey
    }

    val lastKnownLocation: Flow<Pair<Double, Double>?> = context.dataStore.data.map { prefs ->
        val lat = prefs[Keys.LAST_LATITUDE]
        val lon = prefs[Keys.LAST_LONGITUDE]
        if (lat != null && lon != null) Pair(lat, lon) else null
    }

    suspend fun setAzanSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AZAN_SOUND_ENABLED] = enabled }
    }

    suspend fun setCalculationMethod(method: Int) {
        context.dataStore.edit { it[Keys.CALCULATION_METHOD] = method }
    }

    suspend fun saveLastLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit {
            it[Keys.LAST_LATITUDE] = latitude
            it[Keys.LAST_LONGITUDE] = longitude
        }
    }
}
