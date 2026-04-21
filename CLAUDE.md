# Desafío de Reacción y Atención — Contexto para Claude

## Qué es este proyecto

App Android de reflejos y atención desarrollada en Kotlin con Jetpack Compose.
El jugador reacciona a estímulos visuales (palabras, números, colores) dentro de un tiempo límite.
Incluye un modo "reacción inversa" donde a veces la respuesta correcta es NO reaccionar.

## Stack técnico

- Kotlin 2.0.21
- Jetpack Compose (BOM 2024.09.03) + Material 3
- Arquitectura: MVVM + Clean Architecture (domain / data / presentation)
- Inyección de dependencias: Hilt 2.52
- Persistencia local: Room 2.6.1 (sin internet, sin backend)
- Navegación: Navigation Compose 2.8.4
- Async: Coroutines + StateFlow
- Sonido: ToneGenerator (API nativa, sin archivos .ogg)
- Build: AGP 8.7.3, KSP 2.0.21-1.0.28, minSdk 26, targetSdk 35, JVM 17

## Estructura de archivos

```
app/src/main/java/com/example/reactionchallenge/
├── domain/
│   ├── model/GameModels.kt              — Enums, data classes, constantes
│   └── logic/
│       ├── StimulusGenerator.kt         — Genera estímulos aleatorios por nivel
│       ├── InverseReactionValidator.kt  — Valida si reaccionar/ignorar fue correcto
│       └── GameTimer.kt                 — Countdown con StateFlow, anti-race-condition
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── entity/PlayerEntity.kt
│   │   ├── entity/GameSessionEntity.kt
│   │   ├── dao/PlayerDao.kt
│   │   └── dao/GameSessionDao.kt
│   └── repository/GameRepository.kt
├── presentation/
│   ├── config/ConfigViewModel.kt + ConfigScreen.kt
│   ├── game/GameViewModel.kt + GameScreen.kt
│   ├── results/ResultsViewModel.kt + ResultsScreen.kt
│   └── shared/SharedGameViewModel.kt   — Bus de datos entre pantallas (activity-scoped)
├── navigation/AppNavigation.kt          — NavHost con 3 rutas: config/game/results
├── di/AppModule.kt                      — Provee Room DB, DAOs (Hilt SingletonComponent)
├── util/SoundManager.kt
├── ui/theme/                            — Color.kt, Typography.kt, Theme.kt
├── ReactionApp.kt                       — @HiltAndroidApp
└── MainActivity.kt                     — @AndroidEntryPoint, enableEdgeToEdge
```

## Archivos de build

- `build.gradle.kts` (raíz): declara plugins con `apply false`
- `app/build.gradle.kts`: dependencias, namespace `com.example.reactionchallenge`
- `settings.gradle.kts`: nombre del proyecto `ReactionChallenge`, incluye `:app`
- `gradle.properties`: `android.useAndroidX=true`, `android.enableJetifier=true`

## Flujo de navegación

```
ConfigScreen → (SharedGameViewModel.pendingConfig) → GameScreen → (SharedGameViewModel.lastResult) → ResultsScreen
                                                                         ↑ "Jugar de nuevo" ──────────┘
                    ← "Cambiar configuración" ────────────────────────────────────────────────────────┘
```

## Flujo del juego (GameViewModel)

```
initGame(config)
  └─► scheduleNextStimulus()
        ├─ si quedan iteraciones → showStimulus() → GameTimer.start()
        │     └─ onUserReact() o onTimerExpired() → processResult() → scheduleNextStimulus()
        └─ si terminaron → evaluateLevel()
              ├─ passed + más niveles → LEVEL_TRANSITION
              ├─ passed + último     → GAME_WON → persistSession()
              └─ no passed           → GAME_OVER → persistSession()
```

## Modelos clave

```kotlin
enum class Difficulty { TRAINING, EASY, MEDIUM, HARD }
enum class StimulusType { WORD, NUMBER, COLOR }
enum class InverseRuleType { REACT_TO_ALL_EXCEPT_RED, NO_REACT_TO_PRIME, ... }  // 6 reglas
enum class GamePhase { IDLE, INTER_STIMULUS, SHOWING_STIMULUS, LEVEL_TRANSITION, GAME_OVER, GAME_WON }
enum class FeedbackType { CORRECT, INCORRECT, TIMEOUT }

data class GameConfig(playerName, difficulty, iterationsPerLevel=20, customReactionTimeSeconds?, inverseReactionMode, inverseRuleType?)
data class GameSessionResult(playerName, difficulty, totalScore, levelsCompleted, totalLevels, levelStats, timestamp)
```

## Reglas de negocio importantes

- Tiempo de reacción: nunca supera 30 s (`MAX_REACTION_TIME_SECONDS`), mínimo 3 s
- Dificultad dinámica: cada nivel reduce el tiempo en `difficulty.timeReductionPerLevel` segundos
- Pasar nivel: requiere ≥ 70% de aciertos (`LEVEL_PASS_THRESHOLD = 0.70f`)
- Modo TRAINING: no suma puntos, no persiste en Room
- Anti-race-condition en GameTimer: flag `reactionHandled` evita doble procesamiento
- Puntuación: `baseScore(difficulty) + speedBonus(0-100) + levelBonus(level×10)`

## Modo Reacción Inversa

`InverseReactionValidator.validate(stimulus, userReacted, rule)` devuelve `true` si la acción fue correcta.
`expectedReaction(stimulus, rule)` devuelve si DEBÍA reaccionar (`true`) o ignorar (`false`).
Sin regla (modo normal): siempre devuelve `true` (siempre hay que reaccionar).

## Base de datos Room

Tablas: `players` (id, name, createdAt) y `game_sessions` (id, playerName, difficulty, totalScore, levelsCompleted, totalLevels, totalCorrect, totalAttempts, avgReactionTimeMs, won, timestamp).
`GameRepository` es `@Singleton`. No hay migraciones (usa `fallbackToDestructiveMigration`).

## Problemas ya resueltos

- Faltaba `gradle.properties` con `android.useAndroidX=true` → creado
- Faltaban íconos `mipmap/ic_launcher` → removidos del AndroidManifest.xml
