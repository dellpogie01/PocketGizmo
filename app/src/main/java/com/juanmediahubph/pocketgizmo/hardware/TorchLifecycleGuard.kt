package com.juanmediahubph.pocketgizmo.hardware

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Safety net: if a tool screen is stopped (home button, screen lock, task
 * switch) the LED is turned off even before a composable cleanup can run.
 */
class TorchLifecycleGuard(
    private val torchController: TorchController,
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityStopped(activity: Activity) {
        torchController.forceOff()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
