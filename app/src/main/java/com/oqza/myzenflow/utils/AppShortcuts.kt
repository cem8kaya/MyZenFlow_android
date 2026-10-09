package com.oqza.myzenflow.utils

import android.content.Context
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.oqza.myzenflow.R

/** Long-press launcher shortcuts: start breathing, open focus timer, see the week. */
object AppShortcuts {
    fun publish(context: Context) {
        val shortcuts = listOf(
            build(context, "breathe", R.string.shortcut_breathing_short, R.string.shortcut_breathing_long,
                R.drawable.ic_shortcut_breathing, DeepLinks.ROUTE_BREATHING, rank = 0),
            build(context, "focus", R.string.shortcut_focus_short, R.string.shortcut_focus_long,
                R.drawable.ic_shortcut_focus, DeepLinks.ROUTE_FOCUS, rank = 1),
            build(context, "week", R.string.shortcut_week_short, R.string.shortcut_week_long,
                R.drawable.ic_shortcut_breathing, DeepLinks.ROUTE_WEEKLY, rank = 2)
        )
        runCatching { ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts) }
    }

    private fun build(
        context: Context,
        id: String,
        shortLabel: Int,
        longLabel: Int,
        icon: Int,
        route: String,
        rank: Int
    ) = ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(context.getString(shortLabel))
        .setLongLabel(context.getString(longLabel))
        .setIcon(IconCompat.createWithResource(context, icon))
        .setIntent(DeepLinks.launchIntent(context, route))
        .setRank(rank)
        .build()
}
