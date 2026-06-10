package br.com.ton.estudos.presentation.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.ton.estudos.presentation.components.StatCard
import br.com.ton.estudos.presentation.components.formatSeconds
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // General performance cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Streak Atual",
                    value = "${uiState.streak} Dias",
                    subtitle = "foco contínuo",
                    icon = Icons.Filled.Whatshot,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Estudado",
                    value = formatSeconds(uiState.totalSeconds),
                    subtitle = "acumulado",
                    icon = Icons.Filled.TrendingUp,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Custom Bar Chart: Daily study hours (last 7 days)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Tempo de Estudo (Últimos 7 dias)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

                    val maxValue = (uiState.dailyHours.maxOrNull() ?: 1.0f).coerceAtLeast(1.0f)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val barWidth = 32.dp.toPx()
                        val paddingBetween = (canvasWidth - (barWidth * 7)) / 8

                        for (i in 0..6) {
                            val value = if (i < uiState.dailyHours.size) uiState.dailyHours[i] else 0f
                            val barHeight = (value / maxValue) * (canvasHeight - 40.dp.toPx())

                            val x = paddingBetween + i * (barWidth + paddingBetween)
                            val y = canvasHeight - 20.dp.toPx() - barHeight

                            // Draw background bar track
                            drawRoundRect(
                                color = surfaceVariantColor,
                                topLeft = Offset(x, 0f),
                                size = Size(barWidth, canvasHeight - 20.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )

                            // Draw active bar
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }

                    // Labels below chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
                        days.forEach { day ->
                            Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Key performance insights
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Detalhamento de Produtividade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    InsightRow(label = "Melhor Matéria", value = uiState.bestSubject, icon = Icons.Filled.Star, color = Color(0xFFFFA726))
                    InsightRow(label = "Menor Rendimento", value = uiState.worstSubject, icon = Icons.Filled.TrendingDown, color = Color(0xFFEF5350))
                    InsightRow(label = "Duração Média das Sessões", value = String.format(Locale.getDefault(), "%.1f min", uiState.averageSessionMinutes), icon = Icons.Filled.AccessTime, color = Color(0xFF26A69A))
                    InsightRow(label = "Produtividade Semanal vs Meta", value = "${uiState.weeklyProductivityPct}%", icon = Icons.Filled.CalendarMonth, color = Color(0xFF5C6BC0))
                }
            }

            // Subject Distribution Pie Chart/Stats list
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Distribuição por Matéria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))

                    if (uiState.subjectDistribution.isEmpty()) {
                        Text("Sem dados de matérias disponíveis.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    } else {
                        uiState.subjectDistribution.forEach { (subName, seconds) ->
                            val hours = seconds.toFloat() / 3600f
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(subName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(String.format(Locale.getDefault(), "%.1fh", hours), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(Modifier.height(88.dp))
        }
    }
}

@Composable
fun InsightRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
