package org.screenlite.webkiosk.app

import android.os.Build
import android.view.View
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object FullScreenHelper {
    fun enableImmersiveMode(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            enableImmersiveModeApi30(window)
        } else {
            @Suppress("DEPRECATION")
            enableImmersiveModeLegacy(window)
        }
    }
    private fun enableImmersiveModeApi30(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    @Suppress("DEPRECATION")
    private fun enableImmersiveModeLegacy(window: Window) {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        decorView.systemUiVisibility = uiOptions

        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = uiOptions
            }
        }
    }
}