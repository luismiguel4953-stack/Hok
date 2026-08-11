package com.example

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.GameEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GameTurboViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    private val gameDao = db.gameDao()
    private val boostDao = db.boostLogDao()
    private val engine = GameTurboEngine(app)

    val games: StateFlow<List<GameEntity>> = gameDao.getAllGames()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _memoryUsedPercent = MutableStateFlow(0f)
    val memoryUsedPercent: StateFlow<Float> = _memoryUsedPercent.asStateFlow()

    private val _temperature = MutableStateFlow(0f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _boostMessage = MutableStateFlow("")
    val boostMessage: StateFlow<String> = _boostMessage.asStateFlow()

    fun refreshSystemStats() {
        val stats = engine.readStats()
        _memoryUsedPercent.value = stats.memoryUsedPercent
        _temperature.value = stats.temperatureC
    }

    fun discoverGames() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val ownPackage = getApplication<Application>().packageName
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val discovered = pm.queryIntentActivities(intent, 0)
                .asSequence()
                .mapNotNull { resolve ->
                    val info = resolve.activityInfo?.applicationInfo ?: return@mapNotNull null
                    if (info.packageName == ownPackage || (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) return@mapNotNull null
                    val title = resolve.loadLabel(pm)?.toString()?.trim().orEmpty()
                    if (title.isBlank()) return@mapNotNull null
                    GameEntity(title = title, packageName = info.packageName, isFavorite = false)
                }
                .distinctBy { it.packageName }
                .take(100)
                .toList()

            val existing = gameDao.getAllGames().first().associateBy { it.packageName }
            discovered.forEach { game ->
                val old = existing[game.packageName]
                if (old == null) gameDao.insertGame(game)
            }
        }
    }

    fun launchGame(game: GameEntity) {
        val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(game.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
            viewModelScope.launch { gameDao.updatePlayTime(game.id, 1) }
        }
    }

    fun toggleFavorite(game: GameEntity) {
        viewModelScope.launch { gameDao.updateGame(game.copy(isFavorite = !game.isFavorite)) }
    }

    fun boost() {
        viewModelScope.launch {
            val before = engine.readStats()
            engine.applySafeBoost()
            val after = engine.readStats()
            boostDao.insertLog(
                com.example.data.entity.BoostLogEntity(
                    memoryFreedMb = ((before.availableMemoryMb - after.availableMemoryMb).coerceAtLeast(0L) / 1024 / 1024).toInt(),
                    temperatureBefore = before.temperatureC,
                    temperatureAfter = after.temperatureC,
                    profileApplied = "BALANCED_SAFE"
                )
            )
            _memoryUsedPercent.value = after.memoryUsedPercent
            _temperature.value = after.temperatureC
            _boostMessage.value = "Turbo aplicado. Memoria disponible: ${after.availableMemoryMb / 1024 / 1024} MB"
        }
    }
}
