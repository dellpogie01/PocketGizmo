package com.juanmediahubph.pocketgizmo.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.domain.AppSettings

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer was not provided")
}

val LocalAppSettings = compositionLocalOf { AppSettings() }
