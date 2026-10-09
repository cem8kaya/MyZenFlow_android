package com.oqza.myzenflow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.oqza.myzenflow.data.repository.PreferencesRepository
import com.oqza.myzenflow.data.repository.SessionRepository
import com.oqza.myzenflow.domain.workers.ReminderScheduler
import com.oqza.myzenflow.utils.AppShortcuts
import com.oqza.myzenflow.widget.ZenWidgetProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyZenFlowApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var reminderScheduler: dagger.Lazy<ReminderScheduler> // lazy: WorkManager must not start before workerFactory is injected

    @Inject
    lateinit var sessionRepository: dagger.Lazy<SessionRepository>

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Make sure the reminder chain exists (e.g. after an app update or data restore).
        // KEEP: an already scheduled reminder is not pushed back by every app start.
        AppShortcuts.publish(this)

        // Redraw the home screen widget whenever practice changes
        appScope.launch {
            runCatching {
                sessionRepository.get().getAllPracticeSessions()
                    .map { it.size }
                    .distinctUntilChanged()
                    .collect { ZenWidgetProvider.refresh(this@MyZenFlowApplication) }
            }
        }

        appScope.launch {
            runCatching {
                val prefs = preferencesRepository.userPreferences.first()
                if (prefs.dailyReminderEnabled && prefs.notificationsEnabled) {
                    reminderScheduler.get().schedule(prefs.dailyReminderTime, replace = false)
                } else {
                    reminderScheduler.get().cancel()
                }
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
