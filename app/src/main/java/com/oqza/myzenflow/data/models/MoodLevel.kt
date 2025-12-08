package com.oqza.myzenflow.data.models

enum class MoodLevel(val value: Int) {
    VERY_BAD(1),
    BAD(2),
    NEUTRAL(3),
    GOOD(4),
    VERY_GOOD(5);

    companion object {
        fun fromValue(value: Int): MoodLevel {
            return values().find { it.value == value } ?: NEUTRAL
        }
    }
}
