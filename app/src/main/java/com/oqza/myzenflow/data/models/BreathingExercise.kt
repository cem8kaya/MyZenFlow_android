package com.oqza.myzenflow.data.models

/**
 * Breathing exercise types available in the app
 * Matches iOS BreathingExercise enum
 */
enum class BreathingExercise(
    val displayName: String,
    val inhaleSeconds: Int,
    val holdSeconds: Int,
    val exhaleSeconds: Int,
    val cycles: Int
) {
    BOX_BREATHING(
        displayName = "Box Breathing",
        inhaleSeconds = 4,
        holdSeconds = 4,
        exhaleSeconds = 4,
        cycles = 4
    ),
    DEEP_BREATHING(
        displayName = "Deep Breathing",
        inhaleSeconds = 4,
        holdSeconds = 7,
        exhaleSeconds = 8,
        cycles = 4
    ),
    CALM_BREATHING(
        displayName = "Calm Breathing",
        inhaleSeconds = 4,
        holdSeconds = 0,
        exhaleSeconds = 6,
        cycles = 6
    ),
    ENERGIZING_BREATHING(
        displayName = "Energizing Breathing",
        inhaleSeconds = 3,
        holdSeconds = 3,
        exhaleSeconds = 3,
        cycles = 8
    );

    companion object {
        fun fromString(value: String): BreathingExercise {
            return values().find { it.name == value } ?: BOX_BREATHING
        }
    }
}
