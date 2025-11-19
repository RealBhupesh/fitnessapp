package com.fitforge.app.ui.screens.exercises

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitforge.app.data.model.Exercise
import com.fitforge.app.data.model.ExerciseHistory
import com.fitforge.app.ui.components.charts.StrengthProgressChart
import com.fitforge.app.ui.theme.*

/**
 * Detailed exercise view with:
 * - Video player for form demonstration
 * - Step-by-step instructions
 * - Form tips and common mistakes
 * - Personal records
 * - Exercise history
 * - Muscle activation visualization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exercise: Exercise,
    history: ExerciseHistory?,
    onBack: () -> Unit,
    onStartWorkout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Instructions", "History", "Records")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IronBlack,
                    titleContentColor = OffWhite
                )
            )
        },
        bottomBar = {
            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForgeRed
                )
            ) {
                Icon(Icons.Default.PlayArrow, "Start")
                Spacer(modifier = Modifier.width(8.dp))
                Text("START WORKOUT")
            }
        },
        containerColor = IronBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Video Player Section
            ExerciseVideoPlayer(
                exercise = exercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CharcoalGray,
                contentColor = OffWhite
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> OverviewTab(exercise)
                1 -> InstructionsTab(exercise)
                2 -> HistoryTab(history)
                3 -> RecordsTab(history)
            }
        }
    }
}

/**
 * Video player for exercise demonstration
 */
@Composable
private fun ExerciseVideoPlayer(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CharcoalGray, IronBlack)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // In production, use ExoPlayer or Media3
        // For now, placeholder
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Play video",
                tint = ForgeRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Watch Form Video",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite
            )
            Text(
                text = "${exercise.name} - Proper Technique",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhite.copy(alpha = 0.7f)
            )
        }

        // Video controls overlay (in production)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { },
                label = { Text("0.5x") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = CharcoalGray.copy(alpha = 0.8f)
                )
            )
            AssistChip(
                onClick = { },
                label = { Text("Normal") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = ForgeRed.copy(alpha = 0.8f)
                )
            )
        }
    }
}

/**
 * Overview tab showing quick exercise info
 */
@Composable
private fun OverviewTab(exercise: Exercise) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Info Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    icon = Icons.Default.FitnessCenter,
                    label = "Difficulty",
                    value = exercise.difficulty.uppercase(),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    icon = Icons.Default.Category,
                    label = "Category",
                    value = exercise.category.uppercase(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            InfoCard(
                icon = Icons.Default.Build,
                label = "Equipment",
                value = exercise.equipment.uppercase(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Description
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CharcoalGray
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            color = OffWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Muscle Groups
        item {
            MuscleGroupsCard(
                primaryMuscles = exercise.primaryMuscles,
                secondaryMuscles = exercise.secondaryMuscles
            )
        }
    }
}

/**
 * Instructions tab with step-by-step guide
 */
@Composable
private fun InstructionsTab(exercise: Exercise) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Starting Position
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Start,
                            null,
                            tint = VictoryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Starting Position",
                            style = MaterialTheme.typography.titleMedium,
                            color = OffWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Step-by-step instructions
        item {
            Text(
                text = "STEP-BY-STEP GUIDE",
                style = MaterialTheme.typography.labelLarge,
                color = OffWhite.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }

        items(exercise.instructions) { instruction ->
            InstructionStep(
                stepNumber = exercise.instructions.indexOf(instruction) + 1,
                instruction = instruction
            )
        }

        // Form Tips
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FORM TIPS",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricBlue,
                fontWeight = FontWeight.Bold
            )
        }

        items(exercise.formTips) { tip ->
            FormTipCard(tip = tip)
        }

        // Common Mistakes
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "COMMON MISTAKES TO AVOID",
                style = MaterialTheme.typography.labelLarge,
                color = ForgeRed,
                fontWeight = FontWeight.Bold
            )
        }

        items(listOf(
            "Rounding your back during the movement",
            "Using momentum instead of controlled movement",
            "Not achieving full range of motion",
            "Holding your breath - breathe consistently"
        )) { mistake ->
            CommonMistakeCard(mistake = mistake)
        }
    }
}

/**
 * History tab showing past performance
 */
@Composable
private fun HistoryTab(history: ExerciseHistory?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (history == null) {
            item {
                EmptyHistoryPlaceholder()
            }
        } else {
            // Strength progression chart
            item {
                StrengthProgressChart(
                    data = emptyList(), // Would pass real data
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Recent workouts
            item {
                Text(
                    text = "RECENT WORKOUTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = OffWhite.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Placeholder workout history items
            items(5) { index ->
                WorkoutHistoryCard(
                    date = "Oct ${25 - index}, 2024",
                    sets = 4,
                    weight = 185 + (index * 5),
                    reps = 8,
                    volume = (185 + index * 5) * 8 * 4
                )
            }
        }
    }
}

/**
 * Records tab showing personal bests
 */
@Composable
private fun RecordsTab(history: ExerciseHistory?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (history == null) {
            item {
                EmptyHistoryPlaceholder()
            }
        } else {
            // Personal Records
            item {
                PersonalRecordCard(
                    title = "Max Weight",
                    value = "${history.maxWeight.toInt()} lbs",
                    subtitle = "Heaviest single rep",
                    icon = Icons.Default.FitnessCenter,
                    color = ForgeRed
                )
            }

            item {
                PersonalRecordCard(
                    title = "Max Reps",
                    value = "${history.maxReps}",
                    subtitle = "Most reps in single set",
                    icon = Icons.Default.Repeat,
                    color = ElectricBlue
                )
            }

            item {
                PersonalRecordCard(
                    title = "Total Volume",
                    value = "${history.totalVolume.toInt()} lbs",
                    subtitle = "All-time total lifted",
                    icon = Icons.Default.TrendingUp,
                    color = VictoryGreen
                )
            }

            item {
                PersonalRecordCard(
                    title = "Total Sets",
                    value = "${history.totalSets}",
                    subtitle = "Lifetime sets completed",
                    icon = Icons.Default.CheckCircle,
                    color = ElectricBlue
                )
            }

            // Estimated 1RM
            item {
                val estimated1RM = if (history.maxReps == 1) {
                    history.maxWeight
                } else {
                    history.maxWeight * (1 + history.maxReps / 30f)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CharcoalGray
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ESTIMATED 1RM",
                            style = MaterialTheme.typography.labelLarge,
                            color = OffWhite.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${estimated1RM.toInt()} lbs",
                            style = MaterialTheme.typography.displayMedium,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Based on ${history.maxWeight.toInt()} lbs x ${history.maxReps} reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = OffWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPONENT HELPERS
// ============================================================================

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CharcoalGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OffWhite.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MuscleGroupsCard(
    primaryMuscles: List<String>,
    secondaryMuscles: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "TARGET MUSCLES",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Primary:",
                style = MaterialTheme.typography.labelMedium,
                color = VictoryGreen
            )
            primaryMuscles.forEach { muscle ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(VictoryGreen, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(muscle, style = MaterialTheme.typography.bodyMedium, color = OffWhite)
                }
            }

            if (secondaryMuscles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Secondary:",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                secondaryMuscles.forEach { muscle ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(ElectricBlue, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            muscle,
                            style = MaterialTheme.typography.bodySmall,
                            color = OffWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(stepNumber: Int, instruction: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalGray)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ElectricBlue, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.titleSmall,
                    color = IronBlack,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = OffWhite,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FormTipCard(tip: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ElectricBlue.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = OffWhite
            )
        }
    }
}

@Composable
private fun CommonMistakeCard(mistake: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ForgeRed.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForgeRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = ForgeRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = mistake,
                style = MaterialTheme.typography.bodySmall,
                color = OffWhite
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    date: String,
    sets: Int,
    weight: Int,
    reps: Int,
    volume: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleSmall,
                    color = OffWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$sets sets × $weight lbs × $reps reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = OffWhite.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$volume lbs",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Volume",
                    style = MaterialTheme.typography.labelSmall,
                    color = OffWhite.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PersonalRecordCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CharcoalGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = OffWhite.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OffWhite.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = OffWhite.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No history yet",
                style = MaterialTheme.typography.titleMedium,
                color = OffWhite.copy(alpha = 0.6f)
            )
            Text(
                text = "Complete this exercise to start tracking progress",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhite.copy(alpha = 0.4f)
            )
        }
    }
}
