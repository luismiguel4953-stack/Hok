package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.GameEntity
import com.example.ui.theme.GameTurboTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameTurboViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameTurboTheme {
                GameTurboApp(viewModel, ::openOverlaySettings, ::openUsageAccessSettings)
            }
        }
        viewModel.refreshSystemStats()
        viewModel.discoverGames()
    }

    private fun openOverlaySettings() {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}

@Composable
private fun GameTurboApp(
    viewModel: GameTurboViewModel,
    onOverlaySettings: () -> Unit,
    onUsageAccessSettings: () -> Unit
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val memory by viewModel.memoryUsedPercent.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val message by viewModel.boostMessage.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var showWelcome by remember { mutableStateOf(true) }

    if (showWelcome) {
        AlertDialog(
            onDismissRequest = { showWelcome = false },
            icon = { Image(painterResource(com.example.R.drawable.hok_logo), "Logo HOK", Modifier.size(96.dp)) },
            title = { Text("Bienvenido a HOK", fontWeight = FontWeight.Bold) },
            text = {
                Text("HOK Game Turbo ayuda a organizar tus juegos, consultar el estado del dispositivo y mostrar un HUD de rendimiento.\n\nADVERTENCIA: HOK no obtiene acceso ilimitado al teléfono. Los permisos especiales son controlados por Android y deben ser autorizados por ti desde Ajustes. HOK no puede garantizar aumentos de FPS ni modificar CPU/GPU con privilegios que Android no concede.")
            },
            confirmButton = { Button(onClick = { showWelcome = false }) { Text("ENTRAR A HOK") } },
            dismissButton = { TextButton(onClick = { showWelcome = false }) { Text("Ahora no") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("HOK • Game Turbo") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.Gamepad, null) }, label = { Text("Juegos") })
                NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.Speed, null) }, label = { Text("Turbo") })
                NavigationBarItem(tab == 2, { tab = 2 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Ajustes") })
            }
        }
    ) { padding ->
        when (tab) {
            0 -> GamesScreen(padding, games, viewModel::launchGame, viewModel::toggleFavorite)
            1 -> TurboScreen(padding, memory, temperature, message, viewModel::boost, onOverlaySettings)
            else -> SettingsScreen(padding, onOverlaySettings, onUsageAccessSettings)
        }
    }
}

@Composable
private fun GamesScreen(padding: PaddingValues, games: List<GameEntity>, onLaunch: (GameEntity) -> Unit, onFavorite: (GameEntity) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Image(painterResource(com.example.R.drawable.hok_logo), "HOK", Modifier.size(112.dp))
                Text("HOK", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Text("GAME TURBO", color = MaterialTheme.colorScheme.primary)
            }
        }
        item { Text("Biblioteca", style = MaterialTheme.typography.headlineMedium) }
        item { Text("Juegos detectados en este dispositivo", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (games.isEmpty()) item { Text("No se encontraron juegos todavía.") }
        items(games, key = { it.id }) { game ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(game.title, style = MaterialTheme.typography.titleLarge)
                        Text("${game.profileName} • ${game.targetFps} FPS", color = MaterialTheme.colorScheme.primary)
                        Text("${game.playTimeMinutes} min jugados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilterChip(selected = game.isFavorite, onClick = { onFavorite(game) }, label = { Text(if (game.isFavorite) "★" else "☆") })
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onLaunch(game) }) { Text("Jugar") }
                }
            }
        }
    }
}

@Composable
private fun TurboScreen(padding: PaddingValues, memory: Float, temperature: Float, message: String, onBoost: () -> Unit, onOverlay: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Centro Turbo", style = MaterialTheme.typography.headlineMedium)
        Card { Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Memory, null); Spacer(Modifier.width(10.dp)); Text("Memoria usada: ${(memory * 100).toInt()}%") }
            LinearProgressIndicator(progress = { memory.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Text("Temperatura: ${"%.1f".format(temperature)} °C")
        } }
        Button(onClick = onBoost, modifier = Modifier.fillMaxWidth().height(54.dp)) { Icon(Icons.Default.Bolt, null); Spacer(Modifier.width(8.dp)); Text("ACTIVAR TURBO") }
        Button(onClick = onOverlay, modifier = Modifier.fillMaxWidth()) { Text("ACTIVAR HUD FLOTANTE") }
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.tertiary)
        Text("HOK usa únicamente capacidades permitidas por Android; no mata procesos ajenos ni modifica CPU/GPU de forma privilegiada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsScreen(padding: PaddingValues, onOverlay: () -> Unit, onUsageAccess: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Ajustes y permisos", style = MaterialTheme.typography.headlineMedium)
        Text("Concede a HOK los accesos especiales necesarios desde los ajustes oficiales de Android.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(Modifier.fillMaxWidth(), tonalElevation = 2.dp) { Column(Modifier.padding(16.dp)) {
            Text("HUD flotante", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Permite mostrar RAM y estado térmico sobre los juegos.")
            Spacer(Modifier.height(10.dp))
            Button(onClick = onOverlay) { Text("Abrir permiso de superposición") }
        } }
        Surface(Modifier.fillMaxWidth(), tonalElevation = 2.dp) { Column(Modifier.padding(16.dp)) {
            Text("Acceso de uso", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Permite consultar estadísticas de uso cuando Android las ponga a disposición.")
            Spacer(Modifier.height(10.dp))
            Button(onClick = onUsageAccess) { Text("Abrir acceso de uso") }
        } }
        Text("HOK no puede concederse permisos especiales por sí misma. Android muestra y controla estas autorizaciones en Ajustes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
