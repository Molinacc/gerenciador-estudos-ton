package br.com.ton.estudos.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ton.estudos.domain.model.TimerSession
import br.com.ton.estudos.domain.repository.TimerSessionRepository
import br.com.ton.estudos.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatisticsUiState(
    val dailyHours: List<Float> = emptyList(), // 7 values for Mon-Sun
    val monthlyHours: List<Float> = emptyList(), // 6 values for last 6 months
    val subjectDistribution: Map<String, Long> = emptyMap(), // Subject name to seconds
    val streak: Int = 0,
    val totalSeconds: Long = 0L,
    val averageSessionMinutes: Double = 0.0,
    val bestSubject: String = "-",
    val worstSubject: String = "-",
    val weeklyProductivityPct: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val timerRepo: TimerSessionRepository,
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        calculateStatistics()
    }

    private fun calculateStatistics() {
        viewModelScope.launch {
            combine(
                timerRepo.getAllTimerSessions(),
                profileRepo.getUserProfile()
            ) { sessions, profile ->
                if (sessions.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@combine
                }

                val totalSecs = sessions.sumOf { it.durationSeconds }
                val averageMins = if (sessions.isNotEmpty()) (totalSecs.toDouble() / sessions.size) / 60.0 else 0.0

                // Subject stats
                val subGroups = sessions.groupBy { it.subjectName }
                val subDistribution = subGroups.mapValues { entry -> entry.value.sumOf { it.durationSeconds } }
                val best = subDistribution.maxByOrNull { it.value }?.key ?: "-"
                val worst = subDistribution.minByOrNull { it.value }?.key ?: "-"

                // Weekly productivity against goal
                val weekStart = startOfWeek(System.currentTimeMillis())
                val weekEnd = endOfWeek(System.currentTimeMillis())
                val weekSecs = sessions.filter { it.startTime in weekStart..weekEnd }.sumOf { it.durationSeconds }
                val weeklyGoalSecs = profile.weeklyGoalMinutes * 60L
                val weekProductivity = if (weeklyGoalSecs > 0) (weekSecs * 100 / weeklyGoalSecs).toInt().coerceAtMost(100) else 0

                // Daily hours calculation (last 7 days)
                val dailyList = calculateLast7Days(sessions)

                // Monthly hours calculation (last 6 months)
                val monthlyList = calculateLast6Months(sessions)

                _uiState.update {
                    it.copy(
                        dailyHours = dailyList,
                        monthlyHours = monthlyList,
                        subjectDistribution = subDistribution,
                        streak = profile.totalStudyStreak,
                        totalSeconds = totalSecs,
                        averageSessionMinutes = averageMins,
                        bestSubject = best,
                        worstSubject = worst,
                        weeklyProductivityPct = weekProductivity,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun calculateLast7Days(sessions: List<TimerSession>): List<Float> {
        val cal = Calendar.getInstance()
        val dailySecs = FloatArray(7)
        for (i in 0..6) {
            val checkCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val start = startOfDay(checkCal.timeInMillis)
            val end = endOfDay(checkCal.timeInMillis)
            val daySum = sessions.filter { it.startTime in start..end }.sumOf { it.durationSeconds }
            // convert to hours
            dailySecs[6 - i] = (daySum.toFloat() / 3600f)
        }
        return dailySecs.toList()
    }

    private fun calculateLast6Months(sessions: List<TimerSession>): List<Float> {
        val monthlySecs = FloatArray(6)
        for (i in 0..5) {
            val checkCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, -i)
            }
            val start = startOfMonth(checkCal.timeInMillis)
            val end = endOfMonth(checkCal.timeInMillis)
            val monthSum = sessions.filter { it.startTime in start..end }.sumOf { it.durationSeconds }
            monthlySecs[5 - i] = (monthSum.toFloat() / 3600f)
        }
        return monthlySecs.toList()
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
    private fun endOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
        return cal.timeInMillis
    }
    private fun startOfWeek(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
    private fun endOfWeek(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); add(Calendar.WEEK_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
        return cal.timeInMillis
    }
    private fun startOfMonth(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
    private fun endOfMonth(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, 1); add(Calendar.DAY_OF_YEAR, -1); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
        return cal.timeInMillis
    }
}
