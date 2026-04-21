# Documentación Técnica — Desafío de Reacción y Atención

**Alumno:** _(completar)_  
**Fecha:** Abril 2026  
**Plataforma:** Android (API 26+)  
**Lenguaje:** Kotlin 2.0 · Jetpack Compose  

---

## 1. Descripción General

"Desafío de Reacción y Atención" es un juego Android de reflejos y atención que evalúa
la capacidad del usuario para reaccionar a estímulos visuales dentro de un tiempo límite
configurable. La aplicación opera completamente offline; toda la persistencia es local
mediante una base de datos Room.

---

## 2. Arquitectura del Proyecto

Se adoptó **MVVM (Model-View-ViewModel)** con capas bien diferenciadas:

```
app/
├── domain/
│   ├── model/          GameModels.kt          — Entidades de dominio puras
│   └── logic/          StimulusGenerator.kt   — Generador de estímulos
│                       InverseReactionValidator.kt — Lógica del modo inverso
│                       GameTimer.kt           — Temporizador reactivo
├── data/
│   ├── local/          AppDatabase.kt         — Base de datos Room
│   │   ├── entity/     PlayerEntity, GameSessionEntity
│   │   └── dao/        PlayerDao, GameSessionDao
│   └── repository/     GameRepository.kt      — Única fuente de verdad de datos
├── presentation/
│   ├── config/         ConfigViewModel + ConfigScreen
│   ├── game/           GameViewModel + GameScreen
│   ├── results/        ResultsViewModel + ResultsScreen
│   └── shared/         SharedGameViewModel    — Bus entre pantallas
├── navigation/         AppNavigation.kt       — Grafo de navegación Compose
├── di/                 AppModule.kt           — Módulo Hilt (DI)
└── util/               SoundManager.kt        — Efectos de sonido
```

**Principios aplicados:**
- La capa `domain` no conoce Android ni Room (lógica pura).
- Los ViewModels exponen `StateFlow<UiState>` inmutables; la UI solo observa.
- La inyección de dependencias se gestiona con **Hilt**, eliminando singletons manuales.
- La navegación usa **Navigation Compose** con un `SharedGameViewModel` con scope de
  grafo para pasar objetos complejos entre pantallas sin serialización.

---

## 3. Esquema de Base de Datos (Room)

### Tabla `players`

| Columna     | Tipo    | Descripción                         |
|-------------|---------|-------------------------------------|
| `id`        | INTEGER | PK autoincremental                  |
| `name`      | TEXT    | Nombre único del jugador            |
| `createdAt` | INTEGER | Timestamp de creación (milisegundos)|

### Tabla `game_sessions`

| Columna            | Tipo    | Descripción                              |
|--------------------|---------|------------------------------------------|
| `id`               | INTEGER | PK autoincremental                       |
| `playerName`       | TEXT    | Nombre del jugador (referencia lógica)   |
| `difficulty`       | TEXT    | Nombre del enum `Difficulty`             |
| `totalScore`       | INTEGER | Puntuación final de la sesión            |
| `levelsCompleted`  | INTEGER | Cantidad de niveles aprobados            |
| `totalLevels`      | INTEGER | Total de niveles de la dificultad        |
| `totalCorrect`     | INTEGER | Respuestas correctas globales            |
| `totalAttempts`    | INTEGER | Total de estímulos presentados           |
| `avgReactionTimeMs`| INTEGER | Tiempo medio de reacción (ms)            |
| `won`              | INTEGER | 1 si completó todos los niveles, 0 si no |
| `timestamp`        | INTEGER | Timestamp de la sesión (ms)              |

> **Nota:** Las sesiones de modo Entrenamiento **no** se persisten.

---

## 4. Lógica de Juego

### 4.1 Flujo de una Partida

```
initGame(config)
  └─► scheduleNextStimulus()
        ├─ currentIteration < iterationsPerLevel
        │    └─► showStimulus()
        │          ├─► GameTimer.start()
        │          └─► Espera: onUserReact() ó onTimerExpired()
        │                └─► processResult()
        │                      └─► scheduleNextStimulus()  [recursivo]
        └─ currentIteration == iterationsPerLevel
             └─► evaluateLevel()
                   ├─ passed && más niveles  →  LEVEL_TRANSITION → continueAfterLevelTransition()
                   ├─ passed && último nivel →  GAME_WON → persistSession()
                   └─ no passed             →  GAME_OVER → persistSession()
```

### 4.2 Temporizador (`GameTimer`)

- Implementado con `kotlinx.coroutines.delay` dentro de un `Job` del `viewModelScope`.
- Emite el tiempo restante cada 50 ms mediante un `MutableStateFlow<Long>`.
- La variable interna `reactionHandled` es el **mutex lógico** que evita la condición de
  carrera "el usuario reacciona exactamente cuando el timer expira": la primera llamada
  (sea `stop()` o el callback `onExpired`) gana; la segunda es ignorada.
- El tiempo máximo está **hard-capped** a `MAX_REACTION_TIME_SECONDS = 30` en
  `GameConfig.reactionTimeSeconds`.

### 4.3 Generador de Estímulos (`StimulusGenerator`)

Genera aleatoriamente uno de tres tipos:

| Tipo   | Representación visual        | Variación por nivel              |
|--------|------------------------------|----------------------------------|
| WORD   | Texto grande (ej. "CASA")    | Vocabulario fijo de 20 palabras  |
| NUMBER | Número entero                | Rango aumenta: 1-20 → 1-200      |
| COLOR  | Círculo + nombre del color   | Paleta fija de 8 colores         |

La distribución de tipos cambia con el nivel: en nivel 1 predominan colores y palabras;
a partir del nivel 4 se incrementa la proporción de números (que requieren razonamiento
para el modo inverso).

### 4.4 Dificultad Dinámica

Implementada en `GameConfig.reactionTimeForLevel(level: Int)`:

```kotlin
fun reactionTimeForLevel(level: Int): Int {
    val reduction = difficulty.timeReductionPerLevel * (level - 1)
    return (reactionTimeSeconds - reduction).coerceAtLeast(MIN_REACTION_TIME_SECONDS)
}
```

Cada nivel reduce el tiempo de reacción en `timeReductionPerLevel` segundos
(1 segundo para Fácil/Medio/Difícil, 0 para Entrenamiento). El mínimo absoluto es
`MIN_REACTION_TIME_SECONDS = 3`.

Adicionalmente, el tipo de estímulo generado varía con el nivel (más números en
niveles altos), incrementando la carga cognitiva.

---

## 5. Modo Reacción Inversa

### Concepto

En el modo normal, el jugador siempre debe reaccionar al ver un estímulo. En el modo
inverso, **la respuesta correcta puede ser NO reaccionar**, dependiendo de la regla
activa.

### Implementación (`InverseReactionValidator`)

```kotlin
fun validate(stimulus, userReacted, rule): Boolean =
    userReacted == expectedReaction(stimulus, rule)
```

`expectedReaction` aplica la lógica de la regla. Si no hay regla (modo normal), siempre
devuelve `true` (siempre hay que reaccionar).

### Tabla de reglas implementadas

| Regla                              | Estímulo          | Acción esperada   | Resultado si reaccionó |
|------------------------------------|-------------------|-------------------|------------------------|
| Todos los colores excepto ROJO     | Color VERDE       | Reaccionar        | ✅ Correcto             |
| Todos los colores excepto ROJO     | Color ROJO        | NO reaccionar     | ❌ Incorrecto           |
| No reaccionar a números primos     | Número 127        | NO reaccionar     | ❌ Incorrecto si reaccionó |
| No reaccionar a números primos     | Número 8          | Reaccionar        | ✅ Correcto             |
| No reaccionar a números pares      | Número 14         | NO reaccionar     | ❌ Incorrecto si reaccionó |
| Solo reaccionar a palabras         | Cualquier número  | NO reaccionar     | ❌ Incorrecto si reaccionó |
| Solo reaccionar a palabras         | Palabra "CASA"    | Reaccionar        | ✅ Correcto             |

La verificación de primos usa una criba simplificada O(√n):

```kotlin
private fun isPrime(n: Int): Boolean {
    if (n < 2) return false
    if (n == 2) return true
    if (n % 2 == 0) return false
    var i = 3
    while (i * i <= n) { if (n % i == 0) return false; i += 2 }
    return true
}
```

---

## 6. Puntuación

La puntuación solo se acumula en modos distintos a Entrenamiento:

```
score_per_stimulus = baseScore(difficulty) + speedBonus + levelBonus

  speedBonus = (1 - reactionTime/maxTime) × 100   [0–100 pts]
  levelBonus = level × 10
  baseScore  = { Easy: 100, Medium: 150, Hard: 200 }
```

Reaccionar rápido otorga más puntos. Respuestas incorrectas o timeouts suman 0.

Para pasar un nivel se requiere un **70 % de aciertos** (`LEVEL_PASS_THRESHOLD = 0.70`).

---

## 7. Modos de Dificultad

| Modo          | Tiempo base | Niveles | Puntos | Reducción/nivel |
|---------------|-------------|---------|--------|-----------------|
| Entrenamiento | 20 s        | 3       | 0      | 0 s             |
| Fácil         | 20 s        | 3       | 100    | 1 s             |
| Medio         | 15 s        | 5       | 150    | 1 s             |
| Difícil       | 10 s        | 7       | 200    | 1 s             |

El usuario puede personalizar:
- **Iteraciones por nivel:** 5–50 (defecto 20).
- **Tiempo de reacción:** 3–30 s (defecto según dificultad). Nunca puede exceder 30 s.

---

## 8. Efectos de Sonido (`SoundManager`)

Se utiliza `ToneGenerator` de Android (API estándar, sin archivos externos):

| Evento       | Tono                             |
|--------------|----------------------------------|
| Acierto      | `TONE_PROP_BEEP` (120 ms)        |
| Error        | `TONE_PROP_NACK` (300 ms)        |
| Timeout      | `TONE_CDMA_ABBR_ALERT` (400 ms)  |
| Subir nivel  | `TONE_CDMA_CONFIRM` (500 ms)     |
| Victoria     | `TONE_CDMA_NETWORK_CALLWAITING`  |
| Game Over    | `TONE_CDMA_CALLDROP_LITE`        |

Los tonos se ejecutan en un hilo IO para no bloquear la UI.

---

## 9. Pantallas (UI/UX)

### 9.1 Pantalla de Configuración (`ConfigScreen`)

- Campo de texto: nombre del jugador (con autocompletado de jugadores anteriores).
- Chips de selección: dificultad (Entrenamiento / Fácil / Medio / Difícil).
- Slider de iteraciones por nivel.
- Slider de tiempo de reacción máximo (hard-cap visible en la etiqueta).
- Toggle: Modo Reacción Inversa.
- Radio buttons: selección de regla inversa (visible solo si el modo está activado).
- Validación en tiempo real con mensajes de error inline.

### 9.2 Pantalla de Juego (`GameScreen`)

- **Top bar:** nivel actual / total · puntuación acumulada.
- **Barra de tiempo:** `LinearProgressIndicator` que cambia de verde → amarillo → rojo.
- **Área de estímulo:** animada con `AnimatedVisibility` (scale + fade).
  - Palabra/número: texto 72 sp en negra.
  - Color: círculo de 160 dp del color + etiqueta de nombre.
- **Botón de reacción:** circular, 180 dp, ocupa el tercio inferior. Se deshabilita
  en la fase `INTER_STIMULUS` para evitar pulsaciones anticipadas.
- **Flash de feedback:** el fondo de pantalla cambia de color (verde/rojo/naranja) con
  `animateColorAsState` durante 500 ms al procesar cada resultado.
- **Pantalla de transición de nivel:** muestra estadísticas del nivel completado y un
  botón "Continuar".

### 9.3 Pantalla de Resultados (`ResultsScreen`)

- Titular dinámico ("¡Victoria!" o "Resultados") con color de TopAppBar acorde.
- Banner "★ ¡Nuevo récord personal! ★" si supera su mejor puntuación.
- Tarjeta resumen: score, dificultad, niveles, precisión global, tiempo medio.
- Lista de estadísticas por nivel con indicador visual ✅/❌.
- Ranking global (Top 10 de todas las sesiones).
- Botones: "Jugar de nuevo" (misma configuración) y "Cambiar configuración".

---

## 10. Persistencia y Política de Datos

- **Room Database** (SQLite bajo el capó): archivo `reaction_challenge.db` en el
  almacenamiento privado de la app.
- No hay conexión a internet; toda la información permanece en el dispositivo.
- Los datos de Entrenamiento **no** se guardan (filtro en `GameRepository.saveGameSession`
  y en `GameViewModel.persistSession`).
- El `GameRepository` es `@Singleton` (Hilt), garantizando una única instancia del DAL.

---

## 11. Decisiones Técnicas Clave

| Decisión                           | Justificación                                                    |
|------------------------------------|------------------------------------------------------------------|
| Jetpack Compose (vs XML)           | UI declarativa, animaciones nativas, menos boilerplate           |
| StateFlow (vs LiveData)            | Integración nativa con coroutines; hot stream, seguro en Compose |
| Hilt (vs manual DI)                | Reduce acoplamiento, facilita tests unitarios                    |
| ToneGenerator (vs archivos .ogg)   | Cero dependencias de recursos de audio; funciona en cualquier dispositivo |
| SharedGameViewModel (vs nav-args)  | Evita serializar objetos complejos; scope controlado por el grafo|
| `coerceAtLeast` / `coerceIn`       | Validación de rangos sin excepciones, legible y concisa          |
| `reactionHandled` en `GameTimer`   | Mutex lógico que previene double-fire en la condición de carrera |

---

## 12. Instrucciones de Compilación

1. Abrir el proyecto en **Android Studio Ladybug** (o superior).
2. Sincronizar Gradle (el IDE lo hace automáticamente).
3. Seleccionar un dispositivo/emulador con API 26+.
4. Ejecutar con `Run ▶` o `./gradlew installDebug`.

> **Requisitos mínimos:** Android 8.0 (API 26), ~50 MB de almacenamiento libre.

---

## 13. Posibles Mejoras Futuras

- Modo multijugador local por turnos.
- Exportación de estadísticas a CSV.
- Temas visuales desbloqueables por puntuación.
- Cronómetro inverso animado circular (en lugar de barra lineal).
- Tests unitarios de `InverseReactionValidator` y `GameTimer`.
