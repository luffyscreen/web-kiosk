package org.screenlite.webkiosk.components

import android.view.KeyEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent

@Composable
fun KioskInputOverlay(onTap: () -> Unit) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            }
            .focusable()
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.DirectionCenter &&
                    it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN
                ) {
                    onTap()
                    return@onKeyEvent true
                }
                if (it.key in setOf(
                        Key.DirectionDown,
                        Key.DirectionUp,
                        Key.DirectionLeft,
                        Key.DirectionRight,
                        Key.Back
                    )
                ) {
                    return@onKeyEvent true
                }
                false
            }
            .clickable { onTap() }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
