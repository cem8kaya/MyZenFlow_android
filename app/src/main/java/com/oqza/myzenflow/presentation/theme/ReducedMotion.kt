package com.oqza.myzenflow.presentation.theme

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * True when the user turned system animations off ("Remove animations" / animator scale 0).
 * Decorative and ambient motion (particles, sway, pulses, shimmer) should check this and
 * fall back to static or simple fade visuals.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/** Reads the system animation scale and keeps it live while the app is open. */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    var reduced by remember { mutableStateOf(readReduced(context.contentResolver)) }

    DisposableEffect(context) {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduced = readReduced(resolver)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduced
}

private fun readReduced(resolver: android.content.ContentResolver): Boolean =
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

/**
 * Whether the app is currently drawn with its dark theme. This follows the in-app theme choice
 * (System / Light / Dark), unlike isSystemInDarkTheme() which only knows the device setting.
 */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
@androidx.compose.runtime.ReadOnlyComposable
fun isZenDarkTheme(): Boolean = LocalDarkTheme.current

/** Readable text colour on the breathing screen's gradient (white on dark, deep navy on light). */
@Composable
@androidx.compose.runtime.ReadOnlyComposable
fun breathingContentColor(): androidx.compose.ui.graphics.Color =
    if (isZenDarkTheme()) androidx.compose.ui.graphics.Color.White
    else androidx.compose.ui.graphics.Color(0xFF10233F)
