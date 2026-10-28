package com.juanmediahubph.pocketgizmo.ui.compass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.hardware.CompassReading
import com.juanmediahubph.pocketgizmo.hardware.CompassSensorSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompassViewModel(
    private val source: CompassSensorSource,
) : ViewModel() {

    val isSupported: Boolean = source.isSupported
    val usesRotationVector: Boolean = source.usesRotationVector

    private val _reading = MutableStateFlow<CompassReading?>(null)
    val reading: StateFlow<CompassReading?> = _reading.asStateFlow()

    private var started = false

    fun start() {
        if (started || !isSupported) return
        started = true
        source.start { reading -> _reading.value = reading }
    }

    fun stop() {
        if (!started) return
        started = false
        source.stop()
    }

    override fun onCleared() {
        stop()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { CompassViewModel(CompassSensorSource(container.appContext)) }
        }
    }
}
