package com.example.reactionchallenge.presentation.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reactionchallenge.data.local.entity.GameSessionEntity
import com.example.reactionchallenge.domain.model.GameSessionResult
import com.example.reactionchallenge.domain.model.LevelStats
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    result: GameSessionResult,
    onPlayAgain: () -> Unit,
    onBackToConfig: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    LaunchedEffect(result) { viewModel.loadResults(result) }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (result.won) "¡Victoria!" else "Resultados")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (result.won)
                        Color(0xFF1B5E20) else Color(0xFFB71C1C)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Resultado general ───────────────────────────────────────────
            val headline = if (result.won) "¡Felicitaciones, ${result.playerName}!"
                           else "¡Buen intento, ${result.playerName}!"
            Text(headline, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

            if (state.isNewBest) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF176))
                ) {
                    Text(
                        "★ ¡Nuevo récord personal! ★",
                        modifier  = Modifier.padding(12.dp),
                        color     = Color(0xFF212121),
                        fontWeight= FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Tarjeta de puntuación ───────────────────────────────────────
            ScoreCard(result = result)

            // ── Estadísticas por nivel ──────────────────────────────────────
            Text("Estadísticas por nivel", style = MaterialTheme.typography.titleMedium)
            result.levelStats.forEach { LevelStatsRow(it) }

            // ── Ranking global ──────────────────────────────────────────────
            if (state.topSessions.isNotEmpty()) {
                HorizontalDivider()
                Text("Ranking Global (Top 10)", style = MaterialTheme.typography.titleMedium)
                state.topSessions.forEachIndexed { i, s ->
                    RankingRow(index = i + 1, session = s, highlight = s.totalScore == result.totalScore)
                }
            }

            // ── Acciones ────────────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = onPlayAgain,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("JUGAR DE NUEVO", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick  = onBackToConfig,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("CAMBIAR CONFIGURACIÓN")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta de puntuación principal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScoreCard(result: GameSessionResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatRow("Puntuación total",    "${result.totalScore}")
            StatRow("Dificultad",          result.difficulty.displayName)
            StatRow("Niveles completados", "${result.levelsCompleted} / ${result.totalLevels}")
            StatRow("Aciertos",            "${result.totalCorrect} / ${result.totalAttempts}")
            StatRow("Precisión global",    "${"%.0f".format(result.overallAccuracy * 100)} %")
            StatRow("Tiempo medio",        "${result.avgReactionTimeMs} ms")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fila de estadísticas de un nivel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LevelStatsRow(stats: LevelStats) {
    val bg = if (stats.passed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    Card(
        colors   = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Nivel ${stats.level}", fontWeight = FontWeight.SemiBold)
            Text("${stats.correct}/${stats.total} ✓")
            Text("${"%.0f".format(stats.accuracy * 100)} %")
            Text("${stats.avgReactionTimeMs} ms")
            Text(if (stats.passed) "✅" else "❌", fontSize = 18.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fila de ranking
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RankingRow(index: Int, session: GameSessionEntity, highlight: Boolean) {
    val sdf = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }
    val bg  = if (highlight) MaterialTheme.colorScheme.primaryContainer
              else           MaterialTheme.colorScheme.surface
    Surface(color = bg, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("#$index", fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
            Text(session.playerName, modifier = Modifier.weight(1f))
            Text(session.difficulty)
            Spacer(Modifier.width(8.dp))
            Text("${session.totalScore} pts", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(sdf.format(Date(session.timestamp)),
                style = MaterialTheme.typography.bodySmall)
        }
    }
    HorizontalDivider(thickness = 0.5.dp)
}
