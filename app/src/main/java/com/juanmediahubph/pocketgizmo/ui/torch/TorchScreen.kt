package com.juanmediahubph.pocketgizmo.ui.torch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanmediahubph.pocketgizmo.R
import com.juanmediahubph.pocketgizmo.hardware.TorchError
import com.juanmediahubph.pocketgizmo.ui.components.CameraPermissionCard
import com.juanmediahubph.pocketgizmo.ui.components.GizmoToolScaffold
import com.juanmediahubph.pocketgizmo.ui.components.InfoCard
import com.juanmediahubph.pocketgizmo.ui.components.UnavailablePanel
import com.juanmediahubph.pocketgizmo.ui.components.rememberCameraPermission
import com.juanmediahubph.pocketgizmo.ui.components.rememberTapFeedback
import com.juanmediahubph.pocketgizmo.ui.theme.LocalGizmoPalette

@Composable
fun TorchScreen(
    viewModel: TorchViewModel,
    onBack: () -> Unit,
    onOpenScreenLight: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberCameraPermission()
    val tap = rememberTapFeedback()
    val lifecycleOwner = LocalLifecycleOwner.current
    val palette = LocalGizmoPalette.current
    val torchPalette = palette.torch

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopForLifecycle()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopForLifecycle()
        }
    }

    GizmoToolScaffold(
        title = stringResource(R.string.tool_torch),
        onBack = {
            viewModel.stopForLifecycle()
            onBack()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (!permission.granted) {
                CameraPermissionCard(
                    blocked = permission.permanentlyDenied,
                    onAllow = permission.request,
                    onOpenSettings = permission.openSettings,
                )
            }

            if (state.available == false) {
                UnavailablePanel(
                    title = stringResource(R.string.torch_unavailable_title),
                    body = stringResource(R.string.torch_unavailable_body),
                    iconRes = R.drawable.ic_tool_torch,
                    actionLabel = stringResource(R.string.open_screen_light),
                    onAction = {
                        tap()
                        onOpenScreenLight()
                    },
                )
            } else {
                val toggleLabel = stringResource(
                    if (state.on) R.string.torch_turn_off else R.string.torch_turn_on,
                )
                StatusBadge(
                    on = state.on,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(Modifier.height(4.dp))

                Surface(
                    onClick = {
                        tap()
                        viewModel.toggle()
                    },
                    modifier = Modifier
                        .size(184.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = toggleLabel
                        },
                    shape = CircleShape,
                    color = if (state.on) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (state.on) {
                        MaterialTheme.colorScheme.onTertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    shadowElevation = if (state.on) 10.dp else 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_tool_torch),
                            contentDescription = null,
                            modifier = Modifier.size(78.dp),
                        )
                    }
                }

                Text(
                    text = toggleLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (state.error != null) {
                    ErrorCard(
                        error = state.error!!,
                        onAllow = permission.request,
                        onOpenSettings = permission.openSettings,
                    )
                }

                Spacer(Modifier.height(2.dp))
                InfoCard(
                    text = stringResource(R.string.torch_battery_note),
                    icon = painterResource(R.drawable.ic_tool_torch),
                    containerColor = palette.torch.container,
                )
                InfoCard(
                    text = stringResource(R.string.torch_foreground_note),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(on: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (on) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tool_torch),
                contentDescription = null,
                tint = if (on) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = stringResource(if (on) R.string.torch_status_on else R.string.torch_status_off),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (on) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun ErrorCard(
    error: TorchError,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val text = when (error) {
        TorchError.NO_FLASH -> stringResource(R.string.torch_unavailable_body)
        TorchError.CAMERA_IN_USE -> stringResource(R.string.torch_error_in_use)
        TorchError.PERMISSION_DENIED -> stringResource(R.string.torch_permission_blocked_body)
        TorchError.UNAVAILABLE -> stringResource(R.string.torch_error_unavailable)
    }
    if (error == TorchError.PERMISSION_DENIED) {
        CameraPermissionCard(
            blocked = true,
            onAllow = onAllow,
            onOpenSettings = onOpenSettings,
        )
    } else {
        InfoCard(
            text = text,
            containerColor = MaterialTheme.colorScheme.errorContainer,
        )
    }
}
