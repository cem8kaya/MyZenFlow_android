package com.oqza.myzenflow.domain.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.oqza.myzenflow.MainActivity
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.models.TimerSessionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing Pomodoro timer notifications
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        private const val TIMER_CHANNEL_ID = "pomodoro_timer_channel"
        private const val REMINDER_CHANNEL_ID = "daily_reminder_channel"
        private const val TIMER_NOTIFICATION_ID = 1001
        private const val REMINDER_NOTIFICATION_ID = 1002
        private const val PROGRESS_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Timer completion channel
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for timer completion and breaks"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            }

            // Daily reminder channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for focus sessions"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(timerChannel)
            manager.createNotificationChannel(reminderChannel)
        }
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for older versions
        }
    }

    /**
     * Show timer completion notification
     */
    fun showTimerCompletionNotification(sessionType: TimerSessionType, nextSessionType: TimerSessionType?) {
        if (!hasNotificationPermission()) return

        val title = when (sessionType) {
            TimerSessionType.WORK -> "Çalışma Tamamlandı! 🎉"
            TimerSessionType.SHORT_BREAK -> "Mola Bitti!"
            TimerSessionType.LONG_BREAK -> "Uzun Mola Bitti!"
        }

        val message = when (nextSessionType) {
            TimerSessionType.WORK -> "Hazır mısın? Yeni çalışma seansına başla."
            TimerSessionType.SHORT_BREAK -> "Kısa bir mola zamanı! 5 dakika dinlen."
            TimerSessionType.LONG_BREAK -> "Harika gidiyorsun! Uzun mola zamanı."
            null -> "Tüm seanslar tamamlandı! Harika iş çıkardın! 🌟"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You'll need to add proper icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        try {
            notificationManager.notify(TIMER_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }

    /**
     * Show progress notification for running timer
     */
    fun showProgressNotification(
        sessionType: TimerSessionType,
        timeRemaining: String,
        progress: Float,
        isPaused: Boolean
    ) {
        if (!hasNotificationPermission()) return

        val title = when (sessionType) {
            TimerSessionType.WORK -> "🎯 Çalışma Seansı"
            TimerSessionType.SHORT_BREAK -> "☕ Kısa Mola"
            TimerSessionType.LONG_BREAK -> "🌟 Uzun Mola"
        }

        val message = if (isPaused) {
            "Duraklatıldı - $timeRemaining kaldı"
        } else {
            "$timeRemaining kaldı"
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(!isPaused) // Can't be dismissed while running
            .setContentIntent(pendingIntent)
            .setProgress(100, (progress * 100).toInt(), false)
            .setSound(null) // No sound for progress updates
            .setVibrate(null) // No vibration for progress updates
            .build()

        try {
            notificationManager.notify(PROGRESS_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }

    /**
     * Cancel progress notification
     */
    fun cancelProgressNotification() {
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
    }

    /**
     * Show daily reminder notification
     */
    fun showDailyReminderNotification() {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Günlük Odaklanma Zamanı! 🎯")
            .setContentText("Bugünkü hedeflerine ulaşmak için odaklanma seansına başla.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(REMINDER_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
