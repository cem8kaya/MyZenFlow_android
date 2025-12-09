package com.oqza.myzenflow.data.models

enum class SessionType(val displayName: String) {
    MEDITATION("Meditasyon"),
    BREATHING("Nefes Egzersizi"),
    MINDFULNESS("Farkındalık"),
    SLEEP("Uyku"),
    FOCUS("Odaklanma");

    companion object {
        fun fromString(value: String): SessionType {
            return values().find { it.name == value } ?: MEDITATION
        }
    }
}
