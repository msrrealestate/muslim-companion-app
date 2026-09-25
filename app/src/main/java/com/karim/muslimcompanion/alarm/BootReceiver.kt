package com.karim.muslimcompanion.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.karim.muslimcompanion.worker.PrayerSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Re-schedules today's prayer alarms after the device reboots, since
 * AlarmManager alarms do not survive a power cycle.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val request = OneTimeWorkRequestBuilder<PrayerSyncWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
