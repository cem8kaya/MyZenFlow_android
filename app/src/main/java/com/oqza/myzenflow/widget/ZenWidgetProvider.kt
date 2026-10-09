package com.oqza.myzenflow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.oqza.myzenflow.R
import com.oqza.myzenflow.data.repository.SessionRepository
import com.oqza.myzenflow.utils.DeepLinks
import com.oqza.myzenflow.utils.StreakCalculator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Home screen widget: current streak, minutes practiced today and a one-tap breathing start.
 * Data is read locally from the app database; the widget never uses the network.
 */
class ZenWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun sessionRepository(): SessionRepository
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val views = buildViews(context)
                appWidgetIds.forEach { manager.updateAppWidget(it, views) }
            } catch (_: Exception) {
                // Keep the previous widget content if the database is unavailable
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun buildViews(context: Context): RemoteViews {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .sessionRepository()
        val practice = repository.getAllPracticeSessions().first()
        val today = LocalDate.now()
        val streak = StreakCalculator.current(practice.map { it.date.toLocalDate() }, today)
        val minutesToday = practice.filter { it.date.toLocalDate() == today }.sumOf { it.duration } / 60

        return RemoteViews(context.packageName, R.layout.widget_zenflow).apply {
            setTextViewText(R.id.widget_streak, context.getString(R.string.widget_streak_value, streak))
            setTextViewText(R.id.widget_minutes, context.getString(R.string.widget_minutes_value, minutesToday))
            setOnClickPendingIntent(R.id.widget_root, activity(context, DeepLinks.ROUTE_WEEKLY, 1))
            setOnClickPendingIntent(R.id.widget_start, activity(context, DeepLinks.ROUTE_BREATHING, 2))
        }
    }

    private fun activity(context: Context, route: String, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            DeepLinks.launchIntent(context, route),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    companion object {
        /** Ask all placed widgets to redraw (e.g. after a session was saved). */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ZenWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val intent = Intent(context, ZenWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
