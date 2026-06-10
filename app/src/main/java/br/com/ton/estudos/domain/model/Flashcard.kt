package br.com.ton.estudos.domain.model

data class Flashcard(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val tags: String = "",
    val difficulty: Difficulty = Difficulty.NEW,
    val nextReviewMillis: Long = System.currentTimeMillis(),
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val reviewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class Difficulty(val label: String, val value: Int) {
    NEW("Novo", 0),
    AGAIN("Errei", 1),
    HARD("Difícil", 2),
    GOOD("Médio", 3),
    EASY("Fácil", 4)
}
