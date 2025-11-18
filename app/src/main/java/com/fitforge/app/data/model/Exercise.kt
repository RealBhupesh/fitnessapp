package com.fitforge.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitforge.app.data.database.Converters

@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val category: String, // "strength", "cardio", "flexibility"
    val equipment: String = "bodyweight", // "barbell", "dumbbell", "machine", etc.
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val difficulty: String = "intermediate",
    val instructions: List<String> = emptyList(),
    val videoUrl: String? = null,
    val gifUrl: String? = null,
    val thumbnailUrl: String? = null,
    val formTips: List<String> = emptyList()
)

@Entity(tableName = "exercise_sets")
data class ExerciseSet(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Float = 0f,
    val restSeconds: Int = 90,
    val tempo: String? = null, // "2-0-2" format
    val formScore: Int? = null, // 0-100
    val rpe: Int? = null, // Rate of Perceived Exertion 1-10
    val notes: String = "",
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercise_history")
data class ExerciseHistory(
    @PrimaryKey val id: String,
    val userId: String,
    val exerciseId: String,
    val workoutId: String,
    val sets: Int,
    val avgReps: Float,
    val maxWeight: Float,
    val totalVolume: Float, // sets * reps * weight
    val avgFormScore: Float? = null,
    val personalRecord: Boolean = false,
    val completedAt: Long = System.currentTimeMillis()
)
