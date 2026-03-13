package com.app.findthebug.presentation.master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.findthebug.data.remote.model.websocket.WebSocketMessage
import com.app.findthebug.presentation.components.BackButton
import com.app.findthebug.presentation.viewmodel.GameViewModel

@Composable
fun MasterReviewScreen(
    gameViewModel: GameViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val isLoading by gameViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by gameViewModel.errorMessage.collectAsStateWithLifecycle()
    var reviewData by remember { mutableStateOf<WebSocketMessage.SolutionForReviewResponse?>(null) }

    LaunchedEffect(Unit) {
        gameViewModel.solutionForReview.collect { data ->
            reviewData = data
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F2429))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Revisar Solução",
                    color = Color(0xFFE9EEF1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (reviewData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF00B7C3))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aguardando submissão do time...",
                            color = Color(0xFFB0B8C0),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                val data = reviewData!!

                Text(
                    text = "Respostas da equipe",
                    color = Color(0xFFE9EEF1),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                data.questions.forEachIndexed { index, question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232A30))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Pergunta ${index + 1}:",
                                color = Color(0xFFB0B8C0),
                                fontSize = 14.sp
                            )
                            Text(
                                text = question,
                                color = Color(0xFFE9EEF1),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Resposta:",
                                color = Color(0xFFB0B8C0),
                                fontSize = 14.sp
                            )
                            Text(
                                text = data.teamAnswers.getOrElse(index) { "[sem resposta]" },
                                color = Color(0xFF8AB4F8),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gabarito:",
                                color = Color(0xFFB0B8C0),
                                fontSize = 14.sp
                            )
                            Text(
                                text = data.correctAnswers.getOrElse(index) { "[gabarito indefinido]" },
                                color = Color(0xFFB583FF),
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            gameViewModel.validateSolution(false)
                            onComplete()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B6B),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Rejeitar")
                    }
                    Button(
                        onClick = {
                            gameViewModel.validateSolution(true)
                            onComplete()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B7C3),
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Aprovar")
                        }
                    }
                }
            }
        }
    }
}