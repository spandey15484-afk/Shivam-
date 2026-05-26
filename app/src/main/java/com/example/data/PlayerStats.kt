package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "player_stats")
data class PlayerStats(
    @PrimaryKey val id: Int = 1,
    val currentLevel: Int = 1,
    val highScore: Int = 0,
    val totalStars: Int = 0,
    val totalSweetsMatched: Int = 0,
    val totalGamesPlayed: Int = 0,
    val isPremiumUnlocked: Boolean = false,
    val freeTrialsUsed: Int = 0
)

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    suspend fun getStats(): PlayerStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStats(stats: PlayerStats)
}
