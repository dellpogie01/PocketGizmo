package com.juanmediahubph.pocketgizmo.domain

import kotlin.math.abs

object CompassMath {

    private val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    fun normalize(degrees: Float): Float {
        val wrapped = degrees % 360f
        return if (wrapped < 0f) wrapped + 360f else wrapped
    }

    fun shortestDelta(from: Float, to: Float): Float {
        var delta = normalize(to) - normalize(from)
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }

    /** Exponential smoothing that always takes the short way around north. */
    fun smooth(previous: Float?, target: Float, alpha: Float = 0.18f): Float {
        if (previous == null) return normalize(target)
        val safeAlpha = alpha.coerceIn(0f, 1f)
        return normalize(previous + shortestDelta(previous, target) * safeAlpha)
    }

    fun directionLabel(heading: Float): String {
        val index = (((normalize(heading) + 22.5f) / 45f).toInt()) % 8
        return directions[index]
    }

    fun rotationOffsetDegrees(rotation: Int): Float = when (rotation) {
        1 -> 90f   // Surface.ROTATION_90
        2 -> 180f  // Surface.ROTATION_180
        3 -> 270f  // Surface.ROTATION_270
        else -> 0f
    }

    fun headingFromAzimuth(azimuthDegrees: Float, displayRotation: Int): Float =
        normalize(azimuthDegrees + rotationOffsetDegrees(displayRotation))

    /** True when the device is close enough to flat for the fallback to be trustworthy. */
    fun isUsableTilt(accelerometerZ: Float, gravity: Float = 9.81f): Boolean =
        abs(accelerometerZ) > gravity * 0.35f
}
