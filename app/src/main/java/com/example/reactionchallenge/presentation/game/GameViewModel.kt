package com.example.reactionchallenge.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reactionchallenge.data.repository.GameRepository
import com.example.reactionchallenge.domain.logic.*
import com.example.reactionchallenge.domain.model.*
import com.example.reactionchallenge.util.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// Estado de la UI
// ─────────────────────────────────────────────────────────────────────────────

enum class GamePhase {
    IDLE,               // Esperando initGame()
    INTER_STIMULUS,     // Pausa breve entre estímulos
    SHOWING_STIMULUS,   // Estímulo visible + timer activo
    LEVEL_TRANSITION,   // Nivel completado (pasado o fallado)
    GAME_OVER,
    GAME_WON
}

enum class FeedbackType { CORRECT, INCORRECT, TIMEOUT }

data class GameUiState(
    val phase: GamePhase = GamePhase.IDLE,
    val currentLevel: Int = 1,
    val totalLevels: Int = 3,
    val currentIteration: Int = 0,
    val iterationsPerLevel: Int = DEFAULT_ITERATIONS_PER_LEVEL,
    val currentStimulus: Stimulus? = null,
    val timerProgress: Float = 1f,
    val remainingMs: Long = 0L,
    val score: Int = 0,
    val feedback: FeedbackType? = null,
    val inverseRule: InverseRuleType? = null,
    val levelStats: List<LevelStats> = emptyList(),
    val correctInLevel: Int = 0,
    val incorrectInLevel: Int = 0,
    // Modo normal de dos opciones: lista vacía = modo inverso (botón único)
    val options: List<Stimulus> = emptyList(),
    val correctOptionIndex: Int = 0
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val stimulusGenerator: StimulusGenerator,
    private val validator: InverseReactionValidator,
    private val sound: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Resultado final emitido una sola vez al terminar la partida
    private val _sessionResult = MutableSharedFlow<GameSessionResult>(replay = 1)
    val sessionResult: SharedFlow<GameSessionResult> = _sessionResult.asSharedFlow()

    private lateinit var config: GameConfig
    private var timer: GameTimer? = null
    private var timerObserverJob: Job? = null
    private val levelResults = mutableListOf<StimulusResult>()
    private val allLevelStats = mutableListOf<LevelStats>()

    // ── Inicialización ────────────────────────────────────────────────────────

    fun initGame(cfg: GameConfig) {
        config = cfg
        levelResults.clear()
        allLevelStats.clear()
        _uiState.value = GameUiState(
            totalLevels       = cfg.difficulty.totalLevels,
            iterationsPerLevel= cfg.iterationsPerLevel,
            inverseRule       = if (cfg.inverseReactionMode) cfg.inverseRuleType else null
        )
        scheduleNextStimulus()
    }

    // ── Flujo principal ───────────────────────────────────────────────────────

    private fun scheduleNextStimulus() {
        val state = _uiState.value
        if (state.currentIteration >= state.iterationsPerLevel) {
            evaluateLevel()
            return
        }
        _uiState.update { it.copy(phase = GamePhase.INTER_STIMULUS, currentStimulus = null, feedback = null, options = emptyList()) }
        viewModelScope.launch {
            delay(750)
            showStimulus()
        }
    }

    private fun showStimulus() {
        val level = _uiState.value.currentLevel
        val stimulus = stimulusGenerator.generate(level = level)
        val timeMs = config.reactionTimeForLevel(level) * 1000L

        val (options, correctIdx) = if (!config.inverseReactionMode) {
            val distractor = stimulusGenerator.generateDistractor(stimulus, level)
            val correctIndex = if (kotlin.random.Random.nextBoolean()) 0 else 1
            val opts = if (correctIndex == 0) listOf(stimulus, distractor) else listOf(distractor, stimulus)
            opts to correctIndex
        } else {
            emptyList<Stimulus>() to 0
        }

        _uiState.update {
            it.copy(
                phase              = GamePhase.SHOWING_STIMULUS,
                currentStimulus    = stimulus,
                currentIteration   = it.currentIteration + 1,
                timerProgress      = 1f,
                remainingMs        = timeMs,
                options            = options,
                correctOptionIndex = correctIdx
            )
        }

        // Arrancar temporizador
        timer?.reset()
        val newTimer = GameTimer(totalTimeMs = timeMs)
        timer = newTimer

        timerObserverJob?.cancel()
        timerObserverJob = viewModelScope.launch {
            newTimer.remainingMs.collect { remaining ->
                _uiState.update {
                    it.copy(
                        remainingMs   = remaining,
                        timerProgress = remaining.toFloat() / timeMs.toFloat()
                    )
                }
            }
        }

        newTimer.start(viewModelScope) { onTimerExpired(stimulus) }
    }

    // ── Eventos del jugador ───────────────────────────────────────────────────

    /** Llamado cuando el jugador pulsa el botón de reacción (modo inverso). */
    fun onUserReact() {
        if (_uiState.value.phase != GamePhase.SHOWING_STIMULUS) return
        val elapsed = timer?.stop() ?: return
        val stimulus = _uiState.value.currentStimulus ?: return
        processResult(stimulus = stimulus, userReacted = true, reactionMs = elapsed)
    }

    /** Llamado cuando el jugador elige una de las dos opciones (modo normal). */
    fun onUserSelectOption(selectedIndex: Int) {
        if (_uiState.value.phase != GamePhase.SHOWING_STIMULUS) return
        val elapsed = timer?.stop() ?: return
        val stimulus = _uiState.value.currentStimulus ?: return
        val isCorrect = selectedIndex == _uiState.value.correctOptionIndex
        processResult(stimulus = stimulus, userReacted = true, reactionMs = elapsed, forcedCorrect = isCorrect)
    }

    private fun onTimerExpired(stimulus: Stimulus) {
        if (_uiState.value.phase != GamePhase.SHOWING_STIMULUS) return
        timerObserverJob?.cancel()
        val maxMs = config.reactionTimeForLevel(_uiState.value.currentLevel) * 1000L
        processResult(stimulus = stimulus, userReacted = false, reactionMs = maxMs)
    }

    // ── Procesamiento de resultado ────────────────────────────────────────────

    private fun processResult(stimulus: Stimulus, userReacted: Boolean, reactionMs: Long, forcedCorrect: Boolean? = null) {
        timerObserverJob?.cancel()
        val rule = if (config.inverseReactionMode) config.inverseRuleType else null
        val isCorrect = forcedCorrect ?: validator.validate(stimulus, userReacted, rule)
        val expected  = validator.expectedReaction(stimulus, rule)

        levelResults += StimulusResult(
            stimulus         = stimulus,
            userReacted      = userReacted,
            reactionTimeMs   = if (userReacted) reactionMs else 0L,
            isCorrect        = isCorrect,
            expectedReaction = expected
        )

        val feedback = when {
            isCorrect && userReacted -> FeedbackType.CORRECT
            isCorrect && !userReacted-> FeedbackType.CORRECT  // ignorar era lo correcto
            !userReacted             -> FeedbackType.TIMEOUT
            else                     -> FeedbackType.INCORRECT
        }

        val scoreGain = if (isCorrect && config.difficulty != Difficulty.TRAINING)
            computeScore(reactionMs, _uiState.value.currentLevel) else 0

        _uiState.update {
            it.copy(
                feedback        = feedback,
                score           = it.score + scoreGain,
                correctInLevel  = it.correctInLevel  + if (isCorrect) 1 else 0,
                incorrectInLevel= it.incorrectInLevel + if (!isCorrect) 1 else 0
            )
        }

        // Sonido
        when (feedback) {
            FeedbackType.CORRECT   -> sound.playCorrect()
            FeedbackType.INCORRECT -> sound.playIncorrect()
            FeedbackType.TIMEOUT   -> sound.playTimeout()
        }

        viewModelScope.launch {
            delay(500)
            scheduleNextStimulus()
        }
    }

    // ── Evaluación de nivel ───────────────────────────────────────────────────

    private fun evaluateLevel() {
        val state      = _uiState.value
        val correct    = levelResults.count { it.isCorrect }
        val total      = levelResults.size
        val avgMs      = levelResults.filter { it.userReacted }
                            .map { it.reactionTimeMs }
                            .let { if (it.isEmpty()) 0L else it.average().toLong() }
        val passed     = correct.toFloat() / total >= LEVEL_PASS_THRESHOLD

        val stats = LevelStats(
            level            = state.currentLevel,
            correct          = correct,
            total            = total,
            avgReactionTimeMs= avgMs,
            passed           = passed
        )
        allLevelStats += stats
        levelResults.clear()

        if (!passed) {
            _uiState.update { it.copy(phase = GamePhase.GAME_OVER, levelStats = allLevelStats.toList()) }
            sound.playGameOver()
            persistSession()
            return
        }

        val nextLevel = state.currentLevel + 1
        if (nextLevel > state.totalLevels) {
            _uiState.update { it.copy(phase = GamePhase.GAME_WON, levelStats = allLevelStats.toList()) }
            sound.playVictory()
            persistSession()
            return
        }

        sound.playLevelUp()
        _uiState.update {
            it.copy(
                phase            = GamePhase.LEVEL_TRANSITION,
                currentLevel     = nextLevel,
                currentIteration = 0,
                correctInLevel   = 0,
                incorrectInLevel = 0,
                levelStats       = allLevelStats.toList()
            )
        }
    }

    fun continueAfterLevelTransition() {
        scheduleNextStimulus()
    }

    // ── Puntuación ────────────────────────────────────────────────────────────

    private fun computeScore(reactionMs: Long, level: Int): Int {
        val maxMs      = config.reactionTimeForLevel(level) * 1000L
        val speedRatio = 1f - (reactionMs.toFloat() / maxMs).coerceIn(0f, 1f)
        val speedBonus = (speedRatio * 100).toInt()
        val levelBonus = level * 10
        return (config.difficulty.baseScore + speedBonus + levelBonus).coerceAtLeast(10)
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    private fun persistSession() {
        if (config.difficulty == Difficulty.TRAINING) return
        val state = _uiState.value
        val result = GameSessionResult(
            playerName      = config.playerName,
            difficulty      = config.difficulty,
            totalScore      = state.score,
            levelsCompleted = allLevelStats.count { it.passed },
            totalLevels     = state.totalLevels,
            levelStats      = allLevelStats.toList()
        )
        viewModelScope.launch {
            repository.saveGameSession(result)
            _sessionResult.emit(result)
        }
    }

    // Para redirigir a ResultsScreen también cuando la partida termina sin persistir (training)
    fun buildSessionResult(): GameSessionResult {
        val state = _uiState.value
        return GameSessionResult(
            playerName      = config.playerName,
            difficulty      = config.difficulty,
            totalScore      = state.score,
            levelsCompleted = allLevelStats.count { it.passed },
            totalLevels     = state.totalLevels,
            levelStats      = allLevelStats.toList()
        )
    }

    override fun onCleared() {
        super.onCleared()
        timer?.reset()
        timerObserverJob?.cancel()
    }
}
