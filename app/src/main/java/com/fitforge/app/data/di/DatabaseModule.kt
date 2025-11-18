package com.fitforge.app.data.di

import android.content.Context
import androidx.room.Room
import com.fitforge.app.data.database.FitForgeDatabase
import com.fitforge.app.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitForgeDatabase {
        return Room.databaseBuilder(
            context,
            FitForgeDatabase::class.java,
            "fitforge_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: FitForgeDatabase): UserDao = database.userDao()

    @Provides
    fun provideWorkoutDao(database: FitForgeDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideExerciseDao(database: FitForgeDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideNutritionDao(database: FitForgeDatabase): NutritionDao = database.nutritionDao()

    @Provides
    fun provideProgressDao(database: FitForgeDatabase): ProgressDao = database.progressDao()
}
