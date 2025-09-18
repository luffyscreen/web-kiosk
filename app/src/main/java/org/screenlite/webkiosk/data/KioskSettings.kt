package org.screenlite.webkiosk.data

import kotlinx.coroutines.flow.Flow

interface KioskSettings {
    fun getCheckInterval(): Flow<Long>
    suspend fun setCheckInterval(interval: Long)
    fun getStartUrl(): Flow<String>
    suspend fun setStartUrl(url: String)
    fun getRotation(): Flow<Rotation>
    suspend fun setRotation(rotation: Rotation)
    fun getIdleTimeout(): Flow<Long>
    suspend fun setIdleTimeout(timeout: Long)
    fun getIdleBrightness(): Flow<Int>
    suspend fun setIdleBrightness(brightness: Int)
    fun getActiveBrightness(): Flow<Int>
    suspend fun setActiveBrightness(brightness: Int)
}