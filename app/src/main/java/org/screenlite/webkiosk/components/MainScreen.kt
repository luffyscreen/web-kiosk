package org.screenlite.webkiosk.components

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.screenlite.webkiosk.data.KioskSettingsFactory

@Composable
fun MainScreen(activity: Activity, modifier: Modifier) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("about:blank") }
    val kioskSettings = remember { KioskSettingsFactory.get(context) }

    LaunchedEffect(Unit) {
        kioskSettings.getStartUrl().collect { newUrl ->
            url = newUrl
        }
    }

    key(url) {
        WebViewComponent(url = url, activity = activity, modifier)
    }
}