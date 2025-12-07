package com.oqza.myzenflow.data.database

import androidx.room.TypeConverter
import com.oqza.myzenflow.data.models.BreathingExercise
import com.oqza.myzenflow.data.models.MoodLevel
import com.oqza.myzenflow.data.models.SessionType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Type converters for Room database
 * Handles conversion of complex types to/from database-compatible types
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun fromSessionType(value: String?): SessionType? {
        return value?.let { SessionType.fromString(it) }
    }

    @TypeConverter
    fun sessionTypeToString(type: SessionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromBreathingExercise(value: String?): BreathingExercise? {
        return value?.let { BreathingExercise.fromString(it) }
    }

    @TypeConverter
    fun breathingExerciseToString(exercise: BreathingExercise?): String? {
        return exercise?.name
    }

    @TypeConverter
    fun fromMoodLevel(value: Int?): MoodLevel? {
        return value?.let { MoodLevel.fromValue(it) }
    }

    @TypeConverter
    fun moodLevelToInt(mood: MoodLevel?): Int? {
        return mood?.value
    }
}
