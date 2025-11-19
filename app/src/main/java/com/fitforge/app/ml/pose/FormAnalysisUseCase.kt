package com.fitforge.app.ml.pose

import javax.inject.Inject
import kotlin.math.abs

/**
 * Use Case for analyzing exercise form using pose detection data.
 *
 * Provides real-time feedback on:
 * - Joint angles and alignment
 * - Range of motion
 * - Movement symmetry
 * - Common form errors
 * - Injury risk indicators
 *
 * Supported Exercises:
 * - Squat
 * - Bench Press
 * - Deadlift
 * - Shoulder Press
 * - Bicep Curl
 * - Plank
 */
class FormAnalysisUseCase @Inject constructor(
    private val poseManager: PoseDetectionManager
) {

    /**
     * Analyze exercise form from pose detection data
     */
    fun analyzeForm(
        exerciseName: String,
        poseResult: PoseDetectionResult,
        phase: ExercisePhase
    ): FormAnalysisResult {
        if (!poseResult.isValid) {
            return FormAnalysisResult(
                overallScore = 0,
                formErrors = listOf("Unable to detect body pose clearly"),
                recommendations = listOf("Ensure full body is visible in camera"),
                metrics = emptyMap(),
                phase = phase
            )
        }

        return when (exerciseName.lowercase()) {
            "squat", "back squat", "front squat" -> analyzeSquat(poseResult, phase)
            "bench press", "barbell bench press" -> analyzeBenchPress(poseResult, phase)
            "deadlift", "conventional deadlift" -> analyzeDeadlift(poseResult, phase)
            "shoulder press", "overhead press" -> analyzeShoulderPress(poseResult, phase)
            "bicep curl", "barbell curl" -> analyzeBicepCurl(poseResult, phase)
            "plank" -> analyzePlank(poseResult)
            else -> FormAnalysisResult(
                overallScore = 50,
                formErrors = emptyList(),
                recommendations = listOf("Form analysis not yet available for $exerciseName"),
                metrics = emptyMap(),
                phase = phase
            )
        }
    }

    /**
     * Analyze squat form
     */
    private fun analyzeSquat(pose: PoseDetectionResult, phase: ExercisePhase): FormAnalysisResult {
        val keypoints = pose.keypoints.associateBy { it.id }
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        val metrics = mutableMapOf<String, Float>()

        val leftHip = keypoints[PoseDetectionManager.LEFT_HIP]
        val rightHip = keypoints[PoseDetectionManager.RIGHT_HIP]
        val leftKnee = keypoints[PoseDetectionManager.LEFT_KNEE]
        val rightKnee = keypoints[PoseDetectionManager.RIGHT_KNEE]
        val leftAnkle = keypoints[PoseDetectionManager.LEFT_ANKLE]
        val rightAnkle = keypoints[PoseDetectionManager.RIGHT_ANKLE]
        val leftShoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER]

        if (leftHip == null || rightHip == null || leftKnee == null || rightKnee == null ||
            leftAnkle == null || rightAnkle == null || leftShoulder == null) {
            return FormAnalysisResult(
                overallScore = 0,
                formErrors = listOf("Cannot detect all necessary body parts"),
                recommendations = listOf("Ensure side profile view for best analysis"),
                metrics = emptyMap(),
                phase = phase
            )
        }

        // 1. Check knee angle (depth)
        val kneeAngle = poseManager.calculateAngle(leftHip, leftKnee, leftAnkle)
        metrics["knee_angle"] = kneeAngle

        when {
            phase == ExercisePhase.BOTTOM && kneeAngle > 120 -> {
                errors.add("Squat depth too shallow")
                recommendations.add("Aim for thighs parallel to ground (90° knee angle)")
            }
            phase == ExercisePhase.BOTTOM && kneeAngle < 70 -> {
                errors.add("Squatting too deep - may strain knees")
                recommendations.add("Stop when thighs are just below parallel")
            }
            phase == ExercisePhase.BOTTOM && kneeAngle in 85f..95f -> {
                // Perfect depth
            }
        }

        // 2. Check hip angle (for squat pattern)
        val hipAngle = poseManager.calculateAngle(leftShoulder, leftHip, leftKnee)
        metrics["hip_angle"] = hipAngle

        if (phase == ExercisePhase.BOTTOM && hipAngle > 100) {
            errors.add("Hips not dropping back enough")
            recommendations.add("Push hips back more - think 'sitting back into a chair'")
        }

        // 3. Check knee tracking (knees over toes)
        if (phase == ExercisePhase.BOTTOM) {
            val kneeToAnkleDist = abs(leftKnee.position.x - leftAnkle.position.x)
            val hipToKneeDist = abs(leftHip.position.x - leftKnee.position.x)

            if (kneeToAnkleDist > hipToKneeDist * 1.3) {
                errors.add("Knees tracking too far forward")
                recommendations.add("Keep knees over toes - push hips back more")
            }
        }

        // 4. Check back angle (torso lean)
        val torsoAngle = calculateTorsoAngle(leftShoulder, leftHip)
        metrics["torso_angle"] = torsoAngle

        when {
            torsoAngle < 40 -> {
                errors.add("Excessive forward lean - back rounding risk")
                recommendations.add("Keep chest up and core braced")
            }
            torsoAngle > 80 && phase == ExercisePhase.BOTTOM -> {
                errors.add("Too upright - may indicate poor hip mobility")
                recommendations.add("Slight forward lean is normal for squats")
            }
        }

        // 5. Check symmetry (left vs right)
        val rightKneeAngle = poseManager.calculateAngle(rightHip, rightKnee, rightAnkle)
        val asymmetry = abs(kneeAngle - rightKneeAngle)
        metrics["asymmetry"] = asymmetry

        if (asymmetry > 10) {
            errors.add("Uneven squat - one side lower than other")
            recommendations.add("Focus on even weight distribution and controlled descent")
        }

        // Calculate overall score
        val score = calculateSquatScore(kneeAngle, hipAngle, torsoAngle, asymmetry, phase)

        return FormAnalysisResult(
            overallScore = score,
            formErrors = errors,
            recommendations = recommendations,
            metrics = metrics,
            phase = phase
        )
    }

    /**
     * Analyze bench press form
     */
    private fun analyzeBenchPress(pose: PoseDetectionResult, phase: ExercisePhase): FormAnalysisResult {
        val keypoints = pose.keypoints.associateBy { it.id }
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        val metrics = mutableMapOf<String, Float>()

        val leftShoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER]
        val rightShoulder = keypoints[PoseDetectionManager.RIGHT_SHOULDER]
        val leftElbow = keypoints[PoseDetectionManager.LEFT_ELBOW]
        val rightElbow = keypoints[PoseDetectionManager.RIGHT_ELBOW]
        val leftWrist = keypoints[PoseDetectionManager.LEFT_WRIST]
        val rightWrist = keypoints[PoseDetectionManager.RIGHT_WRIST]

        if (leftShoulder == null || rightShoulder == null || leftElbow == null ||
            rightElbow == null || leftWrist == null || rightWrist == null) {
            return FormAnalysisResult(
                overallScore = 0,
                formErrors = listOf("Cannot detect upper body clearly"),
                recommendations = listOf("Ensure camera shows full upper body"),
                metrics = emptyMap(),
                phase = phase
            )
        }

        // 1. Check elbow angle
        val elbowAngle = poseManager.calculateAngle(leftShoulder, leftElbow, leftWrist)
        metrics["elbow_angle"] = elbowAngle

        when {
            phase == ExercisePhase.BOTTOM && elbowAngle > 110 -> {
                errors.add("Not lowering bar enough")
                recommendations.add("Lower bar until forearms are vertical (90° elbow)")
            }
            phase == ExercisePhase.BOTTOM && elbowAngle < 70 -> {
                errors.add("Lowering too far - shoulder injury risk")
                recommendations.add("Stop at 90° elbow angle")
            }
            phase == ExercisePhase.TOP && elbowAngle < 160 -> {
                errors.add("Not fully extending at top")
                recommendations.add("Lock out elbows at the top (don't hyperextend)")
            }
        }

        // 2. Check elbow flare (angle from body)
        val elbowFlare = abs(leftElbow.position.y - leftShoulder.position.y)
        val shoulderWidth = abs(leftShoulder.position.x - rightShoulder.position.x)
        val flareRatio = elbowFlare / shoulderWidth
        metrics["elbow_flare"] = flareRatio

        if (flareRatio > 0.9f && phase == ExercisePhase.BOTTOM) {
            errors.add("Elbows flaring out too much")
            recommendations.add("Tuck elbows at 45° angle to protect shoulders")
        }

        // 3. Check bar path (wrists above elbows)
        if (phase == ExercisePhase.BOTTOM) {
            val wristAboveElbow = leftWrist.position.y < leftElbow.position.y
            if (!wristAboveElbow) {
                errors.add("Bar path incorrect - wrists should be above elbows")
                recommendations.add("Keep forearms vertical throughout the movement")
            }
        }

        // 4. Check symmetry
        val rightElbowAngle = poseManager.calculateAngle(rightShoulder, rightElbow, rightWrist)
        val asymmetry = abs(elbowAngle - rightElbowAngle)
        metrics["asymmetry"] = asymmetry

        if (asymmetry > 12) {
            errors.add("Uneven press - one arm lowering more than other")
            recommendations.add("Control descent evenly and maintain balanced bar")
        }

        val score = calculateBenchScore(elbowAngle, flareRatio, asymmetry, phase)

        return FormAnalysisResult(
            overallScore = score,
            formErrors = errors,
            recommendations = recommendations,
            metrics = metrics,
            phase = phase
        )
    }

    /**
     * Analyze deadlift form
     */
    private fun analyzeDeadlift(pose: PoseDetectionResult, phase: ExercisePhase): FormAnalysisResult {
        val keypoints = pose.keypoints.associateBy { it.id }
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        val metrics = mutableMapOf<String, Float>()

        val leftShoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER]
        val leftHip = keypoints[PoseDetectionManager.LEFT_HIP]
        val leftKnee = keypoints[PoseDetectionManager.LEFT_KNEE]
        val leftAnkle = keypoints[PoseDetectionManager.LEFT_ANKLE]

        if (leftShoulder == null || leftHip == null || leftKnee == null || leftAnkle == null) {
            return FormAnalysisResult(
                overallScore = 0,
                formErrors = listOf("Cannot detect full body"),
                recommendations = listOf("Ensure side view for deadlift analysis"),
                metrics = emptyMap(),
                phase = phase
            )
        }

        // 1. Check back angle (should be relatively straight)
        val backAngle = calculateTorsoAngle(leftShoulder, leftHip)
        metrics["back_angle"] = backAngle

        if (backAngle < 30 && phase == ExercisePhase.BOTTOM) {
            errors.add("Back rounding detected - STOP!")
            recommendations.add("Reset: Chest up, neutral spine, engage lats")
        } else if (backAngle < 45 && phase == ExercisePhase.BOTTOM) {
            errors.add("Excessive forward lean")
            recommendations.add("Push chest up more before pulling")
        }

        // 2. Check hip hinge angle
        val hipAngle = poseManager.calculateAngle(leftShoulder, leftHip, leftKnee)
        metrics["hip_angle"] = hipAngle

        if (phase == ExercisePhase.BOTTOM && hipAngle > 140) {
            errors.add("Hips too high - not engaging legs enough")
            recommendations.add("Lower hips slightly and drive through legs")
        } else if (phase == ExercisePhase.BOTTOM && hipAngle < 80) {
            errors.add("Hips too low - turning into a squat")
            recommendations.add("Raise hips slightly - deadlift is a hip hinge, not a squat")
        }

        // 3. Check lockout at top
        if (phase == ExercisePhase.TOP) {
            val topHipAngle = poseManager.calculateAngle(leftShoulder, leftHip, leftKnee)
            if (topHipAngle < 165) {
                errors.add("Not locking out hips completely")
                recommendations.add("Squeeze glutes and push hips through at top")
            }
        }

        // 4. Check bar path (should be vertical, close to body)
        val shoulderToAnkle = abs(leftShoulder.position.x - leftAnkle.position.x)
        metrics["bar_distance"] = shoulderToAnkle

        if (shoulderToAnkle > 50) { // Adjust threshold based on image size
            errors.add("Bar too far from body")
            recommendations.add("Keep bar close - drag up shins and thighs")
        }

        val score = calculateDeadliftScore(backAngle, hipAngle, phase)

        return FormAnalysisResult(
            overallScore = score,
            formErrors = errors,
            recommendations = recommendations,
            metrics = metrics,
            phase = phase
        )
    }

    /**
     * Analyze shoulder press form
     */
    private fun analyzeShoulderPress(pose: PoseDetectionResult, phase: ExercisePhase): FormAnalysisResult {
        val keypoints = pose.keypoints.associateBy { it.id }
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        val metrics = mutableMapOf<String, Float>()

        val leftShoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER]
        val leftElbow = keypoints[PoseDetectionManager.LEFT_ELBOW]
        val leftWrist = keypoints[PoseDetectionManager.LEFT_WRIST]
        val leftHip = keypoints[PoseDetectionManager.LEFT_HIP]

        if (leftShoulder == null || leftElbow == null || leftWrist == null || leftHip == null) {
            return FormAnalysisResult(0, listOf("Cannot detect upper body"), emptyList(), emptyMap(), phase)
        }

        // Check elbow position
        val elbowAngle = poseManager.calculateAngle(leftShoulder, leftElbow, leftWrist)
        metrics["elbow_angle"] = elbowAngle

        if (phase == ExercisePhase.TOP && elbowAngle < 160) {
            errors.add("Not pressing to full lockout")
            recommendations.add("Fully extend arms overhead")
        }

        // Check torso lean (should be upright)
        val torsoAngle = calculateTorsoAngle(leftShoulder, leftHip)
        metrics["torso_lean"] = torsoAngle

        if (torsoAngle < 75) {
            errors.add("Excessive backward lean")
            recommendations.add("Keep core tight and torso upright")
        }

        val score = if (errors.isEmpty()) 95 else 70

        return FormAnalysisResult(score, errors, recommendations, metrics, phase)
    }

    /**
     * Analyze bicep curl form
     */
    private fun analyzeBicepCurl(pose: PoseDetectionResult, phase: ExercisePhase): FormAnalysisResult {
        val keypoints = pose.keypoints.associateBy { it.id }
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        val metrics = mutableMapOf<String, Float>()

        val leftShoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER]
        val leftElbow = keypoints[PoseDetectionManager.LEFT_ELBOW]
        val leftWrist = keypoints[PoseDetectionManager.LEFT_WRIST]

        if (leftShoulder == null || leftElbow == null || leftWrist == null) {
            return FormAnalysisResult(0, listOf("Cannot detect arms"), emptyList(), emptyMap(), phase)
        }

        // Check curl angle
        val elbowAngle = poseManager.calculateAngle(leftShoulder, leftElbow, leftWrist)
        metrics["elbow_angle"] = elbowAngle

        when {
            phase == ExercisePhase.TOP && elbowAngle > 50 -> {
                errors.add("Not curling high enough")
                recommendations.add("Curl until forearm touches bicep")
            }
            phase == ExercisePhase.BOTTOM && elbowAngle < 160 -> {
                errors.add("Not fully extending at bottom")
                recommendations.add("Lower until arms are fully extended")
            }
        }

        // Check elbow movement (should stay stationary)
        val elbowMovement = abs(leftElbow.position.y - leftShoulder.position.y)
        if (elbowMovement > 30) { // Threshold depends on image scale
            errors.add("Elbows swinging - using momentum")
            recommendations.add("Pin elbows to sides and curl with biceps only")
        }

        val score = if (errors.isEmpty()) 90 else 65

        return FormAnalysisResult(score, errors, recommendations, metrics, phase)
    }

    /**
     * Analyze plank form
     */
    private fun analyzePlank(pose: PoseDetectionResult): FormAnalysisResult {
        val keypoints = pose.keypoints.associateBy { it.id }
        val errors = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        val metrics = mutableMapOf<String, Float>()

        val leftShoulder = keypoints[PoseDetectionManager.LEFT_SHOULDER]
        val leftHip = keypoints[PoseDetectionManager.LEFT_HIP]
        val leftAnkle = keypoints[PoseDetectionManager.LEFT_ANKLE]

        if (leftShoulder == null || leftHip == null || leftAnkle == null) {
            return FormAnalysisResult(0, listOf("Cannot detect body"), emptyList(), emptyMap(), ExercisePhase.HOLD)
        }

        // Check body alignment (should be straight line)
        val bodyAngle = poseManager.calculateAngle(leftShoulder, leftHip, leftAnkle)
        metrics["body_angle"] = bodyAngle

        when {
            bodyAngle < 160 -> {
                errors.add("Hips sagging - lower back at risk")
                recommendations.add("Engage core and squeeze glutes")
            }
            bodyAngle > 185 -> {
                errors.add("Hips too high - not engaging core")
                recommendations.add("Lower hips to form straight line")
            }
        }

        val score = if (abs(180 - bodyAngle) < 10) 95 else 70

        return FormAnalysisResult(score, errors, recommendations, metrics, ExercisePhase.HOLD)
    }

    // ============================================================================
    // SCORING FUNCTIONS
    // ============================================================================

    private fun calculateSquatScore(
        kneeAngle: Float,
        hipAngle: Float,
        torsoAngle: Float,
        asymmetry: Float,
        phase: ExercisePhase
    ): Int {
        var score = 100

        if (phase == ExercisePhase.BOTTOM) {
            // Knee angle scoring
            when {
                kneeAngle in 85f..95f -> score += 0 // Perfect
                kneeAngle in 75f..85f || kneeAngle in 95f..105f -> score -= 5
                kneeAngle in 65f..75f || kneeAngle in 105f..120f -> score -= 15
                else -> score -= 25
            }

            // Hip angle scoring
            when {
                hipAngle in 70f..95f -> score += 0 // Good hip hinge
                hipAngle in 60f..70f || hipAngle in 95f..110f -> score -= 10
                else -> score -= 20
            }
        }

        // Torso angle
        when {
            torsoAngle in 45f..75f -> score += 0 // Good
            torsoAngle in 35f..45f || torsoAngle in 75f..85f -> score -= 10
            else -> score -= 20
        }

        // Asymmetry penalty
        if (asymmetry > 10) score -= 15
        if (asymmetry > 20) score -= 10

        return score.coerceIn(0, 100)
    }

    private fun calculateBenchScore(
        elbowAngle: Float,
        flareRatio: Float,
        asymmetry: Float,
        phase: ExercisePhase
    ): Int {
        var score = 100

        if (phase == ExercisePhase.BOTTOM && abs(elbowAngle - 90) > 20) score -= 20
        if (flareRatio > 0.9f) score -= 15
        if (asymmetry > 12) score -= 20

        return score.coerceIn(0, 100)
    }

    private fun calculateDeadliftScore(backAngle: Float, hipAngle: Float, phase: ExercisePhase): Int {
        var score = 100

        if (backAngle < 40) score -= 40 // Critical - back rounding
        else if (backAngle < 50) score -= 20

        if (phase == ExercisePhase.BOTTOM) {
            when {
                hipAngle in 90f..130f -> score += 0 // Good
                else -> score -= 15
            }
        }

        return score.coerceIn(0, 100)
    }

    private fun calculateTorsoAngle(shoulder: Keypoint, hip: Keypoint): Float {
        val dx = shoulder.position.x - hip.position.x
        val dy = shoulder.position.y - hip.position.y
        return Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

enum class ExercisePhase {
    TOP,        // Lockout/extended position
    MIDDLE,     // Transition
    BOTTOM,     // Contracted/lowered position
    HOLD        // Static hold (e.g., plank)
}

data class FormAnalysisResult(
    val overallScore: Int,          // 0-100
    val formErrors: List<String>,    // Detected errors
    val recommendations: List<String>, // How to fix
    val metrics: Map<String, Float>,  // Angles, distances, etc.
    val phase: ExercisePhase
)
