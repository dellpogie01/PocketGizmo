package com.juanmediahubph.pocketgizmo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observe(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun get(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(entity: SettingsEntity)

    @Query("UPDATE app_settings SET theme = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)

    @Query("UPDATE app_settings SET buttonSounds = :enabled WHERE id = 1")
    suspend fun updateButtonSounds(enabled: Boolean)

    @Query("UPDATE app_settings SET haptics = :enabled WHERE id = 1")
    suspend fun updateHaptics(enabled: Boolean)

    @Query("UPDATE app_settings SET keepScreenAwake = :enabled WHERE id = 1")
    suspend fun updateKeepScreenAwake(enabled: Boolean)

    @Query("UPDATE app_settings SET screenLightColor = :color, screenLightBrightness = :brightness WHERE id = 1")
    suspend fun updateScreenLight(color: Long, brightness: Float)

    @Query("UPDATE app_settings SET screenLightBrightness = :brightness WHERE id = 1")
    suspend fun updateScreenLightBrightness(brightness: Float)

    @Query("UPDATE app_settings SET levelOffsetX = :x, levelOffsetY = :y WHERE id = 1")
    suspend fun updateLevelCalibration(x: Float, y: Float)

    @Query(
        """
        UPDATE app_settings SET
            theme = :theme,
            buttonSounds = :buttonSounds,
            haptics = :haptics,
            screenLightColor = :screenLightColor,
            screenLightBrightness = :screenLightBrightness,
            keepScreenAwake = :keepScreenAwake,
            levelOffsetX = 0,
            levelOffsetY = 0
        WHERE id = 1
        """,
    )
    suspend fun resetToDefaults(
        theme: String,
        buttonSounds: Boolean,
        haptics: Boolean,
        screenLightColor: Long,
        screenLightBrightness: Float,
        keepScreenAwake: Boolean,
    )
}
