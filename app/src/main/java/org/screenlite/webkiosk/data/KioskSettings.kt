package org.screenlite.webkiosk.data

import kotlinx.coroutines.flow.Flow

interface KioskSettings {
    fun getCheckInterval(): Flow<Long>
    suspend fun setCheckInterval(interval: Long)
    fun getStartUrl(): Flow<String>
    suspend fun setStartUrl(url: String)
    fun getRotation(): Flow<Rotation>
    suspend fun setRotation(rotation: Rotation)
}