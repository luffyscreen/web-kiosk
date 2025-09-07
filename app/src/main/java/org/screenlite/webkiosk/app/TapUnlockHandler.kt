package org.screenlite.webkiosk.app

class TapUnlockHandler(
    private val timeout: Long = 2000L,
    private val requiredTaps: Int = 5,
    private val onUnlocked: () -> Unit
) {
    private var clickCount = 0
    private var lastClickTime = 0L

    fun registerTap() {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > timeout) {
            clickCount = 0
        }
        clickCount++
        lastClickTime = now

        if (clickCount >= requiredTaps) {
            onUnlocked()
            clickCount = 0
        }
    }
}
