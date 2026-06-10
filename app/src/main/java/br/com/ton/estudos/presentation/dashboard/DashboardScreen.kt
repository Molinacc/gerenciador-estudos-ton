package br.com.ton.estudos.presentation.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.ton.estudos.domain.model.StudySession
import br.com.ton.estudos.presentation.components.*
import br.com.ton.estudos.presentation.navigation.Screen
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Bom dia"
            hour < 18 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = uiState.userProfile.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { navController.navigate(Screen.Profile.route) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Perfil",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Stats Cards Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    StatCard(
                        modifier = Modifier.width(160.dp),
                        title = "Hoje",
                        value = formatSeconds(uiState.todaySeconds),
                        subtitle = "estudado",
                        icon = Icons.Filled.AccessTime,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                item {
                    StatCard(
                        modifier = Modifier.width(160.dp),
                        title = "Semana",
                        value = formatSeconds(uiState.weekSeconds),
                        subtitle = "estudado",
                        icon = Icons.Filled.DateRange,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                item {
                    StatCard(
                        modifier = Modifier.width(140.dp),
                        title = "Matérias",
                        value = uiState.subjectCount.toString(),
                        subtitle = "cadastradas",
                        icon = Icons.Filled.MenuBook,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                item {
                    StatCard(
                        modifier = Modifier.width(140.dp),
                        title = "Sequência",
                        value = "${uiState.streak} dias",
                        subtitle = "consecutivos",
                        icon = Icons.Filled.Whatshot,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Daily Goal Progress
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Meta Diária",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        val goalSeconds = uiState.userProfile.dailyGoalMinutes * 60L
                        val pct = if (goalSeconds > 0) (uiState.todaySeconds * 100 / goalSeconds).toInt().coerceAtMost(100) else 0
                        Text(
                            "$pct%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val goalSeconds = uiState.userProfile.dailyGoalMinutes * 60L
                    AnimatedProgressBar(
                        progress = if (goalSeconds > 0) (uiState.todaySeconds.toFloat() / goalSeconds) else 0f,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${formatSeconds(uiState.todaySeconds)} de ${formatMinutes(uiState.userProfile.dailyGoalMinutes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Completion rate
            if (uiState.totalSessions > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val completionPct = (uiState.completedSessions * 100f / uiState.totalSessions).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Taxa de Conclusão", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("$completionPct%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        AnimatedProgressBar(
                            progress = completionPct / 100f,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            progressColor = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("${uiState.completedSessions} de ${uiState.totalSessions} sessões concluídas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Upcoming sessions
            SectionHeader(
                title = "Próximas Atividades",
                modifier = Modifier.padding(horizontal = 20.dp),
                action = {
                    TextButton(onClick = { navController.navigate(Screen.Schedule.route) }) {
                        Text("Ver todas")
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            if (uiState.upcomingSessions.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.EventNote, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        Text("Nenhuma atividade agendada", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { navController.navigate(Screen.Schedule.route) }) { Text("Criar atividade") }
                    }
                }
            } else {
                uiState.upcomingSessions.forEach { session ->
                    UpcomingSessionItem(
                        session = session,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(88.dp))
        }
    }
}

@Composable
fun UpcomingSessionItem(session: StudySession, modifier: Modifier = Modifier) {
    val sessionColor = try { Color(android.graphics.Color.parseColor(session.color.ifBlank { "#5C6BC0" })) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(sessionColor)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                if (session.subjectName.isNotBlank()) {
                    Text(session.subjectName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val timeLabel = formatTimeFromMinutes(session.startTimeMinutes)
                Text(timeLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(formatMinutes(session.durationMinutes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

fun formatTimeFromMinutes(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return String.format("%02d:%02d", h, m)
}
