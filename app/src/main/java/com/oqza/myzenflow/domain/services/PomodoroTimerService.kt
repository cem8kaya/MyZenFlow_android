package com.oqza.myzenflow.domain.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.datastore.PomodoroTimerDataStore
import com.oqza.myzenflow.data.models.TimerSessionType
import com.oqza.myzenflow.data.models.TimerStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Foreground Service for Pomodoro timer
 * Ensures reliable countdown even when app is in background or screen is off
 */
@AndroidEntryPoint
class PomodoroTimerService : Service() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var timerDataStore: PomodoroTimerDataStore

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    companion object {
        const val ACTION_START = "com.oqza.myzenflow.ACTION_START_TIMER"
        const val ACTION_PAUSE = "com.oqza.myzenflow.ACTION_PAUSE_TIMER"
        const val ACTION_STOP = "com.oqza.myzenflow.ACTION_STOP_TIMER"
        const val ACTION_UPDATE = "com.oqza.myzenflow.ACTION_UPDATE_TIMER"

        const val EXTRA_TARGET_TIME = "extra_target_time"
        const val EXTRA_SESSION_TYPE = "extra_session_type"
        const val EXTRA_TOTAL_DURATION = "extra_total_duration"

        const val BROADCAST_TIMER_TICK = "com.oqza.myzenflow.TIMER_TICK"
        const val BROADCAST_TIMER_COMPLETE = "com.oqza.myzenflow.TIMER_COMPLETE"

        const val EXTRA_TIME_REMAINING = "extra_time_remaining"
        const val EXTRA_PROGRESS = "extra_progress"

        private const val NOTIFICATION_ID = 1003
        private const val UPDATE_INTERVAL_MS = 1000L // Update every second
        private const val NOTIFICATION_UPDATE_INTERVAL_S = 5 // Update notification every 5 seconds
    }

    inner class LocalBinder : Binder() {
        fun getService(): PomodoroTimerService = this@PomodoroTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val targetTime = intent.getLongExtra(EXTRA_TARGET_TIME, 0L)
                val sessionTypeString = intent.getStringExtra(EXTRA_SESSION_TYPE) ?: TimerSessionType.WORK.name
                val totalDuration = intent.getIntExtra(EXTRA_TOTAL_DURATION, 0)

                val sessionType = try {
                    TimerSessionType.valueOf(sessionTypeString)
                } catch (e: IllegalArgumentException) {
                    TimerSessionType.WORK
                }

                startTimer(targetTime, sessionType, totalDuration)
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }

        return START_STICKY
    }

    /**
     * Start the timer countdown
     */
    private fun startTimer(targetCompletionTime: Long, sessionType: TimerSessionType, totalDurationSeconds: Int) {
        // Cancel any existing timer
        timerJob?.cancel()

        // Create foreground notification
        val notification = NotificationCompat.Builder(this, "pomodoro_timer_channel")
            .setContentTitle("Starting timer...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Start countdown job
        timerJob = serviceScope.launch {
            var tickCount = 0

            while (isActive && System.currentTimeMillis() < targetCompletionTime) {
                // Check if timer is still running (not paused)
                val timerState = timerDataStore.timerStateFlow.first()

                if (timerState.timerStatus != TimerStatus.RUNNING) {
                    // Timer was paused or stopped
                    break
                }

                // Calculate remaining time
                val remainingMillis = targetCompletionTime - System.currentTimeMillis()
                val remainingSeconds = (remainingMillis / 1000).toInt().coerceAtLeast(0)

                // Calculate progress
                val progress = if (totalDurationSeconds > 0) {
                    1f - (remainingSeconds.toFloat() / totalDurationSeconds.toFloat())
                } else {
                    0f
                }

                // Broadcast timer tick to update UI
                val tickIntent = Intent(BROADCAST_TIMER_TICK).apply {
                    putExtra(EXTRA_TIME_REMAINING, remainingSeconds)
                    putExtra(EXTRA_PROGRESS, progress)
                }
                sendBroadcast(tickIntent)

                // Update notification every N seconds
                if (tickCount % NOTIFICATION_UPDATE_INTERVAL_S == 0) {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    val timeRemaining = String.format("%02d:%02d", minutes, seconds)

                    notificationHelper.showProgressNotification(
                        sessionType = sessionType,
                        timeRemaining = timeRemaining,
                        progress = progress,
                        isPaused = false
                    )
                }

                tickCount++

                // Check if timer completed
                if (remainingSeconds <= 0) {
                    onTimerComplete()
                    break
                }

                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Pause the timer
     */
    private fun pauseTimer() {
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Stop the timer
     */
    private fun stopTimer() {
        timerJob?.cancel()
        notificationHelper.cancelProgressNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Handle timer completion
     */
    private fun onTimerComplete() {
        // Broadcast completion
        val completeIntent = Intent(BROADCAST_TIMER_COMPLETE)
        sendBroadcast(completeIntent)

        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
}
