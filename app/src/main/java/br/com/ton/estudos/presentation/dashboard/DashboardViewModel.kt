package br.com.ton.estudos.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ton.estudos.domain.model.StudySession
import br.com.ton.estudos.domain.model.UserProfile
import br.com.ton.estudos.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val todaySeconds: Long = 0L,
    val weekSeconds: Long = 0L,
    val subjectCount: Int = 0,
    val upcomingSessions: List<StudySession> = emptyList(),
    val completedSessions: Int = 0,
    val totalSessions: Int = 0,
    val userProfile: UserProfile = UserProfile(),
    val streak: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val timerRepo: TimerSessionRepository,
    private val subjectRepo: SubjectRepository,
    private val sessionRepo: StudySessionRepository,
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val todayEnd = endOfDay(now)
        val weekStart = startOfWeek(now)
        val weekEnd = endOfWeek(now)

        combine(
            timerRepo.getTotalSecondsForPeriod(todayStart, todayEnd),
            timerRepo.getTotalSecondsForPeriod(weekStart, weekEnd),
            subjectRepo.getSubjectCount(),
            sessionRepo.getUpcomingSessions(todayStart, 5),
            sessionRepo.getCompletedSessionsCount(),
            sessionRepo.getTotalSessionsCount(),
            profileRepo.getUserProfile()
        ) { values ->
            val todaySec = values[0] as Long
            val weekSec = values[1] as Long
            val subCount = values[2] as Int
            @Suppress("UNCHECKED_CAST")
            val upcoming = values[3] as List<StudySession>
            val completed = values[4] as Int
            val total = values[5] as Int
            val profile = values[6] as UserProfile
            DashboardUiState(
                todaySeconds = todaySec,
                weekSeconds = weekSec,
                subjectCount = subCount,
                upcomingSessions = upcoming,
                completedSessions = completed,
                totalSessions = total,
                userProfile = profile,
                streak = profile.totalStudyStreak,
                isLoading = false
            )
        }.onEach { _uiState.value = it }
            .launchIn(viewModelScope)
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
}
