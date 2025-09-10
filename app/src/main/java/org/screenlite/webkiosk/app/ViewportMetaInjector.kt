package org.screenlite.webkiosk.app

import android.util.Log
import android.webkit.WebView

object ViewportMetaInjector {

    fun inject(webView: WebView) {
        webView.post {
            val width = webView.width
            val height = webView.height

            Log.d("ViewportMetaInjector", "Injecting viewport meta: width=$width, height=$height")

            webView.evaluateJavascript(
                """
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
                        html, body {
                            max-width: 100vw !important;
                            overflow-x: hidden !important;
                        }
                    `;
                    document.head.appendChild(style);
                })();
                """.trimIndent(),
                null
            )
        }
    }
}
