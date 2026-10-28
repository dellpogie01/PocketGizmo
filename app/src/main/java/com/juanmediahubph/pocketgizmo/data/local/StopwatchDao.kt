package com.juanmediahubph.pocketgizmo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StopwatchDao {

    @Query("SELECT * FROM stopwatch_session WHERE id = 1")
    fun observeSession(): Flow<StopwatchSessionEntity?>

    @Query("SELECT * FROM stopwatch_session WHERE id = 1")
    suspend fun getSession(): StopwatchSessionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: StopwatchSessionEntity)

    @Update
    suspend fun updateSession(session: StopwatchSessionEntity)

    @Query("SELECT * FROM stopwatch_laps ORDER BY lapNumber ASC")
    fun observeLaps(): Flow<List<LapEntity>>

    @Query("SELECT * FROM stopwatch_laps ORDER BY lapNumber DESC LIMIT 1")
    suspend fun lastLap(): LapEntity?

    @Insert
    suspend fun insertLap(lap: LapEntity)

    @Query("DELETE FROM stopwatch_laps")
    suspend fun deleteAllLaps()

    @Query("DELETE FROM stopwatch_session")
    suspend fun deleteSession()

    @Transaction
    suspend fun clearAll() {
        deleteAllLaps()
        deleteSession()
    }
}
