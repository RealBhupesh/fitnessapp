package com.fitforge.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitforge.app.data.database.Converters

@Entity(tableName = "workouts")
@TypeConverters(Converters::class)
data class Workout(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String = "",
    val category: String, // "strength", "cardio", "flexibility", "hiit"
    val duration: Int, // in minutes
    val exercises: List<String> = emptyList(), // List of exercise IDs
    val intensity: String = "medium", // "low", "medium", "high"
    val equipment: List<String> = emptyList(), // "barbell", "dumbbell", "bodyweight", etc.
    val targetMuscles: List<String> = emptyList(), // "chest", "back", "legs", etc.
    val caloriesBurned: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val performanceScore: Float? = null, // 0-10 rating
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledFor: Long? = null
)

@Entity(tableName = "workout_templates")
@TypeConverters(Converters::class)
data class WorkoutTemplate(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val duration: Int,
    val exercises: List<ExerciseTemplate>,
    val intensity: String,
    val equipment: List<String>,
    val targetMuscles: List<String>,
    val imageUrl: String? = null,
    val isCustom: Boolean = false,
    val difficulty: String = "intermediate" // "beginner", "intermediate", "advanced"
)

data class ExerciseTemplate(
    val exerciseId: String,
    val sets: Int,
    val reps: String, // "8-12" or "10" or "AMRAP"
    val restSeconds: Int = 90,
    val weight: Float? = null,
    val order: Int
)
