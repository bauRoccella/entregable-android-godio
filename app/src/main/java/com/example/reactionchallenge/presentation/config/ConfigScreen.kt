package com.example.reactionchallenge.presentation.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reactionchallenge.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onStartGame: (GameConfig) -> Unit,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Desafío de Reacción y Atención") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Nombre del jugador ──────────────────────────────────────────
            SectionTitle("Jugador")
            OutlinedTextField(
                value = state.playerName,
                onValueChange = viewModel::onPlayerNameChange,
                label = { Text("Nombre del jugador") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationError != null && state.playerName.isBlank()
            )
            if (state.knownPlayers.isNotEmpty()) {
                Text(
                    "Jugadores anteriores: ${state.knownPlayers.take(5).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            SectionTitle("Dificultad")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = state.selectedDifficulty == diff,
                        onClick  = { viewModel.onDifficultySelected(diff) },
                        label    = { Text(diff.displayName) }
                    )
                }
            }
            if (state.selectedDifficulty == Difficulty.TRAINING) {
                InfoCard("Modo Entrenamiento: los puntos no se cuentan ni se guardan.")
            }


            SectionTitle("Iteraciones por nivel (${state.iterationsPerLevel})")
            Slider(
                value = state.iterationsPerLevel.toFloat(),
                onValueChange = { viewModel.onIterationsChange(it.toInt()) },
                valueRange = 5f..50f,
                steps = 8
            )


            val displayTime = state.customReactionTime
                ?: state.selectedDifficulty.defaultTimeSeconds
            SectionTitle("Tiempo de reacción máximo: $displayTime s (límite ${MAX_REACTION_TIME_SECONDS}s)")
            Slider(
                value = displayTime.toFloat(),
                onValueChange = { viewModel.onCustomReactionTimeChange(it.toInt()) },
                valueRange = MIN_REACTION_TIME_SECONDS.toFloat()..MAX_REACTION_TIME_SECONDS.toFloat(),
                steps = MAX_REACTION_TIME_SECONDS - MIN_REACTION_TIME_SECONDS - 1
            )
            if (state.customReactionTime == null) {
                Text(
                    "Usando tiempo por defecto de la dificultad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            SectionTitle("Modo Reacción Inversa")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.inverseMode,
                    onCheckedChange = { viewModel.onInverseModeToggle() }
                )
                Spacer(Modifier.width(12.dp))
                Text("Activar modo inverso")
            }

            if (state.inverseMode) {
                InfoCard(
                    "En este modo, a veces la respuesta correcta es NO reaccionar.\n" +
                    "Selecciona la regla que determina cuándo actuar."
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InverseRuleType.entries.forEach { rule ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = state.selectedRule == rule,
                                onClick  = { viewModel.onRuleSelected(rule) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = rule.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }


            state.validationError?.let { error ->
                Text(
                    text  = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            Button(
                onClick = {
                    val config = viewModel.buildConfig()
                    if (config != null) onStartGame(config)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("INICIAR JUEGO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun InfoCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text     = message,
            modifier = Modifier.padding(12.dp),
            style    = MaterialTheme.typography.bodySmall
        )
    }
}
