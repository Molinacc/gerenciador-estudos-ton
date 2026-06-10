package br.com.ton.estudos.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_sessions")
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String = "",
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val sessionType: String = "FREE",
    val pomodoroCount: Int = 0
)
