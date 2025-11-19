package com.fitforge.app.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Recovery Score Calculation
 *
 * Calculates a comprehensive recovery score (0-100) based on:
 * - Sleep quality and duration
 * - Heart Rate Variability (HRV)
 * - Resting heart rate
 * - Muscle soreness levels
 * - Stress levels
 * - Nutrition adherence
 * - Workout frequency/intensity
 */
@Singleton
class RecoveryScoreUseCase @Inject constructor() {

    data class RecoveryInput(
        val sleepHours: Float? = null,          // Hours slept last night
        val sleepQuality: Int? = null,          // 0-100
        val hrv: Int? = null,                   // Heart Rate Variability in ms
        val baselineHRV: Int? = null,           // User's baseline HRV
        val restingHeartRate: Int? = null,      // Current RHR
        val baselineRHR: Int? = null,           // User's baseline RHR
        val muscleSoreness: Map<String, Int> = emptyMap(), // Body part to soreness (0-10)
        val stressLevel: Int? = null,           // 0-100
        val nutritionScore: Int? = null,        // Yesterday's nutrition quality (0-100)
        val lastWorkoutHours: Int? = null,      // Hours since last workout
        val weeklyWorkouts: Int = 0,            // Workouts this week
        val trainingVolume: Float = 0f          // Total volume this week
    )

    data class RecoveryResult(
        val score: Int,                         // Overall 0-100
        val sleepScore: Int,
        val hrvScore: Int,
        val sorenessScore: Int,
        val stressScore: Int,
        val readinessLevel: ReadinessLevel,
        val recommendation: String,
        val insights: List<String>
    )

    enum class ReadinessLevel {
        EXCELLENT,      // 90-100: Perfect for high intensity
        GOOD,           // 70-89: Ready for normal training
        MODERATE,       // 50-69: Light training or active recovery
        POOR            // 0-49: Rest day or very light activity
    }

    fun calculateRecoveryScore(input: RecoveryInput): RecoveryResult {
        val sleepScore = calculateSleepScore(input.sleepHours, input.sleepQuality)
        val hrvScore = calculateHRVScore(input.hrv, input.baselineHRV)
        val rhrScore = calculateRHRScore(input.restingHeartRate, input.baselineRHR)
        val sorenessScore = calculateSorenessScore(input.muscleSoreness)
        val stressScore = calculateStressScore(input.stressLevel)
        val nutritionScore = input.nutritionScore ?: 75
        val recoveryTimeScore = calculateRecoveryTimeScore(input.lastWorkoutHours)
        val volumeScore = calculateVolumeScore(input.weeklyWorkouts, input.trainingVolume)

        // Weighted average of all factors
        val overallScore = (
            sleepScore * 0.30 +          // Sleep is crucial
            hrvScore * 0.20 +             // HRV is a great indicator
            rhrScore * 0.10 +             // RHR matters
            sorenessScore * 0.15 +        // Soreness affects performance
            stressScore * 0.10 +          // Stress impacts recovery
            nutritionScore * 0.10 +       // Nutrition supports recovery
            recoveryTimeScore * 0.03 +    // Time since last workout
            volumeScore * 0.02            // Training volume context
        ).toInt().coerceIn(0, 100)

        val readiness = when {
            overallScore >= 90 -> ReadinessLevel.EXCELLENT
            overallScore >= 70 -> ReadinessLevel.GOOD
            overallScore >= 50 -> ReadinessLevel.MODERATE
            else -> ReadinessLevel.POOR
        }

        val insights = generateInsights(
            sleepScore, hrvScore, sorenessScore, stressScore,
            nutritionScore, input
        )

        val recommendation = generateRecommendation(readiness, overallScore, insights)

        return RecoveryResult(
            score = overallScore,
            sleepScore = sleepScore,
            hrvScore = hrvScore,
            sorenessScore = sorenessScore,
            stressScore = stressScore,
            readinessLevel = readiness,
            recommendation = recommendation,
            insights = insights
        )
    }

    private fun calculateSleepScore(hours: Float?, quality: Int?): Int {
        if (hours == null) return 75 // Default if no data

        val durationScore = when {
            hours >= 8f -> 100
            hours >= 7f -> 85
            hours >= 6f -> 65
            hours >= 5f -> 40
            else -> 20
        }

        val qualityScore = quality ?: 75

        return ((durationScore * 0.6) + (qualityScore * 0.4)).toInt()
    }

    private fun calculateHRVScore(hrv: Int?, baseline: Int?): Int {
        if (hrv == null || baseline == null) return 75

        val difference = hrv - baseline
        val percentChange = (difference.toFloat() / baseline * 100)

        return when {
            percentChange >= 10 -> 100   // HRV much higher than baseline (excellent)
            percentChange >= 5 -> 90     // HRV higher (very good)
            percentChange >= 0 -> 80     // HRV at baseline (good)
            percentChange >= -5 -> 65    // HRV slightly low (okay)
            percentChange >= -10 -> 45   // HRV low (poor recovery)
            else -> 25                   // HRV very low (needs rest)
        }.coerceIn(0, 100)
    }

    private fun calculateRHRScore(rhr: Int?, baseline: Int?): Int {
        if (rhr == null || baseline == null) return 75

        val difference = rhr - baseline

        return when {
            difference <= -3 -> 100      // RHR lower than baseline (excellent)
            difference <= 0 -> 85        // RHR at baseline (good)
            difference <= 3 -> 70        // RHR slightly elevated (okay)
            difference <= 6 -> 50        // RHR elevated (caution)
            else -> 30                   // RHR significantly elevated (poor recovery)
        }.coerceIn(0, 100)
    }

    private fun calculateSorenessScore(soreness: Map<String, Int>): Int {
        if (soreness.isEmpty()) return 100 // No soreness = perfect

        val avgSoreness = soreness.values.average()

        return when {
            avgSoreness <= 1 -> 100      // Minimal soreness
            avgSoreness <= 3 -> 85       // Light soreness (DOMS)
            avgSoreness <= 5 -> 65       // Moderate soreness
            avgSoreness <= 7 -> 40       // Significant soreness
            else -> 20                   // Severe soreness (needs rest)
        }.toInt().coerceIn(0, 100)
    }

    private fun calculateStressScore(stressLevel: Int?): Int {
        if (stressLevel == null) return 75

        return max(0, 100 - stressLevel) // Inverse: high stress = low score
    }

    private fun calculateRecoveryTimeScore(hoursSinceWorkout: Int?): Int {
        if (hoursSinceWorkout == null) return 75

        return when {
            hoursSinceWorkout >= 48 -> 100   // 2+ days = full recovery
            hoursSinceWorkout >= 24 -> 85    // 1 day = good
            hoursSinceWorkout >= 12 -> 65    // 12 hours = okay
            else -> 50                       // < 12 hours = still recovering
        }
    }

    private fun calculateVolumeScore(workouts: Int, volume: Float): Int {
        // Check if training load is appropriate
        return when {
            workouts <= 3 && volume < 30000 -> 100  // Light week
            workouts <= 5 && volume < 50000 -> 85   // Normal week
            workouts <= 6 && volume < 70000 -> 70   // Heavy week
            else -> 50                               // Very heavy (may need deload)
        }
    }

    private fun generateInsights(
        sleepScore: Int,
        hrvScore: Int,
        sorenessScore: Int,
        stressScore: Int,
        nutritionScore: Int,
        input: RecoveryInput
    ): List<String> {
        val insights = mutableListOf<String>()

        if (sleepScore < 70) {
            insights.add("⚠️ Sleep: Aim for 8+ hours. Sleep is when muscle growth happens!")
        }

        if (hrvScore < 70 && input.hrv != null) {
            insights.add("💗 HRV below baseline. Your body needs more recovery time.")
        }

        if (sorenessScore < 70) {
            val maxSoreness = input.muscleSoreness.maxByOrNull { it.value }
            maxSoreness?.let {
                insights.add("💪 ${it.key} is sore (${it.value}/10). Consider active recovery or rest.")
            }
        }

        if (stressScore < 70) {
            insights.add("😌 High stress detected. Consider meditation, yoga, or a rest day.")
        }

        if (nutritionScore < 70) {
            insights.add("🍎 Yesterday's nutrition was suboptimal. Focus on protein and hydration today.")
        }

        if (insights.isEmpty()) {
            insights.add("✅ All recovery metrics looking great! You're ready to train hard.")
        }

        return insights
    }

    private fun generateRecommendation(
        readiness: ReadinessLevel,
        score: Int,
        insights: List<String>
    ): String {
        return when (readiness) {
            ReadinessLevel.EXCELLENT ->
                "🔥 Perfect day for high-intensity training! Push for PRs and progressive overload."
            ReadinessLevel.GOOD ->
                "💪 Ready for normal training. Stick to your program and maintain good form."
            ReadinessLevel.MODERATE ->
                "⚠️ Consider light training or active recovery. Listen to your body."
            ReadinessLevel.POOR ->
                "🛑 Rest day recommended. Recovery is when you get stronger. Come back tomorrow ready!"
        }
    }

    /**
     * Predicts tomorrow's recovery based on today's workout
     */
    fun predictRecovery(
        currentScore: Int,
        plannedWorkoutIntensity: String, // "light", "moderate", "high"
        plannedDuration: Int // minutes
    ): Int {
        val impact = when (plannedWorkoutIntensity) {
            "light" -> -5
            "moderate" -> -15
            "high" -> -25
            else -> -10
        }

        val volumeImpact = (plannedDuration / 30) * -3

        return max(0, min(100, currentScore + impact + volumeImpact))
    }
}
