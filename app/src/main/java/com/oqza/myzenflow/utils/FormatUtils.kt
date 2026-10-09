package com.oqza.myzenflow.utils

import android.content.Context
import com.oqza.myzenflow.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * FormatUtils - Locale-aware formatting utilities
 *
 * Features:
 * - Date and time formatting with locale
 * - Number formatting with locale
 * - Duration formatting
 * - Plural formatting
 */
object FormatUtils {

    /**
     * Format LocalDate with locale
     *
     * @param date The date to format
     * @param locale The locale to use
     * @param style The format style (SHORT, MEDIUM, LONG, FULL)
     * @return Formatted date string
     */
    fun formatDate(
        date: LocalDate,
        locale: Locale,
        style: FormatStyle = FormatStyle.MEDIUM
    ): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale)
        return date.format(formatter)
    }

    /**
     * Format LocalDate with custom pattern
     *
     * @param date The date to format
     * @param pattern The pattern (e.g., "dd MMM yyyy", "EEEE")
     * @param locale The locale to use
     * @return Formatted date string
     */
    fun formatDate(
        date: LocalDate,
        pattern: String,
        locale: Locale
    ): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        return date.format(formatter)
    }

    /**
     * Format LocalDateTime with locale
     *
     * @param dateTime The date-time to format
     * @param locale The locale to use
     * @param dateStyle The date format style
     * @param timeStyle The time format style
     * @return Formatted date-time string
     */
    fun formatDateTime(
        dateTime: LocalDateTime,
        locale: Locale,
        dateStyle: FormatStyle = FormatStyle.MEDIUM,
        timeStyle: FormatStyle = FormatStyle.SHORT
    ): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle)
            .withLocale(locale)
        return dateTime.format(formatter)
    }

    /**
     * Format LocalDateTime with custom pattern
     *
     * @param dateTime The date-time to format
     * @param pattern The pattern (e.g., "dd MMM yyyy HH:mm")
     * @param locale The locale to use
     * @return Formatted date-time string
     */
    fun formatDateTime(
        dateTime: LocalDateTime,
        pattern: String,
        locale: Locale
    ): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        return dateTime.format(formatter)
    }

    /**
     * Format time (HH:mm)
     *
     * @param dateTime The date-time to format
     * @param locale The locale to use
     * @return Formatted time string (e.g., "14:30")
     */
    fun formatTime(
        dateTime: LocalDateTime,
        locale: Locale
    ): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm", locale)
        return dateTime.format(formatter)
    }

    /**
     * Format day of week
     *
     * @param date The date to format
     * @param locale The locale to use
     * @return Day of week (e.g., "Monday" / "Pazartesi")
     */
    fun formatDayOfWeek(
        date: LocalDate,
        locale: Locale
    ): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE", locale)
        return date.format(formatter)
    }

    /**
     * Format short day of week
     *
     * @param date The date to format
     * @param locale The locale to use
     * @return Short day of week (e.g., "Mon" / "Pzt")
     */
    fun formatShortDayOfWeek(
        date: LocalDate,
        locale: Locale
    ): String {
        val formatter = DateTimeFormatter.ofPattern("EEE", locale)
        return date.format(formatter)
    }

    /**
     * Format month
     *
     * @param date The date to format
     * @param locale The locale to use
     * @return Month name (e.g., "January" / "Ocak")
     */
    fun formatMonth(
        date: LocalDate,
        locale: Locale
    ): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM", locale)
        return date.format(formatter)
    }

    /**
     * Format short month
     *
     * @param date The date to format
     * @param locale The locale to use
     * @return Short month name (e.g., "Jan" / "Oca")
     */
    fun formatShortMonth(
        date: LocalDate,
        locale: Locale
    ): String {
        val formatter = DateTimeFormatter.ofPattern("MMM", locale)
        return date.format(formatter)
    }

    /**
     * Format decimal number with locale
     *
     * @param value The value to format
     * @param decimals Number of decimal places
     * @param locale The locale to use
     * @return Formatted number string
     */
    fun formatDecimal(
        value: Float,
        decimals: Int,
        locale: Locale
    ): String {
        val symbols = DecimalFormatSymbols(locale)
        val pattern = if (decimals > 0) {
            "0.${"0".repeat(decimals)}"
        } else {
            "0"
        }
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(value)
    }

    /**
     * Format integer with locale (adds thousand separators)
     *
     * @param value The value to format
     * @param locale The locale to use
     * @return Formatted number string
     */
    fun formatInteger(
        value: Int,
        locale: Locale
    ): String {
        val symbols = DecimalFormatSymbols(locale)
        val formatter = DecimalFormat("#,###", symbols)
        return formatter.format(value)
    }

    /**
     * Format duration in minutes
     *
     * @param minutes The duration in minutes
     * @param context Context for string resources
     * @return Formatted duration (e.g., "25 min" / "25 dk")
     */
    fun formatMinutes(
        minutes: Int,
        context: Context
    ): String {
        return context.getString(R.string.minutes_count, minutes)
    }

    /**
     * Format duration in hours and minutes
     *
     * @param totalMinutes Total minutes
     * @param locale The locale to use
     * @return Formatted duration (e.g., "2h 30m" / "2s 30dk")
     */
    fun formatDuration(
        totalMinutes: Int,
        locale: Locale
    ): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when (locale.language) {
            "tr" -> {
                when {
                    hours > 0 && minutes > 0 -> "${hours}s ${minutes}dk"
                    hours > 0 -> "${hours}s"
                    else -> "${minutes}dk"
                }
            }
            else -> {
                when {
                    hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                    hours > 0 -> "${hours}h"
                    else -> "${minutes}m"
                }
            }
        }
    }

    /**
     * Format session count
     *
     * @param count Number of sessions
     * @param context Context for string resources
     * @return Formatted session count (e.g., "5 sessions" / "5 seans")
     */
    fun formatSessions(
        count: Int,
        context: Context
    ): String {
        return context.resources.getQuantityString(R.plurals.sessions_count, count, count)
    }

    /**
     * Format days count
     *
     * @param days Number of days
     * @param context Context for string resources
     * @return Formatted days count (e.g., "7 days" / "7 gün")
     */
    fun formatDays(
        days: Int,
        context: Context
    ): String {
        return context.resources.getQuantityString(R.plurals.days_count, days, days)
    }

    /**
     * Format percentage
     *
     * @param value The value (0.0 to 1.0)
     * @param locale The locale to use
     * @return Formatted percentage (e.g., "75%")
     */
    fun formatPercentage(
        value: Float,
        locale: Locale
    ): String {
        val percentage = (value * 100).toInt()
        return "$percentage%"
    }

    /**
     * Format volume level
     *
     * @param volume The volume (0.0 to 1.0)
     * @param locale The locale to use
     * @return Formatted volume (e.g., "75%")
     */
    fun formatVolume(
        volume: Float,
        locale: Locale
    ): String {
        return formatPercentage(volume, locale)
    }
}

/**
 * Extension functions for easier formatting
 */

/**
 * Format LocalDate with locale
 */
fun LocalDate.format(locale: Locale, style: FormatStyle = FormatStyle.MEDIUM): String {
    return FormatUtils.formatDate(this, locale, style)
}

/**
 * Format LocalDate with pattern
 */
fun LocalDate.format(pattern: String, locale: Locale): String {
    return FormatUtils.formatDate(this, pattern, locale)
}

/**
 * Format LocalDateTime with locale
 */
fun LocalDateTime.format(
    locale: Locale,
    dateStyle: FormatStyle = FormatStyle.MEDIUM,
    timeStyle: FormatStyle = FormatStyle.SHORT
): String {
    return FormatUtils.formatDateTime(this, locale, dateStyle, timeStyle)
}

/**
 * Format LocalDateTime with pattern
 */
fun LocalDateTime.format(pattern: String, locale: Locale): String {
    return FormatUtils.formatDateTime(this, pattern, locale)
}

/**
 * Format Int as minutes
 */
fun Int.formatMinutes(context: Context): String {
    return FormatUtils.formatMinutes(this, context)
}

/**
 * Format Int as sessions
 */
fun Int.formatSessions(context: Context): String {
    return FormatUtils.formatSessions(this, context)
}

/**
 * Format Int as days
 */
fun Int.formatDays(context: Context): String {
    return FormatUtils.formatDays(this, context)
}

/**
 * Format Int with thousand separators
 */
fun Int.formatInteger(locale: Locale): String {
    return FormatUtils.formatInteger(this, locale)
}

/**
 * Format Float as decimal
 */
fun Float.formatDecimal(decimals: Int, locale: Locale): String {
    return FormatUtils.formatDecimal(this, decimals, locale)
}

/**
 * Format Float as percentage
 */
fun Float.formatPercentage(locale: Locale): String {
    return FormatUtils.formatPercentage(this, locale)
}
