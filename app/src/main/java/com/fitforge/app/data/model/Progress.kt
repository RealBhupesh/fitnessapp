package com.fitforge.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fitforge.app.data.database.Converters

@Entity(tableName = "body_metrics")
data class BodyMetric(
    @PrimaryKey val id: String,
    val userId: String,
    val weight: Float,
    val bodyFatPercentage: Float? = null,
    val muscleMass: Float? = null,
    val measurements: Map<String, Float> = emptyMap(), // "chest", "waist", "arms", etc.
    val photoUrls: List<String> = emptyList(),
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String // "YYYY-MM-DD"
)

@Entity(tableName = "recovery_metrics")
data class RecoveryMetric(
    @PrimaryKey val id: String,
    val userId: String,
    val date: String, // "YYYY-MM-DD"
    val recoveryScore: Int, // 0-100
    val hrv: Int? = null, // Heart Rate Variability in ms
    val restingHeartRate: Int? = null,
    val sleepHours: Float? = null,
    val sleepQuality: Int? = null, // 0-100
    val deepSleepHours: Float? = null,
    val remSleepHours: Float? = null,
    val soreness: Map<String, Int> = emptyMap(), // body part to soreness level 0-10
    val stressLevel: Int? = null, // 0-100
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
@TypeConverters(Converters::class)
data class Achievement(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val category: String, // "strength", "consistency", "nutrition", "milestone"
    val badgeIcon: String,
    val xpReward: Int,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val requirement: Int = 100
)

@Entity(tableName = "challenges")
@TypeConverters(Converters::class)
data class Challenge(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val type: String, // "workout_count", "protein_days", "streak", etc.
    val requirement: Int,
    val progress: Int = 0,
    val xpReward: Int,
    val badgeId: String? = null,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true
)
