package com.juanmediahubph.pocketgizmo.hardware

import android.app.Activity
import android.view.WindowManager
import androidx.core.view.WindowCompat

/**
 * Applies a window-local brightness override and keep-awake flag for Screen
 * Light, then restores exactly what was there before. It never touches the
 * device's global brightness settings.
 */
class ScreenLightWindowController(private val activity: Activity) {

    private val window = activity.window
    private val savedBrightness = window.attributes.screenBrightness
    private val savedKeepScreenOn =
        (window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
    private var applied = false

    fun apply(brightness: Float, keepAwake: Boolean) {
        val updated = window.attributes
        updated.screenBrightness = brightness.coerceIn(MIN_BRIGHTNESS, 1f)
        window.attributes = updated
        if (keepAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        applied = true
    }

    fun restore() {
        if (!applied) return
        val restored = window.attributes
        restored.screenBrightness = savedBrightness
        window.attributes = restored
        if (savedKeepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        applied = false
    }

    fun setLightSystemBarIcons(lightBackground: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = lightBackground
        controller.isAppearanceLightNavigationBars = lightBackground
    }

    private companion object {
        const val MIN_BRIGHTNESS = 0.02f
    }
}
