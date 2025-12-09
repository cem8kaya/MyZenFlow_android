package com.oqza.myzenflow.domain.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqza.myzenflow.data.datastore.PomodoroTimerDataStore
import com.oqza.myzenflow.data.models.TimerSessionType
import com.oqza.myzenflow.data.models.TimerStatus
import com.oqza.myzenflow.domain.services.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker for background timer execution
 * Handles timer countdown and notifications when app is in background
 */
@HiltWorker
class PomodoroTimerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val timerDataStore: PomodoroTimerDataStore
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "pomodoro_timer_work"
        const val KEY_TARGET_COMPLETION_TIME = "target_completion_time"
        const val KEY_SESSION_TYPE = "session_type"
        const val KEY_TOTAL_DURATION = "total_duration"
    }

    override suspend fun doWork(): Result {
        return try {
            // Get input data
            val targetCompletionTime = inputData.getLong(KEY_TARGET_COMPLETION_TIME, 0L)
            val sessionTypeString = inputData.getString(KEY_SESSION_TYPE) ?: TimerSessionType.WORK.name
            val totalDurationSeconds = inputData.getInt(KEY_TOTAL_DURATION, 0)

            val sessionType = try {
                TimerSessionType.valueOf(sessionTypeString)
            } catch (e: IllegalArgumentException) {
                TimerSessionType.WORK
            }

            // Check if target time is valid
            if (targetCompletionTime == 0L || System.currentTimeMillis() >= targetCompletionTime) {
                return Result.failure()
            }

            // Monitor timer state
            while (System.currentTimeMillis() < targetCompletionTime && !isStopped) {
                // Check if timer is still running
                val timerState = timerDataStore.timerStateFlow.first()

                if (timerState.timerStatus != TimerStatus.RUNNING) {
                    // Timer was paused or stopped, exit worker
                    return Result.success()
                }

                // Calculate remaining time
                val remainingMillis = targetCompletionTime - System.currentTimeMillis()
                val remainingSeconds = (remainingMillis / 1000).toInt()

                // Update notification every 10 seconds
                if (remainingSeconds > 0 && remainingSeconds % 10 == 0) {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    val timeRemaining = String.format("%02d:%02d", minutes, seconds)
                    val progress = if (totalDurationSeconds > 0) {
                        1f - (remainingSeconds.toFloat() / totalDurationSeconds.toFloat())
                    } else {
                        0f
                    }

                    notificationHelper.showProgressNotification(
                        sessionType = sessionType,
                        timeRemaining = timeRemaining,
                        progress = progress,
                        isPaused = false
                    )
                }

                // Wait 1 second before next check
                delay(1000)
            }

            // Timer completed
            if (System.currentTimeMillis() >= targetCompletionTime) {
                // Determine next session type
                val timerState = timerDataStore.timerStateFlow.first()
                val nextSessionType = when (timerState.sessionType) {
                    TimerSessionType.WORK -> {
                        val newCompletedSessions = timerState.completedWorkSessions + 1
                        if (newCompletedSessions % timerState.totalCycles == 0) {
                            TimerSessionType.LONG_BREAK
                        } else {
                            TimerSessionType.SHORT_BREAK
                        }
                    }
                    TimerSessionType.SHORT_BREAK, TimerSessionType.LONG_BREAK -> {
                        TimerSessionType.WORK
                    }
                }

                // Show completion notification
                notificationHelper.showTimerCompletionNotification(
                    sessionType = sessionType,
                    nextSessionType = nextSessionType
                )

                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
