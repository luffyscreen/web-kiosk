package org.screenlite.webkiosk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import org.screenlite.webkiosk.app.FullScreenHelper
import org.screenlite.webkiosk.app.StayOnTopServiceStarter
import org.screenlite.webkiosk.app.TapUnlockHandler
import org.screenlite.webkiosk.components.KioskInputOverlay
import org.screenlite.webkiosk.components.MainScreen
import org.screenlite.webkiosk.ui.theme.ScreenliteWebKioskTheme

class MainActivity : ComponentActivity() {
    private lateinit var unlockHandler: TapUnlockHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FullScreenHelper.enableImmersiveMode(this.window)

        StayOnTopServiceStarter.ensureRunning(this)

        unlockHandler = TapUnlockHandler {
            openSettings()
        }

        setContent {
            ScreenliteWebKioskTheme {
                Box(Modifier.fillMaxSize()) {
                    MainScreen()
                    KioskInputOverlay(onTap = { unlockHandler.registerTap() })
                }
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}
