package com.fitforge.app.domain.usecase.nutrition

import com.fitforge.app.data.model.DailyNutrition
import com.fitforge.app.data.model.Workout
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow

/**
 * Use Case for predicting body composition changes based on nutrition adherence,
 * training volume, and metabolic adaptations.
 *
 * Uses scientifically-backed formulas:
 * - Energy balance (3500 cal = 1 lb fat)
 * - Protein requirements for muscle retention/growth
 * - Training volume impact on muscle mass
 * - Metabolic adaptation curves
 */
class BodyCompositionPredictionUseCase @Inject constructor() {

    /**
     * Predict body composition changes over a specified time period
     */
    fun execute(
        currentStats: BodyStats,
        nutritionHistory: List<DailyNutrition>,
        workoutHistory: List<Workout>,
        targetDays: Int = 30
    ): BodyCompositionPrediction {
        // Calculate average daily calorie intake and expenditure
        val avgDailyCalories = nutritionHistory.map { it.calories }.average().toInt()
        val avgProtein = nutritionHistory.map { it.protein }.average().toInt()

        // Calculate weekly training volume
        val weeklyVolume = calculateWeeklyVolume(workoutHistory)
        val workoutsPerWeek = (workoutHistory.size.toFloat() / (nutritionHistory.size / 7f))

        // Calculate BMR and TDEE
        val bmr = calculateBMR(currentStats)
        val tdee = calculateTDEE(bmr, workoutsPerWeek, weeklyVolume)

        // Calculate daily energy balance
        val dailyBalance = avgDailyCalories - tdee

        // Predict changes
        val prediction = calculateChanges(
            currentStats = currentStats,
            dailyBalance = dailyBalance,
            avgProtein = avgProtein,
            weeklyVolume = weeklyVolume,
            workoutsPerWeek = workoutsPerWeek,
            days = targetDays
        )

        return prediction
    }

    /**
     * Calculate Basal Metabolic Rate using Mifflin-St Jeor equation
     */
    private fun calculateBMR(stats: BodyStats): Int {
        val weightKg = stats.weightLbs * 0.453592
        return if (stats.isMale) {
            (10 * weightKg + 6.25 * stats.heightCm - 5 * stats.age + 5).toInt()
        } else {
            (10 * weightKg + 6.25 * stats.heightCm - 5 * stats.age - 161).toInt()
        }
    }

    /**
     * Calculate Total Daily Energy Expenditure
     */
    private fun calculateTDEE(bmr: Int, workoutsPerWeek: Float, weeklyVolume: Float): Int {
        // Base activity multiplier
        val baseMultiplier = when {
            workoutsPerWeek < 2 -> 1.2
            workoutsPerWeek < 4 -> 1.375
            workoutsPerWeek < 6 -> 1.55
            else -> 1.725
        }

        // Additional calories from training volume
        val volumeCalories = (weeklyVolume / 1000 * 50).toInt() // 50 cal per 1000 lbs volume

        return (bmr * baseMultiplier).toInt() + (volumeCalories / 7)
    }

    /**
     * Calculate weekly training volume from workout history
     */
    private fun calculateWeeklyVolume(workouts: List<Workout>): Float {
        if (workouts.isEmpty()) return 0f

        val weeksOfData = (workouts.size / 7f).coerceAtLeast(1f)
        val totalVolume = workouts.sumOf { it.totalVolume.toDouble() }.toFloat()

        return totalVolume / weeksOfData
    }

    /**
     * Calculate predicted body composition changes
     */
    private fun calculateChanges(
        currentStats: BodyStats,
        dailyBalance: Int,
        avgProtein: Int,
        weeklyVolume: Float,
        workoutsPerWeek: Float,
        days: Int
    ): BodyCompositionPrediction {
        // Total energy surplus/deficit over period
        val totalBalance = dailyBalance * days

        // Calculate fat change (3500 calories = 1 lb of fat)
        val rawFatChange = totalBalance / 3500f

        // Calculate muscle change based on several factors
        val muscleChange = calculateMuscleChange(
            currentStats = currentStats,
            dailyBalance = dailyBalance,
            avgProtein = avgProtein,
            weeklyVolume = weeklyVolume,
            workoutsPerWeek = workoutsPerWeek,
            days = days
        )

        // Net weight change is fat + muscle + water
        val waterWeight = calculateWaterWeightChange(dailyBalance, days)
        val totalWeightChange = rawFatChange + muscleChange + waterWeight

        // Calculate new stats
        val newWeight = currentStats.weightLbs + totalWeightChange
        val newFatMass = (currentStats.weightLbs * currentStats.bodyFatPct / 100) + rawFatChange
        val newLeanMass = (currentStats.weightLbs * (1 - currentStats.bodyFatPct / 100)) + muscleChange
        val newBodyFatPct = (newFatMass / newWeight * 100).toFloat()

        // Generate insights and recommendations
        val insights = generateInsights(
            currentStats, dailyBalance, avgProtein, muscleChange, rawFatChange, workoutsPerWeek
        )

        return BodyCompositionPrediction(
            days = days,
            currentWeight = currentStats.weightLbs,
            predictedWeight = newWeight,
            weightChange = totalWeightChange,
            currentBodyFat = currentStats.bodyFatPct,
            predictedBodyFat = newBodyFatPct,
            bodyFatChange = newBodyFatPct - currentStats.bodyFatPct,
            predictedFatMass = newFatMass,
            fatMassChange = rawFatChange,
            predictedLeanMass = newLeanMass,
            leanMassChange = muscleChange,
            dailyCalorieBalance = dailyBalance,
            insights = insights,
            confidence = calculateConfidence(currentStats, avgProtein, workoutsPerWeek)
        )
    }

    /**
     * Calculate muscle gain/loss based on multiple factors
     */
    private fun calculateMuscleChange(
        currentStats: BodyStats,
        dailyBalance: Int,
        avgProtein: Int,
        weeklyVolume: Float,
        workoutsPerWeek: Float,
        days: Int
    ): Float {
        // Base muscle change potential
        val monthlyPotential = when {
            currentStats.trainingAge < 1 -> 1.5f // Newbie gains (1-2 lbs/month)
            currentStats.trainingAge < 2 -> 1.0f // Intermediate
            currentStats.trainingAge < 4 -> 0.5f // Advanced
            else -> 0.25f // Elite
        }

        val weeksInPeriod = days / 7f
        val baseMuscleGain = monthlyPotential / 4f * weeksInPeriod

        // Adjust based on calorie surplus/deficit
        val calorieMultiplier = when {
            dailyBalance > 300 -> 1.2f // Surplus promotes muscle gain
            dailyBalance > 100 -> 1.0f // Slight surplus
            dailyBalance > -200 -> 0.3f // Small deficit - minimal muscle gain
            dailyBalance > -500 -> -0.2f // Moderate deficit - slight muscle loss
            else -> -0.5f // Large deficit - more muscle loss
        }

        // Protein adequacy (aim for 0.8-1g per lb body weight)
        val proteinTarget = currentStats.weightLbs * 0.8f
        val proteinRatio = (avgProtein / proteinTarget).coerceIn(0.5f, 1.5f)
        val proteinMultiplier = when {
            proteinRatio >= 1.0f -> 1.0f // Sufficient protein
            proteinRatio >= 0.8f -> 0.85f // Slightly low
            proteinRatio >= 0.6f -> 0.6f // Low protein
            else -> 0.4f // Very low - muscle loss likely
        }

        // Training volume impact
        val volumeMultiplier = when {
            weeklyVolume < 5000 -> 0.5f // Low volume
            weeklyVolume < 15000 -> 0.85f // Moderate volume
            weeklyVolume < 30000 -> 1.0f // Good volume
            weeklyVolume < 50000 -> 1.1f // High volume (slight bonus)
            else -> 1.0f // Very high (no additional benefit, risk of overtraining)
        }

        // Frequency impact
        val frequencyMultiplier = when {
            workoutsPerWeek < 2 -> 0.6f
            workoutsPerWeek < 4 -> 0.9f
            workoutsPerWeek < 6 -> 1.0f
            else -> 0.95f // More isn't always better
        }

        // Calculate final muscle change
        var muscleChange = baseMuscleGain * calorieMultiplier * proteinMultiplier *
                          volumeMultiplier * frequencyMultiplier

        // Cap muscle gain at realistic values (2 lbs per month max for newbies)
        val maxGain = 2.0f / 4f * weeksInPeriod
        muscleChange = muscleChange.coerceIn(-2f, maxGain)

        return muscleChange
    }

    /**
     * Calculate water weight changes (glycogen, sodium, hydration)
     */
    private fun calculateWaterWeightChange(dailyBalance: Int, days: Int): Float {
        // Initial water weight change from glycogen
        // Every gram of glycogen stores ~3g of water
        // Low carb/calorie = glycogen depletion = water loss
        return when {
            dailyBalance < -500 -> -2f // Significant water loss
            dailyBalance < -200 -> -1f // Moderate water loss
            dailyBalance > 300 -> 1.5f // Water gain from increased glycogen
            dailyBalance > 100 -> 0.5f // Slight water gain
            else -> 0f
        }
    }

    /**
     * Generate insights about the prediction
     */
    private fun generateInsights(
        stats: BodyStats,
        dailyBalance: Int,
        avgProtein: Int,
        muscleChange: Float,
        fatChange: Float,
        workoutsPerWeek: Float
    ): List<String> {
        val insights = mutableListOf<String>()

        // Overall trajectory
        when {
            fatChange < -2 && muscleChange > 0.5 -> {
                insights.add("Excellent recomposition! Losing fat while gaining muscle.")
            }
            fatChange < -1 && muscleChange >= 0 -> {
                insights.add("Great fat loss with muscle preservation.")
            }
            fatChange > 1 && muscleChange > 0.5 -> {
                insights.add("Lean bulking trajectory. Gaining muscle with minimal fat.")
            }
            fatChange > 2 && muscleChange < 0.5 -> {
                insights.add("Warning: Gaining mostly fat. Consider reducing calorie surplus.")
            }
            fatChange > 0 && muscleChange < 0 -> {
                insights.add("Alert: Gaining fat while losing muscle. Adjust diet and training.")
            }
        }

        // Protein adequacy
        val proteinTarget = stats.weightLbs * 0.8f
        if (avgProtein < proteinTarget * 0.8) {
            insights.add("Low protein intake (${avgProtein}g vs ${proteinTarget.toInt()}g target). Increase to preserve muscle.")
        } else if (avgProtein >= proteinTarget) {
            insights.add("Excellent protein intake supporting muscle growth and recovery.")
        }

        // Calorie balance
        when {
            abs(dailyBalance) < 100 -> {
                insights.add("Near maintenance calories. Good for recomposition but slow progress.")
            }
            dailyBalance < -700 -> {
                insights.add("Aggressive deficit. Risk of muscle loss and metabolic adaptation. Consider increasing calories slightly.")
            }
            dailyBalance > 500 -> {
                insights.add("Large surplus. Faster muscle gain but also more fat gain. Consider moderating surplus.")
            }
        }

        // Training frequency
        if (workoutsPerWeek < 3) {
            insights.add("Low training frequency. Increase to 3-5x per week for better results.")
        } else if (workoutsPerWeek > 6) {
            insights.add("Very high training frequency. Ensure adequate recovery to prevent overtraining.")
        }

        // Rate of change
        val weeklyWeightChange = (fatChange + muscleChange) / 4f
        when {
            weeklyWeightChange < -2 -> {
                insights.add("Rapid weight loss (>2 lbs/week). Slower rate reduces muscle loss risk.")
            }
            weeklyWeightChange > 1 -> {
                insights.add("Fast weight gain (>1 lb/week). Slower rate minimizes fat gain.")
            }
            weeklyWeightChange in -1f..0.5f -> {
                insights.add("Optimal rate of change for body recomposition.")
            }
        }

        return insights
    }

    /**
     * Calculate prediction confidence based on data quality
     */
    private fun calculateConfidence(
        stats: BodyStats,
        avgProtein: Int,
        workoutsPerWeek: Float
    ): Float {
        var confidence = 0.7f // Base confidence

        // More data = higher confidence
        if (workoutsPerWeek >= 3) confidence += 0.1f

        // Consistent protein tracking
        if (avgProtein > 0) confidence += 0.1f

        // Realistic stats
        if (stats.bodyFatPct in 5f..40f) confidence += 0.05f
        if (stats.weightLbs in 100f..400f) confidence += 0.05f

        return confidence.coerceIn(0f, 1f)
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class BodyStats(
    val weightLbs: Float,
    val heightCm: Float,
    val age: Int,
    val isMale: Boolean,
    val bodyFatPct: Float,
    val trainingAge: Int // Years of consistent training
)

data class BodyCompositionPrediction(
    val days: Int,
    val currentWeight: Float,
    val predictedWeight: Float,
    val weightChange: Float,
    val currentBodyFat: Float,
    val predictedBodyFat: Float,
    val bodyFatChange: Float,
    val predictedFatMass: Float,
    val fatMassChange: Float,
    val predictedLeanMass: Float,
    val leanMassChange: Float,
    val dailyCalorieBalance: Int,
    val insights: List<String>,
    val confidence: Float
)
