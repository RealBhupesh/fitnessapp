package com.fitforge.app.data.repository

import com.fitforge.app.data.database.dao.NutritionDao
import com.fitforge.app.data.model.DailyNutrition
import com.fitforge.app.data.model.Meal
import com.fitforge.app.data.model.NutritionGoal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionRepository @Inject constructor(
    private val nutritionDao: NutritionDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getMealsByDate(userId: String, date: String): Flow<List<Meal>> =
        nutritionDao.getMealsByDate(userId, date)

    fun getTodaysMeals(userId: String): Flow<List<Meal>> {
        val today = LocalDate.now().format(dateFormatter)
        return nutritionDao.getMealsByDate(userId, today)
    }

    fun getNutritionGoal(userId: String): Flow<NutritionGoal?> =
        nutritionDao.getNutritionGoal(userId)

    fun getDailyNutrition(userId: String, date: String): Flow<DailyNutrition?> =
        nutritionDao.getDailyNutrition(userId, date)

    fun getTodaysDailyNutrition(userId: String): Flow<DailyNutrition?> {
        val today = LocalDate.now().format(dateFormatter)
        return nutritionDao.getDailyNutrition(userId, today)
    }

    fun getRecentDailyNutrition(userId: String, days: Int = 7): Flow<List<DailyNutrition>> =
        nutritionDao.getRecentDailyNutrition(userId, days)

    suspend fun insertMeal(meal: Meal) {
        nutritionDao.insertMeal(meal)
        updateDailyNutrition(meal.userId, meal.date)
    }

    suspend fun updateMeal(meal: Meal) {
        nutritionDao.updateMeal(meal)
        updateDailyNutrition(meal.userId, meal.date)
    }

    suspend fun deleteMeal(meal: Meal) {
        nutritionDao.deleteMeal(meal)
        updateDailyNutrition(meal.userId, meal.date)
    }

    suspend fun insertNutritionGoal(goal: NutritionGoal) =
        nutritionDao.insertNutritionGoal(goal)

    private suspend fun updateDailyNutrition(userId: String, date: String) {
        // Recalculate daily nutrition totals
        // This would aggregate all meals for the day
    }
}
