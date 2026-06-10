package br.com.ton.estudos.presentation.flashcards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.ton.estudos.domain.model.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardStudyScreen(
    deckId: Long,
    navController: NavController,
    viewModel: FlashcardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadStudySession(deckId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estudar Deck") },
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val totalCards = uiState.studyCards.size
            val currentIndex = uiState.currentCardIndex

            if (totalCards == 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum card disponível neste deck para revisão.", textAlign = TextAlign.Center)
                }
                return@Scaffold
            }

            if (currentIndex >= totalCards) {
                // Session Completed Screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Sucesso",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Sessão Concluída!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Você revisou todos os cards selecionados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Voltar para Decks")
                    }
                }
                return@Scaffold
            }

            val card = uiState.studyCards[currentIndex]

            // Progress text
            Text(
                text = "Card ${currentIndex + 1} de $totalCards",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { (currentIndex.toFloat() / totalCards.toFloat()) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 12.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Card Flip layout
            val rotation by animateFloatAsState(
                targetValue = if (uiState.isFlipped) 180f else 0f,
                animationSpec = tween(durationMillis = 400),
                label = "cardFlip"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(320.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { viewModel.flipCard() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // Front side
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = card.front,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            if (card.tags.isNotBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    card.tags.split(",").forEach { tag ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(tag.trim(), fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Back side (requires mirror scale)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer { rotationY = 180f }
                        ) {
                            Text(
                                text = card.back,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Control Buttons
            if (!uiState.isFlipped) {
                Button(
                    onClick = { viewModel.flipCard() },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Mostrar Resposta")
                }
            } else {
                // Difficulty Ratings
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text("Como foi a dificuldade?", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.submitDifficulty(card, Difficulty.AGAIN) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Novamente")
                        }
                        Button(
                            onClick = { viewModel.submitDifficulty(card, Difficulty.HARD) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Difícil")
                        }
                        Button(
                            onClick = { viewModel.submitDifficulty(card, Difficulty.GOOD) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Médio")
                        }
                        Button(
                            onClick = { viewModel.submitDifficulty(card, Difficulty.EASY) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Fácil")
                        }
                    }
                }
            }
        }
    }
}
