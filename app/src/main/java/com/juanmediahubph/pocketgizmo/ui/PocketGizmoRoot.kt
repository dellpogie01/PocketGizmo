package com.juanmediahubph.pocketgizmo.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.domain.AppSettings
import com.juanmediahubph.pocketgizmo.ui.compass.CompassScreen
import com.juanmediahubph.pocketgizmo.ui.compass.CompassViewModel
import com.juanmediahubph.pocketgizmo.ui.home.HomeScreen
import com.juanmediahubph.pocketgizmo.ui.level.LevelScreen
import com.juanmediahubph.pocketgizmo.ui.level.LevelViewModel
import com.juanmediahubph.pocketgizmo.ui.screenlight.ScreenLightScreen
import com.juanmediahubph.pocketgizmo.ui.screenlight.ScreenLightViewModel
import com.juanmediahubph.pocketgizmo.ui.settings.AboutScreen
import com.juanmediahubph.pocketgizmo.ui.settings.SettingsScreen
import com.juanmediahubph.pocketgizmo.ui.settings.SettingsViewModel
import com.juanmediahubph.pocketgizmo.ui.sos.SosScreen
import com.juanmediahubph.pocketgizmo.ui.sos.SosViewModel
import com.juanmediahubph.pocketgizmo.ui.stopwatch.StopwatchScreen
import com.juanmediahubph.pocketgizmo.ui.stopwatch.StopwatchViewModel
import com.juanmediahubph.pocketgizmo.ui.torch.TorchScreen
import com.juanmediahubph.pocketgizmo.ui.torch.TorchViewModel

@Composable
fun PocketGizmoNavHost(
    container: AppContainer,
    settings: AppSettings,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenTool = { route ->
                    navController.navigate(route) { launchSingleTop = true }
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                },
            )
        }

        composable(Routes.TORCH) {
            val viewModel: TorchViewModel = viewModel(
                factory = TorchViewModel.factory(container),
            )
            TorchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenScreenLight = {
                    navController.navigate(Routes.SCREEN_LIGHT) { launchSingleTop = true }
                },
            )
        }

        composable(Routes.SOS) {
            val viewModel: SosViewModel = viewModel(
                factory = SosViewModel.factory(container),
            )
            SosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenScreenLight = {
                    navController.navigate(Routes.SCREEN_LIGHT) { launchSingleTop = true }
                },
            )
        }

        composable(Routes.SCREEN_LIGHT) {
            val viewModel: ScreenLightViewModel = viewModel(
                factory = ScreenLightViewModel.factory(container, settings),
            )
            ScreenLightScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.COMPASS) {
            val viewModel: CompassViewModel = viewModel(
                factory = CompassViewModel.factory(container),
            )
            CompassScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.LEVEL) {
            val viewModel: LevelViewModel = viewModel(
                factory = LevelViewModel.factory(container, settings),
            )
            LevelScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.STOPWATCH) {
            val viewModel: StopwatchViewModel = viewModel(
                factory = StopwatchViewModel.factory(container),
            )
            StopwatchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(container),
            )
            SettingsScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onOpenAbout = {
                    navController.navigate(Routes.ABOUT) { launchSingleTop = true }
                },
                onThemeChange = viewModel::setTheme,
                onButtonSoundsChange = viewModel::setButtonSounds,
                onHapticsChange = viewModel::setHaptics,
                onKeepAwakeChange = viewModel::setKeepScreenAwake,
                onResetSettings = viewModel::resetSettings,
                onClearStopwatch = viewModel::clearStopwatchData,
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
