package com.juanmediahubph.pocketgizmo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.juanmediahubph.pocketgizmo.data.local.AppDatabase
import com.juanmediahubph.pocketgizmo.data.local.LapEntity
import com.juanmediahubph.pocketgizmo.data.local.SettingsEntity
import com.juanmediahubph.pocketgizmo.data.local.StopwatchSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPersistenceTest {

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
    fun defaultsMatchTheDocumentedValues() = runTest {
        val dao = database.settingsDao()
        dao.insertDefaults(SettingsEntity())

        val stored = requireNotNull(dao.get())
        assertEquals("SYSTEM", stored.theme)
        assertFalse(stored.buttonSounds)
        assertTrue(stored.haptics)
        assertEquals(0xFFFFFFFF, stored.screenLightColor)
        assertEquals(0.7f, stored.screenLightBrightness, 0.0001f)
        assertTrue(stored.keepScreenAwake)
        assertEquals(0f, stored.levelOffsetX, 0.0001f)
        assertEquals(0f, stored.levelOffsetY, 0.0001f)
    }

    @Test
    fun insertsAreIgnoredWhenDefaultsAlreadyExist() = runTest {
        val dao = database.settingsDao()
        dao.insertDefaults(SettingsEntity())
        dao.updateTheme("DARK")

        dao.insertDefaults(SettingsEntity(theme = "LIGHT"))

        assertEquals("DARK", dao.get()?.theme)
    }

    @Test
    fun everySettingSurvivesReadBack() = runTest {
        val dao = database.settingsDao()
        dao.insertDefaults(SettingsEntity())

        dao.updateTheme("DARK")
        dao.updateButtonSounds(true)
        dao.updateHaptics(false)
        dao.updateKeepScreenAwake(false)
        dao.updateScreenLight(color = 0xFF00FF00, brightness = 0.35f)
        dao.updateLevelCalibration(x = 1.25f, y = -2.5f)

        val stored = requireNotNull(dao.get())
        assertEquals("DARK", stored.theme)
        assertTrue(stored.buttonSounds)
        assertFalse(stored.haptics)
        assertFalse(stored.keepScreenAwake)
        assertEquals(0xFF00FF00, stored.screenLightColor)
        assertEquals(0.35f, stored.screenLightBrightness, 0.0001f)
        assertEquals(1.25f, stored.levelOffsetX, 0.0001f)
        assertEquals(-2.5f, stored.levelOffsetY, 0.0001f)
    }

    @Test
    fun theObservedFlowReflectsUpdates() = runTest {
        val dao = database.settingsDao()
        dao.insertDefaults(SettingsEntity())

        dao.updateTheme("LIGHT")

        assertEquals("LIGHT", dao.observe().first()?.theme)
    }

    @Test
    fun resetRestoresDefaultsButKeepsStopwatchRecords() = runTest {
        val settings = database.settingsDao()
        val stopwatch = database.stopwatchDao()
        settings.insertDefaults(SettingsEntity())
        settings.updateTheme("DARK")
        settings.updateScreenLight(color = 0xFFFF0000, brightness = 0.2f)
        settings.updateLevelCalibration(x = 3f, y = 4f)

        stopwatch.insertSession(
            StopwatchSessionEntity(running = false, accumulatedMillis = 12_345L),
        )
        stopwatch.insertLap(
            LapEntity(lapNumber = 1, totalMillis = 12_345L, splitMillis = 12_345L),
        )

        settings.resetToDefaults(
            theme = "SYSTEM",
            buttonSounds = false,
            haptics = true,
            screenLightColor = 0xFFFFFFFF,
            screenLightBrightness = 0.7f,
            keepScreenAwake = true,
        )

        val stored = requireNotNull(settings.get())
        assertEquals("SYSTEM", stored.theme)
        assertEquals(0xFFFFFFFF, stored.screenLightColor)
        assertEquals(0.7f, stored.screenLightBrightness, 0.0001f)
        assertEquals(0f, stored.levelOffsetX, 0.0001f)
        assertEquals(0f, stored.levelOffsetY, 0.0001f)

        assertEquals(12_345L, stopwatch.getSession()?.accumulatedMillis)
        assertEquals(1, stopwatch.observeLaps().first().size)
    }
}
