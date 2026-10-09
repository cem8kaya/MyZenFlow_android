package com.oqza.myzenflow.utils

import android.content.Context
import android.content.Intent
import com.oqza.myzenflow.MainActivity

/**
 * Launch targets used by app shortcuts and the home screen widget.
 * MainActivity is exported, so only routes in [ALLOWED_ROUTES] are ever opened.
 */
object DeepLinks {
    const val EXTRA_ROUTE = "com.oqza.myzenflow.extra.ROUTE"

    const val ROUTE_BREATHING = "breathing"
    const val ROUTE_FOCUS = "focus"
    const val ROUTE_WEEKLY = "weekly_summary"

    val ALLOWED_ROUTES = setOf(ROUTE_BREATHING, ROUTE_FOCUS, ROUTE_WEEKLY)

    fun launchIntent(context: Context, route: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_ROUTE, route)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    /** Returns the route from [intent] if it is a known destination, otherwise null. */
    fun routeFrom(intent: Intent?): String? =
        intent?.getStringExtra(EXTRA_ROUTE)?.takeIf { it in ALLOWED_ROUTES }
}
