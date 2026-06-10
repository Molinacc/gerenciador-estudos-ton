package br.com.ton.estudos.data.repository

import br.com.ton.estudos.data.local.database.dao.FlashcardDao
import br.com.ton.estudos.data.local.database.dao.FlashcardDeckDao
import br.com.ton.estudos.data.local.database.entity.FlashcardDeckEntity
import br.com.ton.estudos.data.local.database.entity.FlashcardEntity
import br.com.ton.estudos.domain.model.Difficulty
import br.com.ton.estudos.domain.model.Flashcard
import br.com.ton.estudos.domain.model.FlashcardDeck
import br.com.ton.estudos.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val deckDao: FlashcardDeckDao,
    private val cardDao: FlashcardDao
) : FlashcardRepository {
    override fun getAllDecks(): Flow<List<FlashcardDeck>> =
        deckDao.getAllDecks().map { it.map { e -> e.toDomain() } }

    override fun getDecksBySubject(subjectId: Long): Flow<List<FlashcardDeck>> =
        deckDao.getDecksBySubject(subjectId).map { it.map { e -> e.toDomain() } }

    override suspend fun getDeckById(id: Long): FlashcardDeck? =
        deckDao.getDeckById(id)?.toDomain()

    override suspend fun insertDeck(deck: FlashcardDeck): Long =
        deckDao.insertDeck(deck.toEntity())

    override suspend fun updateDeck(deck: FlashcardDeck) =
        deckDao.updateDeck(deck.toEntity())

    override suspend fun deleteDeck(deck: FlashcardDeck) =
        deckDao.deleteDeck(deck.toEntity())

    override fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>> =
        cardDao.getCardsByDeck(deckId).map { it.map { e -> e.toDomain() } }

    override fun getDueCards(now: Long): Flow<List<Flashcard>> =
        cardDao.getDueCards(now).map { it.map { e -> e.toDomain() } }

    override fun searchCards(query: String): Flow<List<Flashcard>> =
        cardDao.searchCards(query).map { it.map { e -> e.toDomain() } }

    override fun getDueCardsCount(now: Long): Flow<Int> = cardDao.getDueCardsCount(now)

    override suspend fun insertCard(card: Flashcard): Long = cardDao.insertCard(card.toEntity())
    override suspend fun insertCards(cards: List<Flashcard>) = cardDao.insertCards(cards.map { it.toEntity() })
    override suspend fun updateCard(card: Flashcard) = cardDao.updateCard(card.toEntity())
    override suspend fun deleteCard(card: Flashcard) = cardDao.deleteCard(card.toEntity())

    private fun FlashcardDeckEntity.toDomain() = FlashcardDeck(id, subjectId, name, description, color, 0, 0, createdAt)
    private fun FlashcardDeck.toEntity() = FlashcardDeckEntity(id, subjectId, name, description, color, createdAt)
    private fun FlashcardEntity.toDomain() = Flashcard(
        id, deckId, front, back, tags,
        Difficulty.entries.firstOrNull { it.value == difficulty } ?: Difficulty.NEW,
        nextReviewMillis, intervalDays, easeFactor, reviewCount, createdAt
    )
    private fun Flashcard.toEntity() = FlashcardEntity(
        id, deckId, front, back, tags, difficulty.value, nextReviewMillis, intervalDays, easeFactor, reviewCount, createdAt
    )
}
