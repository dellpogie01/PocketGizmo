package com.juanmediahubph.pocketgizmo.ui.level

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.data.repository.SettingsRepository
import com.juanmediahubph.pocketgizmo.domain.AppSettings
import com.juanmediahubph.pocketgizmo.hardware.LevelReading
import com.juanmediahubph.pocketgizmo.hardware.LevelSensorSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

data class LevelUiState(
    val supported: Boolean,
    val hasReading: Boolean = false,
    val tiltX: Float = 0f,
    val tiltY: Float = 0f,
    val isLevel: Boolean = true,
    val calibrationActive: Boolean = false,
    val calibrationVersion: Int = 0,
)

private data class Offsets(val x: Float, val y: Float)

class LevelViewModel(
    private val source: LevelSensorSource,
    private val repository: SettingsRepository,
    initial: AppSettings,
) : ViewModel() {

    private val reading = MutableStateFlow<LevelReading?>(null)
    private val offsets = MutableStateFlow(
        Offsets(initial.levelOffsetX, initial.levelOffsetY),
    )
    private val calibrationVersion = MutableStateFlow(0)
    private var started = false

    val uiState: StateFlow<LevelUiState> = combine(
        reading,
        offsets,
        calibrationVersion,
    ) { current, currentOffsets, version ->
        if (current == null) {
            LevelUiState(
                supported = source.isSupported,
                calibrationActive = currentOffsets.x != 0f || currentOffsets.y != 0f,
                calibrationVersion = version,
            )
        } else {
            val tiltX = current.tiltX - currentOffsets.x
            val tiltY = current.tiltY - currentOffsets.y
            val level = abs(tiltX) <= TOLERANCE_DEGREES && abs(tiltY) <= TOLERANCE_DEGREES
            LevelUiState(
                supported = source.isSupported,
                hasReading = true,
                tiltX = tiltX,
                tiltY = tiltY,
                isLevel = level,
                calibrationActive = currentOffsets.x != 0f || currentOffsets.y != 0f,
                calibrationVersion = version,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LevelUiState(supported = source.isSupported),
    )

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                offsets.value = Offsets(settings.levelOffsetX, settings.levelOffsetY)
            }
        }
    }

    fun start() {
        if (started || !source.isSupported) return
        started = true
        source.start { reading.value = it }
    }

    fun stop() {
        if (!started) return
        started = false
        source.stop()
    }

    fun calibrate() {
        val current = reading.value ?: return
        viewModelScope.launch {
            repository.setLevelCalibration(current.tiltX, current.tiltY)
            calibrationVersion.value += 1
        }
    }

    fun resetCalibration() {
        viewModelScope.launch {
            repository.setLevelCalibration(0f, 0f)
            offsets.value = Offsets(0f, 0f)
        }
    }

    override fun onCleared() {
        stop()
    }

    companion object {
        const val TOLERANCE_DEGREES = 1f

        fun magnitude(tiltX: Float, tiltY: Float): Float =
            sqrt(tiltX * tiltX + tiltY * tiltY)

        fun factory(
            container: AppContainer,
            initial: AppSettings,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LevelViewModel(
                    source = LevelSensorSource(container.appContext),
                    repository = container.settingsRepository,
                    initial = initial,
                )
            }
        }
    }
}
