package com.fitforge.app.ml.pose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Manager for TensorFlow Lite-based pose detection and exercise form analysis.
 *
 * Uses MoveNet or PoseNet models to detect 17 key body points in real-time
 * and analyzes them for exercise form quality.
 *
 * Key Points (MoveNet):
 * 0: nose, 1-2: eyes, 3-4: ears,
 * 5-6: shoulders, 7-8: elbows, 9-10: wrists,
 * 11-12: hips, 13-14: knees, 15-16: ankles
 */
@Singleton
class PoseDetectionManager @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private var inputImageWidth = 192
    private var inputImageHeight = 192
    private var isInitialized = false

    companion object {
        private const val MODEL_NAME = "movenet_thunder.tflite" // Or movenet_lightning for faster inference
        private const val NUM_KEYPOINTS = 17
        private const val CONFIDENCE_THRESHOLD = 0.3f

        // Keypoint indices
        const val NOSE = 0
        const val LEFT_EYE = 1
        const val RIGHT_EYE = 2
        const val LEFT_EAR = 3
        const val RIGHT_EAR = 4
        const val LEFT_SHOULDER = 5
        const val RIGHT_SHOULDER = 6
        const val LEFT_ELBOW = 7
        const val RIGHT_ELBOW = 8
        const val LEFT_WRIST = 9
        const val RIGHT_WRIST = 10
        const val LEFT_HIP = 11
        const val RIGHT_HIP = 12
        const val LEFT_KNEE = 13
        const val RIGHT_KNEE = 14
        const val LEFT_ANKLE = 15
        const val RIGHT_ANKLE = 16
    }

    /**
     * Initialize the TensorFlow Lite interpreter
     */
    fun initialize() {
        try {
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                // Use GPU delegate if available
                // addDelegate(GpuDelegate())
                // Or use NNAPI for hardware acceleration
                setUseNNAPI(true)
            }

            interpreter = Interpreter(
                FileUtil.loadMappedFile(context, MODEL_NAME),
                options
            )

            isInitialized = true
        } catch (e: Exception) {
            // Model not found - in production, download from Firebase ML or bundle with app
            isInitialized = false
            // Fallback to mock data for development
        }
    }

    /**
     * Detect pose keypoints from camera frame
     */
    fun detectPose(bitmap: Bitmap): PoseDetectionResult {
        if (!isInitialized || interpreter == null) {
            // Return mock data for development/testing
            return getMockPoseData()
        }

        try {
            // Preprocess image
            val tensorImage = preprocessImage(bitmap)

            // Prepare output tensor
            val outputArray = Array(1) { Array(1) { Array(NUM_KEYPOINTS) { FloatArray(3) } } }

            // Run inference
            interpreter?.run(tensorImage.buffer, outputArray)

            // Parse keypoints
            val keypoints = parseKeypoints(outputArray[0][0], bitmap.width.toFloat(), bitmap.height.toFloat())

            // Calculate overall confidence
            val avgConfidence = keypoints.map { it.confidence }.average().toFloat()

            return PoseDetectionResult(
                keypoints = keypoints,
                confidence = avgConfidence,
                timestamp = System.currentTimeMillis(),
                isValid = avgConfidence > CONFIDENCE_THRESHOLD
            )
        } catch (e: Exception) {
            return PoseDetectionResult(
                keypoints = emptyList(),
                confidence = 0f,
                timestamp = System.currentTimeMillis(),
                isValid = false
            )
        }
    }

    /**
     * Preprocess image for model input
     */
    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(inputImageHeight, inputImageWidth))
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage.fromBitmap(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        return tensorImage
    }

    /**
     * Parse raw model output into keypoints
     */
    private fun parseKeypoints(
        output: Array<FloatArray>,
        imageWidth: Float,
        imageHeight: Float
    ): List<Keypoint> {
        return output.mapIndexed { index, data ->
            Keypoint(
                id = index,
                position = PointF(
                    data[1] * imageWidth,  // X coordinate
                    data[0] * imageHeight   // Y coordinate
                ),
                confidence = data[2],
                name = getKeypointName(index)
            )
        }
    }

    /**
     * Get keypoint name from index
     */
    private fun getKeypointName(index: Int): String {
        return when (index) {
            NOSE -> "nose"
            LEFT_EYE -> "left_eye"
            RIGHT_EYE -> "right_eye"
            LEFT_EAR -> "left_ear"
            RIGHT_EAR -> "right_ear"
            LEFT_SHOULDER -> "left_shoulder"
            RIGHT_SHOULDER -> "right_shoulder"
            LEFT_ELBOW -> "left_elbow"
            RIGHT_ELBOW -> "right_elbow"
            LEFT_WRIST -> "left_wrist"
            RIGHT_WRIST -> "right_wrist"
            LEFT_HIP -> "left_hip"
            RIGHT_HIP -> "right_hip"
            LEFT_KNEE -> "left_knee"
            RIGHT_KNEE -> "right_knee"
            LEFT_ANKLE -> "left_ankle"
            RIGHT_ANKLE -> "right_ankle"
            else -> "unknown"
        }
    }

    /**
     * Calculate angle between three keypoints
     */
    fun calculateAngle(point1: Keypoint, point2: Keypoint, point3: Keypoint): Float {
        val radians = atan2(point3.position.y - point2.position.y, point3.position.x - point2.position.x) -
                     atan2(point1.position.y - point2.position.y, point1.position.x - point2.position.x)

        var angle = Math.toDegrees(radians.toDouble()).toFloat()

        // Normalize angle to 0-180 range
        if (angle > 180) angle -= 360
        if (angle < 0) angle = kotlin.math.abs(angle)

        return angle
    }

    /**
     * Calculate distance between two keypoints
     */
    fun calculateDistance(point1: Keypoint, point2: Keypoint): Float {
        val dx = point1.position.x - point2.position.x
        val dy = point1.position.y - point2.position.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Check if keypoint is visible with sufficient confidence
     */
    fun isKeypointVisible(keypoint: Keypoint): Boolean {
        return keypoint.confidence > CONFIDENCE_THRESHOLD
    }

    /**
     * Get mock pose data for development (when model not available)
     */
    private fun getMockPoseData(): PoseDetectionResult {
        // Return realistic mock keypoints for testing
        val mockKeypoints = listOf(
            Keypoint(NOSE, PointF(100f, 80f), 0.95f, "nose"),
            Keypoint(LEFT_EYE, PointF(90f, 70f), 0.92f, "left_eye"),
            Keypoint(RIGHT_EYE, PointF(110f, 70f), 0.93f, "right_eye"),
            Keypoint(LEFT_SHOULDER, PointF(80f, 140f), 0.98f, "left_shoulder"),
            Keypoint(RIGHT_SHOULDER, PointF(120f, 140f), 0.97f, "right_shoulder"),
            Keypoint(LEFT_ELBOW, PointF(70f, 200f), 0.96f, "left_elbow"),
            Keypoint(RIGHT_ELBOW, PointF(130f, 200f), 0.95f, "right_elbow"),
            Keypoint(LEFT_WRIST, PointF(60f, 260f), 0.94f, "left_wrist"),
            Keypoint(RIGHT_WRIST, PointF(140f, 260f), 0.93f, "right_wrist"),
            Keypoint(LEFT_HIP, PointF(85f, 280f), 0.99f, "left_hip"),
            Keypoint(RIGHT_HIP, PointF(115f, 280f), 0.98f, "right_hip"),
            Keypoint(LEFT_KNEE, PointF(82f, 380f), 0.97f, "left_knee"),
            Keypoint(RIGHT_KNEE, PointF(118f, 380f), 0.96f, "right_knee"),
            Keypoint(LEFT_ANKLE, PointF(80f, 480f), 0.95f, "left_ankle"),
            Keypoint(RIGHT_ANKLE, PointF(120f, 480f), 0.94f, "right_ankle")
        )

        return PoseDetectionResult(
            keypoints = mockKeypoints,
            confidence = 0.95f,
            timestamp = System.currentTimeMillis(),
            isValid = true,
            isMock = true
        )
    }

    /**
     * Clean up resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        isInitialized = false
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class Keypoint(
    val id: Int,
    val position: PointF,
    val confidence: Float,
    val name: String
)

data class PoseDetectionResult(
    val keypoints: List<Keypoint>,
    val confidence: Float,
    val timestamp: Long,
    val isValid: Boolean,
    val isMock: Boolean = false
)
