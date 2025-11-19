package com.fitforge.app.data.repository

import com.fitforge.app.data.database.dao.NutritionDao
import com.fitforge.app.data.database.dao.UserDao
import com.fitforge.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Comprehensive Repository for nutrition-related data operations.
 * Implements macro tracking, meal analysis, nutrition insights, and calorie management.
 */
@Singleton
class NutritionRepository @Inject constructor(
    private val nutritionDao: NutritionDao,
    private val userDao: UserDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ============================================================================
    // BASIC MEAL OPERATIONS
    // ============================================================================

    fun getMealsByDate(userId: String, date: String): Flow<List<Meal>> =
        nutritionDao.getMealsByDate(userId, date)

    fun getTodaysMeals(userId: String): Flow<List<Meal>> {
        val today = LocalDate.now().format(dateFormatter)
        return nutritionDao.getMealsByDate(userId, today)
    }

    fun getNutritionGoal(userId: String): Flow<NutritionGoal?> =
        nutritionDao.getNutritionGoal(userId)

    fun getDailyNutrition(userId: String, date: String): Flow<DailyNutrition?> =
        nutritionDao.getDailyNutrition(userId, date)

    fun getTodaysDailyNutrition(userId: String): Flow<DailyNutrition?> {
        val today = LocalDate.now().format(dateFormatter)
        return nutritionDao.getDailyNutrition(userId, today)
    }

    fun getRecentDailyNutrition(userId: String, days: Int = 7): Flow<List<DailyNutrition>> =
        nutritionDao.getRecentDailyNutrition(userId, days)

    suspend fun insertMeal(meal: Meal) {
        nutritionDao.insertMeal(meal)
        updateDailyNutrition(meal.userId, meal.date)
    }

    suspend fun updateMeal(meal: Meal) {
        nutritionDao.updateMeal(meal)
        updateDailyNutrition(meal.userId, meal.date)
    }

    suspend fun deleteMeal(meal: Meal) {
        nutritionDao.deleteMeal(meal)
        updateDailyNutrition(meal.userId, meal.date)
    }

    suspend fun insertNutritionGoal(goal: NutritionGoal) =
        nutritionDao.insertNutritionGoal(goal)

    // ============================================================================
    // ADVANCED NUTRITION TRACKING & ANALYSIS
    // ============================================================================

    /**
     * Get comprehensive nutrition summary for today
     */
    fun getTodaysNutritionSummary(userId: String): Flow<NutritionSummary> {
        val today = LocalDate.now().format(dateFormatter)

        return getTodaysMeals(userId)
            .combine(getNutritionGoal(userId)) { meals, goal ->
                calculateNutritionSummary(meals, goal, today)
            }
    }

    /**
     * Get nutrition summary for a specific date
     */
    fun getNutritionSummaryForDate(userId: String, date: String): Flow<NutritionSummary> {
        return getMealsByDate(userId, date)
            .combine(getNutritionGoal(userId)) { meals, goal ->
                calculateNutritionSummary(meals, goal, date)
            }
    }

    /**
     * Calculate comprehensive nutrition summary
     */
    private fun calculateNutritionSummary(
        meals: List<Meal>,
        goal: NutritionGoal?,
        date: String
    ): NutritionSummary {
        val totalCalories = meals.sumOf { it.calories }
        val totalProtein = meals.sumOf { it.protein.toInt() }
        val totalCarbs = meals.sumOf { it.carbs.toInt() }
        val totalFat = meals.sumOf { it.fat.toInt() }

        val goalCalories = goal?.calories ?: 2000
        val goalProtein = goal?.protein ?: 150
        val goalCarbs = goal?.carbs ?: 200
        val goalFat = goal?.fat ?: 65

        return NutritionSummary(
            date = date,
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            goalCalories = goalCalories,
            goalProtein = goalProtein,
            goalCarbs = goalCarbs,
            goalFat = goalFat,
            caloriesRemaining = goalCalories - totalCalories,
            proteinRemaining = goalProtein - totalProtein,
            carbsRemaining = goalCarbs - totalCarbs,
            fatRemaining = goalFat - totalFat,
            calorieProgress = (totalCalories.toFloat() / goalCalories * 100).coerceIn(0f, 150f),
            proteinProgress = (totalProtein.toFloat() / goalProtein * 100).coerceIn(0f, 150f),
            carbsProgress = (totalCarbs.toFloat() / goalCarbs * 100).coerceIn(0f, 150f),
            fatProgress = (totalFat.toFloat() / goalFat * 100).coerceIn(0f, 150f),
            mealsLogged = meals.size,
            adherenceScore = calculateAdherenceScore(totalCalories, goalCalories, totalProtein, goalProtein, totalCarbs, goalCarbs, totalFat, goalFat)
        )
    }

    /**
     * Calculate adherence score (0-100) based on how close to goals
     */
    private fun calculateAdherenceScore(
        totalCalories: Int,
        goalCalories: Int,
        totalProtein: Int,
        goalProtein: Int,
        totalCarbs: Int,
        goalCarbs: Int,
        totalFat: Int,
        goalFat: Int
    ): Int {
        val calorieScore = calculateMacroScore(totalCalories, goalCalories, tolerance = 0.10)
        val proteinScore = calculateMacroScore(totalProtein, goalProtein, tolerance = 0.15)
        val carbsScore = calculateMacroScore(totalCarbs, goalCarbs, tolerance = 0.15)
        val fatScore = calculateMacroScore(totalFat, goalFat, tolerance = 0.15)

        return ((calorieScore * 0.40 + proteinScore * 0.30 + carbsScore * 0.15 + fatScore * 0.15) * 100).toInt()
    }

    /**
     * Calculate individual macro score with tolerance
     */
    private fun calculateMacroScore(actual: Int, goal: Int, tolerance: Double): Float {
        val difference = abs(actual - goal).toFloat()
        val maxDifference = (goal * tolerance).toFloat()

        return if (difference <= maxDifference) {
            1f - (difference / maxDifference) * 0.5f
        } else {
            0.5f - ((difference - maxDifference) / goal).coerceIn(0f, 0.5f)
        }
    }

    /**
     * Get weekly nutrition averages
     */
    fun getWeeklyNutritionAverages(userId: String): Flow<WeeklyNutritionSummary> {
        return getRecentDailyNutrition(userId, 7)
            .combine(getNutritionGoal(userId)) { dailyNutrition, goal ->
                calculateWeeklySummary(dailyNutrition, goal)
            }
    }

    /**
     * Calculate weekly nutrition summary
     */
    private fun calculateWeeklySummary(
        dailyNutrition: List<DailyNutrition>,
        goal: NutritionGoal?
    ): WeeklyNutritionSummary {
        if (dailyNutrition.isEmpty()) {
            return WeeklyNutritionSummary(
                avgCalories = 0,
                avgProtein = 0,
                avgCarbs = 0,
                avgFat = 0,
                daysTracked = 0,
                consistency = 0f,
                avgAdherence = 0f,
                calorieVariance = 0f
            )
        }

        val avgCalories = dailyNutrition.map { it.calories }.average().toInt()
        val avgProtein = dailyNutrition.map { it.protein }.average().toInt()
        val avgCarbs = dailyNutrition.map { it.carbs }.average().toInt()
        val avgFat = dailyNutrition.map { it.fat }.average().toInt()

        val calorieVariance = calculateVariance(dailyNutrition.map { it.calories.toFloat() })

        val adherenceScores = dailyNutrition.map { day ->
            calculateAdherenceScore(
                day.calories, goal?.calories ?: 2000,
                day.protein, goal?.protein ?: 150,
                day.carbs, goal?.carbs ?: 200,
                day.fat, goal?.fat ?: 65
            )
        }

        return WeeklyNutritionSummary(
            avgCalories = avgCalories,
            avgProtein = avgProtein,
            avgCarbs = avgCarbs,
            avgFat = avgFat,
            daysTracked = dailyNutrition.size,
            consistency = (dailyNutrition.size / 7f) * 100,
            avgAdherence = adherenceScores.average().toFloat(),
            calorieVariance = calorieVariance
        )
    }

    /**
     * Calculate variance for consistency tracking
     */
    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average().toFloat()
        val squaredDifferences = values.map { (it - mean) * (it - mean) }
        return squaredDifferences.average().toFloat()
    }

    // ============================================================================
    // MACRO DISTRIBUTION & INSIGHTS
    // ============================================================================

    /**
     * Get macro distribution pie chart data
     */
    fun getMacroDistribution(userId: String, date: String): Flow<MacroDistribution> {
        return getMealsByDate(userId, date)
            .map { meals ->
                val totalProtein = meals.sumOf { it.protein.toInt() }
                val totalCarbs = meals.sumOf { it.carbs.toInt() }
                val totalFat = meals.sumOf { it.fat.toInt() }

                val proteinCalories = totalProtein * 4
                val carbsCalories = totalCarbs * 4
                val fatCalories = totalFat * 9

                val totalMacroCalories = (proteinCalories + carbsCalories + fatCalories).toFloat()

                MacroDistribution(
                    proteinGrams = totalProtein,
                    carbsGrams = totalCarbs,
                    fatGrams = totalFat,
                    proteinCalories = proteinCalories,
                    carbsCalories = carbsCalories,
                    fatCalories = fatCalories,
                    proteinPercentage = if (totalMacroCalories > 0) (proteinCalories / totalMacroCalories * 100) else 0f,
                    carbsPercentage = if (totalMacroCalories > 0) (carbsCalories / totalMacroCalories * 100) else 0f,
                    fatPercentage = if (totalMacroCalories > 0) (fatCalories / totalMacroCalories * 100) else 0f
                )
            }
    }

    /**
     * Get nutrition insights and recommendations
     */
    suspend fun getNutritionInsights(userId: String): List<NutritionInsight> {
        val insights = mutableListOf<NutritionInsight>()
        val recentNutrition = getRecentDailyNutrition(userId, 7).first()
        val goal = getNutritionGoal(userId).first()

        if (recentNutrition.isEmpty()) {
            insights.add(NutritionInsight(
                type = InsightType.WARNING,
                title = "Start Tracking Nutrition",
                message = "You haven't logged any meals recently. Start tracking to get personalized insights!",
                actionable = true
            ))
            return insights
        }

        // Check protein intake
        val avgProtein = recentNutrition.map { it.protein }.average().toInt()
        val goalProtein = goal?.protein ?: 150

        if (avgProtein < goalProtein * 0.8) {
            insights.add(NutritionInsight(
                type = InsightType.WARNING,
                title = "Low Protein Intake",
                message = "You're averaging ${avgProtein}g protein vs ${goalProtein}g goal. Increase lean meats, eggs, or protein shakes.",
                actionable = true
            ))
        } else if (avgProtein >= goalProtein) {
            insights.add(NutritionInsight(
                type = InsightType.SUCCESS,
                title = "Great Protein Intake!",
                message = "You're consistently hitting your protein goals. This supports muscle growth and recovery.",
                actionable = false
            ))
        }

        // Check calorie consistency
        val calorieVariance = calculateVariance(recentNutrition.map { it.calories.toFloat() })
        if (calorieVariance > 50000) {
            insights.add(NutritionInsight(
                type = InsightType.INFO,
                title = "Inconsistent Calorie Intake",
                message = "Your daily calories vary significantly. More consistency helps achieve your goals faster.",
                actionable = true
            ))
        }

        // Check meal frequency
        val avgMealsPerDay = recentNutrition.map { it.mealsLogged ?: 3 }.average()
        if (avgMealsPerDay < 3) {
            insights.add(NutritionInsight(
                type = InsightType.INFO,
                title = "Low Meal Frequency",
                message = "You're averaging ${avgMealsPerDay.roundToInt()} meals/day. Consider 4-5 smaller meals for better energy.",
                actionable = true
            ))
        }

        // Check water intake (if tracked)
        val avgWater = recentNutrition.mapNotNull { it.water }.average()
        if (avgWater < 80) {
            insights.add(NutritionInsight(
                type = InsightType.WARNING,
                title = "Hydration Check",
                message = "Aim for 100+ oz water daily. Proper hydration boosts performance and recovery.",
                actionable = true
            ))
        }

        return insights
    }

    /**
     * Calculate recommended calorie intake based on user stats
     */
    suspend fun calculateRecommendedCalories(
        userId: String,
        activityLevel: ActivityLevel = ActivityLevel.MODERATE,
        goal: FitnessGoal = FitnessGoal.MAINTAIN
    ): CalorieRecommendation {
        val user = userDao.getUser(userId)

        // Default values if user not found
        val weight = user?.weight ?: 180f
        val heightCm = user?.height ?: 175f
        val age = user?.age ?: 30
        val isMale = user?.gender == "male"

        // Calculate BMR using Mifflin-St Jeor equation
        val bmr = if (isMale) {
            (10 * weight * 0.453592 + 6.25 * heightCm - 5 * age + 5).toInt()
        } else {
            (10 * weight * 0.453592 + 6.25 * heightCm - 5 * age - 161).toInt()
        }

        // Apply activity multiplier
        val activityMultiplier = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHT -> 1.375
            ActivityLevel.MODERATE -> 1.55
            ActivityLevel.ACTIVE -> 1.725
            ActivityLevel.VERY_ACTIVE -> 1.9
        }

        val maintenanceCalories = (bmr * activityMultiplier).toInt()

        // Adjust based on goal
        val (targetCalories, proteinRatio, carbRatio, fatRatio) = when (goal) {
            FitnessGoal.FAT_LOSS -> {
                val deficit = 500
                Quadruple(maintenanceCalories - deficit, 0.35, 0.35, 0.30) // Higher protein for muscle retention
            }
            FitnessGoal.MUSCLE_GAIN -> {
                val surplus = 300
                Quadruple(maintenanceCalories + surplus, 0.30, 0.45, 0.25) // Higher carbs for energy
            }
            FitnessGoal.MAINTAIN -> {
                Quadruple(maintenanceCalories, 0.30, 0.40, 0.30) // Balanced
            }
            else -> {
                Quadruple(maintenanceCalories, 0.30, 0.40, 0.30)
            }
        }

        val proteinGrams = ((targetCalories * proteinRatio) / 4).toInt()
        val carbsGrams = ((targetCalories * carbRatio) / 4).toInt()
        val fatGrams = ((targetCalories * fatRatio) / 9).toInt()

        return CalorieRecommendation(
            bmr = bmr,
            maintenanceCalories = maintenanceCalories,
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            rationale = generateCalorieRationale(goal, activityLevel, maintenanceCalories, targetCalories)
        )
    }

    /**
     * Generate rationale for calorie recommendation
     */
    private fun generateCalorieRationale(
        goal: FitnessGoal,
        activityLevel: ActivityLevel,
        maintenance: Int,
        target: Int
    ): String {
        val goalText = when (goal) {
            FitnessGoal.FAT_LOSS -> "fat loss with muscle retention"
            FitnessGoal.MUSCLE_GAIN -> "lean muscle gain"
            FitnessGoal.MAINTAIN -> "weight maintenance"
            else -> "general fitness"
        }

        val activityText = when (activityLevel) {
            ActivityLevel.SEDENTARY -> "sedentary lifestyle"
            ActivityLevel.LIGHT -> "light activity"
            ActivityLevel.MODERATE -> "moderate activity"
            ActivityLevel.ACTIVE -> "active lifestyle"
            ActivityLevel.VERY_ACTIVE -> "very active lifestyle"
        }

        val difference = abs(target - maintenance)
        val direction = if (target > maintenance) "surplus" else "deficit"

        return "Based on your $activityText and goal of $goalText, we recommend $target calories/day (${difference} cal $direction from maintenance). This balanced approach promotes sustainable results."
    }

    // ============================================================================
    // DAILY NUTRITION UPDATES
    // ============================================================================

    /**
     * Recalculate and update daily nutrition totals
     */
    private suspend fun updateDailyNutrition(userId: String, date: String) {
        val meals = getMealsByDate(userId, date).first()

        val dailyNutrition = DailyNutrition(
            userId = userId,
            date = date,
            calories = meals.sumOf { it.calories },
            protein = meals.sumOf { it.protein.toInt() },
            carbs = meals.sumOf { it.carbs.toInt() },
            fat = meals.sumOf { it.fat.toInt() },
            fiber = meals.sumOf { it.fiber ?: 0 },
            sugar = meals.sumOf { it.sugar ?: 0 },
            water = meals.sumOf { it.water ?: 0 },
            mealsLogged = meals.size
        )

        nutritionDao.insertDailyNutrition(dailyNutrition)
    }
}

// ============================================================================
// DATA CLASSES FOR NUTRITION ANALYTICS
// ============================================================================

data class NutritionSummary(
    val date: String,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val goalCalories: Int,
    val goalProtein: Int,
    val goalCarbs: Int,
    val goalFat: Int,
    val caloriesRemaining: Int,
    val proteinRemaining: Int,
    val carbsRemaining: Int,
    val fatRemaining: Int,
    val calorieProgress: Float,
    val proteinProgress: Float,
    val carbsProgress: Float,
    val fatProgress: Float,
    val mealsLogged: Int,
    val adherenceScore: Int
)

data class WeeklyNutritionSummary(
    val avgCalories: Int,
    val avgProtein: Int,
    val avgCarbs: Int,
    val avgFat: Int,
    val daysTracked: Int,
    val consistency: Float,
    val avgAdherence: Float,
    val calorieVariance: Float
)

data class MacroDistribution(
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinCalories: Int,
    val carbsCalories: Int,
    val fatCalories: Int,
    val proteinPercentage: Float,
    val carbsPercentage: Float,
    val fatPercentage: Float
)

data class NutritionInsight(
    val type: InsightType,
    val title: String,
    val message: String,
    val actionable: Boolean
)

enum class InsightType {
    SUCCESS, WARNING, INFO, DANGER
}

data class CalorieRecommendation(
    val bmr: Int,
    val maintenanceCalories: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val rationale: String
)

enum class ActivityLevel {
    SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
}

enum class FitnessGoal {
    FAT_LOSS, MUSCLE_GAIN, MAINTAIN, STRENGTH, ENDURANCE
}

// Helper class for quadruple return values
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
