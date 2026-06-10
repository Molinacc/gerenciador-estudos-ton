package br.com.ton.estudos.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long? = null,
    val name: String,
    val description: String = "",
    val color: String = "#5C6BC0",
    val createdAt: Long = System.currentTimeMillis()
)
