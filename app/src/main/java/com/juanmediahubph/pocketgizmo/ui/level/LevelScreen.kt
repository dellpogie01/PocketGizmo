package com.juanmediahubph.pocketgizmo.ui.level

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanmediahubph.pocketgizmo.R
import com.juanmediahubph.pocketgizmo.ui.components.GizmoToolScaffold
import com.juanmediahubph.pocketgizmo.ui.components.InfoCard
import com.juanmediahubph.pocketgizmo.ui.components.UnavailablePanel
import com.juanmediahubph.pocketgizmo.ui.components.rememberTapFeedback
import com.juanmediahubph.pocketgizmo.ui.theme.LocalGizmoPalette
import kotlin.math.sqrt

@Composable
fun LevelScreen(
    viewModel: LevelViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val palette = LocalGizmoPalette.current
    val tap = rememberTapFeedback()
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.start()
                Lifecycle.Event.ON_STOP -> viewModel.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            viewModel.start()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stop()
        }
    }

    GizmoToolScaffold(
        title = stringResource(R.string.tool_level),
        onBack = {
            viewModel.stop()
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
            if (!state.supported) {
                UnavailablePanel(
                    title = stringResource(R.string.level_unavailable_title),
                    body = stringResource(R.string.level_unavailable_body),
                    iconRes = R.drawable.ic_tool_level,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    LevelDial(
                        tiltX = state.tiltX,
                        tiltY = state.tiltY,
                        isLevel = state.isLevel,
                        hasReading = state.hasReading,
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    )
                }

                val statusText = when {
                    !state.hasReading -> stringResource(R.string.level_waiting)
                    state.isLevel -> stringResource(R.string.level_status_level)
                    else -> stringResource(R.string.level_status_tilted)
                }
                val statusIcon = if (state.hasReading && state.isLevel) {
                    R.drawable.ic_check
                } else {
                    R.drawable.ic_tool_level
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    shape = MaterialTheme.shapes.large,
                    color = if (state.hasReading && state.isLevel) {
                        palette.level.container
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(statusIcon),
                            contentDescription = null,
                            tint = if (state.hasReading && state.isLevel) {
                                palette.level.accent
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LevelValueCard(
                        label = stringResource(R.string.level_horizontal),
                        value = if (state.hasReading) state.tiltX else null,
                        modifier = Modifier.weight(1f),
                    )
                    LevelValueCard(
                        label = stringResource(R.string.level_vertical),
                        value = if (state.hasReading) state.tiltY else null,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = {
                            tap()
                            viewModel.calibrate()
                            Toast.makeText(
                                context,
                                R.string.level_calibrated,
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        enabled = state.hasReading,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.level_calibrate),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            tap()
                            viewModel.resetCalibration()
                        },
                        enabled = state.calibrationActive,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.level_reset_calibration),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                InfoCard(
                    text = stringResource(R.string.level_tolerance_note),
                    icon = painterResource(R.drawable.ic_tool_level),
                )
                InfoCard(text = stringResource(R.string.level_calibration_note))
                InfoCard(text = stringResource(R.string.level_approx_note))
            }
        }
    }
}

@Composable
private fun LevelValueCard(
    label: String,
    value: Float?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (value != null) {
                    stringResource(R.string.level_degrees, value)
                } else {
                    stringResource(R.string.value_placeholder)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun LevelDial(
    tiltX: Float,
    tiltY: Float,
    isLevel: Boolean,
    hasReading: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val accent = LocalGizmoPalette.current.level.accent
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outer = size.minDimension / 2f - 4.dp.toPx()

        drawCircle(color = scheme.surfaceContainerLow, radius = outer, center = center)
        drawCircle(
            color = scheme.outlineVariant,
            radius = outer,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )
        drawLine(
            color = scheme.outlineVariant.copy(alpha = 0.7f),
            start = Offset(center.x - outer * 0.62f, center.y),
            end = Offset(center.x + outer * 0.62f, center.y),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = scheme.outlineVariant.copy(alpha = 0.7f),
            start = Offset(center.x, center.y - outer * 0.62f),
            end = Offset(center.x, center.y + outer * 0.62f),
            strokeWidth = 1.dp.toPx(),
        )
        drawCircle(
            color = scheme.outlineVariant.copy(alpha = 0.55f),
            radius = outer * 0.62f,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )

        val toleranceColor = if (hasReading && isLevel) scheme.primary else scheme.outline
        drawCircle(
            color = toleranceColor,
            radius = outer * 0.16f,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )

        val magnitude = sqrt(tiltX * tiltX + tiltY * tiltY)
        val clampedScale = if (magnitude > MAX_TILT_DEGREES) {
            MAX_TILT_DEGREES / magnitude
        } else {
            1f
        }
        val bubbleRadius = outer * 0.14f
        val travel = outer * 0.78f - bubbleRadius
        val bubbleCenter = Offset(
            x = center.x + (tiltX * clampedScale / MAX_TILT_DEGREES) * travel,
            y = center.y - (tiltY * clampedScale / MAX_TILT_DEGREES) * travel,
        )
        val bubbleColor = when {
            !hasReading -> scheme.onSurfaceVariant.copy(alpha = 0.55f)
            isLevel -> scheme.primary
            else -> accent
        }

        drawCircle(
            color = scheme.surface,
            radius = bubbleRadius + 3.dp.toPx(),
            center = bubbleCenter,
        )
        drawCircle(
            color = bubbleColor,
            radius = bubbleRadius,
            center = bubbleCenter,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = bubbleRadius * 0.34f,
            center = Offset(
                bubbleCenter.x - bubbleRadius * 0.3f,
                bubbleCenter.y - bubbleRadius * 0.32f,
            ),
        )
    }
}

private const val MAX_TILT_DEGREES = 12f
