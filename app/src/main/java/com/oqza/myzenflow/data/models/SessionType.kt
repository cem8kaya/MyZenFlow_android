package com.oqza.myzenflow.data.models

enum class SessionType {
    MEDITATION,
    BREATHING,
    MINDFULNESS,
    SLEEP;

    companion object {
        fun fromString(value: String): SessionType {
            return values().find { it.name == value } ?: MEDITATION
        }
    }
}
