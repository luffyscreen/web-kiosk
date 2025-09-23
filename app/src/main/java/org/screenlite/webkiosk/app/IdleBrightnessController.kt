package org.screenlite.webkiosk.app

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.screenlite.webkiosk.data.KioskSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

const val TAG = "IdleBrightness"
class IdleBrightnessController(
    private val activity: Activity,
    private val settings: KioskSettings
) {
    private val handler = Handler(Looper.getMainLooper())
    private var idleTimeout: Long = 60_000L
    private var idleBrightness: Int = 0
    private var activeBrightness: Int = 100

    private val checkIdleRunnable = Runnable {
        Log.d(TAG, "Idle timeout reached → switching to idle brightness: $idleBrightness%")
        setBrightness(idleBrightness)
        if (idleBrightness == 0) {
            _isIdleMode.value = true
        }
    }

    private val _isIdleMode = MutableStateFlow(false)
    val isIdleMode = _isIdleMode.asStateFlow()

    fun start() {
        Log.d(TAG, "Starting IdleBrightnessController...")

        CoroutineScope(Dispatchers.Main).launch {
            idleTimeout = (settings.getIdleTimeout().first() * 1000)
            idleBrightness = settings.getIdleBrightness().first()
            activeBrightness = settings.getActiveBrightness().first()

            Log.d(
                TAG,
                "Loaded settings → timeout=${idleTimeout}ms, idleBrightness=$idleBrightness%, activeBrightness=$activeBrightness%"
            )

            resetIdleTimer()
        }
    }

    private fun resetIdleTimer() {
        handler.removeCallbacks(checkIdleRunnable)
        setBrightness(activeBrightness)
        _isIdleMode.value = false
        Log.d(
            TAG,
            "Idle timer reset → switching to active brightness: $activeBrightness% (next idle in ${idleTimeout}ms)"
        )
        handler.postDelayed(checkIdleRunnable, idleTimeout)
    }

    private fun setBrightness(level: Int) {
        try {
            val lp = activity.window.attributes
            lp.screenBrightness = level / 100f
            activity.window.attributes = lp
            Log.d(TAG, "Screen brightness set to $level%")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set brightness", e)
        }
    }

    fun stop() {
        handler.removeCallbacks(checkIdleRunnable)
        Log.d(TAG, "Stopped IdleBrightnessController")
    }

    fun onUserInteraction() {
        Log.d(TAG, "User interaction detected → resetting idle timer")
        resetIdleTimer()
    }
}
