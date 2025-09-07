package org.screenlite.webkiosk.app

import android.content.Intent
import androidx.activity.ComponentActivity
import org.screenlite.webkiosk.service.StayOnTopService

object StayOnTopServiceStarter {
    fun ensureRunning(activity: ComponentActivity) {
        if (!StayOnTopService.isRunning) {
            val intent = Intent(activity, StayOnTopService::class.java)
            activity.startForegroundService(intent)
        }
    }
}