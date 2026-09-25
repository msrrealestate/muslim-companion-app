package com.karim.muslimcompanion.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.toDegrees
import kotlin.math.toRadians

object QiblaCalculator {

    private const val MECCA_LATITUDE = 21.422478
    private const val MECCA_LONGITUDE = 39.826206

    /**
     * Calculates the great-circle initial bearing (0-360°, clockwise from true north)
     * from the device's current location to the Kaaba in Mecca.
     */
    fun calculateBearing(currentLat: Double, currentLon: Double): Float {
        val lat1 = toRadians(currentLat)
        val lat2 = toRadians(MECCA_LATITUDE)
        val deltaLon = toRadians(MECCA_LONGITUDE - currentLon)

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        var bearing = toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        return bearing.toFloat()
    }
}
