package br.com.ton.estudos.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val tags: String = "",
    val difficulty: Int = 0,
    val nextReviewMillis: Long = System.currentTimeMillis(),
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val reviewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
