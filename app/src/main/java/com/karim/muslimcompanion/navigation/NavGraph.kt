package com.karim.muslimcompanion.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.karim.muslimcompanion.ui.screens.PrayerTimesScreen
import com.karim.muslimcompanion.ui.screens.QiblaScreen
import com.karim.muslimcompanion.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.PrayerTimes.route) {
        composable(Screen.PrayerTimes.route) { PrayerTimesScreen() }
        composable(Screen.Qibla.route) { QiblaScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}
