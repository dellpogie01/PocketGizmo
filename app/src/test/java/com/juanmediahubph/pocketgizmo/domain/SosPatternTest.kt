package com.juanmediahubph.pocketgizmo.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SosPatternTest {

    @Test
    fun patternIsDotDotDotDashDashDashDotDotDot() {
        val onLengths = mutableListOf<Int>()
        var index = 0
        val steps = SosPattern.steps
        while (index < steps.size) {
            val step = steps[index]
            if (step.on) {
                onLengths += step.units
            }
            index++
        }
        assertEquals(
            listOf(1, 1, 1, 3, 3, 3, 1, 1, 1),
            onLengths,
        )
    }

    @Test
    fun gapsAreExplicitAndCorrect() {
        val offLengths = SosPattern.steps.filter { !it.on }.map { it.units }
        assertEquals(
            listOf(1, 1, 3, 1, 1, 3, 1, 1, 7),
            offLengths,
        )
    }

    @Test
    fun repetitionLastsThirtyFourUnits() {
        assertEquals(34, SosPattern.totalUnits)
        assertEquals(34 * 300L, SosPattern.totalDurationMillis)
        assertEquals(10_200L, SosPattern.totalDurationMillis)
    }
}
