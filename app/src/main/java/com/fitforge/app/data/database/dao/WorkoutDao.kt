package com.fitforge.app.data.database.dao

import androidx.room.*
import com.fitforge.app.data.model.Workout
import com.fitforge.app.data.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE userId = :userId AND isCompleted = 0 ORDER BY scheduledFor ASC")
    fun getUpcomingWorkouts(userId: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedWorkouts(userId: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkout(workoutId: String): Flow<Workout?>

    @Query("SELECT * FROM workout_templates WHERE category = :category OR :category = ''")
    fun getWorkoutTemplates(category: String = ""): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM workout_templates WHERE id = :templateId")
    suspend fun getWorkoutTemplate(templateId: String): WorkoutTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutTemplate(template: WorkoutTemplate)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND isCompleted = 1 AND completedAt >= :startTime")
    suspend fun getWorkoutCountSince(userId: String, startTime: Long): Int
}
