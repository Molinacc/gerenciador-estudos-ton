package br.com.ton.estudos.data.local.database.dao

import androidx.room.*
import br.com.ton.estudos.data.local.database.entity.FlashcardDeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDeckDao {
    @Query("SELECT * FROM flashcard_decks ORDER BY name ASC")
    fun getAllDecks(): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcard_decks WHERE subjectId = :subjectId")
    fun getDecksBySubject(subjectId: Long): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcard_decks WHERE id = :id")
    suspend fun getDeckById(id: Long): FlashcardDeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long

    @Update
    suspend fun updateDeck(deck: FlashcardDeckEntity)

    @Delete
    suspend fun deleteDeck(deck: FlashcardDeckEntity)
}
