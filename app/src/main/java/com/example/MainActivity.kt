package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                GameTurboApp(viewModel) {
                    if (!Settings.canDrawOverlays(this)) {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                    }
                }
            }
        }
        viewModel.refreshSystemStats()
        viewModel.discoverGames()
    }
}

@Composable
private fun GameTurboApp(viewModel: GameTurboViewModel, onOverlaySettings: () -> Unit) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val memory by viewModel.memoryUsedPercent.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val message by viewModel.boostMessage.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

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
            else -> SettingsScreen(padding, onOverlaySettings)
        }
    }
}

@Composable
private fun GamesScreen(padding: PaddingValues, games: List<GameEntity>, onLaunch: (GameEntity) -> Unit, onFavorite: (GameEntity) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
        Button(onClick = onOverlay, modifier = Modifier.fillMaxWidth()) { Text("Configurar HUD flotante") }
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.tertiary)
        Text("Android limita el control de procesos de otras apps. HOK mide recursos y optimiza su propio estado sin prometer cambios de rendimiento que el sistema no permite.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsScreen(padding: PaddingValues, onOverlay: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        Surface(Modifier.fillMaxWidth(), tonalElevation = 2.dp) { Column(Modifier.padding(16.dp)) {
            Text("HUD de rendimiento", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Permite mostrar un panel flotante sobre los juegos.")
            Spacer(Modifier.height(10.dp))
            Button(onClick = onOverlay) { Text("Conceder permiso de superposición") }
        } }
    }
}
