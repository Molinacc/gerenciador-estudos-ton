package br.com.ton.estudos.presentation.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ton.estudos.domain.model.SessionType
import br.com.ton.estudos.domain.model.Subject
import br.com.ton.estudos.domain.model.TimerSession
import br.com.ton.estudos.domain.model.UserProfile
import br.com.ton.estudos.domain.repository.SubjectRepository
import br.com.ton.estudos.domain.repository.TimerSessionRepository
import br.com.ton.estudos.domain.repository.UserProfileRepository
import br.com.ton.estudos.service.TimerForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerUiState(
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val selectedSubject: Subject? = null,
    val sessionType: SessionType = SessionType.FREE,
    val pomodoroState: PomodoroState = PomodoroState.FOCUS,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val totalPomodorosCompleted: Int = 0,
    val historySessions: List<TimerSession> = emptyList(),
    val isServiceBound: Boolean = false
)

enum class PomodoroState { FOCUS, BREAK }

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subjectRepo: SubjectRepository,
    private val timerRepo: TimerSessionRepository,
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerService: TimerForegroundService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerForegroundService.TimerBinder
            timerService = binder.getService()
            _uiState.update { it.copy(isServiceBound = true) }
            observeService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            _uiState.update { it.copy(isServiceBound = false) }
        }
    }

    init {
        bindTimerService()
        loadData()
    }

    private fun bindTimerService() {
        val intent = Intent(context, TimerForegroundService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeService() {
        timerService?.let { service ->
            service.elapsedSeconds
                .onEach { seconds ->
                    _uiState.update { state ->
                        val finalSeconds = if (state.sessionType == SessionType.POMODORO) {
                            val limitSeconds = if (state.pomodoroState == PomodoroState.FOCUS) {
                                state.focusDurationMinutes * 60L
                            } else {
                                state.breakDurationMinutes * 60L
                            }
                            if (seconds >= limitSeconds) {
                                handlePomodoroIntervalCompleted(state)
                                0L
                            } else {
                                seconds
                            }
                        } else {
                            seconds
                        }
                        state.copy(elapsedSeconds = finalSeconds)
                    }
                }
                .launchIn(viewModelScope)

            service.isRunning
                .onEach { running ->
                    _uiState.update { it.copy(isRunning = running) }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun handlePomodoroIntervalCompleted(state: TimerUiState) {
        timerService?.pauseTimer()
        timerService?.resetTo(0L)
        if (state.pomodoroState == PomodoroState.FOCUS) {
            viewModelScope.launch {
                // Log focus session completed
                timerRepo.insertTimerSession(
                    TimerSession(
                        subjectId = state.selectedSubject?.id,
                        subjectName = state.selectedSubject?.name ?: "Pomodoro Foco",
                        startTime = System.currentTimeMillis() - (state.focusDurationMinutes * 60 * 1000L),
                        endTime = System.currentTimeMillis(),
                        durationSeconds = state.focusDurationMinutes * 60L,
                        sessionType = SessionType.POMODORO,
                        pomodoroCount = 1
                    )
                )
            }
            _uiState.update {
                it.copy(
                    pomodoroState = PomodoroState.BREAK,
                    totalPomodorosCompleted = it.totalPomodorosCompleted + 1
                )
            }
        } else {
            _uiState.update {
                it.copy(pomodoroState = PomodoroState.FOCUS)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            subjectRepo.getAllSubjects().combine(
                timerRepo.getAllTimerSessions()
            ) { subs, history ->
                _uiState.update {
                    it.copy(
                        subjects = subs,
                        historySessions = history,
                        selectedSubject = it.selectedSubject ?: subs.firstOrNull()
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            profileRepo.getUserProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        focusDurationMinutes = profile.pomodoroFocusMinutes,
                        breakDurationMinutes = profile.pomodoroBreakMinutes
                    )
                }
            }
        }
    }

    fun selectSubject(subject: Subject) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun setSessionType(type: SessionType) {
        _uiState.update { it.copy(sessionType = type, pomodoroState = PomodoroState.FOCUS) }
        timerService?.resetTo(0L)
    }

    fun startTimer() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
        }
        context.startService(intent)
        timerService?.startTimer()
    }

    fun pauseTimer() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_PAUSE
        }
        context.startService(intent)
        timerService?.pauseTimer()
    }

    fun stopTimer() {
        val state = _uiState.value
        val elapsed = state.elapsedSeconds
        if (elapsed > 5 && state.sessionType == SessionType.FREE) {
            // Save study session
            viewModelScope.launch {
                timerRepo.insertTimerSession(
                    TimerSession(
                        subjectId = state.selectedSubject?.id,
                        subjectName = state.selectedSubject?.name ?: "Livre",
                        startTime = System.currentTimeMillis() - (elapsed * 1000),
                        endTime = System.currentTimeMillis(),
                        durationSeconds = elapsed,
                        sessionType = SessionType.FREE
                    )
                )
            }
        }
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        context.startService(intent)
        timerService?.stopTimer()
        _uiState.update { it.copy(elapsedSeconds = 0L) }
    }

    fun updatePomodoroTimes(focus: Int, breakTime: Int) {
        viewModelScope.launch {
            val profile = profileRepo.getUserProfileOnce()
            profileRepo.saveUserProfile(
                profile.copy(
                    pomodoroFocusMinutes = focus,
                    pomodoroBreakMinutes = breakTime
                )
            )
        }
    }

    fun deleteTimerSession(session: TimerSession) {
        viewModelScope.launch {
            timerRepo.deleteTimerSession(session)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unbindService(serviceConnection)
        } catch (e: Exception) {
            // Service not bound or already unbound
        }
    }
}
