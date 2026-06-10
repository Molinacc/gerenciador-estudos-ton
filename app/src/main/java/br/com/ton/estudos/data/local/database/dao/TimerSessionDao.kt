package br.com.ton.estudos.data.local.database.dao

import androidx.room.*
import br.com.ton.estudos.data.local.database.entity.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {
    @Query("SELECT * FROM timer_sessions ORDER BY startTime DESC")
    fun getAllTimerSessions(): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE startTime BETWEEN :start AND :end ORDER BY startTime DESC")
    fun getSessionsBetween(start: Long, end: Long): Flow<List<TimerSessionEntity>>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM timer_sessions WHERE startTime BETWEEN :start AND :end")
    fun getTotalSecondsForPeriod(start: Long, end: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM timer_sessions WHERE subjectId = :subjectId")
    fun getTotalSecondsForSubject(subjectId: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimerSession(session: TimerSessionEntity): Long

    @Delete
    suspend fun deleteTimerSession(session: TimerSessionEntity)

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM timer_sessions")
    fun getTotalSecondsAllTime(): Flow<Long>
}
