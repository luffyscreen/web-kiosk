package org.screenlite.webkiosk

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.screenlite.webkiosk.app.FullScreenHelper
import org.screenlite.webkiosk.app.NotificationPermissionHelper
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
                AppContent(unlockHandler)
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}

@Composable
fun AppContent(unlockHandler: TapUnlockHandler) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "Notification permission granted: $isGranted")
    }

    LaunchedEffect(Unit) {
        if (!NotificationPermissionHelper.hasPermission(context)) {
            NotificationPermissionHelper.requestPermission(permissionLauncher)
        }
    }

    Box(Modifier.fillMaxSize()) {
        MainScreen()
        KioskInputOverlay(onTap = { unlockHandler.registerTap() })
    }
}