package org.screenlite.webkiosk.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.stringPreferencesKey
private val Context.dataStore by preferencesDataStore(name = "kiosk_settings")
object DataStoreHelper {
    private val KEY_CHECK_INTERVAL = longPreferencesKey("check_interval")
    private val KEY_START_URL = stringPreferencesKey("start_url")
    private val KEY_ROTATION = longPreferencesKey("rotation")

    fun getCheckInterval(context: Context): Flow<Long> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_CHECK_INTERVAL] ?: 10_000L
        }
    }

    suspend fun setCheckInterval(context: Context, interval: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CHECK_INTERVAL] = interval
        }
    }

    fun getStartUrl(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_START_URL] ?: "https://screenlite.org"
        }
    }

    suspend fun setStartUrl(context: Context, url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_START_URL] = url
        }
    }

    fun getRotation(context: Context): Flow<Int> {
        return context.dataStore.data.map { prefs ->
            (prefs[KEY_ROTATION] ?: 0L).toInt()
        }
    }

    suspend fun setRotation(context: Context, rotation: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROTATION] = rotation.toLong()
        }
    }
}