package com.juanmediahubph.pocketgizmo.ui.stopwatch

import com.juanmediahubph.pocketgizmo.data.local.LapEntity
import com.juanmediahubph.pocketgizmo.data.local.STOPWATCH_ROW_ID
import com.juanmediahubph.pocketgizmo.data.local.StopwatchDao
import com.juanmediahubph.pocketgizmo.data.local.StopwatchSessionEntity
import com.juanmediahubph.pocketgizmo.data.repository.StopwatchRepository
import com.juanmediahubph.pocketgizmo.hardware.MonotonicClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class StopwatchViewModelTest {

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
    fun pauseKeepsTheElapsedTimeAndResumeContinuesFromIt() = runTest(dispatcher) {
        val clock = FakeClock()
        val dao = FakeStopwatchDao()
        val viewModel = StopwatchViewModel(StopwatchRepository(dao), clock)
        collectElapsed(viewModel)

        runCurrent()
        assertTrue(viewModel.loaded.value)

        viewModel.startOrResume()
        runCurrent()
        clock.advance(1_500)
        tick()

        assertTrue(viewModel.sessionState.value.running)
        assertEquals(1_500L, viewModel.elapsedMillis.value)

        viewModel.pause()
        runCurrent()
        assertFalse(viewModel.sessionState.value.running)
        assertEquals(1_500L, viewModel.sessionState.value.accumulatedMillis)
        assertEquals(1_500L, dao.sessionFlow.value?.accumulatedMillis)

        clock.advance(5_000)
        tick()
        assertEquals(1_500L, viewModel.elapsedMillis.value)

        viewModel.startOrResume()
        runCurrent()
        clock.advance(500)
        tick()
        assertEquals(2_000L, viewModel.elapsedMillis.value)
    }

    @Test
    fun aRunningSessionIsRestoredWithItsOriginalAnchor() = runTest(dispatcher) {
        val clock = FakeClock(elapsed = 12_000, wall = 1_700_000_012_000)
        val dao = FakeStopwatchDao()
        dao.sessionFlow.value = StopwatchSessionEntity(
            running = true,
            accumulatedMillis = 1_000,
            anchorElapsedRealtime = 10_000,
            bootReferenceMillis = 1_700_000_000_000,
        )

        val viewModel = StopwatchViewModel(StopwatchRepository(dao), clock)
        collectElapsed(viewModel)
        runCurrent()

        assertTrue(viewModel.loaded.value)
        assertTrue(viewModel.sessionState.value.running)
        assertEquals(10_000L, viewModel.sessionState.value.anchorElapsedRealtime)
        assertEquals(3_000L, viewModel.elapsedMillis.value)
    }

    @Test
    fun aRebootMarksTheSessionStoppedWithoutInventingTime() = runTest(dispatcher) {
        val clock = FakeClock(elapsed = 2_000, wall = 500_000_000_000)
        val dao = FakeStopwatchDao()
        dao.sessionFlow.value = StopwatchSessionEntity(
            running = true,
            accumulatedMillis = 5_000,
            anchorElapsedRealtime = 50_000,
            bootReferenceMillis = 1_000,
        )

        val viewModel = StopwatchViewModel(StopwatchRepository(dao), clock)
        collectElapsed(viewModel)
        runCurrent()

        val restored = viewModel.sessionState.value
        assertFalse(restored.running)
        assertTrue(restored.stoppedByReboot)
        assertEquals(5_000L, restored.accumulatedMillis)
        assertEquals(5_000L, dao.sessionFlow.value?.accumulatedMillis)
        assertFalse(dao.sessionFlow.value?.running ?: true)
    }

    @Test
    fun lapsStoreTheRunningTotalAndTheSplit() = runTest(dispatcher) {
        val clock = FakeClock()
        val dao = FakeStopwatchDao()
        val viewModel = StopwatchViewModel(StopwatchRepository(dao), clock)
        collectElapsed(viewModel)
        runCurrent()

        viewModel.startOrResume()
        runCurrent()

        clock.advance(2_000)
        viewModel.addLap()
        runCurrent()

        clock.advance(1_000)
        viewModel.addLap()
        runCurrent()

        val laps = dao.lapsFlow.value.sortedBy { it.lapNumber }
        assertEquals(2, laps.size)
        assertEquals(1, laps[0].lapNumber)
        assertEquals(2_000L, laps[0].totalMillis)
        assertEquals(2_000L, laps[0].splitMillis)
        assertEquals(2, laps[1].lapNumber)
        assertEquals(3_000L, laps[1].totalMillis)
        assertEquals(1_000L, laps[1].splitMillis)
    }

    @Test
    fun resetClearsTheSessionAndTheLaps() = runTest(dispatcher) {
        val clock = FakeClock()
        val dao = FakeStopwatchDao()
        val viewModel = StopwatchViewModel(StopwatchRepository(dao), clock)
        collectElapsed(viewModel)
        runCurrent()

        viewModel.startOrResume()
        runCurrent()
        clock.advance(1_000)
        viewModel.addLap()
        runCurrent()
        assertTrue(dao.lapsFlow.value.isNotEmpty())

        viewModel.reset()
        runCurrent()

        assertTrue(dao.lapsFlow.value.isEmpty())
        val stored = requireNotNull(dao.sessionFlow.value)
        assertFalse(stored.running)
        assertEquals(0L, stored.accumulatedMillis)
        assertFalse(viewModel.sessionState.value.running)
        assertEquals(0L, viewModel.sessionState.value.accumulatedMillis)
    }

    @Test
    fun aLapIsIgnoredWhilePaused() = runTest(dispatcher) {
        val clock = FakeClock()
        val dao = FakeStopwatchDao()
        val viewModel = StopwatchViewModel(StopwatchRepository(dao), clock)
        collectElapsed(viewModel)
        runCurrent()

        viewModel.addLap()
        runCurrent()
        assertTrue(dao.lapsFlow.value.isEmpty())

        viewModel.startOrResume()
        runCurrent()
        viewModel.pause()
        runCurrent()
        viewModel.addLap()
        runCurrent()
        assertTrue(dao.lapsFlow.value.isEmpty())
    }

    private fun kotlinx.coroutines.test.TestScope.collectElapsed(viewModel: StopwatchViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.elapsedMillis.collect {}
        }
    }

    private fun kotlinx.coroutines.test.TestScope.tick() {
        advanceTimeBy(34)
        runCurrent()
    }

    private class FakeClock(
        var elapsed: Long = 0L,
        var wall: Long = 1_700_000_000_000L,
    ) : MonotonicClock {
        override fun elapsedRealtime(): Long = elapsed

        override fun wallClockMillis(): Long = wall

        fun advance(millis: Long) {
            elapsed += millis
            wall += millis
        }
    }

    private class FakeStopwatchDao : StopwatchDao {

        val sessionFlow = MutableStateFlow<StopwatchSessionEntity?>(null)
        val lapsFlow = MutableStateFlow<List<LapEntity>>(emptyList())

        private var nextId = 1L

        override fun observeSession() = sessionFlow

        override suspend fun getSession(): StopwatchSessionEntity? = sessionFlow.value

        override suspend fun insertSession(session: StopwatchSessionEntity) {
            if (sessionFlow.value == null) sessionFlow.value = session
        }

        override suspend fun updateSession(session: StopwatchSessionEntity) {
            sessionFlow.value = session
        }

        override fun observeLaps() = lapsFlow

        override suspend fun lastLap(): LapEntity? = lapsFlow.value.maxByOrNull { it.lapNumber }

        override suspend fun insertLap(lap: LapEntity) {
            val stored = lap.copy(id = nextId++, sessionId = STOPWATCH_ROW_ID)
            lapsFlow.value = lapsFlow.value + stored
        }

        override suspend fun deleteAllLaps() {
            lapsFlow.value = emptyList()
        }

        override suspend fun deleteSession() {
            sessionFlow.value = null
        }
    }
}
