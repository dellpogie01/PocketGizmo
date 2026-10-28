package com.juanmediahubph.pocketgizmo

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.juanmediahubph.pocketgizmo.domain.AppSettings
import com.juanmediahubph.pocketgizmo.domain.AppTheme
import com.juanmediahubph.pocketgizmo.ui.PocketGizmoNavHost
import com.juanmediahubph.pocketgizmo.ui.components.LocalAppContainer
import com.juanmediahubph.pocketgizmo.ui.components.LocalAppSettings
import com.juanmediahubph.pocketgizmo.ui.components.findActivity
import com.juanmediahubph.pocketgizmo.ui.theme.PocketGizmoTheme

class MainActivity : ComponentActivity() {

    private var contentReady = false
    private val startupStart = SystemClock.uptimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        val container = (application as PocketGizmoApp).container
        val firstLaunch = savedInstanceState == null && !SplashMemory.shown
        SplashMemory.shown = true

        splash.setKeepOnScreenCondition {
            val elapsed = SystemClock.uptimeMillis() - startupStart
            elapsed < MAX_SPLASH_MILLIS &&
                (!contentReady || (firstLaunch && elapsed < TARGET_SPLASH_MILLIS))
        }

        enableEdgeToEdge()

        setContent {
            val settingsFlow = container.settingsRepository.settings
            val loadedSettings by produceState<AppSettings?>(
                initialValue = null,
                key1 = settingsFlow,
            ) {
                settingsFlow.collect { value = it }
            }
            SideEffect { contentReady = loadedSettings != null }

            val settings = loadedSettings
            if (settings == null) {
                PocketGizmoTheme(darkTheme = isSystemInDarkTheme()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    )
                }
            } else {
                val darkTheme = when (settings.theme) {
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                }
                ApplySystemBarIcons(darkTheme = darkTheme)
                PocketGizmoTheme(darkTheme = darkTheme) {
                    CompositionLocalProvider(
                        LocalAppContainer provides container,
                        LocalAppSettings provides settings,
                    ) {
                        PocketGizmoNavHost(container = container, settings = settings)
                    }
                }
            }
        }
    }

    private companion object {
        // The cold-start splash is a short brand moment; warm starts skip the wait.
        const val TARGET_SPLASH_MILLIS = 2_600L
        const val MAX_SPLASH_MILLIS = 8_000L
    }
}

private object SplashMemory {
    @Volatile
    var shown: Boolean = false
}

@Composable
private fun ApplySystemBarIcons(darkTheme: Boolean) {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    SideEffect {
        if (activity == null) return@SideEffect
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
    }
}
