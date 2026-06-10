package br.com.ton.estudos.domain.repository

import br.com.ton.estudos.domain.model.StudySession
import kotlinx.coroutines.flow.Flow

interface StudySessionRepository {
    fun getAllSessions(): Flow<List<StudySession>>
    fun getSessionsByDay(startOfDay: Long, endOfDay: Long): Flow<List<StudySession>>
    fun getSessionsByWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<StudySession>>
    fun getSessionsByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<StudySession>>
    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>>
    fun getUpcomingSessions(fromDate: Long, limit: Int = 5): Flow<List<StudySession>>
    suspend fun insertSession(session: StudySession): Long
    suspend fun updateSession(session: StudySession)
    suspend fun deleteSession(session: StudySession)
    fun getCompletedSessionsCount(): Flow<Int>
    fun getTotalSessionsCount(): Flow<Int>
}
