package com.fitforge.app.presentation.screens.nutrition

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitforge.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onBack: () -> Unit = {}
) {
    var selectedDay by remember { mutableStateOf("Monday") }
    val mealPlan = remember { generateSampleMealPlan() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Meal Plan", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Generate new plan */ }) {
                    Icon(Icons.Default.AutoAwesome, "Generate Plan")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ForgeRed,
                titleContentColor = PureWhite,
                navigationIconContentColor = PureWhite,
                actionIconContentColor = PureWhite
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Plan Overview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "MUSCLE BUILDING PLAN",
                            style = MaterialTheme.typography.labelLarge,
                            color = ForgeRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "7-Day High-Protein Meal Plan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            NutritionStat("2,400", "Calories/day")
                            NutritionStat("180g", "Protein/day")
                            NutritionStat("270g", "Carbs/day")
                            NutritionStat("67g", "Fat/day")
                        }
                    }
                }
            }

            item {
                // Day Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        FilterChip(
                            selected = selectedDay.startsWith(day),
                            onClick = { selectedDay = "$day day" },
                            label = { Text(day, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForgeRed,
                                selectedLabelColor = PureWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Text(
                    selectedDay.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Meals for selected day
            items(mealPlan[selectedDay] ?: emptyList()) { meal ->
                MealPlanCard(meal)
            }

            item {
                // Shopping List Button
                Button(
                    onClick = { /* Show shopping list */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VictoryGreen)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GENERATE SHOPPING LIST", fontWeight = FontWeight.Bold)
                }
            }

            item {
                // Meal Prep Tips
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Info.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, null, tint = Info)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "MEAL PREP TIPS",
                                style = MaterialTheme.typography.labelLarge,
                                color = Info,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf(
                            "Prep proteins in bulk on Sunday",
                            "Pre-portion snacks for the week",
                            "Cook rice and grains in batches",
                            "Keep simple ingredients on hand",
                            "Meal prep 2-3 days at a time for freshness"
                        ).forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("• ", color = Info)
                                Text(tip, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
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

@Composable
fun MealPlanCard(meal: PlannedMeal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        meal.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        meal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    meal.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Foods
            meal.foods.forEach { food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "• ${food.item}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        food.portion,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Macros
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MacroChip("${meal.calories} cal")
                    MacroChip("${meal.protein}g P", CardioZone)
                    MacroChip("${meal.carbs}g C", ElectricBlue)
                    MacroChip("${meal.fat}g F", Warning)
                }
            }
        }
    }
}

@Composable
fun MacroChip(text: String, color: androidx.compose.ui.graphics.Color = TextSecondary) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = color
    )
}

data class PlannedMeal(
    val type: String,
    val name: String,
    val time: String,
    val foods: List<FoodPortion>,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class FoodPortion(
    val item: String,
    val portion: String
)

fun generateSampleMealPlan(): Map<String, List<PlannedMeal>> {
    val monday = listOf(
        PlannedMeal(
            type = "Breakfast",
            name = "Protein Oatmeal Bowl",
            time = "7:00 AM",
            foods = listOf(
                FoodPortion("Oats", "1 cup"),
                FoodPortion("Protein powder", "1 scoop"),
                FoodPortion("Banana", "1 medium"),
                FoodPortion("Peanut butter", "1 tbsp"),
                FoodPortion("Blueberries", "1/2 cup")
            ),
            calories = 520,
            protein = 35,
            carbs = 70,
            fat = 12
        ),
        PlannedMeal(
            type = "Snack",
            name = "Greek Yogurt & Almonds",
            time = "10:00 AM",
            foods = listOf(
                FoodPortion("Greek yogurt", "1 cup"),
                FoodPortion("Almonds", "1 oz"),
                FoodPortion("Honey", "1 tsp")
            ),
            calories = 280,
            protein = 22,
            carbs = 20,
            fat = 12
        ),
        PlannedMeal(
            type = "Lunch",
            name = "Chicken & Rice Bowl",
            time = "1:00 PM",
            foods = listOf(
                FoodPortion("Grilled chicken", "8 oz"),
                FoodPortion("Brown rice", "1.5 cups"),
                FoodPortion("Broccoli", "2 cups"),
                FoodPortion("Olive oil", "1 tbsp")
            ),
            calories = 720,
            protein = 68,
            carbs = 85,
            fat = 14
        ),
        PlannedMeal(
            type = "Pre-Workout",
            name = "Banana & Protein Shake",
            time = "4:00 PM",
            foods = listOf(
                FoodPortion("Banana", "1 large"),
                FoodPortion("Protein powder", "1 scoop"),
                FoodPortion("Almond milk", "1 cup")
            ),
            calories = 280,
            protein = 26,
            carbs = 40,
            fat = 4
        ),
        PlannedMeal(
            type = "Dinner",
            name = "Salmon & Sweet Potato",
            time = "7:00 PM",
            foods = listOf(
                FoodPortion("Grilled salmon", "6 oz"),
                FoodPortion("Sweet potato", "1 large"),
                FoodPortion("Asparagus", "1 cup"),
                FoodPortion("Olive oil", "1 tbsp")
            ),
            calories = 600,
            protein = 45,
            carbs = 55,
            fat = 22
        )
    )

    return mapOf(
        "Monday" to monday,
        "Tuesday" to monday,
        "Wednesday" to monday,
        "Thursday" to monday,
        "Friday" to monday,
        "Saturday" to monday,
        "Sunday" to monday
    )
}
