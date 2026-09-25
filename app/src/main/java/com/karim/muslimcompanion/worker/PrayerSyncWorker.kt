package com.karim.muslimcompanion.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.karim.muslimcompanion.alarm.AlarmScheduler
import com.karim.muslimcompanion.data.location.LocationHelper
import com.karim.muslimcompanion.data.location.LocationResult
import com.karim.muslimcompanion.data.local.PreferencesManager
import com.karim.muslimcompanion.data.repository.PrayerRepository
import com.karim.muslimcompanion.data.repository.PrayerResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Background job that re-fetches today's prayer timings and re-schedules the
 * exact alarms. Triggered after boot and can also be enqueued periodically.
 */
@HiltWorker
class PrayerSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationHelper: LocationHelper,
    private val preferencesManager: PreferencesManager,
    private val prayerRepository: PrayerRepository,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val coordinates = resolveCoordinates() ?: return Result.retry()
            val (latitude, longitude) = coordinates
            val method = preferencesManager.calculationMethod.first()

            when (val result = prayerRepository.fetchTodayTimings(latitude, longitude, method)) {
                is PrayerResult.Success -> {
                    alarmScheduler.scheduleAll(result.schedule)
                    Result.success()
                }
                is PrayerResult.Error -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun resolveCoordinates(): Pair<Double, Double>? {
        val fresh = locationHelper.getCurrentLocation()
        if (fresh is LocationResult.Success) {
            preferencesManager.saveLastLocation(fresh.location.latitude, fresh.location.longitude)
            return Pair(fresh.location.latitude, fresh.location.longitude)
        }
        return preferencesManager.lastKnownLocation.first()
    }
}
