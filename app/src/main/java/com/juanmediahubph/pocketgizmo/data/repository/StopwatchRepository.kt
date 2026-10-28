package com.juanmediahubph.pocketgizmo.data.repository

import com.juanmediahubph.pocketgizmo.data.local.LapEntity
import com.juanmediahubph.pocketgizmo.data.local.StopwatchDao
import com.juanmediahubph.pocketgizmo.data.local.StopwatchSessionEntity
import kotlinx.coroutines.flow.Flow

class StopwatchRepository(private val dao: StopwatchDao) {

    val session: Flow<StopwatchSessionEntity?> = dao.observeSession()

    val laps: Flow<List<LapEntity>> = dao.observeLaps()

    suspend fun getSession(): StopwatchSessionEntity? = dao.getSession()

    suspend fun saveSession(session: StopwatchSessionEntity) {
        dao.insertSession(session)
        dao.updateSession(session)
    }

    suspend fun lastLap(): LapEntity? = dao.lastLap()

    suspend fun addLap(lap: LapEntity) = dao.insertLap(lap)

    suspend fun clearAll() = dao.clearAll()
}
