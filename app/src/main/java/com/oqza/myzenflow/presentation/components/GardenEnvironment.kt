package com.oqza.myzenflow.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.oqza.myzenflow.data.models.TimeOfDay
import com.oqza.myzenflow.presentation.theme.ZenDawnGold
import com.oqza.myzenflow.presentation.theme.ZenDawnPeach
import com.oqza.myzenflow.presentation.theme.ZenLavender
import kotlin.math.PI
import kotlin.math.sin

/**
 * Draws the sky, sun or moon, clouds, ground flowers and birds behind the Zen tree.
 * The scene follows the real time of day, and grows richer as the tree levels up:
 * flowers from level 2, birds from level 4.
 *
 * [drift] (0..1, repeating) moves clouds and birds slowly; pass 0 for a still scene.
 */
fun DrawScope.drawGardenEnvironment(
    timeOfDay: TimeOfDay,
    level: Int,
    groundY: Float,
    drift: Float
) {
    val isNight = timeOfDay == TimeOfDay.NIGHT
    val (top, bottom) = when (timeOfDay) {
        TimeOfDay.MORNING -> Color(0xFFFFD2AE) to Color(0xFFFFF1E0)
        TimeOfDay.AFTERNOON -> Color(0xFF8FC1F2) to Color(0xFFE3F1FF)
        TimeOfDay.EVENING -> Color(0xFF5E4F96) to Color(0xFFF7B28A)
        TimeOfDay.NIGHT -> Color(0xFF0E0F2E) to Color(0xFF2B3070)
    }
    drawRect(brush = Brush.verticalGradient(listOf(top, bottom), endY = groundY))

    // Stars
    if (isNight) {
        STARS.forEach { (fx, fy, r) ->
            drawCircle(Color.White.copy(alpha = 0.55f + 0.3f * r), radius = 1.2f + 1.6f * r, center = Offset(size.width * fx, groundY * fy))
        }
    }

    // Sun or moon
    val (bodyX, bodyY) = when (timeOfDay) {
        TimeOfDay.MORNING -> 0.2f to 0.5f
        TimeOfDay.AFTERNOON -> 0.72f to 0.2f
        TimeOfDay.EVENING -> 0.82f to 0.62f
        TimeOfDay.NIGHT -> 0.78f to 0.22f
    }
    val bodyCenter = Offset(size.width * bodyX, groundY * bodyY)
    val bodyColor = if (isNight) Color(0xFFF4F1E4) else ZenDawnGold
    drawCircle(
        brush = Brush.radialGradient(
            listOf(bodyColor.copy(alpha = 0.45f), Color.Transparent),
            center = bodyCenter,
            radius = 90f
        ),
        radius = 90f,
        center = bodyCenter
    )
    drawCircle(bodyColor, radius = 26f, center = bodyCenter)
    if (isNight) {
        // Crescent: cut a bite with the sky colour
        drawCircle(Color(0xFF1B1E4F), radius = 22f, center = bodyCenter + Offset(12f, -6f))
    }

    // Clouds drift left to right and wrap around
    val cloudColor = if (isNight) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.75f)
    CLOUDS.forEach { (fx, fy, scale) ->
        val x = ((fx + drift) % 1f) * (size.width + 240f) - 120f
        drawCloud(Offset(x, groundY * fy), scale, cloudColor)
    }

    // Soil / grass band
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF6F9A82).copy(alpha = if (isNight) 0.35f else 0.55f), Color(0xFF3F6B52).copy(alpha = if (isNight) 0.5f else 0.7f)),
            startY = groundY,
            endY = size.height
        ),
        topLeft = Offset(0f, groundY),
        size = androidx.compose.ui.geometry.Size(size.width, size.height - groundY)
    )

    // Flowers grow with the tree
    if (level >= 2) {
        val count = (level * 3).coerceAtMost(FLOWERS.size)
        for (i in 0 until count) {
            val (fx, depth, colorIndex) = FLOWERS[i]
            val color = FLOWER_COLORS[colorIndex.toInt() % FLOWER_COLORS.size]
            val x = size.width * fx
            val y = groundY + (size.height - groundY) * depth
            drawLine(Color(0xFF3F6B52), Offset(x, y), Offset(x, y + 9f), strokeWidth = 2f)
            for (p in 0 until 5) {
                val a = (2 * PI * p / 5).toFloat()
                drawCircle(color, radius = 3.4f, center = Offset(x + 4.6f * kotlin.math.cos(a), y + 4.6f * sin(a)))
            }
            drawCircle(ZenDawnGold, radius = 2.4f, center = Offset(x, y))
        }
    }

    // Birds from level 4 (daytime only)
    if (level >= 4 && !isNight) {
        BIRDS.forEach { (fx, fy, s) ->
            val x = ((fx + drift * 2f) % 1f) * (size.width + 80f) - 40f
            val y = groundY * fy + 6f * sin((drift * 2f * PI * 6 + fx * 10).toFloat())
            drawBird(Offset(x, y), s, Color(0xFF3B3552))
        }
    }
}

private fun DrawScope.drawCloud(center: Offset, scale: Float, color: Color) {
    drawCircle(color, radius = 22f * scale, center = center)
    drawCircle(color, radius = 28f * scale, center = center + Offset(26f * scale, -8f * scale))
    drawCircle(color, radius = 20f * scale, center = center + Offset(54f * scale, 0f))
    drawRoundRectLike(center + Offset(-4f * scale, 4f * scale), 70f * scale, 16f * scale, color)
}

private fun DrawScope.drawRoundRectLike(topLeft: Offset, w: Float, h: Float, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = androidx.compose.ui.geometry.Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2f)
    )
}

private fun DrawScope.drawBird(at: Offset, scale: Float, color: Color) {
    val path = Path().apply {
        moveTo(at.x - 9f * scale, at.y)
        quadraticBezierTo(at.x - 4f * scale, at.y - 7f * scale, at.x, at.y)
        quadraticBezierTo(at.x + 4f * scale, at.y - 7f * scale, at.x + 9f * scale, at.y)
    }
    drawPath(path, color, style = Stroke(width = 2f * scale))
}

// Fixed layouts so the scene is stable between frames and launches: (x fraction, y fraction, size/variant)
private val STARS = listOf(
    Triple(0.08f, 0.10f, 0.4f), Triple(0.18f, 0.32f, 0.9f), Triple(0.27f, 0.12f, 0.2f),
    Triple(0.36f, 0.40f, 0.6f), Triple(0.45f, 0.08f, 0.8f), Triple(0.55f, 0.28f, 0.3f),
    Triple(0.63f, 0.14f, 0.7f), Triple(0.90f, 0.40f, 0.5f), Triple(0.95f, 0.10f, 0.9f),
    Triple(0.12f, 0.55f, 0.3f), Triple(0.70f, 0.50f, 0.6f), Triple(0.50f, 0.52f, 0.2f)
)
private val CLOUDS = listOf(Triple(0.05f, 0.25f, 1.0f), Triple(0.45f, 0.40f, 0.8f), Triple(0.78f, 0.18f, 1.2f))
private val BIRDS = listOf(Triple(0.15f, 0.30f, 1.0f), Triple(0.22f, 0.36f, 0.8f), Triple(0.62f, 0.22f, 0.9f))
private val FLOWER_COLORS = listOf(ZenDawnPeach, ZenLavender, ZenDawnGold, Color(0xFFF2A7C3))
private val FLOWERS = listOf(
    Triple(0.10f, 0.25f, 0f), Triple(0.84f, 0.30f, 1f), Triple(0.22f, 0.55f, 2f),
    Triple(0.70f, 0.60f, 3f), Triple(0.35f, 0.20f, 1f), Triple(0.92f, 0.65f, 0f),
    Triple(0.05f, 0.70f, 3f), Triple(0.58f, 0.35f, 2f), Triple(0.76f, 0.22f, 1f),
    Triple(0.30f, 0.75f, 0f), Triple(0.48f, 0.65f, 3f), Triple(0.15f, 0.45f, 1f),
    Triple(0.88f, 0.48f, 2f), Triple(0.64f, 0.78f, 0f), Triple(0.40f, 0.45f, 2f)
)
