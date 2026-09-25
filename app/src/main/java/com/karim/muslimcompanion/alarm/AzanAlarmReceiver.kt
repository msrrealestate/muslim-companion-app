package com.karim.muslimcompanion.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * Woken up by AlarmManager at the exact moment a prayer begins.
 * Immediately hands off to a foreground service so the Azan can keep
 * playing even after this broadcast receiver returns.
 */
class AzanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "الصلاة"

        val serviceIntent = Intent(context, AzanForegroundService::class.java).apply {
            putExtra(AzanForegroundService.EXTRA_PRAYER_NAME, prayerName)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
