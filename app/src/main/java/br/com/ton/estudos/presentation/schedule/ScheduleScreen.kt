package br.com.ton.estudos.presentation.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.ton.estudos.domain.model.Subject
import br.com.ton.estudos.domain.model.StudySession
import br.com.ton.estudos.presentation.components.PriorityChip
import br.com.ton.estudos.presentation.components.SubjectColorDot
import br.com.ton.estudos.presentation.components.formatMinutes
import br.com.ton.estudos.presentation.theme.SubjectColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sdfDay = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val sdfDayName = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val sdfMonthYear = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronograma", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filtros",
                            tint = if (uiState.selectedSubjectId != null || uiState.selectedPriority != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showAddSubjectDialog = true }) {
                        Icon(Icons.Filled.BookmarkAdd, contentDescription = "Nova Matéria")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSessionDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Sessão")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View Mode Selector Tabs
            TabRow(selectedTabIndex = uiState.viewMode.ordinal) {
                ViewMode.values().forEach { mode ->
                    Tab(
                        selected = uiState.viewMode == mode,
                        onClick = { viewModel.setViewMode(mode) },
                        text = {
                            Text(
                                when (mode) {
                                    ViewMode.DAILY -> "Diário"
                                    ViewMode.WEEKLY -> "Semanal"
                                    ViewMode.MONTHLY -> "Mensal"
                                }
                            )
                        }
                    )
                }
            }

            // Month Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cal = Calendar.getInstance().apply { timeInMillis = uiState.selectedDateMillis }
                Text(
                    text = sdfMonthYear.format(cal.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = {
                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val selected = Calendar.getInstance().apply { set(y, m, d) }
                            viewModel.selectDate(selected.timeInMillis)
                        },
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.show()
                }) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Ir para data")
                }
            }

            // Horizontal Week Days selector (Only for Daily view mode)
            if (uiState.viewMode == ViewMode.DAILY) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val today = Calendar.getInstance()
                    // Generate 14 days around the selected date
                    val start = Calendar.getInstance().apply {
                        timeInMillis = uiState.selectedDateMillis
                        add(Calendar.DAY_OF_YEAR, -7)
                    }
                    items(15) { index ->
                        val dayCal = Calendar.getInstance().apply {
                            timeInMillis = start.timeInMillis
                            add(Calendar.DAY_OF_YEAR, index)
                        }
                        val isSelected = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(dayCal.time) ==
                                SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(uiState.selectedDateMillis))

                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                        Card(
                            onClick = { viewModel.selectDate(dayCal.timeInMillis) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = containerColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 14.dp)
                                    .width(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sdfDayName.format(dayCal.time).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = sdfDay.format(dayCal.time),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }

            // Quick Filter Info Bar
            if (uiState.selectedSubjectId != null || uiState.selectedPriority != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = { viewModel.selectSubjectFilter(null); viewModel.selectPriorityFilter(null) },
                        label = { Text("Limpar Filtros") },
                        icon = { Icon(Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Active Filters Expanded panel
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filtrar por Matéria", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.selectedSubjectId == null,
                                    onClick = { viewModel.selectSubjectFilter(null) },
                                    label = { Text("Todas") }
                                )
                            }
                            items(uiState.subjects) { subject ->
                                FilterChip(
                                    selected = uiState.selectedSubjectId == subject.id,
                                    onClick = { viewModel.selectSubjectFilter(subject.id) },
                                    label = { Text(subject.name) },
                                    leadingIcon = { SubjectColorDot(subject.color, 8.dp) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text("Filtrar por Prioridade", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(null to "Todas", 0 to "Baixa", 1 to "Média", 2 to "Alta", 3 to "Urgente").forEach { (pri, name) ->
                                FilterChip(
                                    selected = uiState.selectedPriority == pri,
                                    onClick = { viewModel.selectPriorityFilter(pri) },
                                    label = { Text(name) }
                                )
                            }
                        }
                    }
                }
            }

            // Session List
            if (uiState.sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Nenhum estudo agendado para o período.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.sessions) { session ->
                        SessionListItem(
                            session = session,
                            onToggleComplete = { viewModel.updateSession(session.copy(completed = it)) },
                            onDelete = { viewModel.deleteSession(session) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddSubjectDialog) {
        AddSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onAddSubject = { name, color, icon ->
                viewModel.addSubject(name, color, icon)
                showAddSubjectDialog = false
            }
        )
    }

    if (showAddSessionDialog) {
        AddSessionDialog(
            subjects = uiState.subjects,
            onDismiss = { showAddSessionDialog = false },
            onAddSession = { subject, title, desc, date, startMin, duration, pri ->
                viewModel.addSession(subject.id, title, desc, date, startMin, duration, pri, subject.color)
                showAddSessionDialog = false
            }
        )
    }
}

@Composable
fun SessionListItem(
    session: StudySession,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val sessionColor = try { Color(android.graphics.Color.parseColor(session.color.ifBlank { "#5C6BC0" })) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = session.completed,
                onCheckedChange = onToggleComplete
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (session.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubjectColorDot(session.color, 8.dp)
                    Text(
                        text = session.subjectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriorityChip(session.priority)
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text(
                        text = format.format(Date(session.dateMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%02d:%02d", session.startTimeMinutes / 60, session.startTimeMinutes % 60),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatMinutes(session.durationMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onAddSubject: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(SubjectColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Matéria") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Matéria") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Selecione uma cor", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SubjectColors) { colorHex ->
                        val parsed = Color(android.graphics.Color.parseColor(colorHex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    width = if (selectedColor == colorHex) 3.dp else 0.dp,
                                    color = if (selectedColor == colorHex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAddSubject(name, selectedColor, "book") },
                enabled = name.isNotBlank()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAddSession: (Subject, String, String, Long, Int, Int, Int) -> Unit
) {
    if (subjects.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Aviso") },
            text = { Text("Por favor, crie pelo menos uma matéria antes de agendar uma sessão.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.first()) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var startTimeMinutes by remember { mutableStateOf(480) } // 08:00
    var durationMinutes by remember { mutableStateOf(60) }
    var priority by remember { mutableStateOf(1) } // Média

    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Estudo") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Atividade") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Subject dropdown
                Text("Matéria", style = MaterialTheme.typography.labelMedium)
                var expandedSub by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedSub,
                    onExpandedChange = { expandedSub = it }
                ) {
                    TextField(
                        value = selectedSubject.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSub) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSub,
                        onDismissRequest = { expandedSub = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    selectedSubject = subject
                                    expandedSub = false
                                },
                                leadingIcon = { SubjectColorDot(subject.color, 8.dp) }
                            )
                        }
                    }
                }

                // Date Picker button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Data: ${sdf.format(Date(dateMillis))}")
                    Button(onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selected = Calendar.getInstance().apply { set(y, m, d) }
                                dateMillis = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Text("Selecionar")
                    }
                }

                // Time Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Horário: ${String.format("%02d:%02d", startTimeMinutes / 60, startTimeMinutes % 60)}")
                    Button(onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m -> startTimeMinutes = h * 60 + m },
                            startTimeMinutes / 60, startTimeMinutes % 60, true
                        ).show()
                    }) {
                        Text("Definir Hora")
                    }
                }

                // Duration slider
                Column {
                    Text("Duração: ${formatMinutes(durationMinutes)}")
                    Slider(
                        value = durationMinutes.toFloat(),
                        onValueChange = { durationMinutes = it.toInt() },
                        valueRange = 15f..240f,
                        steps = 14
                    )
                }

                // Priority Segmented Choice
                Text("Prioridade", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Baixa", 1 to "Média", 2 to "Alta", 3 to "Urgente").forEach { (level, name) ->
                        FilterChip(
                            selected = priority == level,
                            onClick = { priority = level },
                            label = { Text(name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddSession(selectedSubject, title, desc, dateMillis, startTimeMinutes, durationMinutes, priority)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Agendar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
