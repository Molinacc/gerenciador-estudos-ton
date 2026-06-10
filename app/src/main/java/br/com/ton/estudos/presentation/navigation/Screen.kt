package br.com.ton.estudos.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Schedule : Screen("schedule")
    object Timer : Screen("timer")
    object Flashcards : Screen("flashcards")
    object FlashcardStudy : Screen("flashcard_study/{deckId}") {
        fun createRoute(deckId: Long) = "flashcard_study/$deckId"
    }
    object Statistics : Screen("statistics")
    object Profile : Screen("profile")
}
