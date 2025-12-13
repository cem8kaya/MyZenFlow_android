package com.oqza.myzenflow.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oqza.myzenflow.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tree visualization component for Zen Garden
 * Displays tree growth across 5 levels with animations
 *
 * @param level Tree level (0-5)
 * @param progress Progress to next level (0-1)
 * @param particles List of particles for animation effects
 * @param modifier Modifier for customization
 */
@Composable
fun TreeVisualization(
    level: Int,
    progress: Float,
    particles: List<Particle> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Animation for gentle swaying
    val infiniteTransition = rememberInfiniteTransition(label = "tree_sway")
    val swayOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway_offset"
    )

    // Colors from theme
    val trunkColor = TreeTrunkColor
    val leavesColor = treeLeavesColor
    val blossomColor = TreeBlossomColor
    val fruitColor = TreeFruitColor
    val groundColor = treeGroundColor

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tree level indicator
        Text(
            text = getTreeLevelName(level),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress indicator
        Text(
            text = if (level < 5) {
                "${(progress * 100).toInt()}% to next level"
            } else {
                "Maximum Level!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tree canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            val centerX = size.width / 2
            val groundY = size.height * 0.85f

            // Draw ground line
            drawLine(
                color = groundColor,
                start = Offset(0f, groundY),
                end = Offset(size.width, groundY),
                strokeWidth = 4f
            )

            // Draw tree based on level
            when (level) {
                0 -> drawSeed(centerX, groundY, trunkColor, progress)
                1 -> drawSprout(centerX, groundY, trunkColor, leavesColor, swayOffset, progress)
                2 -> drawSapling(centerX, groundY, trunkColor, leavesColor, swayOffset, progress)
                3 -> drawYoungTree(centerX, groundY, trunkColor, leavesColor, blossomColor, swayOffset, progress)
                4 -> drawMatureTree(centerX, groundY, trunkColor, leavesColor, fruitColor, swayOffset, progress)
                5 -> drawGrandTree(centerX, groundY, trunkColor, leavesColor, fruitColor, blossomColor, swayOffset)
            }

            // Draw particles
            particles.forEach { particle ->
                drawCircle(
                    color = particle.color.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = Offset(particle.x, particle.y)
                )
            }
        }
    }
}

/**
 * Draw Level 0: Seed
 */
private fun DrawScope.drawSeed(
    x: Float,
    groundY: Float,
    trunkColor: Color,
    progress: Float
) {
    val seedSize = 10f + (progress * 5f)
    drawCircle(
        color = trunkColor,
        radius = seedSize,
        center = Offset(x, groundY - seedSize)
    )
}

/**
 * Draw Level 1: Sprout
 */
private fun DrawScope.drawSprout(
    x: Float,
    groundY: Float,
    trunkColor: Color,
    leavesColor: Color,
    swayOffset: Float,
    progress: Float
) {
    val stemHeight = 30f + (progress * 20f)
    val swayX = x + swayOffset * 0.3f

    // Draw stem
    drawLine(
        color = trunkColor,
        start = Offset(x, groundY),
        end = Offset(swayX, groundY - stemHeight),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Draw small leaves
    drawCircle(
        color = leavesColor,
        radius = 8f + (progress * 4f),
        center = Offset(swayX - 6f, groundY - stemHeight)
    )
    drawCircle(
        color = leavesColor,
        radius = 8f + (progress * 4f),
        center = Offset(swayX + 6f, groundY - stemHeight)
    )
}

/**
 * Draw Level 2: Sapling
 */
private fun DrawScope.drawSapling(
    x: Float,
    groundY: Float,
    trunkColor: Color,
    leavesColor: Color,
    swayOffset: Float,
    progress: Float
) {
    val trunkHeight = 80f + (progress * 30f)
    val swayX = x + swayOffset * 0.5f

    // Draw trunk
    drawLine(
        color = trunkColor,
        start = Offset(x, groundY),
        end = Offset(swayX, groundY - trunkHeight),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Draw branches
    val branchY = groundY - trunkHeight * 0.7f
    drawLine(
        color = trunkColor,
        start = Offset(swayX, branchY),
        end = Offset(swayX - 20f, branchY - 15f),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = trunkColor,
        start = Offset(swayX, branchY),
        end = Offset(swayX + 20f, branchY - 15f),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // Draw leaves
    val leafSize = 15f + (progress * 5f)
    drawCircle(
        color = leavesColor,
        radius = leafSize,
        center = Offset(swayX, groundY - trunkHeight)
    )
    drawCircle(
        color = leavesColor,
        radius = leafSize * 0.8f,
        center = Offset(swayX - 20f, branchY - 15f)
    )
    drawCircle(
        color = leavesColor,
        radius = leafSize * 0.8f,
        center = Offset(swayX + 20f, branchY - 15f)
    )
}

/**
 * Draw Level 3: Young Tree
 */
private fun DrawScope.drawYoungTree(
    x: Float,
    groundY: Float,
    trunkColor: Color,
    leavesColor: Color,
    blossomColor: Color,
    swayOffset: Float,
    progress: Float
) {
    val trunkHeight = 150f + (progress * 30f)
    val swayX = x + swayOffset

    // Draw trunk
    drawLine(
        color = trunkColor,
        start = Offset(x, groundY),
        end = Offset(swayX, groundY - trunkHeight),
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )

    // Draw multiple branches
    val branches = listOf(
        Triple(0.5f, -30f, 25f),
        Triple(0.5f, 30f, 25f),
        Triple(0.7f, -40f, 30f),
        Triple(0.7f, 40f, 30f)
    )

    branches.forEach { (heightRatio, xOffset, length) ->
        val branchStartY = groundY - trunkHeight * heightRatio
        val branchEndX = swayX + xOffset
        val branchEndY = branchStartY - length

        drawLine(
            color = trunkColor,
            start = Offset(swayX, branchStartY),
            end = Offset(branchEndX, branchEndY),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Draw leaves on branches
        drawCircle(
            color = leavesColor,
            radius = 20f,
            center = Offset(branchEndX, branchEndY)
        )

        // Draw blossoms (progress-based)
        if (progress > 0.5f) {
            drawCircle(
                color = blossomColor,
                radius = 5f * (progress - 0.5f) * 2f,
                center = Offset(branchEndX + 5f, branchEndY - 5f)
            )
        }
    }

    // Draw top foliage
    drawCircle(
        color = leavesColor,
        radius = 25f + (progress * 10f),
        center = Offset(swayX, groundY - trunkHeight)
    )
}

/**
 * Draw Level 4: Mature Tree
 */
private fun DrawScope.drawMatureTree(
    x: Float,
    groundY: Float,
    trunkColor: Color,
    leavesColor: Color,
    fruitColor: Color,
    swayOffset: Float,
    progress: Float
) {
    val trunkHeight = 220f + (progress * 30f)
    val swayX = x + swayOffset * 1.5f

    // Draw thick trunk
    drawLine(
        color = trunkColor,
        start = Offset(x, groundY),
        end = Offset(swayX, groundY - trunkHeight),
        strokeWidth = 15f,
        cap = StrokeCap.Round
    )

    // Draw extensive branch system
    val branches = listOf(
        Triple(0.4f, -50f, 40f),
        Triple(0.4f, 50f, 40f),
        Triple(0.6f, -60f, 45f),
        Triple(0.6f, 60f, 45f),
        Triple(0.8f, -45f, 35f),
        Triple(0.8f, 45f, 35f)
    )

    branches.forEach { (heightRatio, xOffset, length) ->
        val branchStartY = groundY - trunkHeight * heightRatio
        val branchEndX = swayX + xOffset
        val branchEndY = branchStartY - length

        drawLine(
            color = trunkColor,
            start = Offset(swayX, branchStartY),
            end = Offset(branchEndX, branchEndY),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )

        // Draw leaves
        drawCircle(
            color = leavesColor,
            radius = 30f,
            center = Offset(branchEndX, branchEndY)
        )

        // Draw fruits
        drawCircle(
            color = fruitColor,
            radius = 6f,
            center = Offset(branchEndX - 10f, branchEndY + 8f)
        )
        drawCircle(
            color = fruitColor,
            radius = 6f,
            center = Offset(branchEndX + 10f, branchEndY + 8f)
        )
    }

    // Draw large top foliage
    drawCircle(
        color = leavesColor,
        radius = 35f + (progress * 10f),
        center = Offset(swayX, groundY - trunkHeight)
    )
}

/**
 * Draw Level 5: Grand Tree (Maximum)
 */
private fun DrawScope.drawGrandTree(
    x: Float,
    groundY: Float,
    trunkColor: Color,
    leavesColor: Color,
    fruitColor: Color,
    blossomColor: Color,
    swayOffset: Float
) {
    val trunkHeight = 280f
    val swayX = x + swayOffset * 2f

    // Draw massive trunk with texture
    drawLine(
        color = trunkColor,
        start = Offset(x, groundY),
        end = Offset(swayX, groundY - trunkHeight),
        strokeWidth = 20f,
        cap = StrokeCap.Round
    )

    // Draw trunk details (bark lines)
    for (i in 0..5) {
        val y = groundY - (trunkHeight * i / 5f)
        drawLine(
            color = trunkColor.copy(alpha = 0.5f),
            start = Offset(x - 10f, y),
            end = Offset(x + 10f, y),
            strokeWidth = 2f
        )
    }

    // Draw extensive branch system
    val branches = listOf(
        Triple(0.3f, -60f, 50f),
        Triple(0.3f, 60f, 50f),
        Triple(0.5f, -70f, 55f),
        Triple(0.5f, 70f, 55f),
        Triple(0.7f, -65f, 45f),
        Triple(0.7f, 65f, 45f),
        Triple(0.85f, -50f, 40f),
        Triple(0.85f, 50f, 40f)
    )

    branches.forEach { (heightRatio, xOffset, length) ->
        val branchStartY = groundY - trunkHeight * heightRatio
        val branchEndX = swayX + xOffset
        val branchEndY = branchStartY - length

        drawLine(
            color = trunkColor,
            start = Offset(swayX, branchStartY),
            end = Offset(branchEndX, branchEndY),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )

        // Draw large leaves
        drawCircle(
            color = leavesColor,
            radius = 40f,
            center = Offset(branchEndX, branchEndY)
        )

        // Draw fruits
        drawCircle(
            color = fruitColor,
            radius = 7f,
            center = Offset(branchEndX - 15f, branchEndY + 10f)
        )
        drawCircle(
            color = fruitColor,
            radius = 7f,
            center = Offset(branchEndX + 15f, branchEndY + 10f)
        )

        // Draw blossoms
        drawCircle(
            color = blossomColor,
            radius = 6f,
            center = Offset(branchEndX - 20f, branchEndY - 5f)
        )
        drawCircle(
            color = blossomColor,
            radius = 6f,
            center = Offset(branchEndX + 20f, branchEndY - 5f)
        )
    }

    // Draw majestic top foliage
    drawCircle(
        color = leavesColor,
        radius = 50f,
        center = Offset(swayX, groundY - trunkHeight)
    )

    // Draw glowing aura around grand tree
    drawCircle(
        color = TreeAuraColor.copy(alpha = 0.2f),
        radius = 60f,
        center = Offset(swayX, groundY - trunkHeight),
        style = Stroke(width = 2f)
    )
}

/**
 * Get tree level name
 */
private fun getTreeLevelName(level: Int): String {
    return when (level) {
        0 -> "🌱 Tohum"
        1 -> "🌿 Fidan"
        2 -> "🌳 Genç Ağaç"
        3 -> "🌲 Olgun Ağaç"
        4 -> "🎋 Muhteşem Ağaç"
        5 -> "🌴 Zen Ağacı"
        else -> "Ağaç"
    }
}

/**
 * Particle data class for effects
 */
data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val alpha: Float,
    val velocityX: Float = 0f,
    val velocityY: Float = 0f
)
