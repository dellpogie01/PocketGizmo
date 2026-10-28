package com.juanmediahubph.pocketgizmo.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.domain.SosPattern
import com.juanmediahubph.pocketgizmo.hardware.TorchControl
import com.juanmediahubph.pocketgizmo.hardware.TorchState
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SosViewModel(private val torch: TorchControl) : ViewModel() {

    val torchState: StateFlow<TorchState> = torch.state

    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active.asStateFlow()

    private var job: Job? = null

    fun start() {
        if (job != null) return
        torch.clearError()
        if (!torch.ensureAvailable()) return

        _active.value = true
        val newJob = viewModelScope.launch(start = CoroutineStart.LAZY) {
            val self = coroutineContext[Job]
            var failed = false
            try {
                while (isActive && !failed) {
                    for (step in SosPattern.steps) {
                        if (!torch.setTorch(step.on)) {
                            failed = true
                            break
                        }
                        delay(step.units * SosPattern.UNIT_MILLIS)
                    }
                }
            } finally {
                torch.forceOff()
                if (job === self) {
                    job = null
                    _active.value = false
                }
            }
        }
        job = newJob
        newJob.start()
    }

    fun stop() {
        job?.cancel()
        job = null
        torch.forceOff()
        _active.value = false
    }

    override fun onCleared() {
        stop()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SosViewModel(container.torchController) }
        }
    }
}
