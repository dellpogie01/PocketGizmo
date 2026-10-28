package com.juanmediahubph.pocketgizmo.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.juanmediahubph.pocketgizmo.domain.CompassMath

data class CompassReading(
    val heading: Float,
    val accuracy: Int,
    val usingRotationVector: Boolean,
)

/**
 * Reads the rotation vector when the device has one, otherwise falls back to
 * accelerometer + magnetometer. Registers listeners only between [start] and
 * [stop]; smoothing happens here, outside the UI.
 */
class CompassSensorSource(context: Context) {

    private val appContext = context.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val rotationVector: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val accelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val magnetometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val isSupported: Boolean =
        rotationVector != null || (accelerometer != null && magnetometer != null)

    val usesRotationVector: Boolean = rotationVector != null

    private var onReading: ((CompassReading) -> Unit)? = null
    private var previousHeading: Float? = null
    private var started = false

    private var accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
    private var usingRotationVector = rotationVector != null

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private val acceleration = FloatArray(3)
    private val magneticField = FloatArray(3)
    private var hasAcceleration = false
    private var hasMagneticField = false

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    usingRotationVector = true
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    publish(rotationMatrix)
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    event.values.copyInto(acceleration)
                    hasAcceleration = true
                    publishFallbackIfReady()
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    event.values.copyInto(magneticField)
                    hasMagneticField = true
                    publishFallbackIfReady()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, newAccuracy: Int) {
            accuracy = newAccuracy
            onReading?.let { callback ->
                val heading = previousHeading
                if (heading != null) {
                    callback(
                        CompassReading(
                            heading = heading,
                            accuracy = newAccuracy,
                            usingRotationVector = usingRotationVector,
                        ),
                    )
                }
            }
        }
    }

    private fun publishFallbackIfReady() {
        if (usingRotationVector || !hasAcceleration || !hasMagneticField) return
        val usable = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            acceleration,
            magneticField,
        )
        if (usable) publish(rotationMatrix)
    }

    private fun publish(matrix: FloatArray) {
        SensorManager.getOrientation(matrix, orientation)
        val rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val heading = CompassMath.headingFromAzimuth(rawAzimuth, displayRotation())
        previousHeading = CompassMath.smooth(previousHeading, heading)
        onReading?.invoke(
            CompassReading(
                heading = previousHeading ?: heading,
                accuracy = accuracy,
                usingRotationVector = usingRotationVector,
            ),
        )
    }

    @Suppress("DEPRECATION")
    private fun displayRotation(): Int {
        val windowManager =
            appContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        return windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    }

    fun start(callback: (CompassReading) -> Unit) {
        if (!isSupported || started) return
        val manager = sensorManager ?: return
        onReading = callback
        previousHeading = null
        started = true
        accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE

        if (rotationVector != null) {
            usingRotationVector = true
            manager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_UI)
        } else {
            usingRotationVector = false
            accelerometer?.let {
                manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
            }
            magnetometer?.let {
                manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stop() {
        if (!started) return
        started = false
        onReading = null
        sensorManager?.unregisterListener(listener)
    }
}
