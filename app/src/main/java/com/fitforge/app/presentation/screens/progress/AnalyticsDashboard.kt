package com.fitforge.app.presentation.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitforge.app.domain.usecase.RecoveryScoreUseCase
import com.fitforge.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    onBack: () -> Unit = {}
) {
    // Sample recovery data
    val recoveryInput = RecoveryScoreUseCase.RecoveryInput(
        sleepHours = 8.2f,
        sleepQuality = 88,
        hrv = 68,
        baselineHRV = 60,
        restingHeartRate = 56,
        baselineRHR = 58,
        muscleSoreness = mapOf("Legs" to 3, "Back" to 1),
        stressLevel = 32,
        nutritionScore = 92,
        lastWorkoutHours = 18,
        weeklyWorkouts = 4,
        trainingVolume = 45000f
    )

    val recoveryScore = remember {
        RecoveryScoreUseCase().calculateRecoveryScore(recoveryInput)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForgeRed,
                    titleContentColor = PureWhite,
                    navigationIconContentColor = PureWhite
                )
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Recovery Score Overview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (recoveryScore.readinessLevel) {
                        RecoveryScoreUseCase.ReadinessLevel.EXCELLENT -> Success.copy(alpha = 0.1f)
                        RecoveryScoreUseCase.ReadinessLevel.GOOD -> VictoryGreen.copy(alpha = 0.1f)
                        RecoveryScoreUseCase.ReadinessLevel.MODERATE -> Warning.copy(alpha = 0.1f)
                        RecoveryScoreUseCase.ReadinessLevel.POOR -> Danger.copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TODAY'S RECOVERY SCORE",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(150.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = recoveryScore.score / 100f,
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 12.dp,
                            color = when (recoveryScore.readinessLevel) {
                                RecoveryScoreUseCase.ReadinessLevel.EXCELLENT -> Success
                                RecoveryScoreUseCase.ReadinessLevel.GOOD -> VictoryGreen
                                RecoveryScoreUseCase.ReadinessLevel.MODERATE -> Warning
                                RecoveryScoreUseCase.ReadinessLevel.POOR -> Danger
                            }
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${recoveryScore.score}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                color = when (recoveryScore.readinessLevel) {
                                    RecoveryScoreUseCase.ReadinessLevel.EXCELLENT -> Success
                                    RecoveryScoreUseCase.ReadinessLevel.GOOD -> VictoryGreen
                                    RecoveryScoreUseCase.ReadinessLevel.MODERATE -> Warning
                                    RecoveryScoreUseCase.ReadinessLevel.POOR -> Danger
                                }
                            )
                            Text(
                                "/100",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        recoveryScore.readinessLevel.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        recoveryScore.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Recovery Factors
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "RECOVERY FACTORS",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    RecoveryFactor(
                        icon = "😴",
                        name = "Sleep",
                        score = recoveryScore.sleepScore,
                        details = "${recoveryInput.sleepHours}h, ${recoveryInput.sleepQuality}% quality"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RecoveryFactor(
                        icon = "💗",
                        name = "HRV",
                        score = recoveryScore.hrvScore,
                        details = "${recoveryInput.hrv}ms (+${recoveryInput.hrv!! - recoveryInput.baselineHRV!!}ms)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RecoveryFactor(
                        icon = "💪",
                        name = "Soreness",
                        score = recoveryScore.sorenessScore,
                        details = "Legs: ${recoveryInput.muscleSoreness["Legs"]}/10"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RecoveryFactor(
                        icon = "😌",
                        name = "Stress",
                        score = recoveryScore.stressScore,
                        details = "${recoveryInput.stressLevel}/100"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // AI Insights
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Info.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, null, tint = Info)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI INSIGHTS",
                            style = MaterialTheme.typography.labelLarge,
                            color = Info,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    recoveryScore.insights.forEach { insight ->
                        Text(
                            insight,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Weekly Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "WEEKLY SUMMARY",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        WeeklyStat("4", "Workouts")
                        WeeklyStat("18.4h", "Active Time")
                        WeeklyStat("45K", "Volume (lbs)")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = 0.8f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = VictoryGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "4/5 workouts completed this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun RecoveryFactor(
    icon: String,
    name: String,
    score: Int,
    details: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${score}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    score >= 80 -> Success
                    score >= 60 -> Warning
                    else -> Danger
                }
            )
            Text(
                "/100",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WeeklyStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ForgeRed
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
