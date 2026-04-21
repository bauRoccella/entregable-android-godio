package com.example.reactionchallenge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una sesión de juego completada (o interrumpida por game-over).
 * No se persiste el modo TRAINING.
 */
@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String,
    val difficulty: String,          // Difficulty.name
    val totalScore: Int,
    val levelsCompleted: Int,
    val totalLevels: Int,
    val totalCorrect: Int,
    val totalAttempts: Int,
    val avgReactionTimeMs: Long,
    val won: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
