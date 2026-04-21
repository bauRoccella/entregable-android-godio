package com.example.reactionchallenge.data.local.dao

import androidx.room.*
import com.example.reactionchallenge.data.local.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    /** Todas las sesiones de un jugador, ordenadas por score descendente. */
    @Query("""
        SELECT * FROM game_sessions
        WHERE playerName = :playerName
        ORDER BY totalScore DESC
    """)
    fun getSessionsByPlayer(playerName: String): Flow<List<GameSessionEntity>>

    /** Top N sesiones globales (ranking). */
    @Query("SELECT * FROM game_sessions ORDER BY totalScore DESC LIMIT :limit")
    fun getTopSessions(limit: Int = 10): Flow<List<GameSessionEntity>>

    /** Mejor puntuación de un jugador en una dificultad concreta. */
    @Query("""
        SELECT MAX(totalScore) FROM game_sessions
        WHERE playerName = :playerName AND difficulty = :difficulty
    """)
    suspend fun getBestScore(playerName: String, difficulty: String): Int?

    @Insert
    suspend fun insert(session: GameSessionEntity): Long

    @Query("DELETE FROM game_sessions WHERE playerName = :playerName")
    suspend fun deleteAllForPlayer(playerName: String)
}
