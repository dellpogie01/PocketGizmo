package com.juanmediahubph.pocketgizmo.ui.compass

import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanmediahubph.pocketgizmo.R
import com.juanmediahubph.pocketgizmo.domain.CompassMath
import com.juanmediahubph.pocketgizmo.ui.components.GizmoToolScaffold
import com.juanmediahubph.pocketgizmo.ui.components.InfoCard
import com.juanmediahubph.pocketgizmo.ui.components.UnavailablePanel
import com.juanmediahubph.pocketgizmo.ui.theme.LocalGizmoPalette
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CompassScreen(
    viewModel: CompassViewModel,
    onBack: () -> Unit,
) {
    val reading by viewModel.reading.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val palette = LocalGizmoPalette.current

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
        title = stringResource(R.string.tool_compass),
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
            if (!viewModel.isSupported) {
                UnavailablePanel(
                    title = stringResource(R.string.compass_unavailable_title),
                    body = stringResource(R.string.compass_unavailable_body),
                    iconRes = R.drawable.ic_tool_compass,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CompassDial(
                        heading = reading?.heading ?: 0f,
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    )
                }

                val heading = reading?.heading
                val degrees = heading?.let {
                    CompassMath.normalize(it).roundToInt() % 360
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    shape = MaterialTheme.shapes.large,
                    color = palette.compass.container,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (degrees != null) {
                                stringResource(R.string.compass_degrees, degrees)
                            } else {
                                stringResource(R.string.compass_waiting)
                            },
                            style = if (degrees != null) {
                                MaterialTheme.typography.displayMedium
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (heading != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = CompassMath.directionLabel(heading),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.compass.accent,
                                )
                                Text(
                                    text = stringResource(R.string.compass_magnetic_north),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                if (reading != null &&
                    reading!!.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                ) {
                    InfoCard(
                        text = stringResource(R.string.compass_low_accuracy),
                        icon = painterResource(R.drawable.ic_tool_compass),
                        containerColor = palette.torch.container,
                    )
                }

                InfoCard(
                    text = stringResource(R.string.compass_calibration_hint),
                    icon = painterResource(R.drawable.ic_tool_compass),
                )

                if (!viewModel.usesRotationVector) {
                    InfoCard(text = stringResource(R.string.compass_fallback_note))
                }

                InfoCard(text = stringResource(R.string.compass_influence_note))
            }
        }
    }
}

@Composable
private fun CompassDial(
    heading: Float,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = LocalGizmoPalette.current
    val northColor = palette.sos.accent
    val density = LocalDensity.current
    val labelSize = with(density) { 15.sp.toPx() }
    val cardinalSize = with(density) { 19.sp.toPx() }
    val paint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    val labels = remember {
        listOf(
            "N" to 0f,
            "NE" to 45f,
            "E" to 90f,
            "SE" to 135f,
            "S" to 180f,
            "SW" to 225f,
            "W" to 270f,
            "NW" to 315f,
        )
    }

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
        drawCircle(
            color = scheme.outlineVariant.copy(alpha = 0.6f),
            radius = outer * 0.64f,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )

        rotate(degrees = -heading, pivot = center) {
            for (degrees in 0 until 360 step 5) {
                val major = degrees % 45 == 0
                val medium = degrees % 15 == 0
                val length = when {
                    major -> 15.dp.toPx()
                    medium -> 9.dp.toPx()
                    else -> 5.dp.toPx()
                }
                val rad = Math.toRadians(degrees.toDouble())
                val sx = sin(rad).toFloat()
                val cy = cos(rad).toFloat()
                drawLine(
                    color = if (major) scheme.onSurface else scheme.outline,
                    start = Offset(
                        center.x + sx * (outer - length),
                        center.y - cy * (outer - length),
                    ),
                    end = Offset(center.x + sx * outer, center.y - cy * outer),
                    strokeWidth = if (major) 3.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        val labelRadius = outer * 0.74f
        drawIntoCanvas { canvas ->
            labels.forEach { (label, degrees) ->
                val rad = Math.toRadians((degrees - heading).toDouble())
                val x = center.x + sin(rad).toFloat() * labelRadius
                val y = center.y - cos(rad).toFloat() * labelRadius
                val isNorth = label == "N"
                paint.color = (if (isNorth) northColor else scheme.onSurface).toArgb()
                paint.textSize = if (label.length == 1) cardinalSize else labelSize
                canvas.nativeCanvas.drawText(
                    label,
                    x,
                    y + paint.textSize * 0.35f,
                    paint,
                )
            }
        }

        val northRad = Math.toRadians((-heading).toDouble())
        val ns = sin(northRad).toFloat()
        val nc = cos(northRad).toFloat()
        val tip = Offset(
            center.x + ns * (outer - 8.dp.toPx()),
            center.y - nc * (outer - 8.dp.toPx()),
        )
        val baseCenter = Offset(
            center.x + ns * (outer - 30.dp.toPx()),
            center.y - nc * (outer - 30.dp.toPx()),
        )
        val half = 11.dp.toPx()
        val northArrow = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(baseCenter.x + nc * half, baseCenter.y + ns * half)
            lineTo(baseCenter.x - nc * half, baseCenter.y - ns * half)
            close()
        }
        drawPath(northArrow, northColor)

        val index = Path().apply {
            moveTo(center.x, center.y - outer + 18.dp.toPx())
            lineTo(center.x - 9.dp.toPx(), center.y - outer + 3.dp.toPx())
            lineTo(center.x + 9.dp.toPx(), center.y - outer + 3.dp.toPx())
            close()
        }
        drawPath(index, scheme.primary)

        drawCircle(color = scheme.onSurfaceVariant, radius = 6.dp.toPx(), center = center)
        drawCircle(color = scheme.surface, radius = 3.dp.toPx(), center = center)
    }
}
