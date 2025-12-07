package com.oqza.myzenflow.presentation.components

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Particle system for tree animations
 * Generates floating particles, sparkles, and leaf effects
 */
class ParticleSystem {
    private val particles = mutableStateListOf<Particle>()
    private var isActive = false

    /**
     * Get current particles
     */
    fun getParticles(): List<Particle> = particles

    /**
     * Start particle generation
     */
    suspend fun start(
        width: Float,
        height: Float,
        particleType: ParticleType = ParticleType.SPARKLE
    ) {
        isActive = true
        while (isActive) {
            // Add new particles
            if (particles.size < 20) {
                particles.add(generateParticle(width, height, particleType))
            }

            // Update existing particles
            updateParticles()

            delay(100) // 10 FPS for particle updates
        }
    }

    /**
     * Stop particle generation
     */
    fun stop() {
        isActive = false
        particles.clear()
    }

    /**
     * Generate a new particle
     */
    private fun generateParticle(
        width: Float,
        height: Float,
        type: ParticleType
    ): Particle {
        return when (type) {
            ParticleType.SPARKLE -> generateSparkle(width, height)
            ParticleType.LEAF -> generateLeaf(width, height)
            ParticleType.BLOSSOM -> generateBlossom(width, height)
            ParticleType.ENERGY -> generateEnergy(width, height)
        }
    }

    /**
     * Generate sparkle particle (for tree growth)
     */
    private fun generateSparkle(width: Float, height: Float): Particle {
        return Particle(
            x = Random.nextFloat() * width,
            y = height * 0.2f + Random.nextFloat() * height * 0.6f,
            size = Random.nextFloat() * 3f + 1f,
            color = Color(0xFFFFD700), // Gold
            alpha = Random.nextFloat() * 0.5f + 0.5f,
            velocityX = (Random.nextFloat() - 0.5f) * 2f,
            velocityY = -Random.nextFloat() * 2f - 1f
        )
    }

    /**
     * Generate falling leaf particle
     */
    private fun generateLeaf(width: Float, height: Float): Particle {
        return Particle(
            x = Random.nextFloat() * width,
            y = 0f,
            size = Random.nextFloat() * 4f + 2f,
            color = Color(0xFF228B22).copy(alpha = 0.8f), // Forest Green
            alpha = Random.nextFloat() * 0.6f + 0.4f,
            velocityX = (Random.nextFloat() - 0.5f) * 3f,
            velocityY = Random.nextFloat() * 2f + 1f
        )
    }

    /**
     * Generate blossom particle
     */
    private fun generateBlossom(width: Float, height: Float): Particle {
        return Particle(
            x = Random.nextFloat() * width,
            y = height * 0.3f,
            size = Random.nextFloat() * 3f + 2f,
            color = Color(0xFFFFB6C1), // Light Pink
            alpha = Random.nextFloat() * 0.5f + 0.5f,
            velocityX = (Random.nextFloat() - 0.5f) * 2f,
            velocityY = Random.nextFloat() * 1.5f + 0.5f
        )
    }

    /**
     * Generate energy particle (for level up)
     */
    private fun generateEnergy(width: Float, height: Float): Particle {
        val centerX = width / 2
        val centerY = height * 0.5f
        val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
        val distance = Random.nextFloat() * 100f

        return Particle(
            x = centerX + kotlin.math.cos(angle) * distance,
            y = centerY + kotlin.math.sin(angle) * distance,
            size = Random.nextFloat() * 4f + 2f,
            color = Color(0xFF00CED1), // Dark Turquoise
            alpha = Random.nextFloat() * 0.7f + 0.3f,
            velocityX = kotlin.math.cos(angle) * -3f,
            velocityY = kotlin.math.sin(angle) * -3f
        )
    }

    /**
     * Update particle positions and remove dead particles
     */
    private fun updateParticles() {
        particles.removeAll { particle ->
            // Remove particles that are too faded or off-screen
            particle.alpha <= 0.1f || particle.y < -50 || particle.y > 1000
        }

        particles.forEachIndexed { index, particle ->
            particles[index] = particle.copy(
                x = particle.x + particle.velocityX,
                y = particle.y + particle.velocityY,
                alpha = (particle.alpha - 0.01f).coerceAtLeast(0f)
            )
        }
    }

    /**
     * Emit burst of particles (for achievements/level up)
     */
    fun emitBurst(
        width: Float,
        height: Float,
        count: Int = 10,
        type: ParticleType = ParticleType.SPARKLE
    ) {
        repeat(count) {
            particles.add(generateParticle(width, height, type))
        }
    }
}

/**
 * Particle type enum
 */
enum class ParticleType {
    SPARKLE,    // Sparkles for growth
    LEAF,       // Falling leaves
    BLOSSOM,    // Blossom petals
    ENERGY      // Energy particles for level up
}

/**
 * Composable function to use particle system
 */
@Composable
fun rememberParticleSystem(): ParticleSystem {
    return remember { ParticleSystem() }
}

/**
 * Effect to start/stop particle system
 */
@Composable
fun LaunchedParticleEffect(
    particleSystem: ParticleSystem,
    width: Float,
    height: Float,
    enabled: Boolean,
    particleType: ParticleType = ParticleType.SPARKLE
) {
    LaunchedEffect(enabled, particleType) {
        if (enabled) {
            particleSystem.start(width, height, particleType)
        } else {
            particleSystem.stop()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            particleSystem.stop()
        }
    }
}
