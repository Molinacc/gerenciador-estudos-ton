package br.com.ton.estudos.domain.repository

import br.com.ton.estudos.domain.model.TimerSession
import kotlinx.coroutines.flow.Flow

interface TimerSessionRepository {
    fun getAllTimerSessions(): Flow<List<TimerSession>>
    fun getSessionsBetween(start: Long, end: Long): Flow<List<TimerSession>>
    fun getTotalSecondsForPeriod(start: Long, end: Long): Flow<Long>
    fun getTotalSecondsForSubject(subjectId: Long): Flow<Long>
    fun getTotalSecondsAllTime(): Flow<Long>
    suspend fun insertTimerSession(session: TimerSession): Long
    suspend fun deleteTimerSession(session: TimerSession)
}
