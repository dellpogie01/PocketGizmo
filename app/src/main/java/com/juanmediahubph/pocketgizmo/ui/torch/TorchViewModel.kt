package com.juanmediahubph.pocketgizmo.ui.torch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.hardware.TorchController
import com.juanmediahubph.pocketgizmo.hardware.TorchState
import kotlinx.coroutines.flow.StateFlow

class TorchViewModel(private val controller: TorchController) : ViewModel() {

    val state: StateFlow<TorchState> = controller.state

    init {
        controller.refreshAvailability()
    }

    fun toggle() {
        if (controller.state.value.on) {
            controller.turnOff()
        } else {
            controller.ensureAvailable()
            controller.turnOn()
        }
    }

    fun clearError() = controller.clearError()

    fun stopForLifecycle() = controller.forceOff()

    override fun onCleared() {
        controller.forceOff()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { TorchViewModel(container.torchController) }
        }
    }
}
