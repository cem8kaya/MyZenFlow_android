package com.oqza.myzenflow.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Font families. System serif/sans are used today; to ship brand fonts, drop the
 * .ttf files into res/font and swap these two values (nothing else needs to change).
 */
object ZenFonts {
    val display: FontFamily = FontFamily.Serif
    val body: FontFamily = FontFamily.SansSerif
}

// ZenFlow Typography System
val ZenTypography = Typography(
    // Display Large
    displayLarge = TextStyle(
        fontFamily = ZenFonts.display,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    // Display Medium
    displayMedium = TextStyle(
        fontFamily = ZenFonts.display,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    // Display Small
    displaySmall = TextStyle(
        fontFamily = ZenFonts.display,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    // Headline Large
    headlineLarge = TextStyle(
        fontFamily = ZenFonts.display,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    // Headline Medium
    headlineMedium = TextStyle(
        fontFamily = ZenFonts.display,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    // Headline Small
    headlineSmall = TextStyle(
        fontFamily = ZenFonts.display,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Title Large
    titleLarge = TextStyle(
        fontFamily = ZenFonts.display,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Title Medium
    titleMedium = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Title Small
    titleSmall = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Body Large
    bodyLarge = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Body Medium
    bodyMedium = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // Body Small
    bodySmall = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // Label Large
    labelLarge = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Label Medium
    labelMedium = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Label Small
    labelSmall = TextStyle(
        fontFamily = ZenFonts.body,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
