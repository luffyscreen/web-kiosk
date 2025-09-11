package org.screenlite.webkiosk.app

import android.os.Build
import android.util.Log
import android.webkit.WebView

object ViewportMetaInjector {
    fun inject(webView: WebView) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            injectLegacy(webView)
        } else {
            injectCurrent(webView)
        }
    }

    private fun injectCurrent(webView: WebView) {
        webView.post {
            val width = webView.width
            val height = webView.height

            Log.d("ViewportMetaInjector", "Injecting viewport meta (current): width=$width, height=$height")

            val script = """
                (function() {
                    var meta = document.querySelector("meta[name=viewport]");
                    if (!meta) {
                        meta = document.createElement('meta');
                        meta.name = "viewport";
                        document.head.appendChild(meta);
                    }
                    meta.content = "width=${width}, height=${height}, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no";
                })();
                (function() {
                    var style = document.createElement('style');
                    style.type = 'text/css';
                    style.innerHTML = `
                        html {
                            max-width: ${width}px !important;
                            overflow-x: hidden !important;
                        }
                    `;
                    document.head.appendChild(style);
                })();
                """

            webView.evaluateJavascript(script, null)
        }
    }

    private fun injectLegacy(webView: WebView) {
        webView.post {
            val width = webView.width
            val height = webView.height

            Log.d("ViewportMetaInjector", "Injecting viewport meta (legacy): width=$width, height=$height")

            val script = """
                javascript:(function() {
                    var meta = document.querySelector("meta[name=viewport]");
                    if (!meta) {
                        meta = document.createElement('meta');
                        meta.name = "viewport";
                        document.head.appendChild(meta);
                    }
                    meta.content = "width=${width}, height=${height}, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no";
                    var style = document.createElement('style');
                    style.type = 'text/css';
                    style.innerHTML = 'html { max-width: ${width}px !important; overflow-x: hidden !important; }';
                    document.head.appendChild(style);
                })();
                """

            webView.loadUrl(script)
        }
    }
}