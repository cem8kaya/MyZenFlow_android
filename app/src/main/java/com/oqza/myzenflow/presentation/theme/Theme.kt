package com.oqza.myzenflow.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ZenPrimaryLight,
    onPrimary = ZenOnPrimaryLight,
    primaryContainer = ZenPrimaryContainerLight,
    onPrimaryContainer = ZenOnPrimaryContainerLight,
    secondary = ZenSecondaryLight,
    onSecondary = ZenOnSecondaryLight,
    secondaryContainer = ZenSecondaryContainerLight,
    onSecondaryContainer = ZenOnSecondaryContainerLight,
    tertiary = ZenTertiaryLight,
    onTertiary = ZenOnTertiaryLight,
    tertiaryContainer = ZenTertiaryContainerLight,
    onTertiaryContainer = ZenOnTertiaryContainerLight,
    error = ZenErrorLight,
    onError = ZenOnErrorLight,
    errorContainer = ZenErrorContainerLight,
    onErrorContainer = ZenOnErrorContainerLight,
    background = ZenBackgroundLight,
    onBackground = ZenOnBackgroundLight,
    surface = ZenSurfaceLight,
    onSurface = ZenOnSurfaceLight,
    surfaceVariant = ZenSurfaceVariantLight,
    onSurfaceVariant = ZenOnSurfaceVariantLight,
    outline = ZenOutlineLight,
    outlineVariant = ZenOutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ZenPrimaryDark,
    onPrimary = ZenOnPrimaryDark,
    primaryContainer = ZenPrimaryContainerDark,
    onPrimaryContainer = ZenOnPrimaryContainerDark,
    secondary = ZenSecondaryDark,
    onSecondary = ZenOnSecondaryDark,
    secondaryContainer = ZenSecondaryContainerDark,
    onSecondaryContainer = ZenOnSecondaryContainerDark,
    tertiary = ZenTertiaryDark,
    onTertiary = ZenOnTertiaryDark,
    tertiaryContainer = ZenTertiaryContainerDark,
    onTertiaryContainer = ZenOnTertiaryContainerDark,
    error = ZenErrorDark,
    onError = ZenOnErrorDark,
    errorContainer = ZenErrorContainerDark,
    onErrorContainer = ZenOnErrorContainerDark,
    background = ZenBackgroundDark,
    onBackground = ZenOnBackgroundDark,
    surface = ZenSurfaceDark,
    onSurface = ZenOnSurfaceDark,
    surfaceVariant = ZenSurfaceVariantDark,
    onSurfaceVariant = ZenOnSurfaceVariantDark,
    outline = ZenOutlineDark,
    outlineVariant = ZenOutlineVariantDark
)

@Composable
fun MyZenFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZenTypography,
        content = content
    )
}
