package com.example.reactionchallenge.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reactionchallenge.data.local.entity.GameSessionEntity
import com.example.reactionchallenge.data.repository.GameRepository
import com.example.reactionchallenge.domain.model.GameSessionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultsUiState(
    val session: GameSessionResult? = null,
    val bestScore: Int = 0,
    val topSessions: List<GameSessionEntity> = emptyList(),
    val isNewBest: Boolean = false
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun loadResults(result: GameSessionResult) {
        _uiState.update { it.copy(session = result) }

        viewModelScope.launch {
            val best = repository.getBestScore(result.playerName, result.difficulty.name)
            val isNewBest = result.totalScore >= best && result.difficulty.baseScore > 0
            _uiState.update { it.copy(bestScore = best, isNewBest = isNewBest) }
        }

        viewModelScope.launch {
            repository.getTopSessions(10).collect { sessions ->
                _uiState.update { it.copy(topSessions = sessions) }
            }
        }
    }
}
