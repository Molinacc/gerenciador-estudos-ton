package br.com.ton.estudos.presentation.profile

import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.ton.estudos.domain.model.UserProfile
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember(uiState.userProfile.name) { mutableStateOf(uiState.userProfile.name) }
    var dailyGoalMinutes by remember(uiState.userProfile.dailyGoalMinutes) { mutableStateOf(uiState.userProfile.dailyGoalMinutes.toFloat()) }
    var weeklyGoalMinutes by remember(uiState.userProfile.weeklyGoalMinutes) { mutableStateOf(uiState.userProfile.weeklyGoalMinutes.toFloat()) }
    var notificationsEnabled by remember(uiState.userProfile.notificationsEnabled) { mutableStateOf(uiState.userProfile.notificationsEnabled) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val updated = uiState.userProfile.copy(photoPath = it.toString())
            viewModel.updateProfile(updated)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Profile photo layout
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(120.dp)
            ) {
                if (uiState.userProfile.photoPath.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.userProfile.photoPath),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Foto de perfil",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = "Mudar foto",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Name field
            TextField(
                value = name,
                onValueChange = {
                    name = it
                    val updated = uiState.userProfile.copy(name = it)
                    viewModel.updateProfile(updated)
                },
                label = { Text("Nome do Estudante") },
                modifier = Modifier.fillMaxWidth()
            )

            // Daily goal slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Meta Diária de Estudos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${dailyGoalMinutes.toInt()} minutos por dia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = dailyGoalMinutes,
                        onValueChange = {
                            dailyGoalMinutes = it
                            val updated = uiState.userProfile.copy(dailyGoalMinutes = it.toInt())
                            viewModel.updateProfile(updated)
                        },
                        valueRange = 30f..480f,
                        steps = 14
                    )
                }
            }

            // Weekly goal slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Meta Semanal de Estudos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${(weeklyGoalMinutes / 60).toInt()} horas por semana",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = weeklyGoalMinutes,
                        onValueChange = {
                            weeklyGoalMinutes = it
                            val updated = uiState.userProfile.copy(weeklyGoalMinutes = it.toInt())
                            viewModel.updateProfile(updated)
                        },
                        valueRange = 300f..3000f,
                        steps = 9
                    )
                }
            }

            // Notifications card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Lembretes de Estudo", fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                val updated = uiState.userProfile.copy(notificationsEnabled = it)
                                viewModel.updateProfile(updated)
                            }
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Horário do lembrete: ${String.format("%02d:%02d", uiState.userProfile.reminderHour, uiState.userProfile.reminderMinute)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        val updated = uiState.userProfile.copy(reminderHour = h, reminderMinute = m)
                                        viewModel.updateProfile(updated)
                                    },
                                    uiState.userProfile.reminderHour, uiState.userProfile.reminderMinute, true
                                ).show()
                            }) {
                                Text("Alterar")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
