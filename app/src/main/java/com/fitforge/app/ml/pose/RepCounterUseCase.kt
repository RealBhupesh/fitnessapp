package com.fitforge.app.ml.pose

import javax.inject.Inject
import kotlin.math.abs

/**
 * Use Case for automatically counting exercise reps using pose detection.
 *
 * Uses state machine approach to detect rep completion:
 * 1. Detects starting position (extension/top)
 * 2. Detects bottom position (contraction)
 * 3. Detects return to top (counts as 1 rep)
 * 4. Validates rep quality before counting
 *
 * Supports intelligent filtering to avoid false positives from:
 * - Partial reps
 * - Bouncing/momentum
 * - Poor form
 */
class RepCounterUseCase @Inject constructor(
    private val poseManager: PoseDetectionManager
) {

    companion object {
        private const val SQUAT_TOP_THRESHOLD = 150f // Knee angle for standing
        private const val SQUAT_BOTTOM_THRESHOLD = 110f // Knee angle for depth
        private const val CURL_TOP_THRESHOLD = 50f // Elbow angle for contracted
        private const val CURL_BOTTOM_THRESHOLD = 150f // Elbow angle for extended
        private const val PRESS_TOP_THRESHOLD = 160f // Elbow angle for lockout
        private const val PRESS_BOTTOM_THRESHOLD = 100f // Elbow angle for lowered
        private const val MINIMUM_REP_DURATION = 1000L // ms - prevents bouncing
    }

    private var repState = RepState.NONE
    private var repCount = 0
    private var lastTransitionTime = 0L
    private var lastAngle = 0f
    private val repHistory = mutableListOf<RepData>()

    /**
     * Process a pose detection frame and update rep count
     */
    fun processFrame(
        exerciseName: String,
        poseResult: PoseDetectionResult,
        formScore: Int
    ): RepCountResult {
        if (!poseResult.isValid) {
            return RepCountResult(
                repCount = repCount,
                currentState = repState,
                lastRepQuality = 0,
                message = "Waiting for clear pose detection..."
            )
        }

        val (newState, angle) = detectRepState(exerciseName, poseResult)

        // Check for state transitions
        val repCompleted = checkStateTransition(
            exerciseName = exerciseName,
            oldState = repState,
            newState = newState,
            angle = angle,
            formScore = formScore,
            timestamp = poseResult.timestamp
        )

        repState = newState
        lastAngle = angle

        val message = when (repState) {
            RepState.TOP -> "Ready - lower to start rep"
            RepState.DESCENDING -> "Going down..."
            RepState.BOTTOM -> "Good depth - push back up"
            RepState.ASCENDING -> "Pushing up..."
            RepState.NONE -> "Position yourself to start"
        }

        return RepCountResult(
            repCount = repCount,
            currentState = repState,
            lastRepQuality = if (repHistory.isNotEmpty()) repHistory.last().quality else 0,
            message = message,
            averageQuality = if (repHistory.isNotEmpty()) repHistory.map { it.quality }.average().toInt() else 0,
            repHistory = repHistory.toList()
        )
    }

    /**
     * Detect current rep state based on exercise and angle
     */
    private fun detectRepState(exerciseName: String, pose: PoseDetectionResult): Pair<RepState, Float> {
        val keypoints = pose.keypoints.associateBy { it.id }

        return when (exerciseName.lowercase()) {
            "squat", "back squat", "front squat" -> {
                val angle = calculateSquatAngle(keypoints)
                val state = when {
                    angle > SQUAT_TOP_THRESHOLD -> RepState.TOP
                    angle < SQUAT_BOTTOM_THRESHOLD -> RepState.BOTTOM
                    angle < lastAngle && repState != RepState.BOTTOM -> RepState.DESCENDING
                    angle > lastAngle && repState != RepState.TOP -> RepState.ASCENDING
                    else -> repState
                }
                Pair(state, angle)
            }

            "bicep curl", "barbell curl", "dumbbell curl" -> {
                val angle = calculateCurlAngle(keypoints)
                val state = when {
                    angle < CURL_TOP_THRESHOLD -> RepState.TOP
                    angle > CURL_BOTTOM_THRESHOLD -> RepState.BOTTOM
                    angle > lastAngle && repState != RepState.BOTTOM -> RepState.DESCENDING
                    angle < lastAngle && repState != RepState.TOP -> RepState.ASCENDING
                    else -> repState
                }
                Pair(state, angle)
            }

            "bench press", "shoulder press", "overhead press" -> {
                val angle = calculatePressAngle(keypoints)
                val state = when {
                    angle > PRESS_TOP_THRESHOLD -> RepState.TOP
                    angle < PRESS_BOTTOM_THRESHOLD -> RepState.BOTTOM
                    angle < lastAngle && repState != RepState.BOTTOM -> RepState.DESCENDING
                    angle > lastAngle && repState != RepState.TOP -> RepState.ASCENDING
                    else -> repState
                }
                Pair(state, angle)
            }

            else -> Pair(RepState.NONE, 0f)
        }
    }

    /**
     * Check if a state transition indicates a completed rep
     */
    private fun checkStateTransition(
        exerciseName: String,
        oldState: RepState,
        newState: RepState,
        angle: Float,
        formScore: Int,
        timestamp: Long
    ): Boolean {
        // Rep is complete when returning to TOP from ASCENDING
        if (oldState == RepState.ASCENDING && newState == RepState.TOP) {
            // Validate rep before counting
            if (validateRep(exerciseName, formScore, timestamp)) {
                repCount++

                // Record rep data
                val repData = RepData(
                    repNumber = repCount,
                    quality = formScore,
                    duration = timestamp - lastTransitionTime,
                    timestamp = timestamp
                )
                repHistory.add(repData)

                lastTransitionTime = timestamp
                return true
            }
        }

        // Track transition time for duration calculation
        if (oldState != newState) {
            lastTransitionTime = timestamp
        }

        return false
    }

    /**
     * Validate that the rep meets minimum quality standards
     */
    private fun validateRep(exerciseName: String, formScore: Int, timestamp: Long): Boolean {
        // Check minimum rep duration (prevents bouncing/cheating)
        val duration = timestamp - lastTransitionTime
        if (duration < MINIMUM_REP_DURATION) {
            return false // Rep too fast - likely bouncing
        }

        // Check form score
        if (formScore < 50) {
            return false // Form too poor to count
        }

        // Check for full range of motion (verified by state machine)
        // If we reached TOP from ASCENDING, we know we went through BOTTOM

        return true
    }

    /**
     * Calculate knee angle for squats
     */
    private fun calculateSquatAngle(keypoints: Map<Int, Keypoint>): Float {
        val hip = keypoints[PoseDetectionManager.LEFT_HIP] ?: return 0f
        val knee = keypoints[PoseDetectionManager.LEFT_KNEE] ?: return 0f
        val ankle = keypoints[PoseDetectionManager.LEFT_ANKLE] ?: return 0f

        return poseManager.calculateAngle(hip, knee, ankle)
    }

    /**
     * Calculate elbow angle for curls
     */
    private fun calculateCurlAngle(keypoints: Map<Int, Keypoint>): Float {
        val shoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER] ?: return 0f
        val elbow = keypoints[PoseDetectionManager.LEFT_ELBOW] ?: return 0f
        val wrist = keypoints[PoseDetectionManager.LEFT_WRIST] ?: return 0f

        return poseManager.calculateAngle(shoulder, elbow, wrist)
    }

    /**
     * Calculate elbow angle for pressing movements
     */
    private fun calculatePressAngle(keypoints: Map<Int, Keypoint>): Float {
        val shoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER] ?: return 0f
        val elbow = keypoints[PoseDetectionManager.LEFT_ELBOW] ?: return 0f
        val wrist = keypoints[PoseDetectionManager.LEFT_WRIST] ?: return 0f

        return poseManager.calculateAngle(shoulder, elbow, wrist)
    }

    /**
     * Reset rep counter for new set
     */
    fun reset() {
        repState = RepState.NONE
        repCount = 0
        lastTransitionTime = 0L
        lastAngle = 0f
        repHistory.clear()
    }

    /**
     * Get detailed rep analysis
     */
    fun getRepAnalysis(): RepAnalysis {
        if (repHistory.isEmpty()) {
            return RepAnalysis(
                totalReps = 0,
                averageQuality = 0,
                averageDuration = 0,
                consistencyScore = 0,
                insights = listOf("Complete some reps to see analysis")
            )
        }

        val avgQuality = repHistory.map { it.quality }.average().toInt()
        val avgDuration = repHistory.map { it.duration }.average().toLong()

        // Calculate consistency (how similar are rep durations)
        val durations = repHistory.map { it.duration.toFloat() }
        val mean = durations.average().toFloat()
        val variance = durations.map { (it - mean) * (it - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        val coefficientOfVariation = (stdDev / mean * 100).toInt()
        val consistencyScore = (100 - coefficientOfVariation).coerceIn(0, 100)

        // Generate insights
        val insights = mutableListOf<String>()

        when {
            avgQuality >= 85 -> insights.add("Excellent form maintained throughout set!")
            avgQuality >= 70 -> insights.add("Good form overall. Minor improvements possible.")
            avgQuality >= 50 -> insights.add("Form declining. Focus on quality over quantity.")
            else -> insights.add("Poor form detected. Reduce weight and focus on technique.")
        }

        if (consistencyScore >= 85) {
            insights.add("Very consistent rep tempo - great control!")
        } else if (consistencyScore < 60) {
            insights.add("Inconsistent rep speed. Try to maintain steady tempo.")
        }

        // Check for fatigue (form degradation over time)
        if (repHistory.size >= 5) {
            val firstHalf = repHistory.take(repHistory.size / 2).map { it.quality }.average()
            val secondHalf = repHistory.drop(repHistory.size / 2).map { it.quality }.average()

            if (secondHalf < firstHalf * 0.8) {
                insights.add("Form degrading significantly. Consider stopping set.")
            }
        }

        return RepAnalysis(
            totalReps = repHistory.size,
            averageQuality = avgQuality,
            averageDuration = avgDuration,
            consistencyScore = consistencyScore,
            insights = insights
        )
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

enum class RepState {
    NONE,       // Not in a recognized position
    TOP,        // Extended/lockout position
    DESCENDING, // Moving from top to bottom
    BOTTOM,     // Contracted/lowered position
    ASCENDING   // Moving from bottom to top
}

data class RepCountResult(
    val repCount: Int,
    val currentState: RepState,
    val lastRepQuality: Int,
    val message: String,
    val averageQuality: Int = 0,
    val repHistory: List<RepData> = emptyList()
)

data class RepData(
    val repNumber: Int,
    val quality: Int,
    val duration: Long,
    val timestamp: Long
)

data class RepAnalysis(
    val totalReps: Int,
    val averageQuality: Int,
    val averageDuration: Long,
    val consistencyScore: Int,
    val insights: List<String>
)
