package com.example.reactionchallenge.presentation.shared

import androidx.lifecycle.ViewModel
import com.example.reactionchallenge.domain.model.GameConfig
import com.example.reactionchallenge.domain.model.GameSessionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel con scope de Activity.
 * Actúa como bus de datos entre pantallas (Config → Game → Results)
 * sin necesidad de serializar objetos complejos en las rutas de navegación.
 */
@HiltViewModel
class SharedGameViewModel @Inject constructor() : ViewModel() {
    var pendingConfig: GameConfig? = null
    var lastResult: GameSessionResult? = null
}
