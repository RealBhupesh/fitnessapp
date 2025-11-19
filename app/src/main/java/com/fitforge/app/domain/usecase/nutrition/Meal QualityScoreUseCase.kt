package com.fitforge.app.domain.usecase.nutrition

import com.fitforge.app.data.model.Meal
import javax.inject.Inject
import kotlin.math.min

/**
 * Use Case for calculating meal quality score based on nutritional composition,
 * meal timing, and macronutrient balance.
 *
 * Scoring Factors (0-100 scale):
 * - Macro Balance (40%): How well balanced are the macros
 * - Nutrient Density (30%): Calories vs nutrition ratio
 * - Meal Timing (15%): Optimal timing for the meal type
 * - Portion Size (15%): Appropriate serving size
 */
class MealQualityScoreUseCase @Inject constructor() {

    /**
     * Calculate comprehensive meal quality score
     */
    fun execute(
        meal: Meal,
        userGoals: NutritionGoals,
        mealType: MealType,
        timeOfDay: Int // Hour of day (0-23)
    ): MealQualityResult {
        val macroBalanceScore = calculateMacroBalance(meal, userGoals, mealType)
        val nutrientDensityScore = calculateNutrientDensity(meal)
        val timingScore = calculateTimingScore(mealType, timeOfDay)
        val portionScore = calculatePortionScore(meal, userGoals)

        val overallScore = (
            macroBalanceScore * 0.40 +
            nutrientDensityScore * 0.30 +
            timingScore * 0.15 +
            portionScore * 0.15
        ).toInt()

        return MealQualityResult(
            overallScore = overallScore,
            grade = calculateGrade(overallScore),
            macroBalanceScore = macroBalanceScore,
            nutrientDensityScore = nutrientDensityScore,
            timingScore = timingScore,
            portionScore = portionScore,
            insights = generateInsights(meal, overallScore, macroBalanceScore, nutrientDensityScore),
            recommendations = generateRecommendations(meal, userGoals, mealType, overallScore)
        )
    }

    /**
     * Calculate macro balance score based on ideal ratios for meal type
     */
    private fun calculateMacroBalance(meal: Meal, goals: NutritionGoals, mealType: MealType): Int {
        val totalCalories = meal.calories.toFloat()
        if (totalCalories == 0f) return 0

        // Calculate actual percentages
        val proteinPct = (meal.protein * 4 / totalCalories * 100).toInt()
        val carbsPct = (meal.carbs * 4 / totalCalories * 100).toInt()
        val fatPct = (meal.fat * 9 / totalCalories * 100).toInt()

        // Ideal ranges depend on meal type
        val (idealProtein, idealCarbs, idealFat) = getIdealMacroRanges(mealType, goals)

        // Score based on how close to ideal ranges
        val proteinScore = calculateRangeScore(proteinPct, idealProtein)
        val carbsScore = calculateRangeScore(carbsPct, idealCarbs)
        val fatScore = calculateRangeScore(fatPct, idealFat)

        // Weighted average (protein is most important)
        return ((proteinScore * 0.45 + carbsScore * 0.35 + fatScore * 0.20) * 100).toInt()
    }

    /**
     * Get ideal macro ranges based on meal type and goals
     */
    private fun getIdealMacroRanges(mealType: MealType, goals: NutritionGoals): Triple<IntRange, IntRange, IntRange> {
        return when (mealType) {
            MealType.BREAKFAST -> Triple(
                20..35,  // Protein %
                40..55,  // Carbs % (higher for energy)
                15..30   // Fat %
            )
            MealType.PRE_WORKOUT -> Triple(
                20..30,  // Moderate protein
                50..65,  // High carbs for energy
                10..20   // Low fat (easier digestion)
            )
            MealType.POST_WORKOUT -> Triple(
                30..45,  // High protein for recovery
                35..50,  // Moderate carbs to replenish glycogen
                10..25   // Low-moderate fat
            )
            MealType.LUNCH -> Triple(
                25..40,  // Good protein
                35..50,  // Balanced carbs
                20..35   // Moderate fat
            )
            MealType.DINNER -> Triple(
                30..45,  // Higher protein
                25..40,  // Lower carbs (less needed at night)
                25..35   // Moderate fat
            )
            MealType.SNACK -> Triple(
                15..30,  // Light protein
                30..50,  // Variable carbs
                20..40   // Can be higher in healthy fats
            )
        }
    }

    /**
     * Calculate how well a value fits within an ideal range
     */
    private fun calculateRangeScore(actual: Int, ideal: IntRange): Float {
        return when {
            actual in ideal -> 1.0f // Perfect
            actual < ideal.first -> {
                // Below range - score decreases linearly
                val difference = ideal.first - actual
                (1.0f - (difference / ideal.first.toFloat())).coerceIn(0f, 1f)
            }
            else -> {
                // Above range - score decreases linearly
                val difference = actual - ideal.last
                (1.0f - (difference / ideal.last.toFloat())).coerceIn(0f, 1f)
            }
        }
    }

    /**
     * Calculate nutrient density score (calories vs nutritional value)
     */
    private fun calculateNutrientDensity(meal: Meal): Int {
        var score = 50 // Base score

        // Bonus for fiber (improves satiety and gut health)
        val fiber = meal.fiber ?: 0
        if (fiber >= 5) score += 15
        else if (fiber >= 3) score += 10
        else if (fiber >= 1) score += 5

        // Penalty for excessive sugar (unless post-workout)
        val sugar = meal.sugar ?: 0
        val totalCarbs = meal.carbs
        val sugarRatio = if (totalCarbs > 0) sugar.toFloat() / totalCarbs else 0f

        when {
            sugarRatio > 0.7 -> score -= 20 // Very high sugar
            sugarRatio > 0.5 -> score -= 15 // High sugar
            sugarRatio > 0.3 -> score -= 10 // Moderate sugar
            sugarRatio < 0.15 -> score += 10 // Low sugar is good
        }

        // Bonus for protein density (protein per 100 calories)
        val proteinPer100Cal = (meal.protein / meal.calories.toFloat() * 100)
        when {
            proteinPer100Cal > 10 -> score += 15 // Excellent protein density
            proteinPer100Cal > 7 -> score += 10 // Good protein density
            proteinPer100Cal > 5 -> score += 5  // Decent protein density
        }

        // Bonus for balanced calorie amount (not too high or low)
        when (meal.calories) {
            in 300..600 -> score += 10 // Ideal meal size
            in 200..300, in 600..800 -> score += 5 // Acceptable
            in 100..200 -> score -= 5 // Too small
            in 800..1000 -> score -= 5 // Large
            else -> if (meal.calories > 1000) score -= 15 // Too large
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Calculate timing score based on optimal meal timing
     */
    private fun calculateTimingScore(mealType: MealType, hourOfDay: Int): Int {
        val optimalHours = when (mealType) {
            MealType.BREAKFAST -> 6..9
            MealType.LUNCH -> 11..14
            MealType.DINNER -> 17..20
            MealType.PRE_WORKOUT -> 6..21 // Flexible
            MealType.POST_WORKOUT -> 6..22 // Flexible
            MealType.SNACK -> 9..21 // Flexible
        }

        return when {
            hourOfDay in optimalHours -> 100
            hourOfDay == optimalHours.first - 1 || hourOfDay == optimalHours.last + 1 -> 80
            hourOfDay == optimalHours.first - 2 || hourOfDay == optimalHours.last + 2 -> 60
            else -> 40
        }
    }

    /**
     * Calculate portion score based on user's daily goals
     */
    private fun calculatePortionScore(meal: Meal, goals: NutritionGoals): Int {
        // Assume 5 meals per day, so each meal should be ~20% of daily calories
        val idealMealCalories = goals.dailyCalories * 0.20
        val actualCalories = meal.calories.toFloat()

        val ratio = actualCalories / idealMealCalories

        return when {
            ratio in 0.8f..1.2f -> 100 // Perfect portion
            ratio in 0.6f..0.8f || ratio in 1.2f..1.4f -> 80 // Good portion
            ratio in 0.4f..0.6f || ratio in 1.4f..1.6f -> 60 // Acceptable
            ratio < 0.4f -> 40 // Too small
            ratio > 1.6f -> 30 // Too large
            else -> 50
        }
    }

    /**
     * Calculate letter grade from score
     */
    private fun calculateGrade(score: Int): String {
        return when {
            score >= 90 -> "A"
            score >= 80 -> "B"
            score >= 70 -> "C"
            score >= 60 -> "D"
            else -> "F"
        }
    }

    /**
     * Generate actionable insights about the meal
     */
    private fun generateInsights(
        meal: Meal,
        overallScore: Int,
        macroScore: Int,
        nutrientScore: Int
    ): List<String> {
        val insights = mutableListOf<String>()

        when {
            overallScore >= 90 -> insights.add("Excellent meal choice! This supports your fitness goals perfectly.")
            overallScore >= 75 -> insights.add("Great meal! A few tweaks could make it even better.")
            overallScore >= 60 -> insights.add("Good effort. Consider the recommendations below for optimization.")
            else -> insights.add("This meal could be significantly improved. See recommendations.")
        }

        // Macro-specific insights
        if (macroScore < 60) {
            val totalCals = meal.calories.toFloat()
            val proteinPct = (meal.protein * 4 / totalCals * 100).toInt()
            val carbsPct = (meal.carbs * 4 / totalCals * 100).toInt()

            when {
                proteinPct < 20 -> insights.add("Protein is too low. Aim for 25-35% of meal calories from protein.")
                proteinPct > 50 -> insights.add("Very high protein. Balance with more carbs or fats.")
                carbsPct > 65 -> insights.add("High carb content. Unless pre-workout, balance with more protein/fats.")
                carbsPct < 20 -> insights.add("Very low carbs. You may need more energy from carbohydrates.")
            }
        }

        // Nutrient density insights
        if (nutrientScore < 60) {
            val sugar = meal.sugar ?: 0
            if (sugar > meal.carbs * 0.5) {
                insights.add("Over 50% of carbs from sugar. Choose complex carbs for sustained energy.")
            }

            val fiber = meal.fiber ?: 0
            if (fiber < 3) {
                insights.add("Low fiber content. Add vegetables, fruits, or whole grains for better satiety.")
            }
        }

        // Portion insights
        if (meal.calories < 200) {
            insights.add("This is quite small for a meal. You may get hungry soon.")
        } else if (meal.calories > 800) {
            insights.add("Large meal. Consider splitting into two smaller meals for better digestion.")
        }

        return insights
    }

    /**
     * Generate specific recommendations for improvement
     */
    private fun generateRecommendations(
        meal: Meal,
        goals: NutritionGoals,
        mealType: MealType,
        score: Int
    ): List<String> {
        if (score >= 85) {
            return listOf("Keep up the great work! This meal is well-optimized for your goals.")
        }

        val recommendations = mutableListOf<String>()
        val totalCals = meal.calories.toFloat()
        val proteinPct = (meal.protein * 4 / totalCals * 100).toInt()

        // Protein recommendations
        when {
            proteinPct < 20 -> {
                val proteinNeeded = (totalCals * 0.25 / 4).toInt() - meal.protein
                recommendations.add("Add ${proteinNeeded}g more protein: Greek yogurt, chicken breast, or protein powder.")
            }
            proteinPct > 45 && mealType != MealType.POST_WORKOUT -> {
                recommendations.add("Reduce protein slightly and add complex carbs or healthy fats for balance.")
            }
        }

        // Fiber recommendations
        val fiber = meal.fiber ?: 0
        if (fiber < 5) {
            recommendations.add("Increase fiber by adding: vegetables, berries, oats, or beans.")
        }

        // Sugar recommendations
        val sugar = meal.sugar ?: 0
        if (sugar > 25 && mealType != MealType.POST_WORKOUT) {
            recommendations.add("Reduce added sugars. Opt for natural sweetness from fruits if needed.")
        }

        // Meal-specific recommendations
        when (mealType) {
            MealType.PRE_WORKOUT -> {
                if (meal.fat > 15) {
                    recommendations.add("Pre-workout meals should be lower in fat for easier digestion.")
                }
                if (meal.carbs < 30) {
                    recommendations.add("Increase carbs for optimal workout energy.")
                }
            }
            MealType.POST_WORKOUT -> {
                if (meal.protein < 25) {
                    recommendations.add("Post-workout meals should have 25-40g protein for muscle recovery.")
                }
            }
            MealType.BREAKFAST -> {
                if (meal.protein < 20) {
                    recommendations.add("Higher protein breakfast helps control hunger throughout the day.")
                }
            }
            MealType.DINNER -> {
                if (meal.carbs > 60) {
                    recommendations.add("Consider reducing carbs at dinner and increasing protein or vegetables.")
                }
            }
            else -> {}
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Minor adjustments to macro ratios would optimize this meal further.")
        }

        return recommendations
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class NutritionGoals(
    val dailyCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val goal: String // "muscle_gain", "fat_loss", "maintenance"
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    PRE_WORKOUT,
    POST_WORKOUT
}

data class MealQualityResult(
    val overallScore: Int,
    val grade: String,
    val macroBalanceScore: Int,
    val nutrientDensityScore: Int,
    val timingScore: Int,
    val portionScore: Int,
    val insights: List<String>,
    val recommendations: List<String>
)
