package com.oqza.myzenflow.utils

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Plural-aware string for Compose ("1 session" / "5 sessions", and the right forms in every
 * language). Reads LocalConfiguration so it updates when the app language changes.
 */
@Composable
fun pluralString(@PluralsRes id: Int, count: Int, vararg formatArgs: Any): String {
    LocalConfiguration.current
    return LocalContext.current.resources.getQuantityString(id, count, *formatArgs)
}
