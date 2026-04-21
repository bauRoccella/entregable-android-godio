package com.example.reactionchallenge.domain.logic

import com.example.reactionchallenge.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Valida si la acción del usuario es correcta según la regla activa.
 *
 * Modo normal   : siempre se espera que el usuario reaccione.
 * Modo inverso  : la regla puede exigir NO reaccionar ante ciertos estímulos.
 *
 * Tabla de ejemplos (del enunciado):
 *  Regla "excepto ROJO" + estímulo "Verde"  → reaccionar  = CORRECTO
 *  Regla "excepto ROJO" + estímulo "ROJO"   → NO reaccionar = CORRECTO
 *                                            → reaccionar  = INCORRECTO
 *  Regla "no primo"     + estímulo "127"    → NO reaccionar = CORRECTO
 */
@Singleton
class InverseReactionValidator @Inject constructor() {

    /**
     * Determina si, dada la regla activa, el usuario DEBE reaccionar.
     * Si no hay regla (modo normal) siempre devuelve `true`.
     */
    fun expectedReaction(stimulus: Stimulus, rule: InverseRuleType?): Boolean {
        rule ?: return true   // sin regla → siempre reaccionar
        return when (rule) {
            InverseRuleType.REACT_TO_ALL_EXCEPT_RED ->
                !(stimulus.type == StimulusType.COLOR && stimulus.displayColor?.name == "ROJO")

            InverseRuleType.REACT_TO_ALL_EXCEPT_BLUE ->
                !(stimulus.type == StimulusType.COLOR && stimulus.displayColor?.name == "AZUL")

            InverseRuleType.NO_REACT_TO_PRIME ->
                when (stimulus.type) {
                    StimulusType.NUMBER -> {
                        val n = stimulus.textValue.toIntOrNull() ?: return true
                        !isPrime(n)      // reaccionar si NO es primo
                    }
                    else -> true
                }

            InverseRuleType.NO_REACT_TO_EVEN ->
                when (stimulus.type) {
                    StimulusType.NUMBER -> {
                        val n = stimulus.textValue.toIntOrNull() ?: return true
                        n % 2 != 0       // reaccionar si es IMPAR
                    }
                    else -> true
                }

            InverseRuleType.REACT_TO_WORDS_ONLY ->
                stimulus.type == StimulusType.WORD

            InverseRuleType.NO_REACT_TO_COLORS ->
                stimulus.type != StimulusType.COLOR
        }
    }

    /**
     * Compara la acción del usuario con la esperada y devuelve si fue correcta.
     */
    fun validate(
        stimulus: Stimulus,
        userReacted: Boolean,
        rule: InverseRuleType?
    ): Boolean = userReacted == expectedReaction(stimulus, rule)

    // ── Utilidad: criba de Eratóstenes simplificada ──────────────────────────

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        if (n == 2) return true
        if (n % 2 == 0) return false
        var i = 3
        while (i * i <= n) {
            if (n % i == 0) return false
            i += 2
        }
        return true
    }
}
