package com.oqza.myzenflow.presentation.components

import com.oqza.myzenflow.presentation.theme.ZenDawnGold
import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import com.oqza.myzenflow.presentation.theme.LocalReducedMotion
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
    val reducedMotion = LocalReducedMotion.current
    // Scale animation
    val scale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = if (reducedMotion) 1f else 0.95f,
        targetValue = if (reducedMotion) 1f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // Glow animation
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = if (reducedMotion) 1f else 0.3f,
        targetValue = if (reducedMotion) 1f else 0.8f,
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
            text = stringResource(R.string.levelup_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = ZenDawnGold,
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
                    text = treeEmoji(newLevel),
                    fontSize = 32.sp
                )
                Text(
                    text = stringResource(R.string.levelup_level, newLevel),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Achievement name
        Text(
            text = treeLevelName(newLevel),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.9f)
        )

        // Encouragement message
        Text(
            text = treeEncouragement(newLevel),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )
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
