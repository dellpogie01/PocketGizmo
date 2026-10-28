package com.juanmediahubph.pocketgizmo.ui.screenlight

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanmediahubph.pocketgizmo.R
import com.juanmediahubph.pocketgizmo.hardware.ScreenLightWindowController
import com.juanmediahubph.pocketgizmo.ui.components.findActivity
import com.juanmediahubph.pocketgizmo.ui.components.rememberTapFeedback
import com.juanmediahubph.pocketgizmo.ui.theme.LocalGizmoPalette
import kotlin.math.roundToInt

private data class LightPreset(@StringRes val labelRes: Int, val argb: Long)

private val LightPresets = listOf(
    LightPreset(R.string.color_white, 0xFFFFFFFF),
    LightPreset(R.string.color_warm, 0xFFFFE0B2),
    LightPreset(R.string.color_red, 0xFFFF0000),
    LightPreset(R.string.color_green, 0xFF00FF00),
    LightPreset(R.string.color_blue, 0xFF0000FF),
)

private val PanelColor = Color(0xF0101414)
private val PanelOutline = Color.White.copy(alpha = 0.42f)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenLightScreen(
    viewModel: ScreenLightViewModel,
    onBack: () -> Unit,
) {
    val selectedColor by viewModel.color.collectAsStateWithLifecycle()
    val brightness by viewModel.brightness.collectAsStateWithLifecycle()
    val keepAwake by viewModel.keepAwake.collectAsStateWithLifecycle()
    val tap = rememberTapFeedback()
    val activity = LocalContext.current.findActivity()
    val lifecycleOwner = LocalLifecycleOwner.current
    val darkTheme = LocalGizmoPalette.current.isDark
    val controller = remember(activity) { activity?.let { ScreenLightWindowController(it) } }
    val lightColor = Color(selectedColor.toInt())
    val latestBrightness by rememberUpdatedState(brightness)
    val latestKeepAwake by rememberUpdatedState(keepAwake)

    fun exit() {
        tap()
        viewModel.commitBrightness()
        onBack()
    }

    DisposableEffect(controller, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START ->
                    controller?.apply(latestBrightness, latestKeepAwake)

                Lifecycle.Event.ON_STOP -> controller?.restore()

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.commitBrightness()
            controller?.restore()
            controller?.setLightSystemBarIcons(lightBackground = !darkTheme)
        }
    }

    LaunchedEffect(brightness, keepAwake, controller) {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            controller?.apply(brightness, keepAwake)
        }
    }

    LaunchedEffect(lightColor, controller, darkTheme) {
        controller?.setLightSystemBarIcons(
            lightBackground = lightColor.luminance() > 0.45f,
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(lightColor),
    ) {
        val panelMaxHeight = maxHeight * 0.62f

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = PanelColor,
                    contentColor = Color.White,
                    border = BorderStroke(1.dp, PanelOutline),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_tool_screenlight),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.screenlight_title),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { exit() },
                    modifier = Modifier.height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanelColor,
                        contentColor = Color.White,
                    ),
                    border = BorderStroke(1.dp, PanelOutline),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.screenlight_exit),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = panelMaxHeight),
            color = PanelColor,
            contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.screenlight_color),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LightPresets.forEach { preset ->
                        val selected = selectedColor == preset.argb
                        val presetColor = Color(preset.argb.toInt())
                        val label = stringResource(preset.labelRes)
                        val description = if (selected) {
                            stringResource(R.string.color_selected, label)
                        } else {
                            label
                        }
                        Column(
                            modifier = Modifier
                                .widthIn(min = 56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .selectable(
                                    selected = selected,
                                    onClick = {
                                        tap()
                                        viewModel.selectColor(preset.argb)
                                    },
                                    role = Role.RadioButton,
                                )
                                .semantics { contentDescription = description }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(50.dp),
                                shape = RoundedCornerShape(15.dp),
                                color = presetColor,
                                border = BorderStroke(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.45f)
                                    },
                                ),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (selected) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp),
                                            tint = if (presetColor.luminance() > 0.5f) {
                                                PanelColor
                                            } else {
                                                Color.White
                                            },
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = if (selected) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                },
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.screenlight_brightness),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(
                            R.string.screenlight_brightness_value,
                            (brightness * 100).roundToInt().coerceIn(5, 100),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }

                Slider(
                    value = brightness,
                    onValueChange = viewModel::previewBrightness,
                    onValueChangeFinished = viewModel::commitBrightness,
                    valueRange = ScreenLightViewModel.MIN_BRIGHTNESS..1f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.32f),
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.keep_screen_awake),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = keepAwake,
                        onCheckedChange = {
                            tap()
                            viewModel.setKeepAwake(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PanelColor,
                            checkedTrackColor = Color.White,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.28f),
                            uncheckedBorderColor = Color.White.copy(alpha = 0.6f),
                        ),
                    )
                }

                Text(
                    text = stringResource(R.string.screenlight_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
    }
}
