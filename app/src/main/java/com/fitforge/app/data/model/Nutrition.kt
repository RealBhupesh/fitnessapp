package com.fitforge.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitforge.app.data.database.Converters

@Entity(tableName = "meals")
@TypeConverters(Converters::class)
data class Meal(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val mealType: String, // "breakfast", "lunch", "dinner", "snack"
    val foods: List<FoodItem> = emptyList(),
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val imageUrl: String? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String // "YYYY-MM-DD"
)

data class FoodItem(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val servingSize: String,
    val servingAmount: Float = 1f,
    val barcode: String? = null,
    val brand: String? = null
)

@Entity(tableName = "nutrition_goals")
data class NutritionGoal(
    @PrimaryKey val userId: String,
    val dailyCalories: Int,
    val dailyProtein: Float,
    val dailyCarbs: Float,
    val dailyFat: Float,
    val waterGoal: Int = 128, // oz or ml
    val goal: String = "maintenance", // "deficit", "maintenance", "surplus"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_nutrition")
data class DailyNutrition(
    @PrimaryKey val id: String,
    val userId: String,
    val date: String, // "YYYY-MM-DD"
    val totalCalories: Int = 0,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f,
    val waterIntake: Int = 0,
    val mealsLogged: Int = 0,
    val goalMet: Boolean = false
)
