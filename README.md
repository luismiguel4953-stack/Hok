# HOK — Game Turbo

Aplicación Android de optimización y gestión de juegos, construida con Kotlin + Jetpack Compose.

## Qué incluye

- Biblioteca automática de aplicaciones con launcher.
- Lanzamiento directo de juegos.
- Favoritos persistentes con Room.
- Perfiles de juego preparados: ULTRA, BALANCED, BATTERY y ESPORTS.
- Panel Turbo con memoria disponible y estado térmico.
- Historial de activaciones Turbo en Room.
- HUD flotante con RAM y temperatura mediante permiso de superposición.
- Arquitectura MVVM con ViewModel, Room y Coroutines.
- Tema oscuro estilo gaming/cyber.
- Tests unitarios, instrumentados y Robolectric.

## Limitaciones reales de Android

HOK no puede elevar arbitrariamente la frecuencia de CPU/GPU, cambiar el gobernador del kernel ni matar procesos de otras aplicaciones desde una app Android normal. El modo Turbo usa únicamente APIs públicas y operaciones seguras. Las cifras mostradas como temperatura son una representación del estado térmico del sistema cuando Android no expone una temperatura física exacta.

## Estructura principal

- `app/src/main/java/com/example/MainActivity.kt` — interfaz principal.
- `GameTurboViewModel.kt` — estado, biblioteca y acciones.
- `GameTurboEngine.kt` — telemetría y operaciones seguras.
- `PerformanceHudService.kt` — HUD flotante.
- `data/` — Room, entidades y DAOs.
- `ui/theme/` — tema visual gaming.

## Construcción

Abrir el proyecto en Android Studio y sincronizar Gradle. El proyecto usa SDK 36 y Java 11.

Antes de una build release, configura las variables de firma (`KEYSTORE_PATH`, `STORE_PASSWORD` y `KEY_PASSWORD`). No guardes claves reales en Git.

## Estado

Base funcional de Game Turbo lista para continuar con perfiles avanzados, estadísticas de sesión, detección de juego activo y mejoras específicas compatibles con las APIs del dispositivo.
