package com.juanmediahubph.pocketgizmo.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

data class LevelReading(
    val tiltX: Float,
    val tiltY: Float,
)

/**
 * Accelerometer bubble level. The gravity vector is low-pass filtered, mapped
 * to the current display rotation and turned into two small tilt angles.
 */
class LevelSensorSource(context: Context) {

    private val appContext = context.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val accelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isSupported: Boolean = accelerometer != null

    private val filtered = FloatArray(3)
    private var hasSample = false
    private var onReading: ((LevelReading) -> Unit)? = null
    private var started = false

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
            if (!hasSample) {
                event.values.copyInto(filtered)
                hasSample = true
            } else {
                for (index in 0..2) {
                    filtered[index] += (event.values[index] - filtered[index]) * SMOOTHING
                }
            }

            val rotation = displayRotation()
            val (horizontal, vertical) = when (rotation) {
                Surface.ROTATION_90 -> -filtered[1] to filtered[0]
                Surface.ROTATION_180 -> -filtered[0] to -filtered[1]
                Surface.ROTATION_270 -> filtered[1] to -filtered[0]
                else -> filtered[0] to filtered[1]
            }

            val verticalAxis = abs(filtered[2]).coerceAtLeast(0.5f)
            val tiltX = Math.toDegrees(atan2(horizontal, verticalAxis).toDouble()).toFloat()
            val tiltY = Math.toDegrees(atan2(vertical, verticalAxis).toDouble()).toFloat()
            onReading?.invoke(LevelReading(tiltX = tiltX, tiltY = tiltY))
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    @Suppress("DEPRECATION")
    private fun displayRotation(): Int {
        val windowManager =
            appContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        return windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    }

    fun start(callback: (LevelReading) -> Unit) {
        if (!isSupported || started) return
        val manager = sensorManager ?: return
        onReading = callback
        hasSample = false
        started = true
        manager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        if (!started) return
        started = false
        onReading = null
        sensorManager?.unregisterListener(listener)
    }

    companion object {
        private const val SMOOTHING = 0.16f

        fun magnitude(tiltX: Float, tiltY: Float): Float = sqrt(tiltX * tiltX + tiltY * tiltY)
    }
}
