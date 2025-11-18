package com.fitforge.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitforge.app.data.database.Converters

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val age: Int,
    val gender: String, // "male", "female", "other"
    val weight: Float, // in lbs or kg
    val height: Float, // in inches or cm
    val bodyFatPercentage: Float? = null,
    val goals: List<String> = emptyList(), // ["muscle_gain", "weight_loss", "strength", "endurance"]
    val activityLevel: String = "moderate", // "sedentary", "light", "moderate", "active", "very_active"
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
