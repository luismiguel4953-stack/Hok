package com.example

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PerformanceHudService : Service() {
    private var windowManager: WindowManager? = null
    private var hud: TextView? = null
    private var updateJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        showHud()
        return START_NOT_STICKY
    }

    private fun showHud() {
        if (hud != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        hud = TextView(this).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(210, 10, 14, 22))
            setPadding(20, 12, 20, 12)
            textSize = 12f
            text = "HOK • TURBO"
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 80
        }
        windowManager?.addView(hud, params)
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            val engine = GameTurboEngine(this@PerformanceHudService)
            while (isActive) {
                val stats = engine.readStats()
                hud?.text = "HOK • ${"%.0f".format(stats.memoryUsedPercent * 100)}% RAM • ${"%.1f".format(stats.temperatureC)}°C"
                delay(2000)
            }
        }
    }

    override fun onDestroy() {
        updateJob?.cancel()
        hud?.let { runCatching { windowManager?.removeView(it) } }
        hud = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.example.Hok.STOP_HUD"
    }
}
