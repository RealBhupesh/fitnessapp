package com.fitforge.app.data.repository

import com.fitforge.app.data.database.dao.WorkoutDao
import com.fitforge.app.data.database.dao.ExerciseDao
import com.fitforge.app.data.database.dao.UserDao
import com.fitforge.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Comprehensive Repository for workout-related data operations.
 * Implements advanced analytics, progressive overload tracking, and performance insights.
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val userDao: UserDao
) {
    // ============================================================================
    // BASIC WORKOUT OPERATIONS
    // ============================================================================

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

    suspend fun getWorkoutCountSince(userId: String, startTime: Long): Int =
        workoutDao.getWorkoutCountSince(userId, startTime)

    // ============================================================================
    // ADVANCED WORKOUT COMPLETION & TRACKING
    // ============================================================================

    /**
     * Complete a workout with full performance tracking and user progression updates
     */
    suspend fun completeWorkout(
        workoutId: String,
        performanceScore: Float,
        duration: Int,
        totalVolume: Float,
        caloriesBurned: Int,
        notes: String? = null
    ) {
        // Get the workout from Flow
        val workout = workoutDao.getWorkout(workoutId).first() ?: return

        // Update workout with completion data
        val completedWorkout = workout.copy(
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )
        workoutDao.updateWorkout(completedWorkout)

        // Update user stats
        val user = userDao.getUser(workout.userId) ?: return
        updateUserStreak(user)
        awardWorkoutXP(user, performanceScore, duration, totalVolume)

        // Track workout analytics
        trackWorkoutAnalytics(workout.userId, duration, totalVolume, caloriesBurned)
    }

    /**
     * Get today's recommended workout based on recovery and schedule
     */
    fun getTodaysWorkout(userId: String): Flow<Workout?> {
        return workoutDao.getUpcomingWorkouts(userId)
            .map { workouts ->
                val today = LocalDate.now()
                workouts.firstOrNull { workout ->
                    // Check if scheduled for today
                    val scheduledDate = LocalDate.ofEpochDay(workout.scheduledDate / (24 * 60 * 60 * 1000))
                    scheduledDate == today
                }
            }
    }

    /**
     * Get workouts in a date range for analytics
     */
    fun getWorkoutsInRange(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Workout>> {
        return workoutDao.getCompletedWorkouts(userId)
            .map { workouts ->
                workouts.filter { workout ->
                    val workoutDate = LocalDateTime.ofEpochSecond(
                        workout.completedAt / 1000,
                        0,
                        java.time.ZoneOffset.UTC
                    )
                    workoutDate.isAfter(startDate) && workoutDate.isBefore(endDate)
                }
            }
    }

    // ============================================================================
    // EXERCISE OPERATIONS & TRACKING
    // ============================================================================

    /**
     * Get all exercises with optional filtering
     */
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    /**
     * Search exercises by query (name, muscle group, equipment)
     */
    fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises()
            .map { exercises ->
                if (query.isBlank()) return@map exercises

                exercises.filter { exercise ->
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.primaryMuscles.any { it.contains(query, ignoreCase = true) } ||
                    exercise.secondaryMuscles.any { it.contains(query, ignoreCase = true) } ||
                    exercise.equipment.contains(query, ignoreCase = true)
                }
            }
    }

    /**
     * Get exercises by category with intelligent sorting
     */
    fun getExercisesByCategory(category: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByCategory(category)
            .map { exercises ->
                // Sort by frequency of use (would need usage tracking)
                exercises.sortedBy { it.name }
            }
    }

    /**
     * Get exercises targeting specific muscle group
     */
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises()
            .map { exercises ->
                exercises.filter { exercise ->
                    exercise.primaryMuscles.contains(muscleGroup) ||
                    exercise.secondaryMuscles.contains(muscleGroup)
                }
            }
    }

    /**
     * Add custom user-created exercise
     */
    suspend fun addCustomExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
    }

    // ============================================================================
    // EXERCISE HISTORY & PERFORMANCE ANALYTICS
    // ============================================================================

    /**
     * Get complete exercise history with all metrics
     */
    fun getExerciseHistory(exerciseId: String): Flow<ExerciseHistory?> {
        return exerciseDao.getExerciseHistory(exerciseId)
    }

    /**
     * Get exercise sets for detailed analysis
     */
    fun getExerciseSets(exerciseId: String, limit: Int = 50): Flow<List<ExerciseSet>> {
        return exerciseDao.getExerciseSets(exerciseId)
            .map { sets -> sets.take(limit) }
    }

    /**
     * Get personal records for an exercise
     */
    suspend fun getPersonalRecords(exerciseId: String): PersonalRecords {
        val history = exerciseDao.getExerciseHistory(exerciseId).first()

        return PersonalRecords(
            exerciseId = exerciseId,
            maxWeight = history?.maxWeight ?: 0f,
            maxReps = history?.maxReps ?: 0,
            maxVolume = history?.totalVolume ?: 0f,
            estimated1RM = calculate1RM(history?.maxWeight ?: 0f, history?.maxReps ?: 0),
            lastPerformed = history?.lastPerformed ?: System.currentTimeMillis(),
            totalSets = history?.totalSets ?: 0
        )
    }

    /**
     * Track a completed exercise set with full metrics
     */
    suspend fun logSet(
        exerciseId: String,
        workoutId: String,
        weight: Float,
        reps: Int,
        formScore: Int = 0,
        rpe: Int = 0,
        notes: String? = null
    ) {
        val set = ExerciseSet(
            id = generateId(),
            exerciseId = exerciseId,
            workoutId = workoutId,
            weight = weight,
            reps = reps,
            formScore = formScore,
            rpe = rpe,
            notes = notes,
            completedAt = System.currentTimeMillis()
        )

        exerciseDao.insertSet(set)

        // Update exercise history
        updateExerciseHistory(exerciseId)
    }

    /**
     * Update exercise history with aggregated stats
     */
    private suspend fun updateExerciseHistory(exerciseId: String) {
        val sets = exerciseDao.getExerciseSets(exerciseId).first()

        if (sets.isEmpty()) return

        val maxWeight = sets.maxOfOrNull { it.weight } ?: 0f
        val maxReps = sets.maxOfOrNull { it.reps } ?: 0
        val totalVolume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        val totalSets = sets.size
        val lastPerformed = sets.maxOfOrNull { it.completedAt } ?: System.currentTimeMillis()

        val history = ExerciseHistory(
            exerciseId = exerciseId,
            totalSets = totalSets,
            totalReps = sets.sumOf { it.reps },
            totalVolume = totalVolume,
            maxWeight = maxWeight,
            maxReps = maxReps,
            lastPerformed = lastPerformed
        )

        exerciseDao.insertHistory(history)
    }

    // ============================================================================
    // PROGRESSIVE OVERLOAD & STRENGTH ANALYTICS
    // ============================================================================

    /**
     * Get volume progression over time for graphing
     */
    fun getVolumeProgression(
        exerciseId: String,
        days: Int = 90
    ): Flow<List<VolumeDataPoint>> {
        val cutoffDate = LocalDate.now().minusDays(days.toLong())

        return exerciseDao.getExerciseSets(exerciseId)
            .map { sets ->
                sets.filter { set ->
                    val setDate = LocalDate.ofEpochDay(set.completedAt / (24 * 60 * 60 * 1000))
                    setDate.isAfter(cutoffDate)
                }
                .groupBy { set ->
                    LocalDate.ofEpochDay(set.completedAt / (24 * 60 * 60 * 1000))
                }
                .map { (date, dateSets) ->
                    VolumeDataPoint(
                        date = date,
                        volume = dateSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat(),
                        sets = dateSets.size,
                        avgWeight = dateSets.map { it.weight }.average().toFloat(),
                        avgReps = dateSets.map { it.reps }.average().toFloat(),
                        maxWeight = dateSets.maxOf { it.weight }
                    )
                }
                .sortedBy { it.date }
            }
    }

    /**
     * Get strength progression (estimated 1RM over time)
     */
    fun getStrengthProgression(
        exerciseId: String,
        days: Int = 90
    ): Flow<List<StrengthDataPoint>> {
        return getVolumeProgression(exerciseId, days)
            .map { volumePoints ->
                volumePoints.map { point ->
                    StrengthDataPoint(
                        date = point.date,
                        estimated1RM = calculate1RM(point.maxWeight, point.avgReps.toInt()),
                        workingWeight = point.avgWeight,
                        volume = point.volume
                    )
                }
            }
    }

    /**
     * Calculate volume load for a workout
     */
    suspend fun calculateWorkoutVolume(workoutId: String): Float {
        val sets = exerciseDao.getExerciseSets("").first()
            .filter { it.workoutId == workoutId }

        return sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
    }

    // ============================================================================
    // WORKOUT ANALYTICS & INSIGHTS
    // ============================================================================

    /**
     * Get weekly summary with comprehensive metrics
     */
    fun getWeeklySummary(userId: String): Flow<WeeklySummary> {
        val startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
        val endOfWeek = startOfWeek.plusDays(7)

        return workoutDao.getCompletedWorkouts(userId)
            .map { allWorkouts ->
                val weekWorkouts = allWorkouts.filter { workout ->
                    val workoutDate = LocalDate.ofEpochDay(workout.completedAt / (24 * 60 * 60 * 1000))
                    !workoutDate.isBefore(startOfWeek) && workoutDate.isBefore(endOfWeek)
                }

                WeeklySummary(
                    workoutsCompleted = weekWorkouts.size,
                    totalDuration = weekWorkouts.sumOf { it.duration },
                    totalVolume = weekWorkouts.sumOf { it.totalVolume.toDouble() }.toFloat(),
                    avgDuration = if (weekWorkouts.isNotEmpty()) weekWorkouts.map { it.duration }.average().toInt() else 0,
                    muscleGroupsWorked = weekWorkouts.flatMap { it.exercises }.distinct().size,
                    totalSets = weekWorkouts.sumOf { it.totalSets }
                )
            }
    }

    /**
     * Get monthly summary with detailed analytics
     */
    fun getMonthlySummary(userId: String, month: Int, year: Int): Flow<MonthlySummary> {
        return workoutDao.getCompletedWorkouts(userId)
            .combine(exerciseDao.getAllExercises()) { workouts, exercises ->
                val monthWorkouts = workouts.filter { workout ->
                    val workoutDate = LocalDate.ofEpochDay(workout.completedAt / (24 * 60 * 60 * 1000))
                    workoutDate.monthValue == month && workoutDate.year == year
                }

                val daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth()
                val workoutDays = monthWorkouts.map {
                    LocalDate.ofEpochDay(it.completedAt / (24 * 60 * 60 * 1000))
                }.distinct().size

                MonthlySummary(
                    month = month,
                    year = year,
                    workoutsCompleted = monthWorkouts.size,
                    totalDuration = monthWorkouts.sumOf { it.duration },
                    totalVolume = monthWorkouts.sumOf { it.totalVolume.toDouble() }.toFloat(),
                    uniqueExercises = monthWorkouts.flatMap { it.exercises }.distinct().size,
                    consistency = (workoutDays.toFloat() / daysInMonth) * 100,
                    topExercises = getTopExercises(monthWorkouts, 5)
                )
            }
    }

    /**
     * Get workout frequency data for charting
     */
    fun getWorkoutFrequency(
        userId: String,
        weeks: Int = 12
    ): Flow<List<FrequencyDataPoint>> {
        val startDate = LocalDate.now().minusWeeks(weeks.toLong())

        return workoutDao.getCompletedWorkouts(userId)
            .map { workouts ->
                workouts.filter { workout ->
                    val workoutDate = LocalDate.ofEpochDay(workout.completedAt / (24 * 60 * 60 * 1000))
                    !workoutDate.isBefore(startDate)
                }
                .groupBy { workout ->
                    val workoutDate = LocalDate.ofEpochDay(workout.completedAt / (24 * 60 * 60 * 1000))
                    // Get Monday of the week
                    workoutDate.minusDays((workoutDate.dayOfWeek.value - 1).toLong())
                }
                .map { (weekStart, weekWorkouts) ->
                    FrequencyDataPoint(
                        weekStart = weekStart,
                        workoutsCompleted = weekWorkouts.size,
                        totalDuration = weekWorkouts.sumOf { it.duration },
                        totalVolume = weekWorkouts.sumOf { it.totalVolume.toDouble() }.toFloat(),
                        avgDuration = weekWorkouts.map { it.duration }.average().toInt()
                    )
                }
                .sortedBy { it.weekStart }
            }
    }

    /**
     * Get muscle group distribution for the current week
     */
    fun getMuscleGroupDistribution(userId: String): Flow<Map<String, Int>> {
        val startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY)

        return workoutDao.getCompletedWorkouts(userId)
            .combine(exerciseDao.getAllExercises()) { workouts, exercises ->
                val weekWorkouts = workouts.filter { workout ->
                    val workoutDate = LocalDate.ofEpochDay(workout.completedAt / (24 * 60 * 60 * 1000))
                    !workoutDate.isBefore(startOfWeek)
                }

                val muscleGroupCount = mutableMapOf<String, Int>()

                weekWorkouts.forEach { workout ->
                    workout.exercises.forEach { exerciseId ->
                        val exercise = exercises.find { it.id == exerciseId }
                        exercise?.primaryMuscles?.forEach { muscle ->
                            muscleGroupCount[muscle] = (muscleGroupCount[muscle] ?: 0) + 1
                        }
                    }
                }

                muscleGroupCount
            }
    }

    // ============================================================================
    // USER PROGRESSION & GAMIFICATION
    // ============================================================================

    /**
     * Update user workout streak
     */
    private suspend fun updateUserStreak(user: User) {
        val today = LocalDate.now()
        val lastWorkout = user.lastWorkoutDate?.let {
            LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
        }

        val newStreak = when {
            lastWorkout == null -> 1
            lastWorkout == today -> user.currentStreak // Same day, maintain streak
            lastWorkout == today.minusDays(1) -> user.currentStreak + 1 // Yesterday, increment
            else -> 1 // Streak broken, reset
        }

        val updatedUser = user.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(user.longestStreak, newStreak),
            lastWorkoutDate = System.currentTimeMillis()
        )

        userDao.updateUser(updatedUser)
        userDao.updateStreak(user.id, newStreak)
    }

    /**
     * Award XP based on workout performance
     */
    private suspend fun awardWorkoutXP(
        user: User,
        performanceScore: Float,
        duration: Int,
        volume: Float
    ) {
        val baseXP = 100
        val durationBonus = (duration / 60) * 10 // 10 XP per minute
        val performanceBonus = (performanceScore * 50).toInt()
        val volumeBonus = (volume / 1000 * 25).toInt() // 25 XP per 1000 lbs

        val totalXP = baseXP + durationBonus + performanceBonus + volumeBonus

        val newXP = user.xp + totalXP
        val newLevel = calculateLevel(newXP)

        userDao.updateXpAndLevel(user.id, newXP, newLevel)
    }

    /**
     * Track workout analytics for insights
     */
    private suspend fun trackWorkoutAnalytics(
        userId: String,
        duration: Int,
        volume: Float,
        calories: Int
    ) {
        // This would save to a separate analytics table for ML insights
        // For now, we're just updating the user's stats
        userDao.incrementWorkoutCount(userId)
    }

    // ============================================================================
    // HELPER FUNCTIONS
    // ============================================================================

    /**
     * Calculate estimated 1RM using Epley formula
     */
    private fun calculate1RM(weight: Float, reps: Int): Float {
        if (reps <= 0 || weight <= 0) return 0f
        if (reps == 1) return weight
        return weight * (1 + reps / 30f)
    }

    /**
     * Calculate user level from XP (exponential curve)
     */
    private fun calculateLevel(xp: Int): Int {
        return (sqrt(xp / 100.0)).toInt() + 1
    }

    /**
     * Get most performed exercises
     */
    private fun getTopExercises(workouts: List<Workout>, limit: Int): List<TopExercise> {
        return workouts
            .flatMap { it.exercises }
            .groupBy { it }
            .map { (exerciseId, occurrences) ->
                TopExercise(
                    exerciseId = exerciseId,
                    timesPerformed = occurrences.size
                )
            }
            .sortedByDescending { it.timesPerformed }
            .take(limit)
    }

    /**
     * Generate unique ID for entities
     */
    private fun generateId(): String {
        return "${System.currentTimeMillis()}-${(Math.random() * 10000).toInt()}"
    }
}

// ============================================================================
// DATA CLASSES FOR ANALYTICS
// ============================================================================

data class PersonalRecords(
    val exerciseId: String,
    val maxWeight: Float,
    val maxReps: Int,
    val maxVolume: Float,
    val estimated1RM: Float,
    val lastPerformed: Long,
    val totalSets: Int
)

data class VolumeDataPoint(
    val date: LocalDate,
    val volume: Float,
    val sets: Int,
    val avgWeight: Float,
    val avgReps: Float,
    val maxWeight: Float
)

data class StrengthDataPoint(
    val date: LocalDate,
    val estimated1RM: Float,
    val workingWeight: Float,
    val volume: Float
)

data class WeeklySummary(
    val workoutsCompleted: Int,
    val totalDuration: Int,
    val totalVolume: Float,
    val avgDuration: Int,
    val muscleGroupsWorked: Int,
    val totalSets: Int
)

data class MonthlySummary(
    val month: Int,
    val year: Int,
    val workoutsCompleted: Int,
    val totalDuration: Int,
    val totalVolume: Float,
    val uniqueExercises: Int,
    val consistency: Float,
    val topExercises: List<TopExercise>
)

data class TopExercise(
    val exerciseId: String,
    val timesPerformed: Int
)

data class FrequencyDataPoint(
    val weekStart: LocalDate,
    val workoutsCompleted: Int,
    val totalDuration: Int,
    val totalVolume: Float,
    val avgDuration: Int
)
