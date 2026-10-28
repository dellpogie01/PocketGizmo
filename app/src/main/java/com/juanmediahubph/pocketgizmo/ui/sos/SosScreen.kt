package com.juanmediahubph.pocketgizmo.ui.sos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun SosScreen(
    viewModel: SosViewModel,
    onBack: () -> Unit,
    onOpenScreenLight: () -> Unit,
) {
    val active by viewModel.active.collectAsStateWithLifecycle()
    val torchState by viewModel.torchState.collectAsStateWithLifecycle()
    val permission = rememberCameraPermission()
    val tap = rememberTapFeedback()
    val lifecycleOwner = LocalLifecycleOwner.current
    val palette = LocalGizmoPalette.current
    var showConfirm by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stop()
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.sos_confirm_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.sos_confirm_message),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirm = false
                        tap()
                        viewModel.start()
                    },
                    modifier = Modifier.heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(stringResource(R.string.sos_start))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirm = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    GizmoToolScaffold(
        title = stringResource(R.string.sos_title),
        onBack = {
            viewModel.stop()
            onBack()
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = if (active) 116.dp else 24.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SosStatusBadge(active = active)

                if (torchState.available == false) {
                    UnavailablePanel(
                        title = stringResource(R.string.sos_unavailable_title),
                        body = stringResource(R.string.sos_unavailable_body),
                        iconRes = R.drawable.ic_tool_sos,
                        actionLabel = stringResource(R.string.open_screen_light),
                        onAction = {
                            tap()
                            onOpenScreenLight()
                        },
                    )
                } else {
                    if (!permission.granted) {
                        CameraPermissionCard(
                            blocked = permission.permanentlyDenied,
                            onAllow = permission.request,
                            onOpenSettings = permission.openSettings,
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.ic_tool_sos),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(88.dp),
                        tint = if (active) {
                            palette.sos.accent
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )

                    Text(
                        text = stringResource(R.string.sos_intro),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    if (!active) {
                        Button(
                            onClick = {
                                tap()
                                if (!permission.granted) {
                                    permission.request()
                                } else {
                                    showConfirm = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_tool_sos),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = stringResource(R.string.sos_start),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }

                    torchState.error?.let { error ->
                        InfoCard(
                            text = sosErrorText(error),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        )
                    }
                }

                InfoCard(
                    text = stringResource(R.string.sos_sensitivity),
                    icon = painterResource(R.drawable.ic_tool_sos),
                    containerColor = palette.sos.container,
                )
                InfoCard(text = stringResource(R.string.sos_limit))
                InfoCard(text = stringResource(R.string.sos_foreground_note))
            }

            if (active) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 8.dp,
                ) {
                    Button(
                        onClick = {
                            tap()
                            viewModel.stop()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_stop),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = stringResource(R.string.sos_stop),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SosStatusBadge(active: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (active) {
            MaterialTheme.colorScheme.errorContainer
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
                painter = painterResource(R.drawable.ic_tool_sos),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (active) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = stringResource(
                    if (active) R.string.sos_active else R.string.sos_inactive,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (active) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun sosErrorText(error: TorchError): String = when (error) {
    TorchError.NO_FLASH -> stringResource(R.string.sos_unavailable_body)
    TorchError.CAMERA_IN_USE -> stringResource(R.string.torch_error_in_use)
    TorchError.PERMISSION_DENIED -> stringResource(R.string.torch_permission_blocked_body)
    TorchError.UNAVAILABLE -> stringResource(R.string.torch_error_unavailable)
}
