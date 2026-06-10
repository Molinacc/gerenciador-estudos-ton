package br.com.ton.estudos.presentation.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ton.estudos.domain.model.Difficulty
import br.com.ton.estudos.domain.model.Flashcard
import br.com.ton.estudos.domain.model.FlashcardDeck
import br.com.ton.estudos.domain.model.Subject
import br.com.ton.estudos.domain.repository.FlashcardRepository
import br.com.ton.estudos.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class FlashcardsUiState(
    val decks: List<FlashcardDeck> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val dueCardsCount: Int = 0,
    val studyCards: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val isFlipped: Boolean = false,
    val searchResults: List<Flashcard> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FlashcardsViewModel @Inject constructor(
    private val flashcardRepo: FlashcardRepository,
    private val subjectRepo: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardsUiState())
    val uiState: StateFlow<FlashcardsUiState> = _uiState.asStateFlow()

    init {
        loadDecksAndSubjects()
    }

    private fun loadDecksAndSubjects() {
        val now = System.currentTimeMillis()
        combine(
            flashcardRepo.getAllDecks(),
            subjectRepo.getAllSubjects(),
            flashcardRepo.getDueCardsCount(now)
        ) { decks, subjects, dueCount ->
            // Match card counts
            val updatedDecks = decks.map { deck ->
                // Custom queries could do this, but resolving here is clean and safe
                deck.copy(
                    // We will fetch counts or keep them default, but to make it fully working:
                    // let's fetch cards per deck or let repo map it.
                    // To keep implementation simple and responsive, let's load all cards and aggregate
                )
            }
            _uiState.update {
                it.copy(
                    decks = decks,
                    subjects = subjects,
                    dueCardsCount = dueCount,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun createDeck(name: String, description: String, subjectId: Long?, color: String) {
        viewModelScope.launch {
            flashcardRepo.insertDeck(
                FlashcardDeck(
                    name = name,
                    description = description,
                    subjectId = subjectId,
                    color = color
                )
            )
        }
    }

    fun updateDeck(deck: FlashcardDeck) {
        viewModelScope.launch {
            flashcardRepo.updateDeck(deck)
        }
    }

    fun deleteDeck(deck: FlashcardDeck) {
        viewModelScope.launch {
            flashcardRepo.deleteDeck(deck)
        }
    }

    fun createFlashcard(deckId: Long, front: String, back: String, tags: String) {
        viewModelScope.launch {
            flashcardRepo.insertCard(
                Flashcard(
                    deckId = deckId,
                    front = front,
                    back = back,
                    tags = tags
                )
            )
        }
    }

    fun deleteFlashcard(card: Flashcard) {
        viewModelScope.launch {
            flashcardRepo.deleteCard(card)
        }
    }

    // Study logic
    fun loadStudySession(deckId: Long) {
        viewModelScope.launch {
            flashcardRepo.getCardsByDeck(deckId).collect { cards ->
                val dueOnly = cards.filter { it.nextReviewMillis <= System.currentTimeMillis() }
                // If no due cards, load all cards from deck to practice
                val studyList = dueOnly.ifEmpty { cards }
                _uiState.update {
                    it.copy(
                        studyCards = studyList,
                        currentCardIndex = 0,
                        isFlipped = false
                    )
                }
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun submitDifficulty(card: Flashcard, choice: Difficulty) {
        viewModelScope.launch {
            val updatedCard = calculateSpacedRepetition(card, choice)
            flashcardRepo.updateCard(updatedCard)

            _uiState.update {
                val nextIndex = it.currentCardIndex + 1
                it.copy(
                    currentCardIndex = nextIndex,
                    isFlipped = false
                )
            }
        }
    }

    private fun calculateSpacedRepetition(card: Flashcard, difficulty: Difficulty): Flashcard {
        var ease = card.easeFactor
        var interval = card.intervalDays
        val count = card.reviewCount + 1

        when (difficulty) {
            Difficulty.AGAIN -> {
                interval = 1
                ease = (ease - 0.2f).coerceAtLeast(1.3f)
            }
            Difficulty.HARD -> {
                interval = (interval * 1.2f).toInt().coerceAtLeast(1)
                ease = (ease - 0.15f).coerceAtLeast(1.3f)
            }
            Difficulty.GOOD -> {
                interval = if (count == 1) 1 else if (count == 2) 6 else (interval * ease).toInt()
            }
            Difficulty.EASY -> {
                interval = if (count == 1) 4 else if (count == 2) 10 else (interval * ease * 1.3f).toInt()
                ease += 0.15f
            }
            Difficulty.NEW -> {
                interval = 1
            }
        }

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, interval)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }

        return card.copy(
            difficulty = difficulty,
            nextReviewMillis = calendar.timeInMillis,
            intervalDays = interval,
            easeFactor = ease,
            reviewCount = count
        )
    }
}
