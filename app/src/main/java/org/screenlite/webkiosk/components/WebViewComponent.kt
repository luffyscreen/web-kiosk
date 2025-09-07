package org.screenlite.webkiosk.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import android.webkit.WebView
import org.screenlite.webkiosk.app.WebViewManager
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.screenlite.webkiosk.app.DataStoreHelper

private const val TAG = "WebViewComponent"

@Composable
fun WebViewComponent(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var hasLoadedPage by remember { mutableStateOf(false) }
    var rotation by remember { mutableIntStateOf(0) }
    var retryCount by remember { mutableIntStateOf(0) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        DataStoreHelper.getRotation(context).collect { newRotation ->
            Log.d(TAG, "Rotation updated: $newRotation")
            rotation = newRotation
            (webViewRef as? RotatedWebView)?.appliedRotation = newRotation.toFloat()
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
        cm.registerDefaultNetworkCallback(callback)
        onDispose {
            Log.d(TAG, "Unregistering network callback")
            cm.unregisterNetworkCallback(callback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!hasError) {
            key(rotation, configuration.orientation, retryTrigger) {
                AndroidView(
                    modifier = modifier.fillMaxSize().background(Color.White),
                    factory = { ctx ->
                        Log.d(TAG, "Creating WebView (rotation=$rotation, retryTrigger=$retryTrigger)")
                        WebViewManager(
                            ctx,
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
                        ).createWebView(rotation).also { webViewRef = it }
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
}
