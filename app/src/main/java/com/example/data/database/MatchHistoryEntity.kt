package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpoch: Long = System.currentTimeMillis(),
    val result: String, // "MENANG" or "KALAH"
    val alliesRemaining: Int,
    val enemiesDefeated: Int,
    val territoriesCaptured: Int,
    val durationSeconds: Int,
    val mode: String = "Solo" // "Solo" or "Multiplayer"
)
