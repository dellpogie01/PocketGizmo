package com.juanmediahubph.pocketgizmo.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

data class CameraPermissionState(
    val granted: Boolean,
    val permanentlyDenied: Boolean,
    val request: () -> Unit,
    val openSettings: () -> Unit,
)

private object CameraPermissionPromptMemory {
    @Volatile
    var askedThisProcess = false
}

/**
 * CAMERA is requested the first time an LED tool is opened in this process.
 * A previously denied permission never blocks the rest of the app; the tool
 * shows a recovery card instead.
 */
@Composable
fun rememberCameraPermission(): CameraPermissionState {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(context.hasCameraPermission()) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { result ->
        granted = result
        permanentlyDenied = !result && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA,
            )
    }

    val openSettings = remember(context, activity) {
        {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            )
            if (activity == null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { (activity ?: context).startActivity(intent) }
            Unit
        }
    }

    val request = remember(launcher, permanentlyDenied, openSettings) {
        {
            if (permanentlyDenied) {
                openSettings()
            } else {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!granted && !CameraPermissionPromptMemory.askedThisProcess) {
            CameraPermissionPromptMemory.askedThisProcess = true
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = context.hasCameraPermission()
                if (granted) permanentlyDenied = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return CameraPermissionState(
        granted = granted,
        permanentlyDenied = permanentlyDenied,
        request = request,
        openSettings = openSettings,
    )
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
