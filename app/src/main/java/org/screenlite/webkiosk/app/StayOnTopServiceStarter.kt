package org.screenlite.webkiosk.app

import android.content.Intent
import android.os.Build
import org.screenlite.webkiosk.service.StayOnTopService
import android.content.Context

object StayOnTopServiceStarter {
    fun ensureRunning(context: Context) {
        if (!StayOnTopService.isRunning) {
            val intent = Intent(context, StayOnTopService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                @Suppress("DEPRECATION")
                context.startService(intent)
            }
        }
    }
}