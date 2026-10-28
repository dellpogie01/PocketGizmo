package com.juanmediahubph.pocketgizmo.ui.screenlight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.data.repository.SettingsRepository
import com.juanmediahubph.pocketgizmo.domain.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScreenLightViewModel(
    private val repository: SettingsRepository,
    initial: AppSettings,
) : ViewModel() {

    private val _color = MutableStateFlow(initial.screenLightColor)
    val color: StateFlow<Long> = _color.asStateFlow()

    private val _brightness = MutableStateFlow(initial.screenLightBrightness)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _keepAwake = MutableStateFlow(initial.keepScreenAwake)
    val keepAwake: StateFlow<Boolean> = _keepAwake.asStateFlow()

    fun selectColor(color: Long) {
        _color.value = color
        persist()
    }

    fun setKeepAwake(enabled: Boolean) {
        _keepAwake.value = enabled
        viewModelScope.launch { repository.setKeepScreenAwake(enabled) }
    }

    fun previewBrightness(value: Float) {
        _brightness.value = value.coerceIn(MIN_BRIGHTNESS, 1f)
    }

    fun commitBrightness() {
        persist()
    }

    private fun persist() {
        val color = _color.value
        val brightness = _brightness.value
        viewModelScope.launch {
            repository.setScreenLight(color, brightness)
        }
    }

    companion object {
        const val MIN_BRIGHTNESS = 0.05f

        fun factory(
            container: AppContainer,
            initial: AppSettings,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ScreenLightViewModel(container.settingsRepository, initial)
            }
        }
    }
}
