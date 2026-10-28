package com.juanmediahubph.pocketgizmo.ui.sos

import com.juanmediahubph.pocketgizmo.hardware.TorchControl
import com.juanmediahubph.pocketgizmo.hardware.TorchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SosViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun flashFollowsTheMorsePatternWithoutDoubledGaps() = runTest(dispatcher) {
        val torch = FakeTorch(testScheduler)
        val viewModel = SosViewModel(torch)

        viewModel.start()
        runCurrent()
        assertTrue(viewModel.active.value)

        advanceTimeBy(SosPatternDuration + 1)
        runCurrent()

        val expected = listOf(
            0L to true,
            300L to false,
            600L to true,
            900L to false,
            1_200L to true,
            1_500L to false,
            2_400L to true,
            3_300L to false,
            3_600L to true,
            4_500L to false,
            4_800L to true,
            5_700L to false,
            6_600L to true,
            6_900L to false,
            7_200L to true,
            7_500L to false,
            7_800L to true,
            8_100L to false,
            10_200L to true,
        )
        assertEquals(expected, torch.events.toList())

        viewModel.stop()
        runCurrent()
        assertFalse(viewModel.active.value)
        assertTrue(torch.forceOffCount > 0)
        assertEquals(false, torch.lastCommand)
    }

    @Test
    fun stopCancelsPendingPulsesAndTurnsTheLedOff() = runTest(dispatcher) {
        val torch = FakeTorch(testScheduler)
        val viewModel = SosViewModel(torch)

        viewModel.start()
        runCurrent()
        advanceTimeBy(2_501)
        runCurrent()
        assertEquals(true, torch.lastCommand)

        viewModel.stop()
        runCurrent()
        assertFalse(viewModel.active.value)
        assertEquals(false, torch.lastCommand)

        val historyAfterStop = torch.events.size
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(historyAfterStop, torch.events.size)
        assertFalse(viewModel.active.value)
    }

    @Test
    fun startingTwiceDoesNotRunTwoPatterns() = runTest(dispatcher) {
        val torch = FakeTorch(testScheduler)
        val viewModel = SosViewModel(torch)

        viewModel.start()
        runCurrent()
        viewModel.start()
        runCurrent()

        advanceTimeBy(601)
        runCurrent()
        assertEquals(
            listOf(0L to true, 300L to false, 600L to true),
            torch.events.toList(),
        )

        viewModel.stop()
    }

    @Test
    fun aMissingFlashNeverStartsFlashing() = runTest(dispatcher) {
        val torch = FakeTorch(testScheduler, available = false)
        val viewModel = SosViewModel(torch)

        viewModel.start()
        runCurrent()

        assertFalse(viewModel.active.value)
        assertTrue(torch.events.isEmpty())
    }

    @Test
    fun aTorchFailureEndsTheSignalAndLeavesTheLedOff() = runTest(dispatcher) {
        val torch = FakeTorch(testScheduler, failOnCall = 4)
        val viewModel = SosViewModel(torch)

        viewModel.start()
        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()

        assertFalse(viewModel.active.value)
        assertTrue(torch.forceOffCount > 0)
        assertEquals(false, torch.lastCommand)

        val historyAfterFailure = torch.events.size
        advanceTimeBy(20_000)
        runCurrent()
        assertEquals(historyAfterFailure, torch.events.size)
    }

    private class FakeTorch(
        private val scheduler: TestCoroutineScheduler,
        private val available: Boolean = true,
        private val failOnCall: Int = Int.MAX_VALUE,
    ) : TorchControl {

        override val state: StateFlow<TorchState> = MutableStateFlow(TorchState(available = available))

        val events = mutableListOf<Pair<Long, Boolean>>()
        var lastCommand: Boolean? = null
            private set
        var forceOffCount = 0
            private set

        private var calls = 0

        override fun clearError() = Unit

        override fun ensureAvailable(): Boolean = available

        override fun setTorch(on: Boolean): Boolean {
            calls++
            if (calls >= failOnCall) return false
            lastCommand = on
            events += scheduler.currentTime to on
            return true
        }

        override fun forceOff() {
            forceOffCount++
            lastCommand = false
        }
    }

    private companion object {
        const val SosPatternDuration = 10_200L
    }
}
