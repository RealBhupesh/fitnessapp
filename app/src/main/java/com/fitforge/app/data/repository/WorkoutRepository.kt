package com.fitforge.app.data.repository

import com.fitforge.app.data.database.dao.WorkoutDao
import com.fitforge.app.data.model.Workout
import com.fitforge.app.data.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {
    fun getUpcomingWorkouts(userId: String): Flow<List<Workout>> =
        workoutDao.getUpcomingWorkouts(userId)

    fun getCompletedWorkouts(userId: String): Flow<List<Workout>> =
        workoutDao.getCompletedWorkouts(userId)

    fun getWorkout(workoutId: String): Flow<Workout?> =
        workoutDao.getWorkout(workoutId)

    fun getWorkoutTemplates(category: String = ""): Flow<List<WorkoutTemplate>> =
        workoutDao.getWorkoutTemplates(category)

    suspend fun getWorkoutTemplate(templateId: String): WorkoutTemplate? =
        workoutDao.getWorkoutTemplate(templateId)

    suspend fun insertWorkout(workout: Workout) = workoutDao.insertWorkout(workout)

    suspend fun insertWorkoutTemplate(template: WorkoutTemplate) =
        workoutDao.insertWorkoutTemplate(template)

    suspend fun updateWorkout(workout: Workout) = workoutDao.updateWorkout(workout)

    suspend fun deleteWorkout(workout: Workout) = workoutDao.deleteWorkout(workout)

    suspend fun completeWorkout(workoutId: String, performanceScore: Float) {
        val workout = workoutDao.getWorkout(workoutId)
        // Update workout completion status
    }

    suspend fun getWorkoutCountSince(userId: String, startTime: Long): Int =
        workoutDao.getWorkoutCountSince(userId, startTime)
}
