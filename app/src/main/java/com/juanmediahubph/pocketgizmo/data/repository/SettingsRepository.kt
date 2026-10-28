package com.juanmediahubph.pocketgizmo.data.repository

import com.juanmediahubph.pocketgizmo.data.local.SettingsDao
import com.juanmediahubph.pocketgizmo.data.local.SettingsEntity
import com.juanmediahubph.pocketgizmo.domain.AppSettings
import com.juanmediahubph.pocketgizmo.domain.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dao: SettingsDao) {

    val settings: Flow<AppSettings> = dao.observe()
        .filterNotNull()
        .map { it.toDomain() }

    suspend fun ensureDefaults() {
        dao.insertDefaults(SettingsEntity())
    }

    suspend fun setTheme(theme: AppTheme) {
        dao.updateTheme(theme.name)
    }

    suspend fun setButtonSounds(enabled: Boolean) {
        dao.updateButtonSounds(enabled)
    }

    suspend fun setHaptics(enabled: Boolean) {
        dao.updateHaptics(enabled)
    }

    suspend fun setKeepScreenAwake(enabled: Boolean) {
        dao.updateKeepScreenAwake(enabled)
    }

    suspend fun setScreenLight(color: Long, brightness: Float) {
        dao.updateScreenLight(color, brightness)
    }

    suspend fun setScreenLightBrightness(brightness: Float) {
        dao.updateScreenLightBrightness(brightness)
    }

    suspend fun setLevelCalibration(offsetX: Float, offsetY: Float) {
        dao.updateLevelCalibration(offsetX, offsetY)
    }

    suspend fun resetSettings() {
        val defaults = SettingsEntity()
        dao.resetToDefaults(
            theme = defaults.theme,
            buttonSounds = defaults.buttonSounds,
            haptics = defaults.haptics,
            screenLightColor = defaults.screenLightColor,
            screenLightBrightness = defaults.screenLightBrightness,
            keepScreenAwake = defaults.keepScreenAwake,
        )
    }
}

private fun SettingsEntity.toDomain() = AppSettings(
    theme = AppTheme.fromStorage(theme),
    buttonSounds = buttonSounds,
    haptics = haptics,
    screenLightColor = screenLightColor,
    screenLightBrightness = screenLightBrightness,
    keepScreenAwake = keepScreenAwake,
    levelOffsetX = levelOffsetX,
    levelOffsetY = levelOffsetY,
)
