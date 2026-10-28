package com.juanmediahubph.pocketgizmo.hardware

import android.os.SystemClock

interface MonotonicClock {
    fun elapsedRealtime(): Long
    fun wallClockMillis(): Long
}

class SystemMonotonicClock : MonotonicClock {
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
    override fun wallClockMillis(): Long = System.currentTimeMillis()
}
