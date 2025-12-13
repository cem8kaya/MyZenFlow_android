package com.oqza.myzenflow.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Unit tests for FormatUtils
 * Tests locale-aware formatting utilities
 */
class FormatUtilsTest {

    @Test
    fun formatDate_withTurkishLocale_returnsCorrectFormat() {
        val date = LocalDate.of(2024, 1, 15)
        val locale = Locale("tr", "TR")

        val result = FormatUtils.formatDate(date, locale, FormatStyle.MEDIUM)

        // Should contain Turkish month name or numbers
        assertTrue(result.contains("15") || result.contains("Oca") || result.contains("Ocak"))
    }

    @Test
    fun formatDate_withCustomPattern_returnsCorrectFormat() {
        val date = LocalDate.of(2024, 1, 15)
        val locale = Locale.US

        val result = FormatUtils.formatDate(date, "dd MMM yyyy", locale)

        assertEquals("15 Jan 2024", result)
    }

    @Test
    fun formatDateTime_withPattern_returnsCorrectFormat() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        val locale = Locale.US

        val result = FormatUtils.formatDateTime(dateTime, "dd MMM yyyy HH:mm", locale)

        assertEquals("15 Jan 2024 14:30", result)
    }

    @Test
    fun formatTime_returns24HourFormat() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        val locale = Locale.US

        val result = FormatUtils.formatTime(dateTime, locale)

        assertEquals("14:30", result)
    }

    @Test
    fun formatTime_withMidnight_returnsCorrectFormat() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 0, 5)
        val locale = Locale.US

        val result = FormatUtils.formatTime(dateTime, locale)

        assertEquals("00:05", result)
    }

    @Test
    fun formatDayOfWeek_withEnglishLocale_returnsFullDayName() {
        val date = LocalDate.of(2024, 1, 15) // Monday
        val locale = Locale.US

        val result = FormatUtils.formatDayOfWeek(date, locale)

        assertEquals("Monday", result)
    }

    @Test
    fun formatShortDayOfWeek_withEnglishLocale_returnsShortDayName() {
        val date = LocalDate.of(2024, 1, 15) // Monday
        val locale = Locale.US

        val result = FormatUtils.formatShortDayOfWeek(date, locale)

        assertEquals("Mon", result)
    }

    @Test
    fun formatMonth_withEnglishLocale_returnsFullMonthName() {
        val date = LocalDate.of(2024, 1, 15)
        val locale = Locale.US

        val result = FormatUtils.formatMonth(date, locale)

        assertEquals("January", result)
    }

    @Test
    fun formatShortMonth_withEnglishLocale_returnsShortMonthName() {
        val date = LocalDate.of(2024, 1, 15)
        val locale = Locale.US

        val result = FormatUtils.formatShortMonth(date, locale)

        assertEquals("Jan", result)
    }

    @Test
    fun formatDecimal_withTwoDecimals_returnsCorrectFormat() {
        val value = 3.14159f
        val locale = Locale.US

        val result = FormatUtils.formatDecimal(value, 2, locale)

        assertEquals("3.14", result)
    }

    @Test
    fun formatDecimal_withZeroDecimals_returnsInteger() {
        val value = 3.14159f
        val locale = Locale.US

        val result = FormatUtils.formatDecimal(value, 0, locale)

        assertEquals("3", result)
    }

    @Test
    fun formatInteger_withThousands_addsThousandSeparator() {
        val value = 1234567
        val locale = Locale.US

        val result = FormatUtils.formatInteger(value, locale)

        assertEquals("1,234,567", result)
    }

    @Test
    fun formatInteger_withTurkishLocale_usesTurkishSeparator() {
        val value = 1234567
        val locale = Locale("tr", "TR")

        val result = FormatUtils.formatInteger(value, locale)

        // Turkish locale uses . as thousand separator
        assertTrue(result.contains("."))
    }

    @Test
    fun formatDuration_withOnlyMinutes_returnsMinutesOnly() {
        val totalMinutes = 45
        val locale = Locale.US

        val result = FormatUtils.formatDuration(totalMinutes, locale)

        assertEquals("45m", result)
    }

    @Test
    fun formatDuration_withHoursAndMinutes_returnsBoth() {
        val totalMinutes = 150 // 2 hours 30 minutes
        val locale = Locale.US

        val result = FormatUtils.formatDuration(totalMinutes, locale)

        assertEquals("2h 30m", result)
    }

    @Test
    fun formatDuration_withOnlyHours_returnsHoursOnly() {
        val totalMinutes = 120 // 2 hours exactly
        val locale = Locale.US

        val result = FormatUtils.formatDuration(totalMinutes, locale)

        assertEquals("2h", result)
    }

    @Test
    fun formatDuration_withTurkishLocale_usesTurkishUnits() {
        val totalMinutes = 150 // 2 hours 30 minutes
        val locale = Locale("tr", "TR")

        val result = FormatUtils.formatDuration(totalMinutes, locale)

        assertEquals("2s 30dk", result)
    }

    @Test
    fun formatDuration_turkishLocale_withOnlyMinutes_usesTurkishUnit() {
        val totalMinutes = 45
        val locale = Locale("tr", "TR")

        val result = FormatUtils.formatDuration(totalMinutes, locale)

        assertEquals("45dk", result)
    }

    @Test
    fun formatPercentage_withHalfValue_returns50Percent() {
        val value = 0.5f
        val locale = Locale.US

        val result = FormatUtils.formatPercentage(value, locale)

        assertEquals("50%", result)
    }

    @Test
    fun formatPercentage_withFullValue_returns100Percent() {
        val value = 1.0f
        val locale = Locale.US

        val result = FormatUtils.formatPercentage(value, locale)

        assertEquals("100%", result)
    }

    @Test
    fun formatPercentage_withZeroValue_returns0Percent() {
        val value = 0.0f
        val locale = Locale.US

        val result = FormatUtils.formatPercentage(value, locale)

        assertEquals("0%", result)
    }

    @Test
    fun formatVolume_delegatesToFormatPercentage() {
        val volume = 0.75f
        val locale = Locale.US

        val result = FormatUtils.formatVolume(volume, locale)

        assertEquals("75%", result)
    }

    // Extension function tests

    @Test
    fun localDateExtension_format_worksCorrectly() {
        val date = LocalDate.of(2024, 1, 15)
        val locale = Locale.US

        val result = date.format("dd MMM yyyy", locale)

        assertEquals("15 Jan 2024", result)
    }

    @Test
    fun localDateTimeExtension_format_worksCorrectly() {
        val dateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        val locale = Locale.US

        val result = dateTime.format("dd MMM yyyy HH:mm", locale)

        assertEquals("15 Jan 2024 14:30", result)
    }

    @Test
    fun intExtension_formatInteger_worksCorrectly() {
        val value = 1234567
        val locale = Locale.US

        val result = value.formatInteger(locale)

        assertEquals("1,234,567", result)
    }

    @Test
    fun floatExtension_formatDecimal_worksCorrectly() {
        val value = 3.14159f
        val locale = Locale.US

        val result = value.formatDecimal(2, locale)

        assertEquals("3.14", result)
    }

    @Test
    fun floatExtension_formatPercentage_worksCorrectly() {
        val value = 0.75f
        val locale = Locale.US

        val result = value.formatPercentage(locale)

        assertEquals("75%", result)
    }
}
