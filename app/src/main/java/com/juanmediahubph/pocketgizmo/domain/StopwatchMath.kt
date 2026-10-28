package com.juanmediahubph.pocketgizmo.domain

import java.util.Locale
import kotlin.math.abs

object StopwatchMath {

    private const val BOOT_REFERENCE_TOLERANCE_MILLIS = 10_000L

    fun bootReference(wallClockMillis: Long, elapsedRealtimeMillis: Long): Long =
        wallClockMillis - elapsedRealtimeMillis

    fun elapsedMillis(
        running: Boolean,
        accumulatedMillis: Long,
        anchorElapsedRealtime: Long,
        nowElapsedRealtime: Long,
    ): Long = if (running) {
        accumulatedMillis + (nowElapsedRealtime - anchorElapsedRealtime).coerceAtLeast(0L)
    } else {
        accumulatedMillis
    }

    fun wasInterruptedByReboot(
        running: Boolean,
        anchorElapsedRealtime: Long,
        storedBootReference: Long,
        nowElapsedRealtime: Long,
        nowBootReference: Long,
    ): Boolean {
        if (!running) return false
        if (anchorElapsedRealtime > nowElapsedRealtime) return true
        return abs(nowBootReference - storedBootReference) > BOOT_REFERENCE_TOLERANCE_MILLIS
    }

    fun format(millis: Long): String {
        val safe = millis.coerceAtLeast(0L)
        val totalSeconds = safe / 1000
        val hundredths = (safe % 1000) / 10
        val seconds = totalSeconds % 60
        val totalMinutes = totalSeconds / 60
        val minutes = totalMinutes % 60
        val hours = totalMinutes / 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d.%02d", hours, minutes, seconds, hundredths)
        } else {
            String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)
        }
    }
}
