package com.karim.muslimcompanion.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import com.karim.muslimcompanion.model.DailyPrayerSchedule
import com.karim.muslimcompanion.model.PrayerKey
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules one exact alarm per prayer using AlarmManager, so the Azan fires
 * even if the app process has been killed.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        private const val REQUEST_CODE_BASE = 1000
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /** Cancels any previously scheduled prayer alarms, then schedules fresh ones for [schedule]. */
    fun scheduleAll(schedule: DailyPrayerSchedule) {
        cancelAll()
        val now = LocalDateTime.now()

        schedule.prayers
            .filter { it.key != PrayerKey.SUNRISE }
            .filter { it.dateTime.isAfter(now) }
            .forEach { prayer ->
                schedule(
                    requestCode = REQUEST_CODE_BASE + prayer.key.ordinal,
                    triggerAtMillis = prayer.dateTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
                    prayerName = prayer.displayName
                )
            }
    }

    private fun schedule(requestCode: Int, triggerAtMillis: Long, prayerName: String) {
        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME, prayerName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Falls back to an inexact alarm if the user has not granted the
            // "Alarms & reminders" special permission on Android 12+.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAll() {
        PrayerKey.values().forEach { key ->
            if (key == PrayerKey.SUNRISE) return@forEach
            val intent = Intent(context, AzanAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + key.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
