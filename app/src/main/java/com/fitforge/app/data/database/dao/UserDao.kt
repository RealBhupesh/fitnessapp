package com.fitforge.app.data.database.dao

import androidx.room.*
import com.fitforge.app.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET currentStreak = :streak WHERE id = :userId")
    suspend fun updateStreak(userId: String, streak: Int)

    @Query("UPDATE users SET totalWorkouts = totalWorkouts + 1 WHERE id = :userId")
    suspend fun incrementWorkoutCount(userId: String)

    @Query("UPDATE users SET xp = :xp, level = :level WHERE id = :userId")
    suspend fun updateXpAndLevel(userId: String, xp: Int, level: Int)
}
