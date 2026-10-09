package com.oqza.myzenflow.presentation.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * Whether premium content is currently available to the user. Provided once in MainActivity as
 * "premium is switched off in this build, or the user owns Premium". Screens use it to decide if
 * a premium item is locked; the default (true) keeps previews and tests unlocked.
 */
val LocalPremiumUnlocked = compositionLocalOf { true }
