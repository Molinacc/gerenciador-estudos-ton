package br.com.ton.estudos.domain.model

data class FlashcardDeck(
    val id: Long = 0,
    val subjectId: Long? = null,
    val name: String,
    val description: String = "",
    val color: String = "#5C6BC0",
    val cardCount: Int = 0,
    val dueCardCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
