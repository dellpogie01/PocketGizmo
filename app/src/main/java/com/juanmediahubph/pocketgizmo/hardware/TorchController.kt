package com.juanmediahubph.pocketgizmo.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TorchError {
    NO_FLASH,
    CAMERA_IN_USE,
    PERMISSION_DENIED,
    UNAVAILABLE,
}

data class TorchState(
    val available: Boolean? = null,
    val on: Boolean = false,
    val error: TorchError? = null,
)

/**
 * Single owner of the LED flash. Torch and SOS share it so the two tools can
 * never drive the flash at the same time. The LED is only ever on while a tool
 * screen is in the foreground; the screen is responsible for calling [turnOff].
 */
class TorchController(context: Context) : TorchControl {

    private val appContext = context.applicationContext
    private val cameraManager =
        appContext.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private val lock = Any()
    private val _state = MutableStateFlow(TorchState())
    override val state: StateFlow<TorchState> = _state.asStateFlow()

    private var resolvedCameraId: String? = null
    private var availabilityChecked = false

    fun refreshAvailability(): Boolean = synchronized(lock) {
        resolvedCameraId = findFlashCameraId()
        availabilityChecked = true
        _state.update { it.copy(available = resolvedCameraId != null) }
        resolvedCameraId != null
    }

    override fun ensureAvailable(): Boolean {
        synchronized(lock) {
            if (!availabilityChecked) {
                resolvedCameraId = findFlashCameraId()
                availabilityChecked = true
                _state.update { it.copy(available = resolvedCameraId != null) }
            }
            return resolvedCameraId != null
        }
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun turnOn(): Boolean = setTorch(true)

    fun turnOff(): Boolean = setTorch(false)

    override fun setTorch(on: Boolean): Boolean = applyTorch(on)

    override fun forceOff() {
        runCatching { applyTorch(false) }
        _state.update { it.copy(on = false) }
    }

    private fun applyTorch(on: Boolean): Boolean = synchronized(lock) {
        val manager = cameraManager
        if (manager == null) {
            _state.update { it.copy(available = false, on = false, error = TorchError.UNAVAILABLE) }
            return false
        }

        if (!availabilityChecked) {
            resolvedCameraId = findFlashCameraId()
            availabilityChecked = true
        }
        val cameraId = resolvedCameraId
        if (cameraId == null) {
            _state.update { it.copy(available = false, on = false, error = TorchError.NO_FLASH) }
            return false
        }

        return try {
            manager.setTorchMode(cameraId, on)
            _state.update { it.copy(available = true, on = on, error = null) }
            true
        } catch (error: CameraAccessException) {
            val mapped = when (error.reason) {
                CameraAccessException.CAMERA_IN_USE,
                CameraAccessException.MAX_CAMERAS_IN_USE,
                -> TorchError.CAMERA_IN_USE

                CameraAccessException.CAMERA_DISABLED,
                CameraAccessException.CAMERA_DISCONNECTED,
                -> TorchError.UNAVAILABLE

                else -> TorchError.UNAVAILABLE
            }
            Log.w(TAG, "Unable to switch torch $on", error)
            _state.update { it.copy(on = false, error = mapped) }
            false
        } catch (error: SecurityException) {
            Log.w(TAG, "Camera permission missing for torch", error)
            _state.update { it.copy(on = false, error = TorchError.PERMISSION_DENIED) }
            false
        } catch (error: IllegalArgumentException) {
            Log.w(TAG, "Camera has no usable flash", error)
            _state.update { it.copy(available = false, on = false, error = TorchError.NO_FLASH) }
            false
        }
    }

    private fun findFlashCameraId(): String? {
        val manager = cameraManager ?: return null
        if (!appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return null
        }
        return try {
            manager.cameraIdList.firstOrNull { id ->
                val characteristics = manager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                facing == CameraCharacteristics.LENS_FACING_BACK && hasFlash == true
            }
        } catch (error: CameraAccessException) {
            Log.w(TAG, "Unable to inspect cameras", error)
            null
        } catch (error: SecurityException) {
            Log.w(TAG, "No access to enumerate cameras", error)
            null
        }
    }

    private companion object {
        const val TAG = "TorchController"
    }
}
