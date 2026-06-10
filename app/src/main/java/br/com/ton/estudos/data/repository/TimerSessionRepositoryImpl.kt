package br.com.ton.estudos.data.repository

import br.com.ton.estudos.data.local.database.dao.TimerSessionDao
import br.com.ton.estudos.data.local.database.entity.TimerSessionEntity
import br.com.ton.estudos.domain.model.SessionType
import br.com.ton.estudos.domain.model.TimerSession
import br.com.ton.estudos.domain.repository.TimerSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerSessionRepositoryImpl @Inject constructor(
    private val dao: TimerSessionDao
) : TimerSessionRepository {
    override fun getAllTimerSessions(): Flow<List<TimerSession>> =
        dao.getAllTimerSessions().map { it.map { e -> e.toDomain() } }

    override fun getSessionsBetween(start: Long, end: Long): Flow<List<TimerSession>> =
        dao.getSessionsBetween(start, end).map { it.map { e -> e.toDomain() } }

    override fun getTotalSecondsForPeriod(start: Long, end: Long): Flow<Long> =
        dao.getTotalSecondsForPeriod(start, end)

    override fun getTotalSecondsForSubject(subjectId: Long): Flow<Long> =
        dao.getTotalSecondsForSubject(subjectId)

    override fun getTotalSecondsAllTime(): Flow<Long> = dao.getTotalSecondsAllTime()

    override suspend fun insertTimerSession(session: TimerSession): Long =
        dao.insertTimerSession(session.toEntity())

    override suspend fun deleteTimerSession(session: TimerSession) =
        dao.deleteTimerSession(session.toEntity())

    private fun TimerSessionEntity.toDomain() = TimerSession(
        id, subjectId, subjectName, startTime, endTime, durationSeconds,
        SessionType.entries.firstOrNull { it.name == sessionType } ?: SessionType.FREE,
        pomodoroCount
    )
    private fun TimerSession.toEntity() = TimerSessionEntity(
        id, subjectId, subjectName, startTime, endTime, durationSeconds, sessionType.name, pomodoroCount
    )
}
