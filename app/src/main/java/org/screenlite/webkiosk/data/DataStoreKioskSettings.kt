package org.screenlite.webkiosk.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kiosk_settings")

class DataStoreKioskSettings(private val context: Context) : KioskSettings {
    private val keyCheckInterval = longPreferencesKey("check_interval")
    private val keyStartUrl = stringPreferencesKey("start_url")
    private val keyRotation = intPreferencesKey("rotation")

    override fun getCheckInterval(): Flow<Long> {
        return context.dataStore.data.map { prefs ->
            prefs[keyCheckInterval] ?: 10_000L
        }
    }

    override suspend fun setCheckInterval(interval: Long) {
        context.dataStore.edit { prefs ->
            prefs[keyCheckInterval] = interval
        }
    }

    override fun getStartUrl(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[keyStartUrl] ?: "https://screenlite.org"
        }
    }

    override suspend fun setStartUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[keyStartUrl] = url
        }
    }

    override fun getRotation(): Flow<Rotation> {
        return context.dataStore.data.map { prefs ->
            val degrees: Int = try {
                prefs[keyRotation] ?: Rotation.ROTATION_0.degrees
            } catch (_: ClassCastException) {
                val legacyKey = longPreferencesKey("rotation")
                (prefs[legacyKey] ?: Rotation.ROTATION_0.degrees.toLong()).toInt()
            }
            getRotationFromDegrees(degrees)
        }
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

    override suspend fun setRotation(rotation: Rotation) {
        context.dataStore.edit { prefs ->
            prefs[keyRotation] = rotation.degrees
        }
    }
}