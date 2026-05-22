package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "typing_sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wpm: Double,
    val accuracy: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String // "PRACTICE", "LEVEL_X", "GAME_FALLING", "GAME_SPRINT"
)
