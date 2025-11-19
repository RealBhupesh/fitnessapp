package com.fitforge.app.domain.usecase.analytics

import com.fitforge.app.data.model.ExerciseSet
import com.fitforge.app.data.repository.VolumeDataPoint
import javax.inject.Inject
import kotlin.math.abs

/**
 * Use Case for detecting training plateaus and providing intelligent recommendations
 * to break through stagnation.
 *
 * Analyzes:
 * - Volume progression over time
 * - Strength (1RM) stagnation
 * - Repeated failed sets
 * - Lack of progressive overload
 * - Overtraining indicators
 */
class PlateauDetectionUseCase @Inject constructor() {

    /**
     * Analyze exercise data to detect plateaus
     */
    fun execute(
        exerciseName: String,
        recentSets: List<ExerciseSet>,
        volumeHistory: List<VolumeDataPoint>,
        weeksToAnalyze: Int = 6
    ): PlateauAnalysis {
        if (recentSets.size < 10) {
            return PlateauAnalysis(
                hasPlateaued = false,
                plateauType = PlateauType.NONE,
                severity = 0,
                weeksStagnant = 0,
                recommendations = listOf("Need more data to detect plateaus. Keep training consistently!"),
                breakThroughStrategy = null
            )
        }

        // Analyze different aspects
        val volumePlateau = detectVolumePlateau(volumeHistory)
        val strengthPlateau = detectStrengthPlateau(recentSets)
        val overtrainingSign = detectOvertraining(recentSets, volumeHistory)

        // Determine overall plateau status
        val hasPlateaued = volumePlateau.weeksStagnant >= 3 || strengthPlateau.weeksStagnant >= 4
        val plateauType = determinePlateauType(volumePlateau, strengthPlateau, overtrainingSign)
        val severity = calculateSeverity(volumePlateau, strengthPlateau, overtrainingSign)

        // Generate recommendations
        val recommendations = generateRecommendations(
            exerciseName, plateauType, severity, volumePlateau, strengthPlateau, overtrainingSign
        )

        // Create break-through strategy
        val strategy = if (hasPlateaued) {
            createBreakThroughStrategy(plateauType, severity, exerciseName)
        } else null

        return PlateauAnalysis(
            hasPlateaued = hasPlateaued,
            plateauType = plateauType,
            severity = severity,
            weeksStagnant = maxOf(volumePlateau.weeksStagnant, strengthPlateau.weeksStagnant),
            recommendations = recommendations,
            breakThroughStrategy = strategy,
            volumeChange = volumePlateau.percentChange,
            strengthChange = strengthPlateau.percentChange,
            overtrainingRisk = overtrainingSign
        )
    }

    /**
     * Detect volume plateau by analyzing volume progression
     */
    private fun detectVolumePlateau(volumeHistory: List<VolumeDataPoint>): PlateauIndicator {
        if (volumeHistory.size < 4) {
            return PlateauIndicator(weeksStagnant = 0, percentChange = 0f)
        }

        // Compare recent 3 weeks to previous 3 weeks
        val recentWeeks = volumeHistory.takeLast(3)
        val previousWeeks = volumeHistory.dropLast(3).takeLast(3)

        val recentAvg = recentWeeks.map { it.volume }.average().toFloat()
        val previousAvg = previousWeeks.map { it.volume }.average().toFloat()

        val percentChange = if (previousAvg > 0) {
            ((recentAvg - previousAvg) / previousAvg * 100)
        } else 0f

        // Plateau if less than 5% change
        val weeksStagnant = if (abs(percentChange) < 5f) {
            countStagnantWeeks(volumeHistory)
        } else 0

        return PlateauIndicator(
            weeksStagnant = weeksStagnant,
            percentChange = percentChange
        )
    }

    /**
     * Detect strength plateau by analyzing estimated 1RM
     */
    private fun detectStrengthPlateau(recentSets: List<ExerciseSet>): PlateauIndicator {
        // Group sets by week
        val setsByWeek = recentSets.groupBy { set ->
            val daysSinceEpoch = set.completedAt / (24 * 60 * 60 * 1000)
            daysSinceEpoch / 7
        }

        if (setsByWeek.size < 4) {
            return PlateauIndicator(weeksStagnant = 0, percentChange = 0f)
        }

        // Calculate max estimated 1RM for each week
        val weekly1RMs = setsByWeek.map { (_, sets) ->
            sets.map { calculate1RM(it.weight, it.reps) }.maxOrNull() ?: 0f
        }

        // Compare recent 3 weeks to previous 3 weeks
        val recent1RMs = weekly1RMs.takeLast(3).average().toFloat()
        val previous1RMs = weekly1RMs.dropLast(3).takeLast(3).average().toFloat()

        val percentChange = if (previous1RMs > 0) {
            ((recent1RMs - previous1RMs) / previous1RMs * 100)
        } else 0f

        // Plateau if less than 2% strength increase
        val weeksStagnant = if (abs(percentChange) < 2f) {
            weekly1RMs.takeLast(6).let { recent ->
                var stagnantCount = 0
                for (i in 1 until recent.size) {
                    if (abs(recent[i] - recent[i - 1]) / recent[i - 1] * 100 < 2f) {
                        stagnantCount++
                    }
                }
                stagnantCount
            }
        } else 0

        return PlateauIndicator(
            weeksStagnant = weeksStagnant,
            percentChange = percentChange
        )
    }

    /**
     * Detect signs of overtraining
     */
    private fun detectOvertraining(
        recentSets: List<ExerciseSet>,
        volumeHistory: List<VolumeDataPoint>
    ): Boolean {
        // Sign 1: Volume increasing but strength decreasing
        if (volumeHistory.size >= 4) {
            val recentVolume = volumeHistory.takeLast(2).map { it.volume }.average()
            val previousVolume = volumeHistory.dropLast(2).takeLast(2).map { it.volume }.average()

            val recentStrength = recentSets.takeLast(10).map { calculate1RM(it.weight, it.reps) }.average()
            val previousStrength = recentSets.dropLast(10).takeLast(10).map { calculate1RM(it.weight, it.reps) }.average()

            if (recentVolume > previousVolume * 1.1 && recentStrength < previousStrength * 0.95) {
                return true // Volume up but strength down = overtraining
            }
        }

        // Sign 2: Consistently failing to hit target reps
        val recentFailures = recentSets.takeLast(15).count { it.reps < it.targetReps }
        if (recentFailures > 10) {
            return true // Failing most sets
        }

        // Sign 3: Decreasing RPE tolerance (if tracked)
        val recentRPE = recentSets.takeLast(10).mapNotNull { it.rpe }.average()
        if (recentRPE > 9) {
            return true // Everything feels very hard
        }

        return false
    }

    /**
     * Count consecutive stagnant weeks
     */
    private fun countStagnantWeeks(volumeHistory: List<VolumeDataPoint>): Int {
        var count = 0
        for (i in volumeHistory.size - 1 downTo 1) {
            val current = volumeHistory[i].volume
            val previous = volumeHistory[i - 1].volume
            val change = abs(current - previous) / previous * 100

            if (change < 5f) {
                count++
            } else {
                break
            }
        }
        return count
    }

    /**
     * Determine the type of plateau
     */
    private fun determinePlateauType(
        volumePlateau: PlateauIndicator,
        strengthPlateau: PlateauIndicator,
        overtraining: Boolean
    ): PlateauType {
        return when {
            overtraining -> PlateauType.OVERTRAINING
            volumePlateau.weeksStagnant >= 4 && strengthPlateau.weeksStagnant >= 4 -> PlateauType.COMPLETE_STAGNATION
            strengthPlateau.weeksStagnant >= 4 -> PlateauType.STRENGTH_PLATEAU
            volumePlateau.weeksStagnant >= 3 -> PlateauType.VOLUME_PLATEAU
            volumePlateau.percentChange < -10 -> PlateauType.REGRESSION
            else -> PlateauType.NONE
        }
    }

    /**
     * Calculate severity (0-100)
     */
    private fun calculateSeverity(
        volumePlateau: PlateauIndicator,
        strengthPlateau: PlateauIndicator,
        overtraining: Boolean
    ): Int {
        var severity = 0

        // Weeks stagnant contributes to severity
        severity += volumePlateau.weeksStagnant * 8
        severity += strengthPlateau.weeksStagnant * 10

        // Negative progress increases severity
        if (volumePlateau.percentChange < 0) severity += 15
        if (strengthPlateau.percentChange < 0) severity += 20

        // Overtraining is serious
        if (overtraining) severity += 30

        return severity.coerceIn(0, 100)
    }

    /**
     * Generate actionable recommendations
     */
    private fun generateRecommendations(
        exerciseName: String,
        plateauType: PlateauType,
        severity: Int,
        volumePlateau: PlateauIndicator,
        strengthPlateau: PlateauIndicator,
        overtraining: Boolean
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when (plateauType) {
            PlateauType.OVERTRAINING -> {
                recommendations.add("Take a deload week: Reduce volume by 40-50% for recovery.")
                recommendations.add("Ensure 7-9 hours of sleep per night to support recovery.")
                recommendations.add("Consider reducing training frequency by 1-2 days this week.")
                recommendations.add("Increase protein intake to support recovery (aim for 1g/lb bodyweight).")
            }

            PlateauType.COMPLETE_STAGNATION -> {
                recommendations.add("Change your rep scheme: If doing 3x8, try 4x6 or 5x10.")
                recommendations.add("Incorporate periodization: Alternate between strength (3-5 reps) and hypertrophy (8-12 reps) blocks.")
                recommendations.add("Try advanced techniques: Drop sets, rest-pause, or cluster sets.")
                recommendations.add("Evaluate your recovery: Poor sleep or nutrition may be limiting progress.")
            }

            PlateauType.STRENGTH_PLATEAU -> {
                recommendations.add("Focus on strength: Lower reps (3-5) with longer rest (3-4 min).")
                recommendations.add("Add accommodating resistance: Bands or chains if available.")
                recommendations.add("Work on weak points: Add accessory exercises targeting limiting factors.")
                recommendations.add("Consider a deload week before pushing for new PRs.")
            }

            PlateauType.VOLUME_PLATEAU -> {
                recommendations.add("Increase training volume: Add 1-2 sets per exercise.")
                recommendations.add("Increase frequency: Train this movement pattern one extra day per week.")
                recommendations.add("Try different exercises: Swap $exerciseName for a variation temporarily.")
                recommendations.add("Focus on progressive overload: Even small weekly increases matter.")
            }

            PlateauType.REGRESSION -> {
                recommendations.add("Immediate deload: Take a full week at 50% volume to recover.")
                recommendations.add("Assess external factors: Stress, sleep, nutrition, illness?")
                recommendations.add("Re-evaluate form: Poor technique may be limiting strength.")
                recommendations.add("Consider taking 3-5 days completely off from training.")
            }

            PlateauType.NONE -> {
                recommendations.add("Keep up the great work! You're making consistent progress.")
                recommendations.add("Continue progressive overload: Small weekly increases add up.")
            }
        }

        // Add specific insights based on data
        if (strengthPlateau.percentChange > 0 && volumePlateau.percentChange < 0) {
            recommendations.add("You're getting stronger but volume is decreasing. Focus on quality over quantity.")
        }

        if (severity > 60) {
            recommendations.add("Serious plateau detected. Consider hiring a coach for personalized programming.")
        }

        return recommendations
    }

    /**
     * Create a detailed strategy to break through the plateau
     */
    private fun createBreakThroughStrategy(
        plateauType: PlateauType,
        severity: Int,
        exerciseName: String
    ): BreakThroughStrategy {
        return when (plateauType) {
            PlateauType.OVERTRAINING -> {
                BreakThroughStrategy(
                    name = "Recovery Protocol",
                    duration = "1-2 weeks",
                    phases = listOf(
                        "Week 1: Reduce volume by 50%, maintain intensity",
                        "Week 2: Gradually return to normal volume",
                        "Prioritize sleep (8+ hours) and nutrition"
                    ),
                    expectedOutcome = "Restored energy, improved performance, reduced fatigue"
                )
            }

            PlateauType.COMPLETE_STAGNATION, PlateauType.STRENGTH_PLATEAU -> {
                BreakThroughStrategy(
                    name = "Strength Block",
                    duration = "4 weeks",
                    phases = listOf(
                        "Week 1: 5x5 at 80% 1RM, 3 min rest",
                        "Week 2: 4x4 at 85% 1RM, 4 min rest",
                        "Week 3: 3x3 at 90% 1RM, 5 min rest",
                        "Week 4: Deload - 3x5 at 70% 1RM, test new 1RM"
                    ),
                    expectedOutcome = "5-10% strength increase, improved neural efficiency"
                )
            }

            PlateauType.VOLUME_PLATEAU -> {
                BreakThroughStrategy(
                    name = "Volume Accumulation",
                    duration = "3 weeks",
                    phases = listOf(
                        "Week 1: Add 1 set to each exercise",
                        "Week 2: Add another set or increase frequency",
                        "Week 3: Maintain new volume, assess adaptation"
                    ),
                    expectedOutcome = "Increased work capacity, muscle growth stimulus"
                )
            }

            PlateauType.REGRESSION -> {
                BreakThroughStrategy(
                    name = "Reset & Rebuild",
                    duration = "2-3 weeks",
                    phases = listOf(
                        "Week 1: Complete rest or light cardio only",
                        "Week 2: Return at 60% previous volume",
                        "Week 3: Gradually increase to 80% previous volume"
                    ),
                    expectedOutcome = "Full recovery, restored motivation, renewed progress"
                )
            }

            PlateauType.NONE -> {
                BreakThroughStrategy(
                    name = "Maintain Course",
                    duration = "Ongoing",
                    phases = listOf("Continue current programming with progressive overload"),
                    expectedOutcome = "Sustained progress toward goals"
                )
            }
        }
    }

    /**
     * Calculate estimated 1RM using Epley formula
     */
    private fun calculate1RM(weight: Float, reps: Int): Float {
        if (reps == 1) return weight
        return weight * (1 + reps / 30f)
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class PlateauIndicator(
    val weeksStagnant: Int,
    val percentChange: Float
)

enum class PlateauType {
    NONE,
    VOLUME_PLATEAU,
    STRENGTH_PLATEAU,
    COMPLETE_STAGNATION,
    OVERTRAINING,
    REGRESSION
}

data class PlateauAnalysis(
    val hasPlateaued: Boolean,
    val plateauType: PlateauType,
    val severity: Int, // 0-100
    val weeksStagnant: Int,
    val recommendations: List<String>,
    val breakThroughStrategy: BreakThroughStrategy?,
    val volumeChange: Float = 0f,
    val strengthChange: Float = 0f,
    val overtrainingRisk: Boolean = false
)

data class BreakThroughStrategy(
    val name: String,
    val duration: String,
    val phases: List<String>,
    val expectedOutcome: String
)
