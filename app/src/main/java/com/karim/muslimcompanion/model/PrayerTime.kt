package com.karim.muslimcompanion.model

import java.time.LocalDateTime

/**
 * A single named prayer (or sunrise) with its calculated date-time for "today".
 */
data class PrayerTime(
    val key: PrayerKey,
    val displayName: String,
    val dateTime: LocalDateTime
)

enum class PrayerKey {
    FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
}

/**
 * Fully parsed set of the day's prayer times, ready for the UI.
 */
data class DailyPrayerSchedule(
    val date: String,
    val prayers: List<PrayerTime>
) {
    /** Returns the next prayer relative to [now], wrapping to tomorrow's Fajr if the day is over. */
    fun nextPrayer(now: LocalDateTime): PrayerTime? {
        return prayers.filter { it.key != PrayerKey.SUNRISE }
            .filter { it.dateTime.isAfter(now) }
            .minByOrNull { it.dateTime }
    }
}

data class QiblaState(
    val bearingToMecca: Float = 0f,
    val deviceHeading: Float = 0f,
    val hasCompassSensor: Boolean = true
) {
    /** Angle the arrow must be rotated by, relative to the top of the screen. */
    val arrowRotation: Float
        get() = (bearingToMecca - deviceHeading + 360f) % 360f
}
