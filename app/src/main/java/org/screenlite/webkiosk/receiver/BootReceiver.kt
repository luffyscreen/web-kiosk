package org.screenlite.webkiosk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.screenlite.webkiosk.app.StayOnTopServiceStarter
import org.screenlite.webkiosk.service.StayOnTopService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "onReceive called with action: ${intent?.action}")

        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_USER_PRESENT,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (!StayOnTopService.isRunning) {
                    StayOnTopServiceStarter.ensureRunning(context)
                    Log.d("BootReceiver", "StayOnTopService started successfully")
                } else {
                    Log.d("BootReceiver", "StayOnTopService is already running")
                }
            }
        }
    }
}