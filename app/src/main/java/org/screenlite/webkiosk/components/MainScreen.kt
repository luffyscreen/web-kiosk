package org.screenlite.webkiosk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.screenlite.webkiosk.app.DataStoreHelper

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var url by remember { mutableStateOf("about:blank") }
    var reloadKey by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        DataStoreHelper.getStartUrl(context).collect { newUrl ->
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

    key(url, reloadKey) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            WebViewComponent(url = url)
        }
    }
}