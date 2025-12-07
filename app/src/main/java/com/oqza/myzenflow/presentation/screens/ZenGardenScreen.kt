package com.oqza.myzenflow.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.oqza.myzenflow.presentation.components.*
import com.oqza.myzenflow.presentation.viewmodels.ZenGardenTab
import com.oqza.myzenflow.presentation.viewmodels.ZenGardenViewModel

/**
 * Zen Garden screen - Main gamification screen
 * Shows tree visualization, achievements, and statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenGardenScreen(
    viewModel: ZenGardenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val particleSystem = rememberParticleSystem()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zen Bahçem") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.selectedTab == ZenGardenTab.TREE,
                    onClick = { viewModel.selectTab(ZenGardenTab.TREE) },
                    text = { Text("Ağaç") },
                    icon = { Icon(Icons.Default.Park, contentDescription = "Ağaç") }
                )
                Tab(
                    selected = uiState.selectedTab == ZenGardenTab.ACHIEVEMENTS,
                    onClick = { viewModel.selectTab(ZenGardenTab.ACHIEVEMENTS) },
                    text = { Text("Başarılar") },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Başarılar") }
                )
                Tab(
                    selected = uiState.selectedTab == ZenGardenTab.STATS,
                    onClick = { viewModel.selectTab(ZenGardenTab.STATS) },
                    text = { Text("İstatistikler") },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "İstatistikler") }
                )
            }

            // Content based on selected tab
            when (uiState.selectedTab) {
                ZenGardenTab.TREE -> TreeTab(
                    uiState = uiState,
                    particleSystem = particleSystem
                )
                ZenGardenTab.ACHIEVEMENTS -> AchievementsTab(uiState = uiState)
                ZenGardenTab.STATS -> StatsTab(uiState = uiState)
            }
        }
    }
}

/**
 * Tree tab - Shows tree visualization and stats cards
 */
@Composable
private fun TreeTab(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState,
    particleSystem: ParticleSystem
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats cards
        item {
            StatsCardsRow(uiState = uiState)
        }

        // Tree visualization
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TreeVisualization(
                        level = uiState.userStats.treeLevel,
                        progress = uiState.userStats.treeGrowthProgress,
                        particles = particleSystem.getParticles()
                    )

                    // Particle effect
                    LaunchedParticleEffect(
                        particleSystem = particleSystem,
                        width = 800f, // Approximate width
                        height = 1200f, // Approximate height
                        enabled = uiState.userStats.currentStreak > 0,
                        particleType = ParticleType.SPARKLE
                    )
                }
            }
        }

        // Growth info
        item {
            GrowthInfoCard(uiState = uiState)
        }
    }
}

/**
 * Stats cards row - Shows key metrics
 */
@Composable
private fun StatsCardsRow(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            StatCard(
                title = "Toplam Seans",
                value = uiState.userStats.totalSessions.toString(),
                icon = Icons.Default.SelfImprovement,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            StatCard(
                title = "Toplam Dakika",
                value = uiState.userStats.totalMinutes.toString(),
                icon = Icons.Default.Timer,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            StatCard(
                title = "Mevcut Seri",
                value = "${uiState.userStats.currentStreak} gün",
                icon = Icons.Default.Whatshot,
                color = Color(0xFFFF6347) // Tomato
            )
        }
        item {
            StatCard(
                title = "Başarılar",
                value = "${uiState.unlockedAchievements.size}/${uiState.achievements.size}",
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFFFFD700) // Gold
            )
        }
    }
}

/**
 * Individual stat card
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Growth info card - Shows next level requirements
 */
@Composable
private fun GrowthInfoCard(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Büyüme İlerlemesi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Seviye ${uiState.userStats.treeLevel}/5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = uiState.userStats.treeGrowthProgress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (uiState.userStats.treeLevel < 5) {
                Text(
                    text = getNextLevelInfo(uiState.userStats.treeLevel, uiState.userStats.totalMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "🎉 Maksimum seviyeye ulaştınız! Muhteşemsiniz!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Get next level info text
 */
private fun getNextLevelInfo(currentLevel: Int, totalMinutes: Int): String {
    val thresholds = listOf(0, 30, 120, 360, 900, 1800)
    if (currentLevel >= 5) return "Maksimum seviye!"

    val nextThreshold = thresholds[currentLevel + 1]
    val minutesNeeded = nextThreshold - totalMinutes

    return "Sonraki seviye için $minutesNeeded dakika daha meditasyon yapın"
}

/**
 * Achievements tab - Placeholder (will be implemented with BadgeGallery)
 */
@Composable
private fun AchievementsTab(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Açılan Başarılar (${uiState.unlockedAchievements.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.unlockedAchievements) { achievement ->
            AchievementCard(achievement = achievement, isUnlocked = true)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kilitli Başarılar (${uiState.lockedAchievements.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.lockedAchievements) { achievement ->
            AchievementCard(achievement = achievement, isUnlocked = false)
        }
    }
}

/**
 * Stats tab - Placeholder (will be implemented with charts)
 */
@Composable
private fun StatsTab(
    uiState: com.oqza.myzenflow.presentation.viewmodels.ZenGardenUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "İstatistikler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Weekly stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bu Hafta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatRow("Seanslar", uiState.userStats.sessionsThisWeek.toString())
                    StatRow("Dakikalar", uiState.userStats.weeklyCompletedMinutes.toString())
                    StatRow("Hedef", "${uiState.userStats.weeklyGoalMinutes} dakika")

                    LinearProgressIndicator(
                        progress = uiState.userStats.weeklyProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Monthly stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bu Ay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatRow("Seanslar", uiState.userStats.sessionsThisMonth.toString())
                    StatRow("Ortalama Seans", "${uiState.userStats.averageSessionDuration} dk")
                    StatRow("En Uzun Seri", "${uiState.userStats.longestStreak} gün")
                }
            }
        }
    }
}

/**
 * Stat row component
 */
@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
