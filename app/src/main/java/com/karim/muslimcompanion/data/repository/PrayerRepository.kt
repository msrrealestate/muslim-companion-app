package com.karim.muslimcompanion.data.repository

import com.karim.muslimcompanion.data.remote.AladhanApiService
import com.karim.muslimcompanion.model.DailyPrayerSchedule
import com.karim.muslimcompanion.model.PrayerKey
import com.karim.muslimcompanion.model.PrayerTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

sealed class PrayerResult {
    data class Success(val schedule: DailyPrayerSchedule) : PrayerResult()
    data class Error(val message: String) : PrayerResult()
}

@Singleton
class PrayerRepository @Inject constructor(
    private val api: AladhanApiService
) {

    /**
     * Fetches today's prayer timings for the given coordinates and converts
     * the raw "HH:mm" strings into concrete LocalDateTime values for today.
     */
    suspend fun fetchTodayTimings(
        latitude: Double,
        longitude: Double,
        calculationMethod: Int
    ): PrayerResult {
        return try {
            val response = api.getTimings(
                latitude = latitude,
                longitude = longitude,
                method = calculationMethod
            )

            if (response.code != 200) {
                return PrayerResult.Error("Server returned code ${response.code}")
            }

            val timings = response.data.timings
            val today = LocalDate.now()

            val prayers = listOf(
                buildPrayerTime(PrayerKey.FAJR, "الفجر", timings.fajr, today),
                buildPrayerTime(PrayerKey.SUNRISE, "الشروق", timings.sunrise, today),
                buildPrayerTime(PrayerKey.DHUHR, "الظهر", timings.dhuhr, today),
                buildPrayerTime(PrayerKey.ASR, "العصر", timings.asr, today),
                buildPrayerTime(PrayerKey.MAGHRIB, "المغرب", timings.maghrib, today),
                buildPrayerTime(PrayerKey.ISHA, "العشاء", timings.isha, today)
            )

            PrayerResult.Success(
                DailyPrayerSchedule(
                    date = response.data.date.readable,
                    prayers = prayers
                )
            )
        } catch (e: Exception) {
            PrayerResult.Error(e.message ?: "Unknown network error")
        }
    }

    private fun buildPrayerTime(
        key: PrayerKey,
        arabicName: String,
        rawTime: String,
        date: LocalDate
    ): PrayerTime {
        // Aladhan sometimes appends a timezone offset like "04:12 (+02)" – strip it.
        val cleaned = rawTime.substringBefore(" ").trim()
        val time = LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("HH:mm"))
        return PrayerTime(
            key = key,
            displayName = arabicName,
            dateTime = LocalDateTime.of(date, time)
        )
    }
}
