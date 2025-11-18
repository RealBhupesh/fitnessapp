package com.fitforge.app.presentation.screens.workout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitforge.app.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    workoutId: String,
    onFinish: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isResting by viewModel.isResting.collectAsState()
    val restTimeRemaining by viewModel.restTimeRemaining.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar with Exit
        TopAppBar(
            title = {
                Column {
                    Text(
                        "UPPER BODY POWER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Exercise 2/7 • 12 min in",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.8f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { /* Show exit dialog */ }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit workout",
                        tint = PureWhite
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Pause workout */ }) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = PureWhite
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ForgeRed,
                titleContentColor = PureWhite,
                navigationIconContentColor = PureWhite
            )
        )

        // Rest Timer Overlay
        if (isResting) {
            RestTimerOverlay(
                timeRemaining = restTimeRemaining,
                onSkip = { viewModel.skipRest() },
                onAddTime = { viewModel.addRestTime(30) }
            )
        } else {
            // Active Exercise
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CurrentExerciseCard(
                        exerciseName = "Barbell Bench Press",
                        setNumber = 2,
                        totalSets = 4,
                        targetReps = "6-8",
                        targetWeight = "230 lbs",
                        lastPerformance = "225 lbs × 8"
                    )
                }

                item {
                    SetLoggingSection(
                        currentSet = 2,
                        reps = uiState.currentReps,
                        weight = uiState.currentWeight,
                        onRepsChange = { viewModel.updateReps(it) },
                        onWeightChange = { viewModel.updateWeight(it) },
                        onCompleteSet = { viewModel.completeSet() }
                    )
                }

                item {
                    PreviousSetsCard(
                        sets = listOf(
                            SetLog(1, 230, 8, 96),
                            SetLog(2, 230, 6, 94)
                        )
                    )
                }

                item {
                    FormTipsCard(
                        tips = listOf(
                            "Keep feet flat on the ground",
                            "Maintain slight arch in lower back",
                            "Lower bar to mid-chest",
                            "Control the descent (2-3 seconds)"
                        )
                    )
                }

                item {
                    WorkoutStatsCard(
                        caloriesBurned = 127,
                        volumeLifted = 1840,
                        avgHeartRate = 142
                    )
                }
            }
        }

        // Bottom Action Bar
        if (!isResting) {
            BottomActionBar(
                onNextExercise = { viewModel.nextExercise() },
                onFinishWorkout = { onFinish() }
            )
        }
    }
}

@Composable
fun CurrentExerciseCard(
    exerciseName: String,
    setNumber: Int,
    totalSets: Int,
    targetReps: String,
    targetWeight: String,
    lastPerformance: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "EXERCISE 2/7",
                style = MaterialTheme.typography.labelMedium,
                color = ForgeRed,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exerciseName.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Set Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SET $setNumber OF $totalSets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$targetWeight × $targetReps",
                    style = MaterialTheme.typography.titleMedium,
                    color = ForgeRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = setNumber.toFloat() / totalSets,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ForgeRed
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Last Performance
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Last: $lastPerformance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SetLoggingSection(
    currentSet: Int,
    reps: Int,
    weight: Float,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Float) -> Unit,
    onCompleteSet: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "LOG SET $currentSet",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Reps Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { if (reps > 0) onRepsChange(reps - 1) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ForgeRed.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, "Decrease", tint = ForgeRed)
                    }
                    Text(
                        text = reps.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = ForgeRed
                    )
                    IconButton(
                        onClick = { onRepsChange(reps + 1) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(ForgeRed.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, "Increase", tint = ForgeRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weight Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight (lbs)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onWeightChange(weight - 5f) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(ElectricBlue.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Text("-5", fontWeight = FontWeight.Bold, color = ElectricBlue)
                    }
                    Text(
                        text = "${weight.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onWeightChange(weight + 5f) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(ElectricBlue.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Text("+5", fontWeight = FontWeight.Bold, color = ElectricBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Complete Set Button
            Button(
                onClick = onCompleteSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForgeRed
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "COMPLETE SET",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RestTimerOverlay(
    timeRemaining: Int,
    onSkip: () -> Unit,
    onAddTime: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IronBlack.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "REST TIME",
                style = MaterialTheme.typography.labelLarge,
                color = PureWhite.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Animated Circle Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = timeRemaining / 90f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = ElectricBlue
                )
                Text(
                    text = formatTime(timeRemaining),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Breathe. You got this. 💪",
                style = MaterialTheme.typography.bodyLarge,
                color = PureWhite.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddTime(30) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+30s")
                }

                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForgeRed
                    )
                ) {
                    Text("SKIP REST", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PreviousSetsCard(sets: List<SetLog>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "PREVIOUS SETS",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            sets.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Set ${set.number}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${set.weight} lbs × ${set.reps} reps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Form: ${set.formScore}/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (set.formScore >= 90) Success else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun FormTipsCard(tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Info,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "FORM TIPS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Info,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            tips.forEach { tip ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ", color = Info)
                    Text(
                        tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutStatsCard(
    caloriesBurned: Int,
    volumeLifted: Int,
    avgHeartRate: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("🔥", "$caloriesBurned cal")
            StatItem("💪", "$volumeLifted lbs")
            StatItem("💗", "$avgHeartRate bpm")
        }
    }
}

@Composable
fun StatItem(icon: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, style = MaterialTheme.typography.headlineMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomActionBar(
    onNextExercise: () -> Unit,
    onFinishWorkout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onFinishWorkout,
                modifier = Modifier.weight(1f)
            ) {
                Text("Finish Early")
            }
            Button(
                onClick = onNextExercise,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Success)
            ) {
                Text("Next Exercise", fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}

data class SetLog(
    val number: Int,
    val weight: Float,
    val reps: Int,
    val formScore: Int
)

data class ActiveWorkoutUiState(
    val currentReps: Int = 8,
    val currentWeight: Float = 230f,
    val isResting: Boolean = false
)
