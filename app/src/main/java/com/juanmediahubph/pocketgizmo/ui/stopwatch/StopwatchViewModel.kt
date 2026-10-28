package com.juanmediahubph.pocketgizmo.ui.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.juanmediahubph.pocketgizmo.AppContainer
import com.juanmediahubph.pocketgizmo.data.local.LapEntity
import com.juanmediahubph.pocketgizmo.data.local.StopwatchSessionEntity
import com.juanmediahubph.pocketgizmo.data.repository.StopwatchRepository
import com.juanmediahubph.pocketgizmo.domain.StopwatchMath
import com.juanmediahubph.pocketgizmo.hardware.MonotonicClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StopwatchViewModel(
    private val repository: StopwatchRepository,
    private val clock: MonotonicClock,
) : ViewModel() {

    private val session = MutableStateFlow(StopwatchSessionEntity())
    val sessionState: StateFlow<StopwatchSessionEntity> = session.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    val laps: StateFlow<List<LapEntity>> = repository.laps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1_000),
        initialValue = emptyList(),
    )

    private val writeMutex = Mutex()
    private val lapMutex = Mutex()

    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(33L)
        }
    }

    val elapsedMillis: StateFlow<Long> = combine(session, ticker) { current, _ ->
        StopwatchMath.elapsedMillis(
            running = current.running,
            accumulatedMillis = current.accumulatedMillis,
            anchorElapsedRealtime = current.anchorElapsedRealtime,
            nowElapsedRealtime = clock.elapsedRealtime(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1_000),
        initialValue = 0L,
    )

    init {
        viewModelScope.launch {
            restoreSession()
            _loaded.value = true
        }
    }

    private suspend fun restoreSession() {
        val stored = repository.getSession()
        val nowElapsed = clock.elapsedRealtime()
        val nowBootReference = StopwatchMath.bootReference(clock.wallClockMillis(), nowElapsed)
        val restored = when {
            stored == null -> StopwatchSessionEntity().also { repository.saveSession(it) }

            StopwatchMath.wasInterruptedByReboot(
                running = stored.running,
                anchorElapsedRealtime = stored.anchorElapsedRealtime,
                storedBootReference = stored.bootReferenceMillis,
                nowElapsedRealtime = nowElapsed,
                nowBootReference = nowBootReference,
            ) -> stored.copy(
                running = false,
                anchorElapsedRealtime = 0L,
                stoppedByReboot = true,
            ).also { repository.saveSession(it) }

            else -> stored
        }
        session.value = restored
    }

    fun startOrResume() {
        if (!_loaded.value) return
        val nowElapsed = clock.elapsedRealtime()
        val updated = session.value.copy(
            running = true,
            anchorElapsedRealtime = nowElapsed,
            bootReferenceMillis = StopwatchMath.bootReference(
                clock.wallClockMillis(),
                nowElapsed,
            ),
            stoppedByReboot = false,
        )
        persist(updated)
    }

    fun pause() {
        if (!_loaded.value || !session.value.running) return
        val nowElapsed = clock.elapsedRealtime()
        val current = session.value
        val elapsed = StopwatchMath.elapsedMillis(
            running = true,
            accumulatedMillis = current.accumulatedMillis,
            anchorElapsedRealtime = current.anchorElapsedRealtime,
            nowElapsedRealtime = nowElapsed,
        )
        persist(
            current.copy(
                running = false,
                accumulatedMillis = elapsed,
                anchorElapsedRealtime = 0L,
                bootReferenceMillis = StopwatchMath.bootReference(
                    clock.wallClockMillis(),
                    nowElapsed,
                ),
            ),
        )
    }

    fun addLap() {
        if (!_loaded.value || !session.value.running) return
        viewModelScope.launch {
            lapMutex.withLock {
                val current = session.value
                val total = StopwatchMath.elapsedMillis(
                    running = current.running,
                    accumulatedMillis = current.accumulatedMillis,
                    anchorElapsedRealtime = current.anchorElapsedRealtime,
                    nowElapsedRealtime = clock.elapsedRealtime(),
                )
                val previous = repository.lastLap()
                val number = (previous?.lapNumber ?: 0) + 1
                val split = total - (previous?.totalMillis ?: 0L)
                repository.addLap(
                    LapEntity(
                        lapNumber = number,
                        totalMillis = total,
                        splitMillis = split,
                    ),
                )
            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            writeMutex.withLock {
                repository.clearAll()
                val fresh = StopwatchSessionEntity()
                repository.saveSession(fresh)
                session.value = fresh
            }
        }
    }

    private fun persist(updated: StopwatchSessionEntity) {
        session.value = updated
        viewModelScope.launch {
            writeMutex.withLock {
                repository.saveSession(updated)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StopwatchViewModel(
                    repository = container.stopwatchRepository,
                    clock = container.clock,
                )
            }
        }
    }
}
