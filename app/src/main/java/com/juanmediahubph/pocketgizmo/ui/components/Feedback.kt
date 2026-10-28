package com.juanmediahubph.pocketgizmo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberTapFeedback(): () -> Unit {
    val container = LocalAppContainer.current
    val settings = LocalAppSettings.current
    val view = LocalView.current
    val haptics = settings.haptics
    val sounds = settings.buttonSounds
    return remember(container, view, haptics, sounds) {
        { container.feedbackController.tap(view, haptics, sounds) }
    }
}
