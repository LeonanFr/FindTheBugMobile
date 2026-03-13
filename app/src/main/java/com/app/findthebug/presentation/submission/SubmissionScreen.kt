package com.app.findthebug.presentation.submission

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.app.findthebug.presentation.components.BackButton
import com.app.findthebug.presentation.viewmodel.GameViewModel

@Composable
fun SubmissionScreen(
    gameViewModel: GameViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val gameState by gameViewModel.currentGameState.collectAsStateWithLifecycle()
    val isLoading by gameViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by gameViewModel.errorMessage.collectAsStateWithLifecycle()

    val questions = gameState?.case?.solutionQuestions ?: listOf(
        "Qual componente originou o erro?",
        "Qual função fez o erro se propagar?",
        "Que outro lugar ele afetou?"
    )

    var answer1 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var answer3 by remember { mutableStateOf("") }

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
                    text = "Declarar Solução",
                    color = Color(0xFFE9EEF1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2E3A44), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Responda às perguntas abaixo. Após a submissão, o Mestre irá revisar. Respostas incorretas podem custar dias.",
                    color = Color(0xFFB0B8C0),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "1. ${questions.getOrElse(0) { "Pergunta 1" }}",
                color = Color(0xFFE9EEF1),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = answer1,
                onValueChange = { answer1 = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Sua resposta...", color = Color(0xFF8A97A1)) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE9EEF1),
                    unfocusedTextColor = Color(0xFFE9EEF1),
                    focusedContainerColor = Color(0xFF1C2126),
                    unfocusedContainerColor = Color(0xFF1C2126),
                    focusedIndicatorColor = Color(0xFF00B7C3),
                    unfocusedIndicatorColor = Color(0xFF3A4A53)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. ${questions.getOrElse(1) { "Pergunta 2" }}",
                color = Color(0xFFE9EEF1),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = answer2,
                onValueChange = { answer2 = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Sua resposta...", color = Color(0xFF8A97A1)) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE9EEF1),
                    unfocusedTextColor = Color(0xFFE9EEF1),
                    focusedContainerColor = Color(0xFF1C2126),
                    unfocusedContainerColor = Color(0xFF1C2126),
                    focusedIndicatorColor = Color(0xFF00B7C3),
                    unfocusedIndicatorColor = Color(0xFF3A4A53)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "3. ${questions.getOrElse(2) { "Pergunta 3" }}",
                color = Color(0xFFE9EEF1),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = answer3,
                onValueChange = { answer3 = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Sua resposta...", color = Color(0xFF8A97A1)) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE9EEF1),
                    unfocusedTextColor = Color(0xFFE9EEF1),
                    focusedContainerColor = Color(0xFF1C2126),
                    unfocusedContainerColor = Color(0xFF1C2126),
                    focusedIndicatorColor = Color(0xFF00B7C3),
                    unfocusedIndicatorColor = Color(0xFF3A4A53)
                )
            )

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

            Button(
                onClick = {
                    val answers = listOf(answer1, answer2, answer3)
                    gameViewModel.submitSolution(answers)
                    onSubmit()
                },
                enabled = !isLoading && answer1.isNotBlank() && answer2.isNotBlank() && answer3.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B7C3),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF3A4A53)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Enviar para o Mestre", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}