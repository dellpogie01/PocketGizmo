package com.juanmediahubph.pocketgizmo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassMathTest {

    @Test
    fun normalizeWrapsBothWays() {
        assertEquals(350f, CompassMath.normalize(-10f), 0.001f)
        assertEquals(10f, CompassMath.normalize(370f), 0.001f)
        assertEquals(0f, CompassMath.normalize(360f), 0.001f)
        assertEquals(180f, CompassMath.normalize(-180f), 0.001f)
    }

    @Test
    fun shortestDeltaCrossesNorthCorrectly() {
        assertEquals(2f, CompassMath.shortestDelta(359f, 1f), 0.001f)
        assertEquals(-2f, CompassMath.shortestDelta(1f, 359f), 0.001f)
        assertEquals(90f, CompassMath.shortestDelta(350f, 80f), 0.001f)
        assertEquals(-90f, CompassMath.shortestDelta(80f, 350f), 0.001f)
    }

    @Test
    fun smoothingTakesTheShortWayAtTheBoundary() {
        val first = CompassMath.smooth(previous = 359f, target = 1f, alpha = 0.5f)
        assertEquals(0f, first, 0.001f)

        val other = CompassMath.smooth(previous = 1f, target = 359f, alpha = 0.5f)
        assertEquals(0f, other, 0.001f)

        val gentle = CompassMath.smooth(previous = 359f, target = 1f, alpha = 0.18f)
        assertEquals(359.36f, gentle, 0.001f)
        assertTrue(CompassMath.shortestDelta(359f, gentle) > 0f)
    }

    @Test
    fun directionsUseEightSectors() {
        assertEquals("N", CompassMath.directionLabel(0f))
        assertEquals("N", CompassMath.directionLabel(22.4f))
        assertEquals("NE", CompassMath.directionLabel(22.5f))
        assertEquals("NE", CompassMath.directionLabel(67.4f))
        assertEquals("E", CompassMath.directionLabel(67.5f))
        assertEquals("E", CompassMath.directionLabel(90f))
        assertEquals("SE", CompassMath.directionLabel(135f))
        assertEquals("S", CompassMath.directionLabel(180f))
        assertEquals("SW", CompassMath.directionLabel(225f))
        assertEquals("W", CompassMath.directionLabel(270f))
        assertEquals("NW", CompassMath.directionLabel(315f))
        assertEquals("NW", CompassMath.directionLabel(337.4f))
        assertEquals("N", CompassMath.directionLabel(337.5f))
        assertEquals("N", CompassMath.directionLabel(359.9f))
    }

    @Test
    fun displayRotationIsCompensated() {
        assertEquals(0f, CompassMath.rotationOffsetDegrees(0), 0f)
        assertEquals(90f, CompassMath.rotationOffsetDegrees(1), 0f)
        assertEquals(180f, CompassMath.rotationOffsetDegrees(2), 0f)
        assertEquals(270f, CompassMath.rotationOffsetDegrees(3), 0f)

        assertEquals(80f, CompassMath.headingFromAzimuth(350f, 1), 0.001f)
        assertEquals(10f, CompassMath.headingFromAzimuth(280f, 1), 0.001f)
    }

    @Test
    fun tiltUsabilityRequiresEnoughGravityOnZ() {
        assertTrue(CompassMath.isUsableTilt(accelerometerZ = 8.5f))
        assertFalse(CompassMath.isUsableTilt(accelerometerZ = 2.0f))
        assertTrue(CompassMath.isUsableTilt(accelerometerZ = -8.5f))
    }
}
