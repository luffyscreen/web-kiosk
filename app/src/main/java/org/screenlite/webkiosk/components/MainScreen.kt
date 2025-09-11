package org.screenlite.webkiosk.components

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.screenlite.webkiosk.data.KioskSettingsFactory

@Composable
fun MainScreen(activity: Activity, modifier: Modifier) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("about:blank") }
    var reloadKey by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val kioskSettings = remember { KioskSettingsFactory.get(context) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(Unit) {
        kioskSettings.getStartUrl().collect { newUrl ->
            url = newUrl
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reloadKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    key(url, reloadKey, configuration.orientation) {
        WebViewComponent(url = url, activity = activity, modifier)
    }
}