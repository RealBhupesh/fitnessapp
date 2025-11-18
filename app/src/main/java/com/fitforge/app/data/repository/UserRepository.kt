package com.fitforge.app.data.repository

import com.fitforge.app.data.database.dao.UserDao
import com.fitforge.app.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getUserFlow(userId: String): Flow<User?> = userDao.getUserFlow(userId)

    suspend fun getUser(userId: String): User? = userDao.getUser(userId)

    suspend fun insertUser(user: User) = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun updateStreak(userId: String, streak: Int) = userDao.updateStreak(userId, streak)

    suspend fun incrementWorkoutCount(userId: String) = userDao.incrementWorkoutCount(userId)

    suspend fun addXP(userId: String, xpToAdd: Int) {
        val user = userDao.getUser(userId) ?: return
        val newXP = user.xp + xpToAdd
        var newLevel = user.level
        var remainingXP = newXP

        // Simple level calculation: 1000 XP per level
        while (remainingXP >= 1000 * newLevel) {
            remainingXP -= 1000 * newLevel
            newLevel++
        }

        userDao.updateXpAndLevel(userId, remainingXP, newLevel)
    }
}
