package com.fitforge.app.data.database.dao

import androidx.room.*
import com.fitforge.app.data.model.DailyNutrition
import com.fitforge.app.data.model.Meal
import com.fitforge.app.data.model.NutritionGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM meals WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getMealsByDate(userId: String, date: String): Flow<List<Meal>>

    @Query("SELECT * FROM nutrition_goals WHERE userId = :userId")
    fun getNutritionGoal(userId: String): Flow<NutritionGoal?>

    @Query("SELECT * FROM daily_nutrition WHERE userId = :userId AND date = :date")
    fun getDailyNutrition(userId: String, date: String): Flow<DailyNutrition?>

    @Query("SELECT * FROM daily_nutrition WHERE userId = :userId ORDER BY date DESC LIMIT :days")
    fun getRecentDailyNutrition(userId: String, days: Int = 7): Flow<List<DailyNutrition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionGoal(goal: NutritionGoal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyNutrition(dailyNutrition: DailyNutrition)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Update
    suspend fun updateDailyNutrition(dailyNutrition: DailyNutrition)

    @Delete
    suspend fun deleteMeal(meal: Meal)
}
