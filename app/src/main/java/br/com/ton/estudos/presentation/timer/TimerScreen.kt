package br.com.ton.estudos.presentation.timer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.ton.estudos.domain.model.SessionType
import br.com.ton.estudos.domain.model.Subject
import br.com.ton.estudos.domain.model.TimerSession
import br.com.ton.estudos.presentation.components.SubjectColorDot
import br.com.ton.estudos.presentation.components.formatSeconds
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronômetro", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configurar Pomodoro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Select Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    SegmentedButton(
                        selected = uiState.sessionType == SessionType.FREE,
                        onClick = { viewModel.setSessionType(SessionType.FREE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Livre")
                    }
                    SegmentedButton(
                        selected = uiState.sessionType == SessionType.POMODORO,
                        onClick = { viewModel.setSessionType(SessionType.POMODORO) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Pomodoro")
                    }
                }
            }

            // Subject Selector Dropdown
            Text(
                "Estudando a matéria:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )

            var expandedSubjectMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                Button(
                    onClick = { expandedSubjectMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    uiState.selectedSubject?.let { subject ->
                        SubjectColorDot(subject.color, 8.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(subject.name)
                    } ?: Text("Selecionar Matéria")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = expandedSubjectMenu,
                    onDismissRequest = { expandedSubjectMenu = false }
                ) {
                    uiState.subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                viewModel.selectSubject(subject)
                                expandedSubjectMenu = false
                            },
                            leadingIcon = { SubjectColorDot(subject.color, 8.dp) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Timer display circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.sessionType == SessionType.POMODORO) {
                        val label = if (uiState.pomodoroState == PomodoroState.FOCUS) "FOCO" else "DESCANSO"
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    // Format remaining time for Pomodoro, elapsed time for Free
                    val timeToShow = if (uiState.sessionType == SessionType.POMODORO) {
                        val limitSeconds = if (uiState.pomodoroState == PomodoroState.FOCUS) {
                            uiState.focusDurationMinutes * 60L
                        } else {
                            uiState.breakDurationMinutes * 60L
                        }
                        (limitSeconds - uiState.elapsedSeconds).coerceAtLeast(0)
                    } else {
                        uiState.elapsedSeconds
                    }

                    Text(
                        text = formatSeconds(timeToShow),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (uiState.sessionType == SessionType.POMODORO && uiState.totalPomodorosCompleted > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Ciclos: ${uiState.totalPomodorosCompleted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isRunning) {
                    FloatingActionButton(
                        onClick = { viewModel.pauseTimer() },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ) {
                        Icon(Icons.Filled.Pause, contentDescription = "Pausar")
                    }
                } else {
                    FloatingActionButton(
                        onClick = { viewModel.startTimer() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Iniciar")
                    }
                }

                if (uiState.elapsedSeconds > 0 || uiState.isRunning) {
                    FloatingActionButton(
                        onClick = { viewModel.stopTimer() },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Finalizar")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // History Log section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    "Histórico Recente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (uiState.historySessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhuma sessão gravada.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    uiState.historySessions.take(10).forEach { session ->
                        HistoryItem(
                            session = session,
                            onDelete = { viewModel.deleteTimerSession(session) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(88.dp))
        }
    }

    if (showSettingsDialog) {
        PomodoroSettingsDialog(
            currentFocus = uiState.focusDurationMinutes,
            currentBreak = uiState.breakDurationMinutes,
            onDismiss = { showSettingsDialog = false },
            onSave = { focus, breakTime ->
                viewModel.updatePomodoroTimes(focus, breakTime)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun HistoryItem(session: TimerSession, onDelete: () -> Unit) {
    val dateString = remember(session.startTime) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(session.startTime))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.subjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(dateString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(session.sessionType.label, fontSize = 10.sp) },
                    modifier = Modifier.height(20.dp)
                )
            }
            Text(
                formatSeconds(session.durationSeconds),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PomodoroSettingsDialog(
    currentFocus: Int,
    currentBreak: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var focus by remember { mutableStateOf(currentFocus.toFloat()) }
    var breakTime by remember { mutableStateOf(currentBreak.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Pomodoro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Tempo de Foco: ${focus.toInt()} minutos")
                    Slider(
                        value = focus,
                        onValueChange = { focus = it },
                        valueRange = 5f..90f,
                        steps = 17
                    )
                }
                Column {
                    Text("Tempo de Descanso: ${breakTime.toInt()} minutos")
                    Slider(
                        value = breakTime,
                        onValueChange = { breakTime = it },
                        valueRange = 1f..30f,
                        steps = 29
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(focus.toInt(), breakTime.toInt()) }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
