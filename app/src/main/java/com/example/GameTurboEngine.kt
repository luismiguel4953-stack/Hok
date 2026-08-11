package com.example

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager

class GameTurboEngine(private val context: Context) {
    data class SystemStats(
        val memoryUsedPercent: Float,
        val availableMemoryMb: Long,
        val temperatureC: Float
    )

    fun readStats(): SystemStats {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val total = memoryInfo.totalMem.coerceAtLeast(1L)
        val used = (total - memoryInfo.availMem).coerceAtLeast(0L)
        return SystemStats(
            memoryUsedPercent = used.toFloat() / total.toFloat(),
            availableMemoryMb = memoryInfo.availMem / (1024L * 1024L),
            temperatureC = readThermalStatusAsTemperature()
        )
    }

    fun applySafeBoost() {
        // A normal Android app cannot kill arbitrary processes.
        // Reclaim only memory belonging to this app.
        System.gc()
    }

    private fun readThermalStatusAsTemperature(): Float {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            return when (powerManager?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> 30f
                PowerManager.THERMAL_STATUS_LIGHT -> 38f
                PowerManager.THERMAL_STATUS_MODERATE -> 45f
                PowerManager.THERMAL_STATUS_SEVERE -> 52f
                PowerManager.THERMAL_STATUS_CRITICAL -> 58f
                PowerManager.THERMAL_STATUS_EMERGENCY -> 65f
                PowerManager.THERMAL_STATUS_SHUTDOWN -> 70f
                else -> 35f
            }
        }
        return 35f
    }
}
