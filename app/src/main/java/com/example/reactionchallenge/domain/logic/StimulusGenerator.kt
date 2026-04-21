package com.example.reactionchallenge.domain.logic

import com.example.reactionchallenge.domain.model.*
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StimulusGenerator @Inject constructor() {

    private val words = listOf(
        "CASA", "ÁRBOL", "SOL", "MAR", "LUNA", "FUEGO", "AGUA", "TIERRA",
        "VIENTO", "NUBE", "FLOR", "PÁJARO", "ESTRELLA", "CIELO", "MONTAÑA",
        "RÍO", "BOSQUE", "NIEVE", "PIEDRA", "CIUDAD"
    )

    /**
     * Genera un estímulo aleatorio adaptado al nivel actual.
     *
     * En niveles bajos predominan colores y palabras (fácil de identificar).
     * En niveles altos se incorporan más números (requieren cálculo mental).
     *
     * @param level  Nivel actual (1-based).
     * @param forceType  Si se especifica, fuerza el tipo de estímulo (útil para tests).
     */
    fun generate(level: Int = 1, forceType: StimulusType? = null): Stimulus {
        val type = forceType ?: pickType(level)
        return when (type) {
            StimulusType.WORD   -> generateWord()
            StimulusType.NUMBER -> generateNumber(level)
            StimulusType.COLOR  -> generateColor()
        }
    }

    // ── Selección de tipo por nivel ──────────────────────────────────────────

    private fun pickType(level: Int): StimulusType {
        // Cuanto más alto el nivel, mayor probabilidad de números
        val pool: List<StimulusType> = when {
            level == 1 -> listOf(
                StimulusType.COLOR, StimulusType.COLOR,
                StimulusType.WORD, StimulusType.WORD,
                StimulusType.NUMBER
            )
            level <= 3 -> listOf(
                StimulusType.COLOR, StimulusType.WORD,
                StimulusType.NUMBER, StimulusType.NUMBER
            )
            else       -> listOf(
                StimulusType.COLOR,
                StimulusType.WORD,
                StimulusType.NUMBER, StimulusType.NUMBER, StimulusType.NUMBER
            )
        }
        return pool.random()
    }

    // ── Generadores individuales ─────────────────────────────────────────────

    private fun generateWord(): Stimulus = Stimulus(
        type = StimulusType.WORD,
        textValue = words.random()
    )

    private fun generateNumber(level: Int): Stimulus {
        val max = when {
            level <= 2 -> 20
            level <= 4 -> 100
            else       -> 200
        }
        return Stimulus(
            type = StimulusType.NUMBER,
            textValue = Random.nextInt(1, max + 1).toString()
        )
    }

    private fun generateColor(): Stimulus {
        val color = STIMULUS_COLORS.random()
        return Stimulus(
            type = StimulusType.COLOR,
            textValue = color.name,
            displayColor = color
        )
    }

    /**
     * Genera un estímulo del mismo tipo que [stimulus] pero con valor diferente.
     * Se usa en modo normal para ofrecer dos opciones al jugador.
     */
    fun generateDistractor(stimulus: Stimulus, level: Int): Stimulus {
        return when (stimulus.type) {
            StimulusType.WORD -> {
                val other = words.filter { it != stimulus.textValue }.random()
                Stimulus(type = StimulusType.WORD, textValue = other)
            }
            StimulusType.NUMBER -> {
                val current = stimulus.textValue.toInt()
                val max = when {
                    level <= 2 -> 20
                    level <= 4 -> 100
                    else       -> 200
                }
                var distractor: Int
                do { distractor = Random.nextInt(1, max + 1) } while (distractor == current)
                Stimulus(type = StimulusType.NUMBER, textValue = distractor.toString())
            }
            StimulusType.COLOR -> {
                val other = STIMULUS_COLORS.filter { it.name != stimulus.textValue }.random()
                Stimulus(type = StimulusType.COLOR, textValue = other.name, displayColor = other)
            }
        }
    }
}
