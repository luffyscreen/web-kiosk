package org.screenlite.webkiosk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.screenlite.webkiosk.service.StayOnTopService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "onReceive called with action: ${intent?.action}")

        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_USER_PRESENT,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                startKioskService(context)
            }
        }
    }

    private fun startKioskService(context: Context) {
        if (StayOnTopService.Companion.isRunning) {
            Log.d("BootReceiver", "StayOnTopService is already running")
            return
        }

        try {
            val serviceIntent = Intent(context, StayOnTopService::class.java).apply {
                setPackage(context.packageName)
            }

            context.startForegroundService(serviceIntent)

            Log.d("BootReceiver", "StayOnTopService started successfully")

        } catch (e: Exception) {
            Log.e("BootReceiver", "Exception starting service: ${e.message}")
        }
    }
}