package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val generalName: String = "Jenderal",
    val avatarRes: Int = 0,
    val totalWins: Int = 0,
    val totalDefeats: Int = 0,
    val totalTerritoriesCaptured: Int = 0,
    val highestAlliedSurviving: Int = 0
)
