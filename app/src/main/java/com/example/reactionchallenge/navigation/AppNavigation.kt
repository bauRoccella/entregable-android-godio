package com.example.reactionchallenge.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reactionchallenge.domain.model.GameConfig
import com.example.reactionchallenge.domain.model.GameSessionResult
import com.example.reactionchallenge.presentation.config.ConfigScreen
import com.example.reactionchallenge.presentation.game.GameScreen
import com.example.reactionchallenge.presentation.results.ResultsScreen
import com.example.reactionchallenge.presentation.shared.SharedGameViewModel

// ── Rutas de navegación ──────────────────────────────────────────────────────
object Routes {
    const val CONFIG  = "config"
    const val GAME    = "game"
    const val RESULTS = "results"
}

/**
 * Grafo de navegación principal.
 *
 * SharedGameViewModel se instancia con scope del NavBackStackEntry raíz,
 * lo que permite que Config, Game y Results compartan el mismo ViewModel
 * sin pasar objetos complejos por argumentos de ruta.
 */
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    // ViewModel compartido al nivel del NavHost (scope: toda la sesión de navegación)
    val sharedVm: SharedGameViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Routes.CONFIG) {

        composable(Routes.CONFIG) {
            ConfigScreen(
                onStartGame = { config: GameConfig ->
                    sharedVm.pendingConfig = config
                    navController.navigate(Routes.GAME) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.GAME) {
            val config = sharedVm.pendingConfig
            if (config == null) {
                // Configuración perdida (proceso recreado): volver a config
                LaunchedEffect(Unit) { navController.popBackStack(Routes.CONFIG, false) }
            } else {
                GameScreen(
                    config = config,
                    onGameFinished = { result: GameSessionResult ->
                        sharedVm.lastResult = result
                        navController.navigate(Routes.RESULTS) {
                            popUpTo(Routes.GAME) { inclusive = true }
                        }
                    },
                    onAbandon = {
                        navController.popBackStack(Routes.CONFIG, false)
                    }
                )
            }
        }

        composable(Routes.RESULTS) {
            val result = sharedVm.lastResult
            if (result == null) {
                LaunchedEffect(Unit) { navController.popBackStack(Routes.CONFIG, false) }
            } else {
                ResultsScreen(
                    result = result,
                    onPlayAgain = {
                        // Reutilizar la misma configuración
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.CONFIG) { inclusive = false }
                        }
                    },
                    onBackToConfig = {
                        navController.popBackStack(Routes.CONFIG, false)
                    }
                )
            }
        }
    }
}
