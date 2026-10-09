package com.oqza.myzenflow.domain.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.data.repository.SessionRepository
import com.oqza.myzenflow.domain.services.NotificationHelper
import com.oqza.myzenflow.utils.StreakCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Daily practice reminder.
 *
 * Respects the user's settings and stays quiet when it has nothing to add:
 * - nothing is shown if reminders or notifications are switched off
 * - nothing is shown if the user already practiced today
 * Then it schedules tomorrow's reminder.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository,
    private val reminderScheduler: ReminderScheduler
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = preferencesRepository.userPreferences.first()
            if (!prefs.dailyReminderEnabled || !prefs.notificationsEnabled) {
                return Result.success() // chain ends; re-enabled from Settings
            }

            val days = sessionRepository.getCompletedSessions().first().map { it.date.toLocalDate() }
            val practicedToday = days.contains(LocalDate.now())
            if (!practicedToday) {
                notificationHelper.showDailyReminderNotification(
                    userName = prefs.userName,
                    streak = StreakCalculator.current(days)
                )
            }

            reminderScheduler.schedule(prefs.dailyReminderTime, replace = true)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "daily_focus_reminder"
    }
}
