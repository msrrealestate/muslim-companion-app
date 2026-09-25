package com.karim.muslimcompanion.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.karim.muslimcompanion.R

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object PrayerTimes : Screen("prayer_times", R.string.nav_prayer_times, Icons.Filled.AccessTime)
    data object Qibla : Screen("qibla", R.string.nav_qibla, Icons.Filled.Explore)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)

    companion object {
        val bottomNavItems = listOf(PrayerTimes, Qibla, Settings)
    }
}
