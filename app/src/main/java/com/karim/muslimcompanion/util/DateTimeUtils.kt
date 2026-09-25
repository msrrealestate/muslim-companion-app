package com.karim.muslimcompanion.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun formatClock(dateTime: LocalDateTime): String = dateTime.format(timeFormatter)

    /** Formats the remaining time between [now] and [target] as "HH:MM:SS". */
    fun formatCountdown(now: LocalDateTime, target: LocalDateTime): String {
        var duration = Duration.between(now, target)
        if (duration.isNegative) duration = Duration.ZERO

        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        val seconds = duration.toSecondsPart()

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
