package com.oqza.myzenflow.domain.workers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily practice reminder.
 *
 * Uses a chain of one-time WorkManager jobs (each schedules the next day) instead of exact
 * alarms: no SCHEDULE_EXACT_ALARM permission is needed and the reminder may arrive a few
 * minutes late when the device is in Doze, which is fine for a gentle nudge.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    /**
     * Schedule the next reminder at [time] ("HH:mm").
     * @param replace true to move an already scheduled reminder (user changed the time),
     * false to keep an existing one (app start).
     */
    fun schedule(time: String, replace: Boolean) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayUntilNext(time, LocalDateTime.now()))
            .build()
        workManager.enqueueUniqueWork(
            ReminderWorker.WORK_NAME,
            if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(ReminderWorker.WORK_NAME)
    }

    companion object {
        /** Time from [now] until the next occurrence of [time]; always in the future. */
        fun delayUntilNext(time: String, now: LocalDateTime): Duration {
            val target = runCatching { LocalTime.parse(time) }.getOrDefault(LocalTime.of(9, 0))
            var next = now.toLocalDate().atTime(target)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return Duration.between(now, next)
        }
    }
}
