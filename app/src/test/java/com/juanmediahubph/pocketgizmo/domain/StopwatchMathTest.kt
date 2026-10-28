package com.juanmediahubph.pocketgizmo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StopwatchMathTest {

    @Test
    fun runningTimeUsesTheAnchor() {
        assertEquals(
            4_700L,
            StopwatchMath.elapsedMillis(
                running = true,
                accumulatedMillis = 2_000L,
                anchorElapsedRealtime = 10_000L,
                nowElapsedRealtime = 12_700L,
            ),
        )
    }

    @Test
    fun pausedTimeDoesNotChange() {
        assertEquals(
            2_000L,
            StopwatchMath.elapsedMillis(
                running = false,
                accumulatedMillis = 2_000L,
                anchorElapsedRealtime = 10_000L,
                nowElapsedRealtime = 99_999L,
            ),
        )
    }

    @Test
    fun aClockThatMovesBackwardsNeverProducesNegativeTime() {
        assertEquals(
            2_000L,
            StopwatchMath.elapsedMillis(
                running = true,
                accumulatedMillis = 2_000L,
                anchorElapsedRealtime = 10_000L,
                nowElapsedRealtime = 9_000L,
            ),
        )
    }

    @Test
    fun rebootIsDetectedFromAnchorsAndBootReference() {
        assertTrue(
            StopwatchMath.wasInterruptedByReboot(
                running = true,
                anchorElapsedRealtime = 50_000L,
                storedBootReference = 1_000L,
                nowElapsedRealtime = 2_000L,
                nowBootReference = 1_000L,
            ),
        )
        assertTrue(
            StopwatchMath.wasInterruptedByReboot(
                running = true,
                anchorElapsedRealtime = 2_000L,
                storedBootReference = 1_000L,
                nowElapsedRealtime = 3_000L,
                nowBootReference = 500_000L,
            ),
        )
        assertFalse(
            StopwatchMath.wasInterruptedByReboot(
                running = true,
                anchorElapsedRealtime = 2_000L,
                storedBootReference = 1_000L,
                nowElapsedRealtime = 3_000L,
                nowBootReference = 1_500L,
            ),
        )
        assertFalse(
            StopwatchMath.wasInterruptedByReboot(
                running = false,
                anchorElapsedRealtime = 50_000L,
                storedBootReference = 1_000L,
                nowElapsedRealtime = 2_000L,
                nowBootReference = 500_000L,
            ),
        )
    }

    @Test
    fun bootReferenceIsWallClockMinusElapsed() {
        assertEquals(
            1_700_000_000_000L,
            StopwatchMath.bootReference(1_700_000_010_000L, 10_000L),
        )
    }

    @Test
    fun formattingShowsHundredthsMinutesAndHours() {
        assertEquals("00:00.00", StopwatchMath.format(0L))
        assertEquals("00:00.99", StopwatchMath.format(999L))
        assertEquals("01:01.23", StopwatchMath.format(61_230L))
        assertEquals("01:00.00", StopwatchMath.format(60_000L))
        assertEquals("1:01:01.23", StopwatchMath.format(3_661_230L))
        assertEquals("00:00.00", StopwatchMath.format(-5L))
    }
}
