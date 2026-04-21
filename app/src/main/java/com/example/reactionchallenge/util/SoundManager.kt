package com.example.reactionchallenge.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona los efectos de sonido del juego usando ToneGenerator
 * (no requiere archivos de audio externos).
 *
 * Todos los tonos se reproducen en un hilo de IO para no bloquear la UI.
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /** Sonido de acierto: tono corto y agudo. */
    fun playCorrect() = playTone(ToneGenerator.TONE_PROP_BEEP, durationMs = 120)

    /** Sonido de error: tono descendente. */
    fun playIncorrect() = playTone(ToneGenerator.TONE_PROP_NACK, durationMs = 300)

    /** Sonido de tiempo agotado: alerta más larga. */
    fun playTimeout() = playTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, durationMs = 400)

    /** Sonido de subir de nivel: confirmación. */
    fun playLevelUp() = playTone(ToneGenerator.TONE_CDMA_CONFIRM, durationMs = 500)

    /** Sonido de victoria. */
    fun playVictory() = playTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING, durationMs = 700)

    /** Sonido de derrota. */
    fun playGameOver() = playTone(ToneGenerator.TONE_CDMA_CALLDROP_LITE, durationMs = 600)

    private fun playTone(toneType: Int, durationMs: Int) {
        scope.launch {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                tg.startTone(toneType, durationMs)
                Thread.sleep(durationMs.toLong() + 50)
                tg.release()
            } catch (_: Exception) {
                // En emuladores o dispositivos sin audio simplemente se ignora
            }
        }
    }
}
