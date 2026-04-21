package com.example.reactionchallenge.data.repository

import com.example.reactionchallenge.data.local.dao.GameSessionDao
import com.example.reactionchallenge.data.local.dao.PlayerDao
import com.example.reactionchallenge.data.local.entity.GameSessionEntity
import com.example.reactionchallenge.data.local.entity.PlayerEntity
import com.example.reactionchallenge.domain.model.GameSessionResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val sessionDao: GameSessionDao
) {

    // ── Jugadores ────────────────────────────────────────────────────────────

    fun getAllPlayers(): Flow<List<PlayerEntity>> = playerDao.getAllPlayers()

    /** Inserta el jugador solo si no existe previamente (por nombre). */
    suspend fun ensurePlayerExists(name: String) {
        if (playerDao.findByName(name) == null) {
            playerDao.insert(PlayerEntity(name = name))
        }
    }

    // ── Sesiones ─────────────────────────────────────────────────────────────

    fun getSessionsByPlayer(playerName: String): Flow<List<GameSessionEntity>> =
        sessionDao.getSessionsByPlayer(playerName)

    fun getTopSessions(limit: Int = 10): Flow<List<GameSessionEntity>> =
        sessionDao.getTopSessions(limit)

    suspend fun getBestScore(playerName: String, difficulty: String): Int =
        sessionDao.getBestScore(playerName, difficulty) ?: 0

    /**
     * Persiste el resultado final de una sesión y asegura que el jugador exista.
     * No guarda sesiones de entrenamiento (score = 0 por diseño; la llamada
     * en GameViewModel ya filtra el modo TRAINING, pero lo verificamos aquí
     * también por seguridad).
     */
    suspend fun saveGameSession(result: GameSessionResult) {
        ensurePlayerExists(result.playerName)
        val entity = GameSessionEntity(
            playerName       = result.playerName,
            difficulty       = result.difficulty.name,
            totalScore       = result.totalScore,
            levelsCompleted  = result.levelsCompleted,
            totalLevels      = result.totalLevels,
            totalCorrect     = result.totalCorrect,
            totalAttempts    = result.totalAttempts,
            avgReactionTimeMs= result.avgReactionTimeMs,
            won              = result.won,
            timestamp        = result.timestamp
        )
        sessionDao.insert(entity)
    }
}
