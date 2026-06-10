package br.com.ton.estudos.domain.repository

import br.com.ton.estudos.domain.model.Flashcard
import br.com.ton.estudos.domain.model.FlashcardDeck
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    fun getAllDecks(): Flow<List<FlashcardDeck>>
    fun getDecksBySubject(subjectId: Long): Flow<List<FlashcardDeck>>
    suspend fun getDeckById(id: Long): FlashcardDeck?
    suspend fun insertDeck(deck: FlashcardDeck): Long
    suspend fun updateDeck(deck: FlashcardDeck)
    suspend fun deleteDeck(deck: FlashcardDeck)
    fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>>
    fun getDueCards(now: Long): Flow<List<Flashcard>>
    fun searchCards(query: String): Flow<List<Flashcard>>
    fun getDueCardsCount(now: Long): Flow<Int>
    suspend fun insertCard(card: Flashcard): Long
    suspend fun insertCards(cards: List<Flashcard>)
    suspend fun updateCard(card: Flashcard)
    suspend fun deleteCard(card: Flashcard)
}
