package com.example.reactionchallenge.domain.logic

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Temporizador de cuenta regresiva para cada estímulo.
 *
 * Emite actualizaciones cada [tickIntervalMs] ms.
 * Llama a [onExpired] cuando llega a 0 (en el hilo del scope provisto).
 *
 * Uso:
 *   val timer = GameTimer(totalTimeMs = 10_000)
 *   timer.start(viewModelScope) { handleTimeout() }
 *   ...
 *   val elapsed = timer.stop()
 */
class GameTimer(
    val totalTimeMs: Long,
    private val tickIntervalMs: Long = 50L
) {
    private var job: Job? = null
    private var startTimestamp = 0L

    private val _remainingMs = MutableStateFlow(totalTimeMs)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    /** Progreso de 1.0 (inicio) a 0.0 (expirado). */
    val progress: Float
        get() = (_remainingMs.value.toFloat() / totalTimeMs).coerceIn(0f, 1f)

    /** Tiempo transcurrido desde que arrancó el temporizador. */
    val elapsedMs: Long
        get() = if (startTimestamp == 0L) 0L
                else (System.currentTimeMillis() - startTimestamp).coerceAtMost(totalTimeMs)

    private var reactionHandled = false   // evita doble procesamiento

    fun start(scope: CoroutineScope, onExpired: () -> Unit) {
        reset()
        startTimestamp = System.currentTimeMillis()

        job = scope.launch {
            while (isActive) {
                delay(tickIntervalMs)
                val elapsed = System.currentTimeMillis() - startTimestamp
                val remaining = (totalTimeMs - elapsed).coerceAtLeast(0L)
                _remainingMs.value = remaining
                if (remaining == 0L) {
                    if (!reactionHandled) {
                        reactionHandled = true
                        onExpired()
                    }
                    break
                }
            }
        }
    }

    /**
     * Detiene el temporizador y devuelve el tiempo de reacción en ms.
     * Retorna -1 si el temporizador no estaba corriendo.
     */
    fun stop(): Long {
        val elapsed = elapsedMs
        job?.cancel()
        job = null
        reactionHandled = true
        return elapsed
    }

    fun reset() {
        job?.cancel()
        job = null
        reactionHandled = false
        startTimestamp = 0L
        _remainingMs.value = totalTimeMs
    }
}
