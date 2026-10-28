package com.juanmediahubph.pocketgizmo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.data.repository.SettingsRepository
import com.juanmediahubph.pocketgizmo.data.repository.StopwatchRepository
import com.juanmediahubph.pocketgizmo.domain.AppSettings
import com.juanmediahubph.pocketgizmo.domain.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val stopwatchRepository: StopwatchRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setButtonSounds(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setButtonSounds(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHaptics(enabled) }
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setKeepScreenAwake(enabled) }
    }

    fun resetSettings() {
        viewModelScope.launch { settingsRepository.resetSettings() }
    }

    fun clearStopwatchData() {
        viewModelScope.launch { stopwatchRepository.clearAll() }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    stopwatchRepository = container.stopwatchRepository,
                )
            }
        }
    }
}
