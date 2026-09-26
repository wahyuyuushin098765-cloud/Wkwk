package com.example.data.repository

import com.example.data.database.GameDao
import com.example.data.database.MatchHistoryEntity
import com.example.data.database.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameDao) {
    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val matchHistory: Flow<List<MatchHistoryEntity>> = dao.getMatchHistory()

    suspend fun saveProfile(profile: PlayerProfileEntity) {
        dao.savePlayerProfile(profile)
    }

    suspend fun recordMatch(
        result: String,
        alliesRemaining: Int,
        enemiesDefeated: Int,
        territoriesCaptured: Int,
        durationSeconds: Int,
        mode: String,
        currentProfile: PlayerProfileEntity?
    ) {
        val match = MatchHistoryEntity(
            result = result,
            alliesRemaining = alliesRemaining,
            enemiesDefeated = enemiesDefeated,
            territoriesCaptured = territoriesCaptured,
            durationSeconds = durationSeconds,
            mode = mode
        )
        dao.insertMatchHistory(match)

        val profile = currentProfile ?: PlayerProfileEntity()
        val isWin = result == "MENANG"
        val updated = profile.copy(
            totalWins = profile.totalWins + (if (isWin) 1 else 0),
            totalDefeats = profile.totalDefeats + (if (isWin) 0 else 1),
            totalTerritoriesCaptured = profile.totalTerritoriesCaptured + territoriesCaptured,
            highestAlliedSurviving = maxOf(profile.highestAlliedSurviving, alliesRemaining)
        )
        dao.savePlayerProfile(updated)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
