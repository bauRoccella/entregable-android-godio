package com.example.reactionchallenge.domain.model

// ─────────────────────────────────────────────────────────────────────────────
// Constantes globales
// ─────────────────────────────────────────────────────────────────────────────
const val MAX_REACTION_TIME_SECONDS = 30
const val MIN_REACTION_TIME_SECONDS = 3
const val DEFAULT_ITERATIONS_PER_LEVEL = 20
const val LEVEL_PASS_THRESHOLD = 0.70f   // 70 % de aciertos para aprobar el nivel

// ─────────────────────────────────────────────────────────────────────────────
// Dificultad
// ─────────────────────────────────────────────────────────────────────────────
enum class Difficulty(
    val displayName: String,
    val defaultTimeSeconds: Int,
    val totalLevels: Int,
    val baseScore: Int,
    val timeReductionPerLevel: Int   // segundos que se restan por cada nivel
) {
    TRAINING("Entrenamiento", 20, 3, 0, 0),
    EASY    ("Fácil",         20, 3, 100, 1),
    MEDIUM  ("Medio",         15, 5, 150, 1),
    HARD    ("Difícil",       10, 7, 200, 1)
}

// ─────────────────────────────────────────────────────────────────────────────
// Tipos de estímulo
// ─────────────────────────────────────────────────────────────────────────────
enum class StimulusType(val displayName: String) {
    WORD  ("Palabra"),
    NUMBER("Número"),
    COLOR ("Color")
}

// ─────────────────────────────────────────────────────────────────────────────
// Paleta de colores para estímulos
// ─────────────────────────────────────────────────────────────────────────────
data class StimulusColor(val name: String, val colorHex: ULong)

val STIMULUS_COLORS = listOf(
    StimulusColor("ROJO",     0xFFE53935UL),
    StimulusColor("AZUL",     0xFF1E88E5UL),
    StimulusColor("VERDE",    0xFF43A047UL),
    StimulusColor("AMARILLO", 0xFFFDD835UL),
    StimulusColor("NARANJA",  0xFFFB8C00UL),
    StimulusColor("MORADO",   0xFF8E24AAUL),
    StimulusColor("ROSA",     0xFFE91E63UL),
    StimulusColor("CIAN",     0xFF00ACC1UL),
)

// ─────────────────────────────────────────────────────────────────────────────
// Estímulo
// ─────────────────────────────────────────────────────────────────────────────
data class Stimulus(
    val id: Long = System.currentTimeMillis(),
    val type: StimulusType,
    val textValue: String,
    val displayColor: StimulusColor? = null   // solo para StimulusType.COLOR
)

// ─────────────────────────────────────────────────────────────────────────────
// Reglas para el Modo Reacción Inversa
// ─────────────────────────────────────────────────────────────────────────────
enum class InverseRuleType(val description: String) {
    REACT_TO_ALL_EXCEPT_RED (
        "Reaccionar ante todos los colores excepto ROJO"
    ),
    REACT_TO_ALL_EXCEPT_BLUE(
        "Reaccionar ante todos los colores excepto AZUL"
    ),
    NO_REACT_TO_PRIME(
        "No reaccionar ante números primos (p. ej., 2, 3, 5, 7, 127…)"
    ),
    NO_REACT_TO_EVEN(
        "No reaccionar ante números pares"
    ),
    REACT_TO_WORDS_ONLY(
        "Reaccionar solo ante palabras; ignorar números y colores"
    ),
    NO_REACT_TO_COLORS(
        "Reaccionar ante palabras y números; ignorar colores"
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Configuración de partida
// ─────────────────────────────────────────────────────────────────────────────
data class GameConfig(
    val playerName: String,
    val difficulty: Difficulty,
    val iterationsPerLevel: Int = DEFAULT_ITERATIONS_PER_LEVEL,
    val customReactionTimeSeconds: Int? = null,
    val inverseReactionMode: Boolean = false,
    val inverseRuleType: InverseRuleType? = null
) {
    /** Tiempo de reacción base validado (nunca supera MAX_REACTION_TIME_SECONDS). */
    val reactionTimeSeconds: Int
        get() = (customReactionTimeSeconds ?: difficulty.defaultTimeSeconds)
            .coerceIn(MIN_REACTION_TIME_SECONDS, MAX_REACTION_TIME_SECONDS)

    /** Dificultad dinámica: cada nivel reduce el tiempo en timeReductionPerLevel. */
    fun reactionTimeForLevel(level: Int): Int {
        val reduction = difficulty.timeReductionPerLevel * (level - 1)
        return (reactionTimeSeconds - reduction).coerceAtLeast(MIN_REACTION_TIME_SECONDS)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Resultado de un único estímulo
// ─────────────────────────────────────────────────────────────────────────────
data class StimulusResult(
    val stimulus: Stimulus,
    val userReacted: Boolean,
    val reactionTimeMs: Long,        // 0 cuando no reaccionó (timeout)
    val isCorrect: Boolean,
    val expectedReaction: Boolean    // true = debía reaccionar / false = debía ignorar
)

// ─────────────────────────────────────────────────────────────────────────────
// Estadísticas por nivel
// ─────────────────────────────────────────────────────────────────────────────
data class LevelStats(
    val level: Int,
    val correct: Int,
    val total: Int,
    val avgReactionTimeMs: Long,
    val passed: Boolean
) {
    val accuracy: Float get() = if (total > 0) correct.toFloat() / total else 0f
}

// ─────────────────────────────────────────────────────────────────────────────
// Resultado final de la sesión
// ─────────────────────────────────────────────────────────────────────────────
data class GameSessionResult(
    val playerName: String,
    val difficulty: Difficulty,
    val totalScore: Int,
    val levelsCompleted: Int,
    val totalLevels: Int,
    val levelStats: List<LevelStats>,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalCorrect: Int   get() = levelStats.sumOf { it.correct }
    val totalAttempts: Int  get() = levelStats.sumOf { it.total }
    val overallAccuracy: Float
        get() = if (totalAttempts > 0) totalCorrect.toFloat() / totalAttempts else 0f
    val avgReactionTimeMs: Long
        get() = if (levelStats.isEmpty()) 0L
                else levelStats.map { it.avgReactionTimeMs }.average().toLong()
    val won: Boolean get() = levelsCompleted >= totalLevels
}
