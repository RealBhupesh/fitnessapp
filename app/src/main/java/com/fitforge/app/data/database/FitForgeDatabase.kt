package com.fitforge.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fitforge.app.data.database.dao.*
import com.fitforge.app.data.model.*

@Database(
    entities = [
        User::class,
        Workout::class,
        WorkoutTemplate::class,
        Exercise::class,
        ExerciseSet::class,
        ExerciseHistory::class,
        Meal::class,
        NutritionGoal::class,
        DailyNutrition::class,
        BodyMetric::class,
        RecoveryMetric::class,
        Achievement::class,
        Challenge::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitForgeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun progressDao(): ProgressDao
}
