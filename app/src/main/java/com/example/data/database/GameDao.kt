package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM match_history ORDER BY dateEpoch DESC LIMIT 50")
    fun getMatchHistory(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchHistory(match: MatchHistoryEntity)

    @Query("DELETE FROM match_history")
    suspend fun clearHistory()
}
