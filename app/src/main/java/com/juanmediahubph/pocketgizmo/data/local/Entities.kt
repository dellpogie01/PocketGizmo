package com.juanmediahubph.pocketgizmo.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SETTINGS_ROW_ID,
    val theme: String = "SYSTEM",
    val buttonSounds: Boolean = false,
    val haptics: Boolean = true,
    val screenLightColor: Long = 0xFFFFFFFF,
    val screenLightBrightness: Float = 0.7f,
    val keepScreenAwake: Boolean = true,
    val levelOffsetX: Float = 0f,
    val levelOffsetY: Float = 0f,
)

const val SETTINGS_ROW_ID = 1

@Entity(tableName = "stopwatch_session")
data class StopwatchSessionEntity(
    @PrimaryKey val id: Int = STOPWATCH_ROW_ID,
    val running: Boolean = false,
    val accumulatedMillis: Long = 0L,
    val anchorElapsedRealtime: Long = 0L,
    val bootReferenceMillis: Long = 0L,
    val stoppedByReboot: Boolean = false,
)

const val STOPWATCH_ROW_ID = 1

@Entity(
    tableName = "stopwatch_laps",
    foreignKeys = [
        ForeignKey(
            entity = StopwatchSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class LapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Int = STOPWATCH_ROW_ID,
    val lapNumber: Int,
    val totalMillis: Long,
    val splitMillis: Long,
)
