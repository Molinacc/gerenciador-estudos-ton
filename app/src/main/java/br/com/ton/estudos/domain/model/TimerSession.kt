package br.com.ton.estudos.domain.model

data class TimerSession(
    val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String = "",
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val sessionType: SessionType = SessionType.FREE,
    val pomodoroCount: Int = 0
)

enum class SessionType(val label: String) {
    FREE("Livre"),
    POMODORO("Pomodoro")
}
