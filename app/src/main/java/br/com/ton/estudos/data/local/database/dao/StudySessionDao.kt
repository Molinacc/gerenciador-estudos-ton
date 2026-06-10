package br.com.ton.estudos.data.local.database.dao

import androidx.room.*
import br.com.ton.estudos.data.local.database.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY dateMillis ASC, startTimeMinutes ASC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateMillis BETWEEN :startOfDay AND :endOfDay ORDER BY startTimeMinutes ASC")
    fun getSessionsByDay(startOfDay: Long, endOfDay: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateMillis BETWEEN :startOfWeek AND :endOfWeek ORDER BY dateMillis ASC, startTimeMinutes ASC")
    fun getSessionsByWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateMillis BETWEEN :startOfMonth AND :endOfMonth ORDER BY dateMillis ASC")
    fun getSessionsByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY dateMillis ASC")
    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateMillis >= :fromDate AND completed = 0 ORDER BY dateMillis ASC, startTimeMinutes ASC LIMIT :limit")
    fun getUpcomingSessions(fromDate: Long, limit: Int = 5): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    @Query("SELECT COUNT(*) FROM study_sessions WHERE completed = 1")
    fun getCompletedSessionsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_sessions")
    fun getTotalSessionsCount(): Flow<Int>
}
