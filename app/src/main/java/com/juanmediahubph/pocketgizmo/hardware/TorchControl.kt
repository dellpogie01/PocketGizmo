package com.juanmediahubph.pocketgizmo.hardware

import kotlinx.coroutines.flow.StateFlow

/**
 * The torch operations shared by the Torch and SOS tools. Keeping this small
 * interface lets the SOS timer be tested without camera hardware.
 */
interface TorchControl {
    val state: StateFlow<TorchState>

    fun clearError()

    fun ensureAvailable(): Boolean

    fun setTorch(on: Boolean): Boolean

    fun forceOff()
}
