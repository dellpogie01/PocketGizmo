package com.juanmediahubph.pocketgizmo.domain

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStorage(value: String?): AppTheme = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

const val DEFAULT_SCREEN_LIGHT_COLOR: Long = 0xFFFFFFFF
const val DEFAULT_SCREEN_LIGHT_BRIGHTNESS: Float = 0.7f

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val buttonSounds: Boolean = false,
    val haptics: Boolean = true,
    val screenLightColor: Long = DEFAULT_SCREEN_LIGHT_COLOR,
    val screenLightBrightness: Float = DEFAULT_SCREEN_LIGHT_BRIGHTNESS,
    val keepScreenAwake: Boolean = true,
    val levelOffsetX: Float = 0f,
    val levelOffsetY: Float = 0f,
)
