package com.juanmediahubph.pocketgizmo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juanmediahubph.pocketgizmo.data.local.AppDatabase
import com.juanmediahubph.pocketgizmo.data.local.LapEntity
import com.juanmediahubph.pocketgizmo.data.local.StopwatchSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StopwatchPersistenceTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun aRunningSessionRoundTrips() = runTest {
        val dao = database.stopwatchDao()
        val session = StopwatchSessionEntity(
            running = true,
            accumulatedMillis = 2_500L,
            anchorElapsedRealtime = 90_000L,
            bootReferenceMillis = 1_700_000_000_000L,
        )

        dao.insertSession(session)

        val stored = requireNotNull(dao.getSession())
        assertEquals(session, stored)
        assertEquals(session, dao.observeSession().first())
    }

    @Test
    fun lapsAreReadBackInLapOrder() = runTest {
        val dao = database.stopwatchDao()
        dao.insertSession(StopwatchSessionEntity())

        dao.insertLap(LapEntity(lapNumber = 1, totalMillis = 1_000L, splitMillis = 1_000L))
        dao.insertLap(LapEntity(lapNumber = 2, totalMillis = 3_500L, splitMillis = 2_500L))
        dao.insertLap(LapEntity(lapNumber = 3, totalMillis = 6_000L, splitMillis = 2_500L))

        val laps = dao.observeLaps().first()
        assertEquals(listOf(1, 2, 3), laps.map { it.lapNumber })
        assertEquals(listOf(1_000L, 3_500L, 6_000L), laps.map { it.totalMillis })
    }

    @Test
    fun theLastLapIsTheHighestNumber() = runTest {
        val dao = database.stopwatchDao()
        dao.insertSession(StopwatchSessionEntity())
        assertNull(dao.lastLap())

        dao.insertLap(LapEntity(lapNumber = 1, totalMillis = 1_000L, splitMillis = 1_000L))
        dao.insertLap(LapEntity(lapNumber = 2, totalMillis = 3_000L, splitMillis = 2_000L))

        val last = requireNotNull(dao.lastLap())
        assertEquals(2, last.lapNumber)
        assertEquals(3_000L, last.totalMillis)
        assertEquals(2_000L, last.splitMillis)
    }

    @Test
    fun savingTheSessionKeepsExistingLaps() = runTest {
        val dao = database.stopwatchDao()
        dao.insertSession(StopwatchSessionEntity())
        dao.insertLap(LapEntity(lapNumber = 1, totalMillis = 1_000L, splitMillis = 1_000L))

        dao.updateSession(
            StopwatchSessionEntity(
                running = true,
                accumulatedMillis = 1_000L,
                anchorElapsedRealtime = 5_000L,
            ),
        )

        assertEquals(1, dao.observeLaps().first().size)
    }

    @Test
    fun clearAllRemovesTheSessionAndTheLaps() = runTest {
        val dao = database.stopwatchDao()
        dao.insertSession(StopwatchSessionEntity(accumulatedMillis = 4_000L))
        dao.insertLap(LapEntity(lapNumber = 1, totalMillis = 4_000L, splitMillis = 4_000L))

        dao.clearAll()

        assertNull(dao.getSession())
        assertTrue(dao.observeLaps().first().isEmpty())
    }

    @Test
    fun deletingLapsKeepsTheSession() = runTest {
        val dao = database.stopwatchDao()
        dao.insertSession(StopwatchSessionEntity(accumulatedMillis = 4_000L))
        dao.insertLap(LapEntity(lapNumber = 1, totalMillis = 4_000L, splitMillis = 4_000L))

        dao.deleteAllLaps()

        assertTrue(dao.observeLaps().first().isEmpty())
        assertEquals(4_000L, dao.getSession()?.accumulatedMillis)
    }
}
