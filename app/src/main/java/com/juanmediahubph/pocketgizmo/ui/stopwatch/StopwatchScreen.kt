package com.juanmediahubph.pocketgizmo.ui.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanmediahubph.pocketgizmo.R
import com.juanmediahubph.pocketgizmo.data.local.LapEntity
import com.juanmediahubph.pocketgizmo.domain.StopwatchMath
import com.juanmediahubph.pocketgizmo.ui.components.GizmoToolScaffold
import com.juanmediahubph.pocketgizmo.ui.components.InfoCard
import com.juanmediahubph.pocketgizmo.ui.components.rememberTapFeedback
import com.juanmediahubph.pocketgizmo.ui.theme.LocalGizmoPalette

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel,
    onBack: () -> Unit,
) {
    val elapsed by viewModel.elapsedMillis.collectAsStateWithLifecycle()
    val session by viewModel.sessionState.collectAsStateWithLifecycle()
    val laps by viewModel.laps.collectAsStateWithLifecycle()
    val loaded by viewModel.loaded.collectAsStateWithLifecycle()
    val tap = rememberTapFeedback()
    val palette = LocalGizmoPalette.current
    val stopwatchPalette = palette.stopwatch
    var showResetConfirm by rememberSaveable { mutableStateOf(false) }

    val hasContent = session.running || elapsed > 0L || laps.isNotEmpty()
    val resetEnabled = loaded && hasContent

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = {
                Text(
                    text = stringResource(R.string.stopwatch_reset_confirm_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.stopwatch_reset_confirm_body),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tap()
                        showResetConfirm = false
                        viewModel.reset()
                    },
                    modifier = Modifier.heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(stringResource(R.string.stopwatch_reset))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirm = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    GizmoToolScaffold(
        title = stringResource(R.string.tool_stopwatch),
        onBack = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                StopwatchStatusBadge(
                    running = session.running,
                    hasElapsed = elapsed > 0L,
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = stopwatchPalette.container,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.stopwatch_elapsed),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        BasicText(
                            text = StopwatchMath.format(elapsed),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                textAlign = TextAlign.Center,
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 20.sp,
                                maxFontSize = 52.sp,
                                stepSize = 1.sp,
                            ),
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = {
                            tap()
                            if (session.running) {
                                viewModel.pause()
                            } else {
                                viewModel.startOrResume()
                            }
                        },
                        enabled = loaded,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (session.running) R.drawable.ic_pause else R.drawable.ic_play,
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(
                                when {
                                    session.running -> R.string.stopwatch_pause
                                    elapsed > 0L -> R.string.stopwatch_resume
                                    else -> R.string.stopwatch_start
                                },
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            tap()
                            viewModel.addLap()
                        },
                        enabled = loaded && session.running,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_flag),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.stopwatch_lap),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        tap()
                        if (hasContent) {
                            showResetConfirm = true
                        } else {
                            viewModel.reset()
                        }
                    },
                    enabled = resetEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reset),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.stopwatch_reset),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            if (session.stoppedByReboot && !session.running) {
                item {
                    InfoCard(
                        text = stringResource(R.string.stopwatch_reboot_notice),
                        icon = painterResource(R.drawable.ic_tool_stopwatch),
                        containerColor = palette.torch.container,
                    )
                }
            }

            item {
                InfoCard(
                    text = stringResource(R.string.stopwatch_explainer),
                    icon = painterResource(R.drawable.ic_tool_stopwatch),
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.stopwatch_laps),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    if (laps.isNotEmpty()) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.stopwatch_lap_count,
                                laps.size,
                                laps.size,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (laps.isEmpty()) {
                item {
                    InfoCard(text = stringResource(R.string.stopwatch_no_laps))
                }
            } else {
                items(laps.asReversed(), key = { it.id }) { lap ->
                    LapRow(lap = lap)
                }
            }
        }
    }
}

@Composable
private fun StopwatchStatusBadge(
    running: Boolean,
    hasElapsed: Boolean,
) {
    val icon = when {
        running -> R.drawable.ic_play
        hasElapsed -> R.drawable.ic_pause
        else -> R.drawable.ic_tool_stopwatch
    }
    val label = when {
        running -> R.string.stopwatch_running
        hasElapsed -> R.string.stopwatch_paused
        else -> R.string.stopwatch_ready
    }
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (running) {
            MaterialTheme.colorScheme.primaryContainer
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
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (running) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (running) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun LapRow(lap: LapEntity) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.stopwatch_lap_number, lap.lapNumber),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${stringResource(R.string.stopwatch_split)}  ${
                        StopwatchMath.format(lap.splitMillis)
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${stringResource(R.string.stopwatch_total)}  ${
                        StopwatchMath.format(lap.totalMillis)
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
