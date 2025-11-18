package com.fitforge.app.data.database.dao

import androidx.room.*
import com.fitforge.app.data.model.Exercise
import com.fitforge.app.data.model.ExerciseHistory
import com.fitforge.app.data.model.ExerciseSet
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE category = :category OR :category = ''")
    fun getExercises(category: String = ""): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExercise(exerciseId: String): Exercise?

    @Query("SELECT * FROM exercise_sets WHERE workoutId = :workoutId ORDER BY setNumber ASC")
    fun getExerciseSets(workoutId: String): Flow<List<ExerciseSet>>

    @Query("SELECT * FROM exercise_history WHERE userId = :userId AND exerciseId = :exerciseId ORDER BY completedAt DESC LIMIT :limit")
    fun getExerciseHistory(userId: String, exerciseId: String, limit: Int = 10): Flow<List<ExerciseHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSet(set: ExerciseSet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseHistory(history: ExerciseHistory)

    @Update
    suspend fun updateExerciseSet(set: ExerciseSet)

    @Delete
    suspend fun deleteExerciseSet(set: ExerciseSet)
}
