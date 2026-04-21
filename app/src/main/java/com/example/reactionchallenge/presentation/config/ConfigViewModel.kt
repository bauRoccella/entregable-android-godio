package com.example.reactionchallenge.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reactionchallenge.data.local.entity.PlayerEntity
import com.example.reactionchallenge.data.repository.GameRepository
import com.example.reactionchallenge.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigUiState(
    val playerName: String = "",
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val iterationsPerLevel: Int = DEFAULT_ITERATIONS_PER_LEVEL,
    val customReactionTime: Int? = null,          // null → usar el valor de la dificultad
    val inverseMode: Boolean = false,
    val selectedRule: InverseRuleType? = null,
    val knownPlayers: List<String> = emptyList(),
    val validationError: String? = null
)

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllPlayers().collect { players ->
                _uiState.update { it.copy(knownPlayers = players.map(PlayerEntity::name)) }
            }
        }
    }

    fun onPlayerNameChange(name: String) =
        _uiState.update { it.copy(playerName = name, validationError = null) }

    fun onDifficultySelected(diff: Difficulty) =
        _uiState.update { it.copy(selectedDifficulty = diff, customReactionTime = null) }

    fun onIterationsChange(value: Int) =
        _uiState.update { it.copy(iterationsPerLevel = value.coerceIn(5, 50)) }

    fun onCustomReactionTimeChange(seconds: Int?) {
        val validated = seconds?.coerceIn(MIN_REACTION_TIME_SECONDS, MAX_REACTION_TIME_SECONDS)
        _uiState.update { it.copy(customReactionTime = validated) }
    }

    fun onInverseModeToggle() =
        _uiState.update { it.copy(inverseMode = !it.inverseMode, selectedRule = null) }

    fun onRuleSelected(rule: InverseRuleType) =
        _uiState.update { it.copy(selectedRule = rule) }

    /**
     * Valida la configuración y devuelve el [GameConfig] listo para iniciar,
     * o `null` si hay errores de validación.
     */
    fun buildConfig(): GameConfig? {
        val state = _uiState.value
        if (state.playerName.isBlank()) {
            _uiState.update { it.copy(validationError = "El nombre del jugador no puede estar vacío.") }
            return null
        }
        if (state.inverseMode && state.selectedRule == null) {
            _uiState.update { it.copy(validationError = "Selecciona una regla para el modo inverso.") }
            return null
        }
        return GameConfig(
            playerName            = state.playerName.trim(),
            difficulty            = state.selectedDifficulty,
            iterationsPerLevel    = state.iterationsPerLevel,
            customReactionTimeSeconds = state.customReactionTime,
            inverseReactionMode   = state.inverseMode,
            inverseRuleType       = if (state.inverseMode) state.selectedRule else null
        )
    }
}
