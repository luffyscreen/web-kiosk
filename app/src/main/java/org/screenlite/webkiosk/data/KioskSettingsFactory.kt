package org.screenlite.webkiosk.data

import android.content.Context
import android.os.Build

object KioskSettingsFactory {
    fun get(context: Context): KioskSettings {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DataStoreKioskSettings(context)
        } else {
            SharedPreferencesKioskSettings(context)
        }
    }
}