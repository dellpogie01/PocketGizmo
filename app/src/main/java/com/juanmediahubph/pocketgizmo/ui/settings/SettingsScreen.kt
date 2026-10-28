package com.juanmediahubph.pocketgizmo.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.juanmediahubph.pocketgizmo.R
import com.juanmediahubph.pocketgizmo.domain.AppSettings
import com.juanmediahubph.pocketgizmo.domain.AppTheme
import com.juanmediahubph.pocketgizmo.ui.components.GizmoToolScaffold
import com.juanmediahubph.pocketgizmo.ui.components.InfoCard
import com.juanmediahubph.pocketgizmo.ui.components.LocalAppContainer
import com.juanmediahubph.pocketgizmo.ui.components.rememberTapFeedback

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onButtonSoundsChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onKeepAwakeChange: (Boolean) -> Unit,
    onResetSettings: () -> Unit,
    onClearStopwatch: () -> Unit,
) {
    val tap = rememberTapFeedback()
    val view = LocalView.current
    val feedback = LocalAppContainer.current.feedbackController
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val resetDone = stringResource(R.string.settings_reset_done)
    val clearedDone = stringResource(R.string.settings_cleared_done)

    GizmoToolScaffold(title = stringResource(R.string.settings), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection(title = stringResource(R.string.settings_theme)) {
                AppTheme.entries.forEachIndexed { index, theme ->
                    ThemeRow(
                        label = when (theme) {
                            AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                            AppTheme.LIGHT -> stringResource(R.string.theme_light)
                            AppTheme.DARK -> stringResource(R.string.theme_dark)
                        },
                        selected = settings.theme == theme,
                        onSelect = {
                            tap()
                            onThemeChange(theme)
                            statusMessage = null
                        },
                    )
                    if (index != AppTheme.entries.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            SettingsSection {
                ToggleRow(
                    title = stringResource(R.string.settings_sounds),
                    description = stringResource(R.string.settings_sounds_desc),
                    checked = settings.buttonSounds,
                    onCheckedChange = { enabled ->
                        onButtonSoundsChange(enabled)
                        feedback.tap(view, settings.haptics, enabled)
                        statusMessage = null
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ToggleRow(
                    title = stringResource(R.string.settings_haptics),
                    description = stringResource(R.string.settings_haptics_desc),
                    checked = settings.haptics,
                    onCheckedChange = { enabled ->
                        onHapticsChange(enabled)
                        feedback.tap(view, enabled, settings.buttonSounds)
                        statusMessage = null
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ToggleRow(
                    title = stringResource(R.string.settings_keep_awake),
                    description = stringResource(R.string.settings_keep_awake_desc),
                    checked = settings.keepScreenAwake,
                    onCheckedChange = {
                        tap()
                        onKeepAwakeChange(it)
                        statusMessage = null
                    },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_local_data)) {
                Text(
                    text = stringResource(R.string.settings_local_data_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        tap()
                        showResetDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                ) {
                    Text(stringResource(R.string.settings_reset))
                }
                Text(
                    text = stringResource(R.string.settings_reset_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Spacer(Modifier.size(4.dp))
                OutlinedButton(
                    onClick = {
                        tap()
                        showClearDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                ) {
                    Text(stringResource(R.string.settings_clear_stopwatch))
                }
                Text(
                    text = stringResource(R.string.settings_clear_stopwatch_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            statusMessage?.let { message ->
                InfoCard(
                    text = message,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            }

            Surface(
                onClick = {
                    tap()
                    onOpenAbout()
                },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_about),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetSettings()
                        statusMessage = resetDone
                    },
                ) {
                    Text(stringResource(R.string.settings_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.settings_clear_stopwatch_confirm_title)) },
            text = { Text(stringResource(R.string.settings_clear_stopwatch_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearStopwatch()
                        statusMessage = clearedDone
                    },
                ) {
                    Text(stringResource(R.string.settings_clear_stopwatch))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ThemeRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
