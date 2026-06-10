package br.com.ton.estudos.data.repository

import br.com.ton.estudos.data.local.database.dao.StudySessionDao
import br.com.ton.estudos.data.local.database.entity.StudySessionEntity
import br.com.ton.estudos.domain.model.StudySession
import br.com.ton.estudos.domain.repository.StudySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudySessionRepositoryImpl @Inject constructor(
    private val sessionDao: StudySessionDao
) : StudySessionRepository {
    override fun getAllSessions(): Flow<List<StudySession>> =
        sessionDao.getAllSessions().map { it.map { e -> e.toDomain() } }

    override fun getSessionsByDay(startOfDay: Long, endOfDay: Long): Flow<List<StudySession>> =
        sessionDao.getSessionsByDay(startOfDay, endOfDay).map { it.map { e -> e.toDomain() } }

    override fun getSessionsByWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<StudySession>> =
        sessionDao.getSessionsByWeek(startOfWeek, endOfWeek).map { it.map { e -> e.toDomain() } }

    override fun getSessionsByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<StudySession>> =
        sessionDao.getSessionsByMonth(startOfMonth, endOfMonth).map { it.map { e -> e.toDomain() } }

    override fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>> =
        sessionDao.getSessionsBySubject(subjectId).map { it.map { e -> e.toDomain() } }

    override fun getUpcomingSessions(fromDate: Long, limit: Int): Flow<List<StudySession>> =
        sessionDao.getUpcomingSessions(fromDate, limit).map { it.map { e -> e.toDomain() } }

    override suspend fun insertSession(session: StudySession): Long =
        sessionDao.insertSession(session.toEntity())

    override suspend fun updateSession(session: StudySession) =
        sessionDao.updateSession(session.toEntity())

    override suspend fun deleteSession(session: StudySession) =
        sessionDao.deleteSession(session.toEntity())

    override fun getCompletedSessionsCount(): Flow<Int> = sessionDao.getCompletedSessionsCount()
    override fun getTotalSessionsCount(): Flow<Int> = sessionDao.getTotalSessionsCount()

    private fun StudySessionEntity.toDomain() = StudySession(
        id = id,
        subjectId = subjectId,
        subjectName = "", // Resolved in viewmodel if needed, or keeping it blank
        subjectColor = color,
        title = title,
        description = description,
        dateMillis = dateMillis,
        startTimeMinutes = startTimeMinutes,
        durationMinutes = durationMinutes,
        priority = priority,
        color = color,
        completed = completed,
        createdAt = createdAt
    )
    private fun StudySession.toEntity() = StudySessionEntity(
        id = id,
        subjectId = subjectId,
        title = title,
        description = description,
        dateMillis = dateMillis,
        startTimeMinutes = startTimeMinutes,
        durationMinutes = durationMinutes,
        priority = priority,
        color = color,
        completed = completed,
        createdAt = createdAt
    )
}
