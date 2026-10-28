package com.juanmediahubph.pocketgizmo.hardware

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import com.juanmediahubph.pocketgizmo.R

class FeedbackController(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val soundPool = SoundPool.Builder().setMaxStreams(2).build()
    private val clickSound = soundPool.load(appContext, R.raw.click, 1)
    private var clickLoaded = false

    init {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            clickLoaded = status == 0
        }
    }

    fun tap(view: View?, hapticsEnabled: Boolean, soundsEnabled: Boolean) {
        if (hapticsEnabled && view != null) {
            runCatching {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }
        if (soundsEnabled) {
            playClick()
        }
    }

    private fun playClick() {
        if (!clickLoaded) return
        val manager = audioManager ?: return
        if (manager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) return
        val effectsEnabled = runCatching {
            Settings.System.getInt(
                appContext.contentResolver,
                Settings.System.SOUND_EFFECTS_ENABLED,
                1,
            )
        }.getOrDefault(1)
        if (effectsEnabled == 0) return
        soundPool.play(clickSound, 0.5f, 0.5f, 1, 0, 1f)
    }
}
