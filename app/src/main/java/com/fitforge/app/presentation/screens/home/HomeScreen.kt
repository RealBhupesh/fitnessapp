package com.fitforge.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitforge.app.presentation.theme.*
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWorkout: (String) -> Unit,
    onNavigateToNutrition: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Section with Time-based Greeting
        HeroSection(userName = uiState.userName, streak = uiState.currentStreak)

        Spacer(modifier = Modifier.height(16.dp))

        // Today's Workout Card
        TodaysWorkoutCard(
            workoutName = uiState.todaysWorkout?.name ?: "Upper Body Power",
            duration = uiState.todaysWorkout?.duration ?: 45,
            exercises = 7,
            intensity = "High",
            readiness = 92,
            onStartWorkout = { onNavigateToWorkout("today") },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nutrition Overview
        NutritionOverviewCard(
            currentCalories = uiState.caloriesConsumed,
            targetCalories = uiState.caloriesTarget,
            protein = uiState.proteinConsumed to uiState.proteinTarget,
            carbs = uiState.carbsConsumed to uiState.carbsTarget,
            fats = uiState.fatsConsumed to uiState.fatsTarget,
            onLogMeal = { onNavigateToNutrition() },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Body Metrics Summary
        BodyMetricsCard(
            currentWeight = uiState.currentWeight,
            weightChange = uiState.weeklyWeightChange,
            bodyFat = uiState.bodyFatPercentage,
            muscleMass = uiState.muscleMass,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Progress
        WeeklyProgressCard(
            workoutsCompleted = uiState.weeklyWorkoutsCompleted,
            workoutsTarget = 5,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
    }
}

@Composable
fun HeroSection(userName: String, streak: Int) {
    val greeting = getTimeBasedGreeting()
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = greeting.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = PureWhite,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "READY TO CRUSH IT, ${userName.uppercase()}?",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Streak indicator
            if (streak > 0) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$streak DAY STREAK",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodaysWorkoutCard(
    workoutName: String,
    duration: Int,
    exercises: Int,
    intensity: String,
    readiness: Int,
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💪 TODAY'S WORKOUT",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = workoutName.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(icon = "⏱", text = "$duration min")
                InfoChip(icon = "💪", text = "$exercises exercises")
                InfoChip(icon = "🔥", text = intensity)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Readiness indicator
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Readiness:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "$readiness%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (readiness >= 80) Success else Warning
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = readiness / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (readiness >= 80) Success else Warning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForgeRed
                )
            ) {
                Text(
                    text = "START WORKOUT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun NutritionOverviewCard(
    currentCalories: Int,
    targetCalories: Int,
    protein: Pair<Float, Float>,
    carbs: Pair<Float, Float>,
    fats: Pair<Float, Float>,
    onLogMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🍎 NUTRITION TODAY",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$currentCalories / $targetCalories cal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(currentCalories * 100 / targetCalories)}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = ForgeRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macros
            MacroBar(label = "Protein", current = protein.first, target = protein.second, color = CardioZone)
            Spacer(modifier = Modifier.height(12.dp))
            MacroBar(label = "Carbs", current = carbs.first, target = carbs.second, color = ElectricBlue)
            Spacer(modifier = Modifier.height(12.dp))
            MacroBar(label = "Fats", current = fats.first, target = fats.second, color = Warning)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogMeal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Meal")
            }
        }
    }
}

@Composable
fun MacroBar(label: String, current: Float, target: Float, color: androidx.compose.ui.graphics.Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${current.toInt()}g / ${target.toInt()}g",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (current / target).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color
        )
    }
}

@Composable
fun BodyMetricsCard(
    currentWeight: Float,
    weightChange: Float,
    bodyFat: Float?,
    muscleMass: Float?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📊 BODY METRICS",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem(
                    value = "%.1f lbs".format(currentWeight),
                    label = "Weight",
                    change = weightChange
                )
                if (bodyFat != null) {
                    MetricItem(
                        value = "%.1f%%".format(bodyFat),
                        label = "Body Fat",
                        change = null
                    )
                }
                if (muscleMass != null) {
                    MetricItem(
                        value = "%.1f lbs".format(muscleMass),
                        label = "Muscle",
                        change = null
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(value: String, label: String, change: Float?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        if (change != null) {
            Text(
                text = "%+.1f lbs".format(change),
                style = MaterialTheme.typography.bodySmall,
                color = if (change < 0) Success else ForgeRed
            )
        }
    }
}

@Composable
fun WeeklyProgressCard(
    workoutsCompleted: Int,
    workoutsTarget: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🎯 WEEKLY PROGRESS",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$workoutsCompleted / $workoutsTarget workouts completed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = workoutsCompleted.toFloat() / workoutsTarget,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = VictoryGreen
            )
        }
    }
}

@Composable
fun InfoChip(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun getTimeBasedGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
}
