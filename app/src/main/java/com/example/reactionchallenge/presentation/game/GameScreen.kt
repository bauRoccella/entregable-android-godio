package com.example.reactionchallenge.presentation.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reactionchallenge.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    config: GameConfig,
    onGameFinished: (GameSessionResult) -> Unit,
    onAbandon: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAbandonDialog by remember { mutableStateOf(false) }

    // Inicializar solo una vez
    LaunchedEffect(Unit) { viewModel.initGame(config) }

    // Navegar a resultados cuando el juego termina
    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.GAME_OVER || state.phase == GamePhase.GAME_WON) {
            onGameFinished(viewModel.buildSessionResult())
        }
    }

    // Color de fondo reactivo según feedback
    val bgColor by animateColorAsState(
        targetValue = when (state.feedback) {
            FeedbackType.CORRECT   -> Color(0xFF1B5E20)
            FeedbackType.INCORRECT -> Color(0xFFB71C1C)
            FeedbackType.TIMEOUT   -> Color(0xFFE65100)
            null                   -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(200),
        label = "bgColor"
    )

    if (showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            title = { Text("¿Abandonar partida?") },
            text  = { Text("Se perderá el progreso actual.") },
            confirmButton = {
                TextButton(onClick = onAbandon) {
                    Text("Abandonar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nivel ${state.currentLevel} / ${state.totalLevels}") },
                navigationIcon = {
                    IconButton(onClick = { showAbandonDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Abandonar partida")
                    }
                },
                actions = {
                    Text(
                        "Puntos: ${state.score}",
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            when (state.phase) {
                GamePhase.SHOWING_STIMULUS, GamePhase.INTER_STIMULUS -> {
                    GamePlayContent(
                        state          = state,
                        onReact        = viewModel::onUserReact,
                        onSelectOption = viewModel::onUserSelectOption
                    )
                }
                GamePhase.LEVEL_TRANSITION -> {
                    LevelTransitionContent(
                        stats     = state.levelStats.lastOrNull(),
                        nextLevel = state.currentLevel,
                        onContinue= viewModel::continueAfterLevelTransition
                    )
                }
                else -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Contenido principal del juego
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GamePlayContent(
    state: GameUiState,
    onReact: () -> Unit,
    onSelectOption: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Barra de progreso del temporizador
        TimerBar(progress = state.timerProgress, remainingMs = state.remainingMs)

        // Indicador de iteración
        Text(
            "${state.currentIteration} / ${state.iterationsPerLevel}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Recordatorio de regla inversa
        state.inverseRule?.let { rule ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Regla: ${rule.description}",
                    modifier = Modifier.padding(10.dp),
                    style    = MaterialTheme.typography.bodySmall,
                    textAlign= TextAlign.Center
                )
            }
        }

        // Estímulo
        StimulusArea(state = state, modifier = Modifier.weight(1f))

        // Botón único (modo inverso) o dos opciones (modo normal)
        if (state.inverseRule != null) {
            Button(
                onClick  = onReact,
                enabled  = state.phase == GamePhase.SHOWING_STIMULUS,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                colors   = ButtonDefaults.buttonColors(
                    containerColor        = MaterialTheme.colorScheme.primary,
                    disabledContainerColor= MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "¡REACCIONAR!",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center
                )
            }
        } else {
            OptionButtons(
                options        = state.options,
                enabled        = state.phase == GamePhase.SHOWING_STIMULUS,
                onSelectOption = onSelectOption
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OptionButtons(
    options: List<Stimulus>,
    enabled: Boolean,
    onSelectOption: (Int) -> Unit
) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        options.forEachIndexed { index, stimulus ->
            Button(
                onClick  = { onSelectOption(index) },
                enabled  = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor        = MaterialTheme.colorScheme.primary,
                    disabledContainerColor= MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (stimulus.type == StimulusType.COLOR) {
                    val color = stimulus.displayColor?.let { Color(it.colorHex.toLong()) }
                        ?: MaterialTheme.colorScheme.primary
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(stimulus.textValue, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text       = stimulus.textValue,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StimulusArea(state: GameUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = state.phase == GamePhase.SHOWING_STIMULUS && state.currentStimulus != null,
            enter   = scaleIn() + fadeIn(),
            exit    = scaleOut() + fadeOut()
        ) {
            state.currentStimulus?.let { StimulusDisplay(it) }
        }
        if (state.phase == GamePhase.INTER_STIMULUS && state.inverseRule != null) {
            Text(
                "¡Prepárate!",
                fontSize   = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Barra de tiempo
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TimerBar(progress: Float, remainingMs: Long) {
    val color = when {
        progress > 0.5f -> Color(0xFF4CAF50)
        progress > 0.25f-> Color(0xFFFFC107)
        else            -> Color(0xFFF44336)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress       = { progress },
            modifier       = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color          = color,
            trackColor     = color.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${(remainingMs / 1000f).let { "%.1f".format(it) }} s",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Visualización del estímulo
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StimulusDisplay(stimulus: Stimulus) {
    when (stimulus.type) {
        StimulusType.WORD, StimulusType.NUMBER -> {
            Text(
                text       = stimulus.textValue,
                fontSize   = 72.sp,
                fontWeight = FontWeight.Black,
                color      = MaterialTheme.colorScheme.onBackground,
                textAlign  = TextAlign.Center
            )
        }
        StimulusType.COLOR -> {
            val color = stimulus.displayColor?.let {
                Color(it.colorHex.toLong())
            } ?: MaterialTheme.colorScheme.primary

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text       = stimulus.textValue,
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pantalla de transición entre niveles
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LevelTransitionContent(
    stats: LevelStats?,
    nextLevel: Int,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Nivel Superado!", fontSize = 36.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(24.dp))
        stats?.let {
            Text("Aciertos: ${it.correct} / ${it.total}")
            Text("Precisión: ${"%.0f".format(it.accuracy * 100)} %")
            Text("Tiempo medio: ${it.avgReactionTimeMs} ms")
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Siguiente: Nivel $nextLevel",
            fontSize   = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("CONTINUAR", fontWeight = FontWeight.Bold)
        }
    }
}
