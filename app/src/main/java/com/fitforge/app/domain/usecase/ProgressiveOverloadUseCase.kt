package com.fitforge.app.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Progressive Overload Algorithm
 *
 * Implements intelligent weight progression based on:
 * - Performance history (reps completed vs target)
 * - Form quality scores
 * - Recovery status
 * - Training frequency
 *
 * Uses proven strength training principles:
 * - 2.5% weight increase when all sets hit upper rep range
 * - 5% increase when consistently exceeding targets
 * - Deload recommendations when form degrades or plateaus occur
 */
@Singleton
class ProgressiveOverloadUseCase @Inject constructor() {

    data class ProgressionRecommendation(
        val action: ProgressionAction,
        val recommendedWeight: Float,
        val reasoning: String,
        val confidence: Float // 0-1
    )

    enum class ProgressionAction {
        INCREASE,      // Add weight
        MAINTAIN,      // Keep current weight, focus on form/reps
        DELOAD,        // Reduce weight (recovery needed)
        VARY_REPS      // Change rep range
    }

    data class SetPerformance(
        val weight: Float,
        val targetReps: Int,
        val actualReps: Int,
        val formScore: Int, // 0-100
        val rpe: Int? = null, // Rate of Perceived Exertion 1-10
        val date: Long
    )

    /**
     * Analyzes recent performance and recommends next workout weight
     */
    fun calculateProgression(
        exerciseName: String,
        recentSets: List<SetPerformance>,
        recoveryScore: Int, // 0-100
        trainingAge: Int = 6 // months of training
    ): ProgressionRecommendation {
        if (recentSets.isEmpty()) {
            return ProgressionRecommendation(
                action = ProgressionAction.MAINTAIN,
                recommendedWeight = 0f,
                reasoning = "No performance history available",
                confidence = 0f
            )
        }

        val currentWeight = recentSets.last().weight
        val avgFormScore = recentSets.takeLast(4).map { it.formScore }.average()
        val consistency = calculateConsistency(recentSets)

        // Check if user is hitting upper rep range consistently
        val hittingTargets = recentSets.takeLast(3).all {
            it.actualReps >= it.targetReps
        }

        // Check if user is exceeding targets significantly
        val exceedingTargets = recentSets.takeLast(3).all {
            it.actualReps > it.targetReps
        }

        // Check for plateau (no progress in 3 weeks)
        val isPlateau = checkForPlateau(recentSets)

        // Check form degradation
        val formDegrading = recentSets.takeLast(3).map { it.formScore }
            .zipWithNext { a, b -> b < a }.count { it } >= 2

        return when {
            // Deload scenarios
            recoveryScore < 60 -> {
                ProgressionRecommendation(
                    action = ProgressionAction.DELOAD,
                    recommendedWeight = currentWeight * 0.85f,
                    reasoning = "Recovery score is low ($recoveryScore/100). Deload to 85% for recovery week.",
                    confidence = 0.9f
                )
            }
            formDegrading && avgFormScore < 85 -> {
                ProgressionRecommendation(
                    action = ProgressionAction.DELOAD,
                    recommendedWeight = currentWeight * 0.90f,
                    reasoning = "Form quality declining. Reduce weight 10% and focus on technique.",
                    confidence = 0.85f
                )
            }
            isPlateau -> {
                ProgressionRecommendation(
                    action = ProgressionAction.VARY_REPS,
                    recommendedWeight = currentWeight * 0.95f,
                    reasoning = "Plateau detected. Try higher reps (12-15) at 95% weight to break through.",
                    confidence = 0.75f
                )
            }

            // Progression scenarios
            exceedingTargets && avgFormScore >= 90 -> {
                val increment = calculateWeightIncrement(currentWeight, trainingAge, aggressive = true)
                ProgressionRecommendation(
                    action = ProgressionAction.INCREASE,
                    recommendedWeight = currentWeight + increment,
                    reasoning = "Crushing your targets with great form! Add ${increment}lbs. 🔥",
                    confidence = 0.95f
                )
            }
            hittingTargets && avgFormScore >= 85 -> {
                val increment = calculateWeightIncrement(currentWeight, trainingAge, aggressive = false)
                ProgressionRecommendation(
                    action = ProgressionAction.INCREASE,
                    recommendedWeight = currentWeight + increment,
                    reasoning = "Consistently hitting reps. Increase by ${increment}lbs. 💪",
                    confidence = 0.85f
                )
            }

            // Maintain scenarios
            avgFormScore < 80 -> {
                ProgressionRecommendation(
                    action = ProgressionAction.MAINTAIN,
                    recommendedWeight = currentWeight,
                    reasoning = "Focus on form quality before adding weight. Target 90+ form score.",
                    confidence = 0.8f
                )
            }
            else -> {
                ProgressionRecommendation(
                    action = ProgressionAction.MAINTAIN,
                    recommendedWeight = currentWeight,
                    reasoning = "Keep building strength at current weight. Aim to hit upper rep range.",
                    confidence = 0.7f
                )
            }
        }
    }

    private fun calculateWeightIncrement(
        currentWeight: Float,
        trainingAge: Int,
        aggressive: Boolean
    ): Float {
        // Novices can add more weight, advanced lifters add less
        val baseIncrement = when {
            trainingAge < 3 -> 10f  // Novice: 10 lbs
            trainingAge < 12 -> 5f   // Intermediate: 5 lbs
            else -> 2.5f             // Advanced: 2.5 lbs
        }

        return if (aggressive) baseIncrement * 1.5f else baseIncrement
    }

    private fun calculateConsistency(sets: List<SetPerformance>): Float {
        if (sets.size < 2) return 1f

        val weights = sets.map { it.weight }
        val variance = weights.map { (it - weights.average()).pow(2) }.average()

        return 1f / (1f + variance.toFloat() / 100f)
    }

    private fun checkForPlateau(sets: List<SetPerformance>): Boolean {
        if (sets.size < 6) return false

        // Check if weight hasn't increased in last 6 workouts
        val recentWeights = sets.takeLast(6).map { it.weight }
        return recentWeights.distinct().size == 1 &&
               sets.takeLast(6).map { it.actualReps }.average() <
               sets.takeLast(6).map { it.targetReps }.average()
    }

    /**
     * Calculates periodization recommendation (when to deload)
     */
    fun shouldDeload(weeksInProgram: Int, totalVolume: Float, recoveryScore: Int): Boolean {
        return when {
            weeksInProgram >= 12 -> true // Deload every 12 weeks
            weeksInProgram >= 6 && recoveryScore < 70 -> true // Mid-cycle deload if recovery poor
            totalVolume > 50000f && recoveryScore < 80 -> true // High volume with lowering recovery
            else -> false
        }
    }

    /**
     * Estimates 1 Rep Max based on working sets
     */
    fun estimate1RM(weight: Float, reps: Int): Float {
        // Epley formula: 1RM = weight × (1 + reps/30)
        return weight * (1 + reps / 30f)
    }

    /**
     * Calculates volume (sets × reps × weight)
     */
    fun calculateVolume(sets: List<SetPerformance>): Float {
        return sets.sumOf { (it.weight * it.actualReps).toDouble() }.toFloat()
    }
}
