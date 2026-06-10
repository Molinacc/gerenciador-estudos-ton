package br.com.ton.estudos.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val description: String = "",
    val dateMillis: Long,
    val startTimeMinutes: Int,
    val durationMinutes: Int,
    val priority: Int = 1,
    val color: String = "",
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
