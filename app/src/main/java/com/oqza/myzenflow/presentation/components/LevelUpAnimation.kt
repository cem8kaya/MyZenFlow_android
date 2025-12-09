package com.oqza.myzenflow.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * Level-up celebration animation
 * Shows when tree levels up with particle explosion and success message
 *
 * @param show Whether to show the animation
 * @param newLevel The new level achieved
 * @param onDismiss Callback when animation completes
 */
@Composable
fun LevelUpAnimation(
    show: Boolean,
    newLevel: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after 3 seconds
    LaunchedEffect(show) {
        if (show) {
            delay(3000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(300)) +
                scaleIn(initialScale = 0.8f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )),
        exit = fadeOut(animationSpec = tween(300)) +
                scaleOut(targetScale = 1.2f, animationSpec = tween(300))
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                LevelUpContent(newLevel = newLevel)
            }
        }
    }
}

/**
 * Level-up content with animations
 */
@Composable
private fun LevelUpContent(newLevel: Int) {
    // Scale animation
    val scale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // Glow animation
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trophy/Star icon
        Text(
            text = "⭐",
            fontSize = 80.sp,
            modifier = Modifier.alpha(glowAlpha)
        )

        // Level Up text
        Text(
            text = "Seviye Atladın!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700), // Gold
            fontSize = 32.sp
        )

        // New level badge
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFFFD700).copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.large
                )
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getTreeEmoji(newLevel),
                    fontSize = 32.sp
                )
                Text(
                    text = "Seviye $newLevel",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Achievement name
        Text(
            text = getTreeLevelName(newLevel),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.9f)
        )

        // Encouragement message
        Text(
            text = getEncouragementMessage(newLevel),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * Get tree emoji for level
 */
private fun getTreeEmoji(level: Int): String {
    return when (level) {
        1 -> "🌿"
        2 -> "🌳"
        3 -> "🌲"
        4 -> "🎋"
        5 -> "🌴"
        else -> "🌱"
    }
}

/**
 * Get tree level name
 */
private fun getTreeLevelName(level: Int): String {
    return when (level) {
        1 -> "Fidan"
        2 -> "Genç Ağaç"
        3 -> "Olgun Ağaç"
        4 -> "Muhteşem Ağaç"
        5 -> "Zen Ağacı"
        else -> "Tohum"
    }
}

/**
 * Get encouragement message based on level
 */
private fun getEncouragementMessage(level: Int): String {
    return when (level) {
        1 -> "Harika bir başlangıç! İlk adımı attın."
        2 -> "Büyüme devam ediyor! Çok iyisin."
        3 -> "Muhteşem ilerleme! Alışkanlık haline geldi."
        4 -> "İnanılmaz! Gerçek bir usta oluyorsun."
        5 -> "Maksimum seviye! Zen ustasısın!"
        else -> "Devam et!"
    }
}

/**
 * Particle burst effect for level-up
 */
@Composable
fun LevelUpParticleBurst(
    particleSystem: ParticleSystem,
    width: Float,
    height: Float,
    trigger: Boolean
) {
    LaunchedEffect(trigger) {
        if (trigger) {
            // Emit multiple bursts
            repeat(3) {
                particleSystem.emitBurst(
                    width = width,
                    height = height,
                    count = 20,
                    type = ParticleType.ENERGY
                )
                delay(200)
            }
        }
    }
}
