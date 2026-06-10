package br.com.ton.estudos.domain.model

data class StudySession(
    val id: Long = 0,
    val subjectId: Long,
    val subjectName: String = "",
    val subjectColor: String = "",
    val title: String,
    val description: String = "",
    val dateMillis: Long,
    val startTimeMinutes: Int,
    val durationMinutes: Int,
    val priority: Int = 1,
    val color: String = "",
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class Priority(val label: String, val value: Int) {
    LOW("Baixa", 0),
    MEDIUM("Média", 1),
    HIGH("Alta", 2),
    URGENT("Urgente", 3)
}
