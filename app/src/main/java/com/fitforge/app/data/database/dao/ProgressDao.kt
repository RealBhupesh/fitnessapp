package com.fitforge.app.data.database.dao

import androidx.room.*
import com.fitforge.app.data.model.Achievement
import com.fitforge.app.data.model.BodyMetric
import com.fitforge.app.data.model.Challenge
import com.fitforge.app.data.model.RecoveryMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM body_metrics WHERE userId = :userId ORDER BY timestamp DESC")
    fun getBodyMetrics(userId: String): Flow<List<BodyMetric>>

    @Query("SELECT * FROM body_metrics WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBodyMetric(userId: String): Flow<BodyMetric?>

    @Query("SELECT * FROM recovery_metrics WHERE userId = :userId AND date = :date")
    fun getRecoveryMetric(userId: String, date: String): Flow<RecoveryMetric?>

    @Query("SELECT * FROM recovery_metrics WHERE userId = :userId ORDER BY timestamp DESC LIMIT :days")
    fun getRecentRecoveryMetrics(userId: String, days: Int = 7): Flow<List<RecoveryMetric>>

    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievements(userId: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND unlockedAt IS NOT NULL")
    fun getUnlockedAchievements(userId: String): Flow<List<Achievement>>

    @Query("SELECT * FROM challenges WHERE isActive = 1")
    fun getActiveChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMetric(metric: BodyMetric)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoveryMetric(metric: RecoveryMetric)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Update
    suspend fun updateChallenge(challenge: Challenge)
}
