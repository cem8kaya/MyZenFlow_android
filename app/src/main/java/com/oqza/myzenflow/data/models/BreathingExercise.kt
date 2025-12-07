package com.oqza.myzenflow.data.models

/**
 * Breathing exercise types available in the app
 * Supports 5 predefined types + custom rhythm
 */
data class BreathingExerciseType(
    val id: String,
    val displayName: String,
    val inhaleSeconds: Int,
    val holdInhaleSeconds: Int,
    val exhaleSeconds: Int,
    val holdExhaleSeconds: Int,
    val cycles: Int,
    val description: String
) {
    companion object {
        // Box Breathing (4-4-4-4): Square breathing pattern
        val BOX_BREATHING = BreathingExerciseType(
            id = "box_breathing",
            displayName = "Box Breathing",
            inhaleSeconds = 4,
            holdInhaleSeconds = 4,
            exhaleSeconds = 4,
            holdExhaleSeconds = 4,
            cycles = 4,
            description = "Equal rhythm for balance and focus"
        )

        // 4-7-8 Technique: Relaxation and sleep
        val TECHNIQUE_478 = BreathingExerciseType(
            id = "technique_478",
            displayName = "4-7-8 Technique",
            inhaleSeconds = 4,
            holdInhaleSeconds = 7,
            exhaleSeconds = 8,
            holdExhaleSeconds = 0,
            cycles = 4,
            description = "Deep relaxation and stress relief"
        )

        // Calming Breath (4-8): Extended exhale for relaxation
        val CALMING_BREATH = BreathingExerciseType(
            id = "calming_breath",
            displayName = "Calming Breath",
            inhaleSeconds = 4,
            holdInhaleSeconds = 0,
            exhaleSeconds = 8,
            holdExhaleSeconds = 0,
            cycles = 6,
            description = "Extended exhale for calmness"
        )

        // Energizing Breath: Fast-paced for energy boost
        val ENERGIZING_BREATH = BreathingExerciseType(
            id = "energizing_breath",
            displayName = "Energizing Breath",
            inhaleSeconds = 2,
            holdInhaleSeconds = 1,
            exhaleSeconds = 2,
            holdExhaleSeconds = 1,
            cycles = 10,
            description = "Quick rhythm for energy and alertness"
        )

        // Coherent Breathing (5-5): Heart rate variability
        val COHERENT_BREATHING = BreathingExerciseType(
            id = "coherent_breathing",
            displayName = "Coherent Breathing",
            inhaleSeconds = 5,
            holdInhaleSeconds = 0,
            exhaleSeconds = 5,
            holdExhaleSeconds = 0,
            cycles = 6,
            description = "Balanced breathing for coherence"
        )

        // All predefined exercises
        val ALL_EXERCISES = listOf(
            BOX_BREATHING,
            TECHNIQUE_478,
            CALMING_BREATH,
            ENERGIZING_BREATH,
            COHERENT_BREATHING
        )

        fun fromId(id: String): BreathingExerciseType? {
            return ALL_EXERCISES.find { it.id == id }
        }

        // Create custom breathing exercise
        fun createCustom(
            displayName: String,
            inhaleSeconds: Int,
            holdInhaleSeconds: Int = 0,
            exhaleSeconds: Int,
            holdExhaleSeconds: Int = 0,
            cycles: Int,
            description: String = "Custom breathing pattern"
        ): BreathingExerciseType {
            return BreathingExerciseType(
                id = "custom_${System.currentTimeMillis()}",
                displayName = displayName,
                inhaleSeconds = inhaleSeconds,
                holdInhaleSeconds = holdInhaleSeconds,
                exhaleSeconds = exhaleSeconds,
                holdExhaleSeconds = holdExhaleSeconds,
                cycles = cycles,
                description = description
            )
        }
    }

    /**
     * Calculate total duration of one cycle in seconds
     */
    val cycleDurationSeconds: Int
        get() = inhaleSeconds + holdInhaleSeconds + exhaleSeconds + holdExhaleSeconds

    /**
     * Calculate total exercise duration in seconds
     */
    val totalDurationSeconds: Int
        get() = cycleDurationSeconds * cycles
}

/**
 * Breathing phase during exercise
 */
enum class BreathingPhase {
    INHALE,
    HOLD_INHALE,
    EXHALE,
    HOLD_EXHALE,
    REST
}
