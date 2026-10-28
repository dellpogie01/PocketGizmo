package com.juanmediahubph.pocketgizmo.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.juanmediahubph.pocketgizmo.R
import com.juanmediahubph.pocketgizmo.ui.Routes
import com.juanmediahubph.pocketgizmo.ui.theme.GizmoPalette
import com.juanmediahubph.pocketgizmo.ui.theme.ToolPalette

enum class ToolAccent {
    TORCH,
    SOS,
    SCREEN_LIGHT,
    COMPASS,
    LEVEL,
    STOPWATCH,
}

enum class GizmoTool(
    val route: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val accent: ToolAccent,
) {
    TORCH(
        route = Routes.TORCH,
        titleRes = R.string.tool_torch,
        descriptionRes = R.string.tool_torch_desc,
        iconRes = R.drawable.ic_tool_torch,
        accent = ToolAccent.TORCH,
    ),
    SOS(
        route = Routes.SOS,
        titleRes = R.string.tool_sos,
        descriptionRes = R.string.tool_sos_desc,
        iconRes = R.drawable.ic_tool_sos,
        accent = ToolAccent.SOS,
    ),
    SCREEN_LIGHT(
        route = Routes.SCREEN_LIGHT,
        titleRes = R.string.tool_screenlight,
        descriptionRes = R.string.tool_screenlight_desc,
        iconRes = R.drawable.ic_tool_screenlight,
        accent = ToolAccent.SCREEN_LIGHT,
    ),
    COMPASS(
        route = Routes.COMPASS,
        titleRes = R.string.tool_compass,
        descriptionRes = R.string.tool_compass_desc,
        iconRes = R.drawable.ic_tool_compass,
        accent = ToolAccent.COMPASS,
    ),
    LEVEL(
        route = Routes.LEVEL,
        titleRes = R.string.tool_level,
        descriptionRes = R.string.tool_level_desc,
        iconRes = R.drawable.ic_tool_level,
        accent = ToolAccent.LEVEL,
    ),
    STOPWATCH(
        route = Routes.STOPWATCH,
        titleRes = R.string.tool_stopwatch,
        descriptionRes = R.string.tool_stopwatch_desc,
        iconRes = R.drawable.ic_tool_stopwatch,
        accent = ToolAccent.STOPWATCH,
    ),
}

fun GizmoPalette.paletteFor(accent: ToolAccent): ToolPalette = when (accent) {
    ToolAccent.TORCH -> torch
    ToolAccent.SOS -> sos
    ToolAccent.SCREEN_LIGHT -> screenLight
    ToolAccent.COMPASS -> compass
    ToolAccent.LEVEL -> level
    ToolAccent.STOPWATCH -> stopwatch
}
