package com.oqza.myzenflow.data.models

/** Where a mood check-in was recorded. */
enum class MoodContext { DAILY, AFTER_SESSION }

/** One mood check-in. Stored locally on the device only. */
data class MoodEntry(
    val epochMillis: Long,
    val level: MoodLevel,
    val context: MoodContext
)
