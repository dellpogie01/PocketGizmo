package com.juanmediahubph.pocketgizmo

import android.app.Application
import android.content.Context
import com.juanmediahubph.pocketgizmo.data.local.AppDatabase
import com.juanmediahubph.pocketgizmo.data.repository.SettingsRepository
import com.juanmediahubph.pocketgizmo.data.repository.StopwatchRepository
import com.juanmediahubph.pocketgizmo.hardware.FeedbackController
import com.juanmediahubph.pocketgizmo.hardware.MonotonicClock
import com.juanmediahubph.pocketgizmo.hardware.SystemMonotonicClock
import com.juanmediahubph.pocketgizmo.hardware.TorchController
import com.juanmediahubph.pocketgizmo.hardware.TorchLifecycleGuard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PocketGizmoApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        registerActivityLifecycleCallbacks(container.torchLifecycleGuard)
    }
}

class AppContainer(context: Context) {

    val appContext: Context = context.applicationContext

    private val database: AppDatabase = AppDatabase.getInstance(appContext)

    val settingsRepository = SettingsRepository(database.settingsDao())

    val stopwatchRepository = StopwatchRepository(database.stopwatchDao())

    val torchController = TorchController(appContext)

    val feedbackController = FeedbackController(appContext)

    val clock: MonotonicClock = SystemMonotonicClock()

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val torchLifecycleGuard = TorchLifecycleGuard(torchController)

    init {
        appScope.launch {
            settingsRepository.ensureDefaults()
        }
    }
}
