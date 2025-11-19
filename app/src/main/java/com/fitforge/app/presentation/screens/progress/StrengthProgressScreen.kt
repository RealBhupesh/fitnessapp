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
import com.fitforge.app.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthProgressScreen(
    onBack: () -> Unit = {}
) {
    val selectedExercise = remember { mutableStateOf("Bench Press") }
    val timeRange = remember { mutableStateOf("3 Months") }

    // Sample data
    val dataPoints = remember {
        listOf(
            StrengthDataPoint(LocalDate.now().minusWeeks(12), 185f, 8),
            StrengthDataPoint(LocalDate.now().minusWeeks(11), 185f, 10),
            StrengthDataPoint(LocalDate.now().minusWeeks(10), 195f, 8),
            StrengthDataPoint(LocalDate.now().minusWeeks(9), 195f, 9),
            StrengthDataPoint(LocalDate.now().minusWeeks(8), 205f, 6),
            StrengthDataPoint(LocalDate.now().minusWeeks(7), 205f, 8),
            StrengthDataPoint(LocalDate.now().minusWeeks(6), 210f, 8),
            StrengthDataPoint(LocalDate.now().minusWeeks(5), 215f, 6),
            StrengthDataPoint(LocalDate.now().minusWeeks(4), 215f, 8),
            StrengthDataPoint(LocalDate.now().minusWeeks(3), 220f, 6),
            StrengthDataPoint(LocalDate.now().minusWeeks(2), 225f, 6),
            StrengthDataPoint(LocalDate.now().minusWeeks(1), 225f, 8),
            StrengthDataPoint(LocalDate.now(), 230f, 8)
        )
    }

    val estimated1RM = dataPoints.map { it.estimated1RM() }
    val currentMax = estimated1RM.last()
    val startingMax = estimated1RM.first()
    val improvement = ((currentMax - startingMax) / startingMax * 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        "Strength Progress",
                        fontWeight = FontWeight.Bold
                    )
                },
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
            // Exercise Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SELECT EXERCISE",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Bench Press", "Squat", "Deadlift").forEach { exercise ->
                            FilterChip(
                                selected = selectedExercise.value == exercise,
                                onClick = { selectedExercise.value = exercise },
                                label = { Text(exercise) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForgeRed,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            // Stats Overview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CURRENT STATS",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatColumn(
                            label = "Estimated 1RM",
                            value = "${currentMax.toInt()} lbs",
                            change = "+${improvement.toInt()}%",
                            positive = true
                        )
                        StatColumn(
                            label = "Working Weight",
                            value = "${dataPoints.last().weight.toInt()} lbs",
                            change = "+${((dataPoints.last().weight - dataPoints.first().weight) / dataPoints.first().weight * 100).toInt()}%",
                            positive = true
                        )
                        StatColumn(
                            label = "Total Volume",
                            value = "${(dataPoints.sumOf { (it.weight * it.reps).toDouble() } / 1000).toInt()}K",
                            change = "12 weeks",
                            positive = true
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Graph Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ESTIMATED 1RM PROGRESSION",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Row {
                            listOf("1M", "3M", "6M", "1Y").forEach { range ->
                                FilterChip(
                                    selected = timeRange.value == range.replace("M", " Month").replace("Y", " Year"),
                                    onClick = { timeRange.value = range },
                                    label = { Text(range, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simple visualization (in production, use Vico Charts)
                    StrengthGraphPlaceholder(dataPoints)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(ForgeRed, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Estimated 1RM",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Recent Workouts
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "RECENT WORKOUTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    dataPoints.takeLast(5).reversed().forEach { point ->
                        WorkoutHistoryItem(point)
                        if (point != dataPoints.last()) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
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
                    containerColor = VictoryGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = VictoryGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI INSIGHTS",
                            style = MaterialTheme.typography.labelLarge,
                            color = VictoryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "🔥 Incredible progress! You've increased your estimated 1RM by ${improvement.toInt()}% in 12 weeks.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "💪 Your linear progression is strong. Based on current trajectory, you could hit 250 lbs by week 16.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "📈 Recommendation: Continue current program for 4 more weeks, then consider a deload week.",
                        style = MaterialTheme.typography.bodyMedium
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
fun StatColumn(
    label: String,
    value: String,
    change: String,
    positive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = ForgeRed
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            change,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (positive) Success else Danger
        )
    }
}

@Composable
fun StrengthGraphPlaceholder(dataPoints: List<StrengthDataPoint>) {
    // This is a placeholder. In production, use Vico Charts library
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(SurfaceVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TextSecondary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Strength Progression Chart",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${dataPoints.first().estimated1RM().toInt()} lbs → ${dataPoints.last().estimated1RM().toInt()} lbs",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ForgeRed
            )
            Text(
                "(Vico Charts integration ready)",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WorkoutHistoryItem(dataPoint: StrengthDataPoint) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                dataPoint.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${dataPoint.weight.toInt()} lbs × ${dataPoint.reps} reps",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "Est. 1RM",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                "${dataPoint.estimated1RM().toInt()} lbs",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ForgeRed
            )
        }
    }
}

data class StrengthDataPoint(
    val date: LocalDate,
    val weight: Float,
    val reps: Int
) {
    // Epley formula for 1RM estimation
    fun estimated1RM(): Float = weight * (1 + reps / 30f)
}
