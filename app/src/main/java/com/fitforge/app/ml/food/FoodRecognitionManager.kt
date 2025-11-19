package com.fitforge.app.ml.food

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for ML Kit-based food recognition from camera images.
 *
 * Uses a two-stage approach:
 * 1. ML Kit Image Labeling for quick food detection
 * 2. Custom TensorFlow Lite model for detailed nutrition estimation (optional)
 * 3. Nutrition database lookup for accurate macro information
 *
 * Supports:
 * - Real-time food detection
 * - Multi-food recognition (detect multiple items in one image)
 * - Portion size estimation
 * - Confidence scoring
 */
@Singleton
class FoodRecognitionManager @Inject constructor(
    private val context: Context,
    private val nutritionDatabase: NutritionDatabaseHelper
) {

    private val imageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.6f) // Only show results with 60%+ confidence
            .build()
        ImageLabeling.getClient(options)
    }

    private var nutritionEstimator: Interpreter? = null

    companion object {
        private const val NUTRITION_MODEL_NAME = "food_nutrition_model.tflite"
        private const val MIN_FOOD_CONFIDENCE = 0.65f

        // Common food categories to filter results
        private val FOOD_CATEGORIES = setOf(
            "food", "dish", "cuisine", "meal", "snack", "dessert", "beverage",
            "fruit", "vegetable", "meat", "seafood", "bread", "pasta", "rice",
            "cheese", "egg", "nut", "bean", "grain", "salad", "soup", "sandwich"
        )
    }

    /**
     * Initialize nutrition estimation model (optional advanced feature)
     */
    fun initialize() {
        try {
            nutritionEstimator = Interpreter(
                FileUtil.loadMappedFile(context, NUTRITION_MODEL_NAME)
            )
        } catch (e: Exception) {
            // Model not available - will use database lookup only
            nutritionEstimator = null
        }
    }

    /**
     * Recognize food items from image
     */
    suspend fun recognizeFood(bitmap: Bitmap): FoodRecognitionResult {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // Run ML Kit image labeling
            val labels = imageLabeler.process(inputImage).await()

            // Filter for food-related labels
            val foodLabels = labels.filter { label ->
                isFoodLabel(label) && label.confidence >= MIN_FOOD_CONFIDENCE
            }

            if (foodLabels.isEmpty()) {
                return FoodRecognitionResult(
                    detectedFoods = emptyList(),
                    totalCalories = 0,
                    totalProtein = 0f,
                    totalCarbs = 0f,
                    totalFat = 0f,
                    confidence = 0f,
                    message = "No food detected. Try getting closer or better lighting."
                )
            }

            // Convert labels to food items with nutrition info
            val foodItems = foodLabels.map { label ->
                createFoodItem(label, bitmap)
            }.sortedByDescending { it.confidence }

            // Calculate totals
            val totalCalories = foodItems.sumOf { it.calories }
            val totalProtein = foodItems.sumOf { it.protein.toDouble() }.toFloat()
            val totalCarbs = foodItems.sumOf { it.carbs.toDouble() }.toFloat()
            val totalFat = foodItems.sumOf { it.fat.toDouble() }.toFloat()
            val avgConfidence = foodItems.map { it.confidence }.average().toFloat()

            return FoodRecognitionResult(
                detectedFoods = foodItems,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat,
                confidence = avgConfidence,
                message = "Detected ${foodItems.size} food item(s)"
            )
        } catch (e: Exception) {
            return FoodRecognitionResult(
                detectedFoods = emptyList(),
                totalCalories = 0,
                totalProtein = 0f,
                totalCarbs = 0f,
                totalFat = 0f,
                confidence = 0f,
                message = "Error recognizing food: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Check if a label is food-related
     */
    private fun isFoodLabel(label: ImageLabel): Boolean {
        val text = label.text.lowercase()

        // Direct food category match
        if (FOOD_CATEGORIES.any { text.contains(it) }) return true

        // Check against known foods in database
        return nutritionDatabase.isFoodInDatabase(text)
    }

    /**
     * Create food item with nutrition information
     */
    private fun createFoodItem(label: ImageLabel, bitmap: Bitmap): DetectedFood {
        val foodName = label.text

        // Look up nutrition in database
        val nutritionInfo = nutritionDatabase.getNutritionInfo(foodName)

        // Estimate portion size (simplified - in production, use advanced CV)
        val portionMultiplier = estimatePortionSize(bitmap)

        return DetectedFood(
            name = foodName,
            calories = (nutritionInfo.calories * portionMultiplier).toInt(),
            protein = nutritionInfo.protein * portionMultiplier,
            carbs = nutritionInfo.carbs * portionMultiplier,
            fat = nutritionInfo.fat * portionMultiplier,
            servingSize = "${(100 * portionMultiplier).toInt()}g",
            confidence = label.confidence,
            category = determineFoodCategory(foodName)
        )
    }

    /**
     * Estimate portion size from image (simplified)
     * In production, this would use object detection for size estimation
     */
    private fun estimatePortionSize(bitmap: Bitmap): Float {
        // Simplified: assume standard serving
        // Advanced: Use object detection to estimate size relative to reference objects
        return 1.0f // 1x standard serving
    }

    /**
     * Determine food category for better organization
     */
    private fun determineFoodCategory(foodName: String): FoodCategory {
        val name = foodName.lowercase()
        return when {
            name.contains("fruit") || name.contains("apple") || name.contains("banana") ||
            name.contains("orange") || name.contains("berry") -> FoodCategory.FRUIT

            name.contains("vegetable") || name.contains("broccoli") || name.contains("carrot") ||
            name.contains("spinach") || name.contains("salad") -> FoodCategory.VEGETABLE

            name.contains("meat") || name.contains("chicken") || name.contains("beef") ||
            name.contains("pork") || name.contains("steak") -> FoodCategory.PROTEIN

            name.contains("fish") || name.contains("salmon") || name.contains("tuna") ||
            name.contains("shrimp") -> FoodCategory.SEAFOOD

            name.contains("bread") || name.contains("rice") || name.contains("pasta") ||
            name.contains("cereal") || name.contains("oat") -> FoodCategory.GRAIN

            name.contains("milk") || name.contains("cheese") || name.contains("yogurt") -> FoodCategory.DAIRY

            name.contains("dessert") || name.contains("cake") || name.contains("cookie") ||
            name.contains("candy") -> FoodCategory.DESSERT

            name.contains("beverage") || name.contains("drink") || name.contains("juice") ||
            name.contains("soda") -> FoodCategory.BEVERAGE

            else -> FoodCategory.OTHER
        }
    }

    /**
     * Clean up resources
     */
    fun close() {
        imageLabeler.close()
        nutritionEstimator?.close()
    }
}

/**
 * Helper class for nutrition database lookups
 * In production, this would connect to a comprehensive food database like USDA or custom API
 */
@Singleton
class NutritionDatabaseHelper @Inject constructor() {

    // Simplified in-memory database for common foods
    // In production, use Room database with USDA FoodData Central API
    private val nutritionData = mapOf(
        "apple" to NutritionInfo(95, 0.5f, 25f, 0.3f),
        "banana" to NutritionInfo(105, 1.3f, 27f, 0.4f),
        "chicken breast" to NutritionInfo(165, 31f, 0f, 3.6f),
        "salmon" to NutritionInfo(206, 22f, 0f, 12f),
        "broccoli" to NutritionInfo(55, 3.7f, 11f, 0.6f),
        "rice" to NutritionInfo(130, 2.7f, 28f, 0.3f),
        "bread" to NutritionInfo(75, 3f, 14f, 1f),
        "egg" to NutritionInfo(78, 6.3f, 0.6f, 5.3f),
        "milk" to NutritionInfo(103, 8f, 12f, 2.4f),
        "cheese" to NutritionInfo(113, 7f, 0.4f, 9f),
        "pasta" to NutritionInfo(131, 5f, 25f, 1.1f),
        "steak" to NutritionInfo(271, 25f, 0f, 19f),
        "salad" to NutritionInfo(33, 2.6f, 6f, 0.2f),
        "pizza" to NutritionInfo(266, 11f, 33f, 10f),
        "burger" to NutritionInfo(354, 16f, 30f, 19f),
        "sandwich" to NutritionInfo(250, 12f, 28f, 10f),
        "yogurt" to NutritionInfo(100, 10f, 13f, 0.4f),
        "oatmeal" to NutritionInfo(158, 6f, 28f, 3f),
        "nuts" to NutritionInfo(175, 5f, 6f, 15f),
        "avocado" to NutritionInfo(160, 2f, 8.5f, 15f),
        "sweet potato" to NutritionInfo(112, 2f, 26f, 0.1f),
        "beans" to NutritionInfo(127, 8.7f, 22f, 0.5f),
        "shrimp" to NutritionInfo(84, 18f, 0f, 1.2f),
        "tuna" to NutritionInfo(132, 28f, 0f, 1f),
        "peanut butter" to NutritionInfo(188, 8f, 7f, 16f),
        "protein shake" to NutritionInfo(150, 25f, 8f, 3f),
        "orange" to NutritionInfo(62, 1.2f, 15f, 0.2f),
        "strawberry" to NutritionInfo(32, 0.7f, 7.7f, 0.3f),
        "blueberry" to NutritionInfo(57, 0.7f, 14f, 0.3f),
        "spinach" to NutritionInfo(23, 2.9f, 3.6f, 0.4f),
        "tomato" to NutritionInfo(18, 0.9f, 3.9f, 0.2f),
        "cucumber" to NutritionInfo(16, 0.7f, 3.6f, 0.1f),
        "carrot" to NutritionInfo(41, 0.9f, 10f, 0.2f),
        "lettuce" to NutritionInfo(5, 0.5f, 1f, 0.1f),
        "beef" to NutritionInfo(250, 26f, 0f, 15f),
        "pork" to NutritionInfo(242, 27f, 0f, 14f),
        "turkey" to NutritionInfo(135, 30f, 0f, 0.7f),
        "tofu" to NutritionInfo(76, 8f, 1.9f, 4.8f),
        "quinoa" to NutritionInfo(120, 4.4f, 21f, 1.9f),
        "almond" to NutritionInfo(164, 6f, 6f, 14f),
        "walnut" to NutritionInfo(185, 4.3f, 3.9f, 18.5f),
        "cashew" to NutritionInfo(157, 5.2f, 9f, 12f),
        "chocolate" to NutritionInfo(235, 2.2f, 26f, 13f),
        "ice cream" to NutritionInfo(207, 3.5f, 24f, 11f),
        "cookie" to NutritionInfo(140, 2f, 19f, 6.5f),
        "cake" to NutritionInfo(257, 3f, 38f, 11f),
        "donut" to NutritionInfo(195, 2.5f, 23f, 11f)
    )

    fun getNutritionInfo(foodName: String): NutritionInfo {
        val normalizedName = foodName.lowercase()

        // Exact match
        nutritionData[normalizedName]?.let { return it }

        // Partial match (e.g., "grilled chicken" matches "chicken")
        nutritionData.forEach { (key, value) ->
            if (normalizedName.contains(key) || key.contains(normalizedName)) {
                return value
            }
        }

        // Default fallback for unknown foods
        return NutritionInfo(150, 5f, 20f, 5f) // Generic estimate
    }

    fun isFoodInDatabase(foodName: String): Boolean {
        val normalizedName = foodName.lowercase()
        return nutritionData.keys.any { it.contains(normalizedName) || normalizedName.contains(it) }
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class NutritionInfo(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

data class DetectedFood(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val servingSize: String,
    val confidence: Float,
    val category: FoodCategory
)

data class FoodRecognitionResult(
    val detectedFoods: List<DetectedFood>,
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val confidence: Float,
    val message: String
)

enum class FoodCategory {
    FRUIT,
    VEGETABLE,
    PROTEIN,
    SEAFOOD,
    GRAIN,
    DAIRY,
    DESSERT,
    BEVERAGE,
    OTHER
}
