package com.oqza.myzenflow.presentation.components

import com.oqza.myzenflow.presentation.theme.ZenDawnGold
import com.oqza.myzenflow.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oqza.myzenflow.data.entities.AchievementEntity
import java.time.format.DateTimeFormatter

/**
 * Achievement card component
 * Shows achievement badge, name, description, and progress
 */
@Composable
fun AchievementCard(
    achievement: AchievementEntity,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val info = AchievementEntity.getAchievementInfo(achievement.type)

    ZenCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Achievement icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = if (isUnlocked) {
                            ZenDawnGold.copy(alpha = 0.2f)
                        } else {
                            Color.Gray.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    )
                    .alpha(if (isUnlocked) 1f else 0.4f),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = info.icon,
                        fontSize = 32.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Achievement info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )

                if (!isUnlocked) {
                    // Progress bar for locked achievements
                    val progressValue = achievement.progress.toFloat() / achievement.progressTarget.toFloat()
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progressValue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${achievement.progress}/${achievement.progressTarget}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // Show unlock date
                    achievement.unlockedAt?.let { date ->
                        Text(
                            text = stringResource(R.string.ach_unlocked_on, date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Badge gallery component
 * Grid view of all achievements
 */
@Composable
fun BadgeGallery(
    achievements: List<AchievementEntity>,
    modifier: Modifier = Modifier
) {
    val unlockedAchievements = achievements.filter { it.isUnlocked }
    val lockedAchievements = achievements.filter { !it.isUnlocked }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary
        ZenCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = unlockedAchievements.size.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = ZenDawnGold
                    )
                    Text(
                        text = stringResource(R.string.ach_unlocked),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = achievements.size.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.ach_total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(unlockedAchievements.size.toFloat() / achievements.size * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.ach_completion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Unlocked achievements grid
        if (unlockedAchievements.isNotEmpty()) {
            Text(
                text = stringResource(R.string.ach_badges_unlocked),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(unlockedAchievements) { achievement ->
                    BadgeItem(achievement = achievement, isUnlocked = true)
                }
            }
        }

        // Locked achievements grid
        if (lockedAchievements.isNotEmpty()) {
            Text(
                text = stringResource(R.string.ach_badges_locked),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lockedAchievements) { achievement ->
                    BadgeItem(achievement = achievement, isUnlocked = false)
                }
            }
        }
    }
}

/**
 * Individual badge item for grid
 */
@Composable
private fun BadgeItem(
    achievement: AchievementEntity,
    isUnlocked: Boolean
) {
    val info = AchievementEntity.getAchievementInfo(achievement.type)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = if (isUnlocked) {
                        ZenDawnGold.copy(alpha = 0.2f)
                    } else {
                        Color.Gray.copy(alpha = 0.1f)
                    },
                    shape = CircleShape
                )
                .alpha(if (isUnlocked) 1f else 0.3f),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = info.icon,
                    fontSize = 28.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = info.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = if (isUnlocked) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            }
        )
    }
}
