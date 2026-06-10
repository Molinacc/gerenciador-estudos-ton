package br.com.ton.estudos.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ton.estudos.domain.model.Subject
import br.com.ton.estudos.domain.model.StudySession
import br.com.ton.estudos.domain.repository.SubjectRepository
import br.com.ton.estudos.domain.repository.StudySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ScheduleUiState(
    val subjects: List<Subject> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val viewMode: ViewMode = ViewMode.DAILY,
    val selectedSubjectId: Long? = null,
    val selectedPriority: Int? = null,
    val isLoading: Boolean = true
)

enum class ViewMode { DAILY, WEEKLY, MONTHLY }

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val subjectRepo: SubjectRepository,
    private val sessionRepo: StudySessionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _viewMode = MutableStateFlow(ViewMode.DAILY)
    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    private val _selectedPriority = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ScheduleUiState> = combine(
        subjectRepo.getAllSubjects(),
        sessionRepo.getAllSessions(),
        _selectedDate,
        _viewMode,
        _selectedSubjectId,
        _selectedPriority
    ) { subjects, sessions, date, mode, subId, priority ->
        val filtered = filterSessions(sessions, date, mode, subId, priority, subjects)
        ScheduleUiState(
            subjects = subjects,
            sessions = filtered,
            selectedDateMillis = date,
            viewMode = mode,
            selectedSubjectId = subId,
            selectedPriority = priority,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleUiState())

    fun selectDate(millis: Long) {
        _selectedDate.value = millis
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun selectSubjectFilter(subjectId: Long?) {
        _selectedSubjectId.value = subjectId
    }

    fun selectPriorityFilter(priority: Int?) {
        _selectedPriority.value = priority
    }

    fun addSubject(name: String, color: String, icon: String) {
        viewModelScope.launch {
            subjectRepo.insertSubject(Subject(name = name, color = color, icon = icon))
        }
    }

    fun addSession(
        subjectId: Long,
        title: String,
        description: String,
        dateMillis: Long,
        startTimeMinutes: Int,
        durationMinutes: Int,
        priority: Int,
        color: String
    ) {
        viewModelScope.launch {
            sessionRepo.insertSession(
                StudySession(
                    subjectId = subjectId,
                    title = title,
                    description = description,
                    dateMillis = dateMillis,
                    startTimeMinutes = startTimeMinutes,
                    durationMinutes = durationMinutes,
                    priority = priority,
                    color = color
                )
            )
        }
    }

    fun updateSession(session: StudySession) {
        viewModelScope.launch {
            sessionRepo.updateSession(session)
        }
    }

    fun deleteSession(session: StudySession) {
        viewModelScope.launch {
            sessionRepo.deleteSession(session)
        }
    }

    private fun filterSessions(
        sessions: List<StudySession>,
        date: Long,
        mode: ViewMode,
        subId: Long?,
        priority: Int?,
        subjects: List<Subject>
    ): List<StudySession> {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        val mappedSessions = sessions.map { session ->
            val sub = subjects.find { it.id == session.subjectId }
            session.copy(
                subjectName = sub?.name ?: "Sem Matéria",
                subjectColor = sub?.color ?: "#5C6BC0"
            )
        }

        return mappedSessions.filter { session ->
            val sessionCal = Calendar.getInstance().apply { timeInMillis = session.dateMillis }
            val matchesTime = when (mode) {
                ViewMode.DAILY -> {
                    sessionCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            sessionCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
                }
                ViewMode.WEEKLY -> {
                    sessionCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            sessionCal.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR)
                }
                ViewMode.MONTHLY -> {
                    sessionCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            sessionCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                }
            }
            val matchesSubject = subId == null || session.subjectId == subId
            val matchesPriority = priority == null || session.priority == priority

            matchesTime && matchesSubject && matchesPriority
        }
    }
}
