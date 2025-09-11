package org.screenlite.webkiosk.components

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import org.screenlite.webkiosk.app.WebViewManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.screenlite.webkiosk.data.KioskSettingsFactory
import org.screenlite.webkiosk.data.Rotation

private const val TAG = "WebViewComponent"
@Composable
fun WebViewComponent(
    url: String,
    activity: Activity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var hasLoadedPage by remember { mutableStateOf(false) }
    var rotation: Rotation by remember { mutableStateOf(Rotation.ROTATION_0) }
    var retryCount by remember { mutableIntStateOf(0) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val kioskSettings = KioskSettingsFactory.get(context)

        kioskSettings.getRotation().collect { newRotation ->
            Log.d(TAG, "Rotation updated: $newRotation")
            rotation = newRotation
        }
    }

    LaunchedEffect(hasError, retryTrigger) {
        if (hasError && !hasLoadedPage) {
            retryCount++
            val delayTime = (1000L * (1 shl (retryCount - 1))).coerceAtMost(30_000L)
            Log.d(TAG, "Retry #$retryCount in ${delayTime}ms (trigger=$retryTrigger)")
            delay(delayTime)
            retryTrigger++
        } else if (!hasError) {
            if (retryCount > 0) Log.d(TAG, "Reset retry count (error cleared)")
            retryCount = 0
        }
    }

    DisposableEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                if (hasError) {
                    hasError = false
                    retryTrigger++
                    Log.d(TAG, "Recovered from error, retryTrigger=$retryTrigger")
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                if (!isLoading && !hasLoadedPage) {
                    Log.e(TAG, "Connection lost before page loaded")
                    hasError = true
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(callback)
        } else {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            @Suppress("DEPRECATION")
            cm.registerNetworkCallback(networkRequest, callback)
        }

        onDispose {
            Log.d(TAG, "Unregistering network callback")
            cm.unregisterNetworkCallback(callback)
        }
    }

    key(rotation, retryTrigger) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                Log.d(TAG, "Creating WebView (rotation=$rotation, retryTrigger=$retryTrigger)")
                WebViewManager(
                    activity,
                    onError = { err ->
                        Log.e(TAG, "WebView error: $err")
                        hasError = err
                        if (err) {
                            hasLoadedPage = false
                        }
                    },
                    onPageLoading = { loading ->
                        isLoading = loading
                        Log.d(TAG, "Page loading=$loading")
                        if (!loading && !hasError) {
                            hasLoadedPage = true
                            Log.d(TAG, "Page loaded successfully")
                        }
                    }
                ).createWebView(rotation)
            },
            update = { webView ->
                if (webView.url != url) {
                    Log.d(TAG, "Loading new URL: $url")
                    webView.loadUrl(url)
                } else if (retryTrigger > 0 && !hasLoadedPage) {
                    Log.d(TAG, "Retry triggered, reloading WebView")
                    webView.reload()
                }
            }
        )
    }

    when {
        hasError -> Box(
            Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Log.w(TAG, "Showing connection error UI")
            Text("Connection error\nRetrying...", color = Color.White)
        }

        isLoading -> Box(
            Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Log.d(TAG, "Showing loading UI")
            Text("Loading...", color = Color.White)
        }
    }
}
