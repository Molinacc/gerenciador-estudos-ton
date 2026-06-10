package br.com.ton.estudos.data.local.database.dao

import androidx.room.*
import br.com.ton.estudos.data.local.database.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY nextReviewMillis ASC")
    fun getCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewMillis <= :now ORDER BY nextReviewMillis ASC")
    fun getDueCards(now: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE front LIKE '%' || :query || '%' OR back LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchCards(query: String): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewMillis <= :now")
    fun getDueCardsCount(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId")
    fun getCardCountByDeck(deckId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Delete
    suspend fun deleteCard(card: FlashcardEntity)
}
