package com.example

import android.app.ActivityManager
import android.content.Context
import android.os.Build

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
            availableMemoryMb = memoryInfo.availMem,
            temperatureC = readThermalTemperature()
        )
    }

    fun applySafeBoost() {
        // Android does not grant ordinary apps permission to kill arbitrary processes.
        // The safe operation here is to release this app's own unused allocations.
        System.gc()
    }

    private fun readThermalTemperature(): Float {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val thermal = context.getSystemService(Context.THERMAL_SERVICE) as? android.os.ThermalService
            thermal?.let { service ->
                return when (service.currentThermalStatus) {
                    android.os.PowerManager.THERMAL_STATUS_NONE -> 30f
                    android.os.PowerManager.THERMAL_STATUS_LIGHT -> 38f
                    android.os.PowerManager.THERMAL_STATUS_MODERATE -> 45f
                    android.os.PowerManager.THERMAL_STATUS_SEVERE -> 52f
                    android.os.PowerManager.THERMAL_STATUS_CRITICAL -> 58f
                    android.os.PowerManager.THERMAL_STATUS_EMERGENCY -> 65f
                    android.os.PowerManager.THERMAL_STATUS_SHUTDOWN -> 70f
                    else -> 35f
                }
            }
        }
        return 35f
    }
}
