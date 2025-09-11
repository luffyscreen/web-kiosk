package org.screenlite.webkiosk.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class SharedPreferencesKioskSettings(context: Context) : KioskSettings {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)

    override fun getCheckInterval(): Flow<Long> = callbackFlow {
        val key = "check_interval"
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(prefs.getLong(key, 10_000L))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getLong(key, 10_000L))

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override suspend fun setCheckInterval(interval: Long) {
        prefs.edit { putLong("check_interval", interval) }
    }

    override fun getStartUrl(): Flow<String> = callbackFlow {
        val key = "start_url"
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(prefs.getString(key, "https://screenlite.org") ?: "https://screenlite.org")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(key, "https://screenlite.org") ?: "https://screenlite.org")

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override suspend fun setStartUrl(url: String) {
        prefs.edit { putString("start_url", url) }
    }

    override fun getRotation(): Flow<Rotation> = callbackFlow {
        val key = "rotation"
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                val degrees = prefs.getInt(key, Rotation.ROTATION_0.degrees)
                trySend(getRotationFromDegrees(degrees))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        val degrees = prefs.getInt(key, Rotation.ROTATION_0.degrees)
        trySend(getRotationFromDegrees(degrees))

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override suspend fun setRotation(rotation: Rotation) {
        prefs.edit { putInt("rotation", rotation.degrees) }
    }

    private fun getRotationFromDegrees(degrees: Int): Rotation {
        return when (degrees) {
            Rotation.ROTATION_0.degrees -> Rotation.ROTATION_0
            Rotation.ROTATION_90.degrees -> Rotation.ROTATION_90
            Rotation.ROTATION_180.degrees -> Rotation.ROTATION_180
            Rotation.ROTATION_270.degrees -> Rotation.ROTATION_270
            else -> Rotation.ROTATION_0
        }
    }
}
