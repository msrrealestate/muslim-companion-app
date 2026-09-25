package com.karim.muslimcompanion.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.karim.muslimcompanion.MainActivity
import com.karim.muslimcompanion.R
import com.karim.muslimcompanion.data.local.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that shows a high-priority notification and plays the
 * Azan audio when a prayer time is reached. Runs even if the app UI is closed.
 *
 * If the app ships a res/raw/azan.mp3 file it will be played in full; otherwise
 * the service falls back to the device's default alarm sound so the feature
 * always works out of the box.
 */
@AndroidEntryPoint
class AzanForegroundService : Service() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val CHANNEL_ID = "azan_channel"
        const val NOTIFICATION_ID = 501
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: getString(R.string.prayer_dhuhr)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(prayerName))

        serviceScope.launch {
            val enabled = preferencesManager.isAzanSoundEnabled.first()
            if (enabled) {
                playAzanSound()
            } else {
                stopSelfSafely()
            }
        }

        return START_NOT_STICKY
    }

    private fun playAzanSound() {
        try {
            val azanResId = resources.getIdentifier("azan", "raw", packageName)
            mediaPlayer = if (azanResId != 0) {
                MediaPlayer.create(this, azanResId)
            } else {
                val alarmUri = RingtoneManager.getActualDefaultRingtoneUri(
                    this,
                    RingtoneManager.TYPE_ALARM
                ) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                MediaPlayer().apply {
                    setDataSource(this@AzanForegroundService, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    prepare()
                }
            }

            mediaPlayer?.setOnCompletionListener {
                stopSelfSafely()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            stopSelfSafely()
        }
    }

    private fun buildNotification(prayerName: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.azan_notification_title, prayerName))
            .setContentText(getString(R.string.azan_notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.azan_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.azan_channel_desc)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopSelfSafely() {
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        serviceScope.cancel()
        super.onDestroy()
    }
}
