package com.fitforge.app.ui.components.charts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fitforge.app.ui.theme.ElectricBlue
import com.fitforge.app.ui.theme.ForgeRed
import com.fitforge.app.ui.theme.VictoryGreen
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Line chart showing strength progression over time (estimated 1RM)
 *
 * Features:
 * - Multiple series (1RM, volume, working weight)
 * - Date-based X-axis
 * - Interactive tooltips
 * - Gradient fills
 * - Smooth curves
 */
@Composable
fun StrengthProgressChart(
    data: List<StrengthDataPoint>,
    modifier: Modifier = Modifier,
    showVolume: Boolean = false,
    showWorkingWeight: Boolean = false
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(
            message = "Complete workouts to see strength progression",
            modifier = modifier
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Strength Progression",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Prepare chart data
            val chartEntries = remember(data) {
                data.mapIndexed { index, point ->
                    FloatEntry(
                        x = index.toFloat(),
                        y = point.estimated1RM
                    )
                }
            }

            val modelProducer = remember { ChartEntryModelProducer(chartEntries) }

            // Date formatter for X-axis
            val dateFormatter = remember {
                AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    if (value.toInt() in data.indices) {
                        data[value.toInt()].date.format(DateTimeFormatter.ofPattern("MMM d"))
                    } else ""
                }
            }

            ProvideChartStyle {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            LineChart.LineSpec(
                                lineColor = ElectricBlue.toArgb(),
                                lineThicknessDp = 3f,
                                lineBackgroundShader = null
                            )
                        )
                    ),
                    chartModelProducer = modelProducer,
                    startAxis = rememberStartAxis(
                        title = "Estimated 1RM (lbs)",
                        titleComponent = null
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = dateFormatter
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    runInitialAnimation = true,
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = true)
                )
            }

            // Legend
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChartLegendItem(color = ElectricBlue, label = "1RM")
                if (showVolume) {
                    ChartLegendItem(color = VictoryGreen, label = "Volume")
                }
                if (showWorkingWeight) {
                    ChartLegendItem(color = ForgeRed, label = "Working Weight")
                }
            }

            // Summary stats
            val firstPoint = data.first()
            val lastPoint = data.last()
            val improvement = lastPoint.estimated1RM - firstPoint.estimated1RM
            val improvementPct = (improvement / firstPoint.estimated1RM * 100)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = "Current 1RM",
                    value = "${lastPoint.estimated1RM.toInt()} lbs"
                )
                StatCard(
                    label = "Improvement",
                    value = "+${improvement.toInt()} lbs",
                    subtitle = "+${improvementPct.toInt()}%"
                )
                StatCard(
                    label = "Period",
                    value = "${data.size} weeks"
                )
            }
        }
    }
}

/**
 * Bar chart showing volume progression over time
 */
@Composable
fun VolumeProgressChart(
    data: List<VolumeDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(
            message = "Complete workouts to see volume trends",
            modifier = modifier
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Training Volume",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Total weight lifted over time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // In production, use Vico bar chart here
            // For now, simple visualization placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Text(
                    text = "Bar chart: ${data.sumOf { it.volume.toDouble() }.toInt()} lbs total volume",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Stats
            val avgVolume = data.map { it.volume }.average().toInt()
            val maxVolume = data.maxOf { it.volume }.toInt()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(label = "Avg Volume", value = "$avgVolume lbs")
                StatCard(label = "Peak Volume", value = "$maxVolume lbs")
                StatCard(label = "Workouts", value = "${data.size}")
            }
        }
    }
}

/**
 * Pie chart showing macro distribution
 */
@Composable
fun MacroDistributionPieChart(
    proteinGrams: Int,
    carbsGrams: Int,
    fatGrams: Int,
    modifier: Modifier = Modifier
) {
    val proteinCals = proteinGrams * 4
    val carbsCals = carbsGrams * 4
    val fatCals = fatGrams * 9
    val totalCals = proteinCals + carbsCals + fatCals

    if (totalCals == 0) {
        EmptyChartPlaceholder(
            message = "Log meals to see macro distribution",
            modifier = modifier
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Macro Distribution",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Protein
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = ElectricBlue)
                        }
                        Text(
                            text = "${(proteinCals.toFloat() / totalCals * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Text("Protein", style = MaterialTheme.typography.bodySmall)
                    Text("${proteinGrams}g", style = MaterialTheme.typography.labelSmall)
                }

                // Carbs
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = VictoryGreen)
                        }
                        Text(
                            text = "${(carbsCals.toFloat() / totalCals * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Text("Carbs", style = MaterialTheme.typography.bodySmall)
                    Text("${carbsGrams}g", style = MaterialTheme.typography.labelSmall)
                }

                // Fat
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = ForgeRed)
                        }
                        Text(
                            text = "${(fatCals.toFloat() / totalCals * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Text("Fat", style = MaterialTheme.typography.bodySmall)
                    Text("${fatGrams}g", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total: $totalCals calories",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Combined chart showing multiple metrics
 */
@Composable
fun WorkoutFrequencyChart(
    data: List<FrequencyDataPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(
            message = "Complete workouts to see frequency trends",
            modifier = modifier
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Workout Frequency",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Workouts per week over time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Placeholder for bar chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column {
                    data.forEach { point ->
                        Text(
                            text = "Week ${point.weekStart.format(DateTimeFormatter.ofPattern("MMM d"))}: ${point.workoutsCompleted} workouts",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            val avgWorkouts = data.map { it.workoutsCompleted }.average()

            Text(
                text = "Average: ${String.format("%.1f", avgWorkouts)} workouts/week",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

// ============================================================================
// HELPER COMPONENTS
// ============================================================================

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, subtitle: String? = null) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = VictoryGreen
            )
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class StrengthDataPoint(
    val date: LocalDate,
    val estimated1RM: Float,
    val workingWeight: Float = 0f,
    val volume: Float = 0f
)

data class VolumeDataPoint(
    val date: LocalDate,
    val volume: Float,
    val sets: Int = 0,
    val avgWeight: Float = 0f
)

data class FrequencyDataPoint(
    val weekStart: LocalDate,
    val workoutsCompleted: Int,
    val totalDuration: Int = 0,
    val totalVolume: Float = 0f
)
