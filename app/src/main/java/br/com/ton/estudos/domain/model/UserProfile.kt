package br.com.ton.estudos.domain.model

data class UserProfile(
    val id: Int = 1,
    val name: String = "Estudante",
    val photoPath: String = "",
    val dailyGoalMinutes: Int = 120,
    val weeklyGoalMinutes: Int = 600,
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val pomodoroFocusMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val totalStudyStreak: Int = 0,
    val lastStudyDate: Long = 0L
)
